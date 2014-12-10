package liquibase.change;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.util.StringUtils;

/**
 * A common parent for all raw SQL related changes regardless of where the sql was sourced from.
 * 
 * Implements the necessary logic to choose how the SQL string should be parsed to generate the statements.
 *
 */
public abstract class BaseSQLChange extends BaseChange implements DbmsTargetedChange {

    private boolean stripComments;
    private boolean splitStatements;
    private String endDelimiter;
    private String sql;
    private String dbms;

    protected String encoding = null;


    protected BaseSQLChange() {
        setStripComments(null);
        setSplitStatements(null);
    }

    public InputStream openSqlStream() throws IOException {
        return null;
    }

    @Override
    @DatabaseChangeProperty(since = "3.0", exampleValue = "h2, oracle")
    public String getDbms() {
        return dbms;
    }

    @Override
    public void setDbms(final String dbms) {
        this.dbms = dbms;
    }

    /**
     * Return if comments should be stripped from the SQL before passing it to the database.
     * <p></p>
     * This will always return a non-null value and should be a boolean rather than a Boolean, but that breaks the Bean Standard.
     */
    @DatabaseChangeProperty(description = "Set to true to remove any comments in the SQL before executing, otherwise false. Defaults to false if not set")
    public Boolean isStripComments() {
        return stripComments;
    }


    /**
     * Return true if comments should be stripped from the SQL before passing it to the database.
     * Passing null sets stripComments to the default value (false).
     */
    public void setStripComments(Boolean stripComments) {
        if (stripComments == null) {
            this.stripComments = false;
        } else {
            this.stripComments = stripComments;
        }
    }

    /**
     * Return if the SQL should be split into multiple statements before passing it to the database.
     * By default, statements are split around ";" and "go" delimiters.
     * <p></p>
     * This will always return a non-null value and should be a boolean rather than a Boolean, but that breaks the Bean Standard.
     */
    @DatabaseChangeProperty(description = "Set to false to not have liquibase split statements on ;'s and GO's. Defaults to true if not set")
    public Boolean isSplitStatements() {
        return splitStatements;
    }

    /**
     * Set whether SQL should be split into multiple statements.
     * Passing null sets stripComments to the default value (true).
     */
    public void setSplitStatements(Boolean splitStatements) {
        if (splitStatements == null) {
            this.splitStatements = true;
        } else {
            this.splitStatements = splitStatements;
        }
    }

    /**
     * Return the raw SQL managed by this Change
     */
    @DatabaseChangeProperty(serializationType = SerializationType.DIRECT_VALUE)
    public String getSql() {
        return sql;
    }

    /**
     * Set the raw SQL managed by this Change. The passed sql is trimmed and set to null if an empty string is passed.
     */
    public void setSql(String sql) {
       this.sql = StringUtils.trimToNull(sql);
    }

    /**
     * Set the end delimiter used to split statements. Will return null if the default delimiter should be used.
     *
     * @see #splitStatements
     */
    @DatabaseChangeProperty(description = "Delimiter to apply to the end of the statement. Defaults to ';', may be set to ''.", exampleValue = "\\nGO")
    public String getEndDelimiter() {
        return endDelimiter;
    }

    /**
     * Set the end delimiter for splitting SQL statements. Set to null to use the default delimiter.
     * @param endDelimiter
     */
    public void setEndDelimiter(String endDelimiter) {
        this.endDelimiter = endDelimiter;
    }

    /**
     * Calculates the checksum based on the contained SQL.
     *
     * @see liquibase.change.AbstractChange#generateCheckSum()
     */
    @Override
    public CheckSum generateCheckSum() {
        InputStream stream = null;
        try {
            stream = openSqlStream();

            String sql = this.sql;
            if (stream == null && sql == null) {
                sql = "";
            }

            if (sql != null) {
                stream = new ByteArrayInputStream(sql.getBytes("UTF-8"));
            }

            return CheckSum.compute(new NormalizingStream(this.getEndDelimiter(), this.isSplitStatements(), this.isStripComments(), stream), false);
        } catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    LogFactory.getLogger().debug("Error closing stream", e);
                }
            }
        }
    }


    protected String normalizeLineEndings(String string) {
        return string.replace("\r", "");
    }

    public static class NormalizingStream extends InputStream {
        private ByteArrayInputStream headerStream;
        private PushbackInputStream stream;

        private byte[] quickBuffer = new byte[100];
        private List<Byte> resizingBuffer = new ArrayList<Byte>();


        private int lastChar = 'X';
        private boolean seenNonSpace = false;

        public NormalizingStream(String endDelimiter, Boolean splitStatements, Boolean stripComments, InputStream stream) {
            this.stream = new PushbackInputStream(stream, 2048);
            this.headerStream = new ByteArrayInputStream((endDelimiter+":"+splitStatements+":"+stripComments+":").getBytes());
        }

        @Override
        public int read() throws IOException {
            if (headerStream != null) {
                int returnChar = headerStream.read();
                if (returnChar != -1) {
                    return returnChar;
                }
                headerStream = null;
            }

            int returnChar = stream.read();
            if (isWhiteSpace(returnChar)) {
                returnChar = ' ';
            }

            while (returnChar == ' ' && (!seenNonSpace || lastChar == ' ')) {
                returnChar = stream.read();

                if (isWhiteSpace(returnChar)) {
                    returnChar = ' ';
                }
            }

            seenNonSpace = true;

            lastChar = returnChar;

            if (lastChar == ' ' && isOnlyWhitespaceRemaining()) {
                return -1;
            }

            return returnChar;
        }

        @Override
        public int available() throws IOException {
            return stream.available();
        }

        @Override
        public boolean markSupported() {
            return stream.markSupported();
        }

        @Override
        public void mark(int readlimit) {
            stream.mark(readlimit);
        }

        @Override
        public void reset() throws IOException {
            stream.reset();
        }

        private boolean isOnlyWhitespaceRemaining() throws IOException {
            try {
                int quickBufferUsed = 0;
                while (true) {
                    byte read = (byte) stream.read();
                    if (quickBufferUsed >= quickBuffer.length) {
                        resizingBuffer.add(read);
                    } else {
                        quickBuffer[quickBufferUsed++] = read;
                    }

                    if (read == -1) {
                        return true;
                    }
                    if (!isWhiteSpace(read)) {
                        if (resizingBuffer.size() > 0) {

                            byte[] buf = new byte[resizingBuffer.size()];
                            for (int i=0; i< resizingBuffer.size(); i++) {
                                buf[i] = resizingBuffer.get(i);
                            }

                            stream.unread(buf);
                        }

                        stream.unread(quickBuffer, 0, quickBufferUsed);
                        return false;
                    }
                }
            } finally {
                resizingBuffer.clear();
            }
        }

        private boolean isWhiteSpace(int read) {
            return read == ' ' || read == '\n' || read == '\r' || read == '\t';
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }
    }
}

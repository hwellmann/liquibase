package liquibase.change.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import liquibase.change.BaseChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeWithColumns;
import liquibase.change.CheckSum;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.UtfBomAwareReader;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;
import liquibase.util.csv.CSVReader;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name = "loadData", description = "Loads data from a CSV file into an existing table. A value of NULL in a cell will be converted to a database NULL rather than the string 'NULL'\n"
    + "\n"
    + "Date/Time values included in the CSV file should be in ISO formathttp://en.wikipedia.org/wiki/ISO_8601 in order to be parsed correctly by Liquibase. Liquibase will initially set the date format to be 'yyyy-MM-dd'T'HH:mm:ss' and then it checks for two special cases which will override the data format string.\n"
    + "\n"
    + "If the string representing the date/time includes a '.', then the date format is changed to 'yyyy-MM-dd'T'HH:mm:ss.SSS'\n"
    + "If the string representing the date/time includes a space, then the date format is changed to 'yyyy-MM-dd HH:mm:ss'\n"
    + "Once the date format string is set, Liquibase will then call the SimpleDateFormat.parse() method attempting to parse the input string so that it can return a Date/Time. If problems occur, then a ParseException is thrown and the input string is treated as a String for the INSERT command to be generated.", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table", since = "1.7")
@MetaInfServices(Change.class)
public class LoadDataChange extends BaseChange implements ChangeWithColumns<LoadDataColumnConfig> {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String file;
    private Boolean relativeToChangelogFile;
    private String encoding = null;
    private String separator = liquibase.util.csv.opencsv.CSVReader.DEFAULT_SEPARATOR + "";
    private String quotchar = liquibase.util.csv.opencsv.CSVReader.DEFAULT_QUOTE_CHARACTER + "";

    private List<LoadDataColumnConfig> columns = new ArrayList<LoadDataColumnConfig>();

    @DatabaseChangeProperty(mustEqualExisting = "table.catalog", since = "3.0")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "table.schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(mustEqualExisting = "table", description = "Name of the table to insert data into", requiredForDatabase = "all")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(description = "CSV file to load", exampleValue = "com/example/users.csv", requiredForDatabase = "all")
    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Boolean isRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    public void setRelativeToChangelogFile(Boolean relativeToChangelogFile) {
        this.relativeToChangelogFile = relativeToChangelogFile;
    }

    @DatabaseChangeProperty(exampleValue = "UTF-8", description = "Encoding of the CSV file (defaults to UTF-8)")
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @DatabaseChangeProperty(exampleValue = ",")
    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        if (separator != null && separator.equals("\\t")) {
            separator = "\t";
        }
        this.separator = separator;
    }

    @DatabaseChangeProperty(exampleValue = "'")
    public String getQuotchar() {
        return quotchar;
    }

    public void setQuotchar(String quotchar) {
        this.quotchar = quotchar;
    }

    @Override
    public void addColumn(LoadDataColumnConfig column) {
        columns.add(column);
    }

    @Override
    @DatabaseChangeProperty(description = "Defines how the data should be loaded.", requiredForDatabase = "all")
    public List<LoadDataColumnConfig> getColumns() {
        return columns;
    }

    @Override
    public void setColumns(List<LoadDataColumnConfig> columns) {
        this.columns = columns;
    }

    protected ColumnConfig getColumnConfig(int index, String header) {
        for (LoadDataColumnConfig config : columns) {
            if (config.getIndex() != null && config.getIndex().equals(index)) {
                return config;
            }
            if (config.getHeader() != null && config.getHeader().equalsIgnoreCase(header)) {
                return config;
            }

            if (config.getName() != null && config.getName().equalsIgnoreCase(header)) {
                return config;
            }
        }
        return null;
    }

    @Override
    public CheckSum generateCheckSum() {
        InputStream stream = null;
        try {
            stream = StreamUtil.openStream(file, isRelativeToChangelogFile(), getChangeSet(),
                getResourceAccessor());
            if (stream == null) {
                throw new UnexpectedLiquibaseException(getFile() + " could not be found");
            }
            stream = new BufferedInputStream(stream);
            return CheckSum.compute(getTableName() + ":" + CheckSum.compute(stream, true));
        }
        catch (IOException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                }
                catch (IOException ignore) {
                }
            }
        }
    }

    public CSVReader getCSVReader() throws IOException {
        ResourceAccessor resourceAccessor = getResourceAccessor();
        if (resourceAccessor == null) {
            throw new UnexpectedLiquibaseException("No file resourceAccessor specified for "
                + getFile());
        }
        InputStream stream = StreamUtil.openStream(getFile(), isRelativeToChangelogFile(),
            getChangeSet(), resourceAccessor);
        if (stream == null) {
            return null;
        }
        Reader streamReader;
        if (getEncoding() == null) {
            streamReader = new UtfBomAwareReader(stream);
        }
        else {
            streamReader = new UtfBomAwareReader(stream, getEncoding());
        }

        char quotchar;
        if (StringUtils.trimToEmpty(getQuotchar()).length() == 0) {
            // hope this is impossible to have a field surrounded with non ascii char 0x01
            quotchar = '\1';
        }
        else {
            quotchar = getQuotchar().charAt(0);
        }

        if (getSeparator() == null) {
            setSeparator(liquibase.util.csv.opencsv.CSVReader.DEFAULT_SEPARATOR + "");
        }

        return new CSVReader(streamReader, getSeparator().charAt(0), quotchar);
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}

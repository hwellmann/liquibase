package liquibase.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.LoadDataChange;
import liquibase.change.core.LoadDataColumnConfig;
import liquibase.changelog.ExecutableChangeSet;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.Warnings;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.structure.core.Column;
import liquibase.util.BooleanParser;
import liquibase.util.StringUtils;
import liquibase.util.csv.CSVReader;

import org.kohsuke.MetaInfServices;


@DatabaseChange(name="loadData",
        description = "Loads data from a CSV file into an existing table. A value of NULL in a cell will be converted to a database NULL rather than the string 'NULL'\n" +
                "\n" +
                "Date/Time values included in the CSV file should be in ISO formathttp://en.wikipedia.org/wiki/ISO_8601 in order to be parsed correctly by Liquibase. Liquibase will initially set the date format to be 'yyyy-MM-dd'T'HH:mm:ss' and then it checks for two special cases which will override the data format string.\n" +
                "\n" +
                "If the string representing the date/time includes a '.', then the date format is changed to 'yyyy-MM-dd'T'HH:mm:ss.SSS'\n" +
                "If the string representing the date/time includes a space, then the date format is changed to 'yyyy-MM-dd HH:mm:ss'\n" +
                "Once the date format string is set, Liquibase will then call the SimpleDateFormat.parse() method attempting to parse the input string so that it can return a Date/Time. If problems occur, then a ParseException is thrown and the input string is treated as a String for the INSERT command to be generated.",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table",
        since="1.7")
@MetaInfServices(ExecutableChange.class)
public class LoadDataAction extends AbstractAction<LoadDataChange> implements ChangeWithColumns<LoadDataColumnConfig> {

    public LoadDataAction() {
        super(new LoadDataChange());
    }

    public LoadDataAction(LoadDataChange change) {
        super(change);
    }

    @Override
    public boolean supports(Database database) {
        return true;
    }



    @Override
    public boolean generateRollbackStatementsVolatile(Database database) {
        return true;
    }

    @DatabaseChangeProperty(mustEqualExisting ="table.catalog", since = "3.0")
    public String getCatalogName() {
        return change.getCatalogName();
    }

    public void setCatalogName(String catalogName) {
        change.setCatalogName(catalogName);
    }

    @DatabaseChangeProperty(mustEqualExisting ="table.schema")
    public String getSchemaName() {
        return change.getSchemaName();
    }

    public void setSchemaName(String schemaName) {
        change.setSchemaName(schemaName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "table", description = "Name of the table to insert data into", requiredForDatabase = "all")
    public String getTableName() {
        return change.getTableName();
    }

    public void setTableName(String tableName) {
        change.setTableName(tableName);
    }

    @DatabaseChangeProperty(description = "CSV file to load", exampleValue = "com/example/users.csv", requiredForDatabase = "all")
    public String getFile() {
        return change.getFile();
    }

    public void setFile(String file) {
        change.setFile(file);
    }

    public Boolean isRelativeToChangelogFile() {
        return change.isRelativeToChangelogFile();
    }

    public void setRelativeToChangelogFile(Boolean relativeToChangelogFile) {
        change.setRelativeToChangelogFile(relativeToChangelogFile);
    }

    @DatabaseChangeProperty(exampleValue = "UTF-8", description = "Encoding of the CSV file (defaults to UTF-8)")
    public String getEncoding() {
        return change.getEncoding();
    }

    public void setEncoding(String encoding) {
        change.setEncoding(encoding);
    }

    @DatabaseChangeProperty(exampleValue = ",")
    public String getSeparator() {
		return change.getSeparator();
	}

	public void setSeparator(String separator) {
	    change.setSeparator(separator);
	}

    @DatabaseChangeProperty(exampleValue = "'")
	public String getQuotchar() {
		return change.getQuotchar();
	}

	public void setQuotchar(String quotchar) {
		change.setQuotchar(quotchar);
	}

	@Override
    public void addColumn(LoadDataColumnConfig column) {
      	change.addColumn(column);
    }

    @Override
    @DatabaseChangeProperty(description = "Defines how the data should be loaded.", requiredForDatabase = "all")
    public List<LoadDataColumnConfig> getColumns() {
        return change.getColumns();
    }

    @Override
    public void setColumns(List<LoadDataColumnConfig> columns) {
        change.setColumns(columns);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        CSVReader reader = null;
        try {
            reader = change.getCSVReader();

            if (reader == null) {
                throw new UnexpectedLiquibaseException("Unable to read file "+this.getFile());
            }

            String[] headers = reader.readNext();
            if (headers == null) {
                throw new UnexpectedLiquibaseException("Data file "+getFile()+" was empty");
            }

            List<SqlStatement> statements = new ArrayList<SqlStatement>();
            String[] line;
            int lineNumber = 0;

            while ((line = reader.readNext()) != null) {
                lineNumber++;

                if (line.length == 0 || (line.length == 1 && StringUtils.trimToNull(line[0]) == null)) {
                    continue; //nothing on this line
                }
                InsertStatement insertStatement = this.createStatement(getCatalogName(), getSchemaName(), getTableName());
                for (int i=0; i<headers.length; i++) {
                    String columnName = null;
                    if( i >= line.length ) {
                      throw new UnexpectedLiquibaseException("CSV Line " + lineNumber + " has only " + (i-1) + " columns, the header has " + headers.length);
                    }

                    Object value = line[i];

                    ColumnConfig columnConfig = getColumnConfig(i, headers[i].trim());
                    if (columnConfig != null) {
                        columnName = columnConfig.getName();

                        if ("skip".equalsIgnoreCase(columnConfig.getType())) {
                            continue;
                        }

                        if (value.toString().equalsIgnoreCase("NULL")) {
                            value = "NULL";
                        } else if (columnConfig.getType() != null) {
                            ColumnConfig valueConfig = new ColumnConfig();
                            if (columnConfig.getType().equalsIgnoreCase("BOOLEAN")) {
                                valueConfig.setValueBoolean(BooleanParser.parseBoolean(value.toString().toLowerCase()));
                            } else if (columnConfig.getType().equalsIgnoreCase("NUMERIC")) {
                                valueConfig.setValueNumeric(value.toString());
                            } else if (columnConfig.getType().toLowerCase().contains("date") ||columnConfig.getType().toLowerCase().contains("time")) {
                                valueConfig.setValueDate(value.toString());
                            } else if (columnConfig.getType().equalsIgnoreCase("STRING")) {
                                valueConfig.setValue(value.toString());
                            } else if (columnConfig.getType().equalsIgnoreCase("COMPUTED")) {
                                liquibase.statement.DatabaseFunction function = new liquibase.statement.DatabaseFunction(value.toString());
                                valueConfig.setValueComputed(function);
                            } else {
                                throw new UnexpectedLiquibaseException("loadData type of "+columnConfig.getType()+" is not supported.  Please use BOOLEAN, NUMERIC, DATE, STRING, COMPUTED or SKIP");
                            }
                            value = valueConfig.getValueObject();
                        }
                    }

                    if (columnName == null) {
                        columnName = headers[i];
                    }

                    if (columnName.contains("(") || columnName.contains(")") && database instanceof AbstractJdbcDatabase) {
                        columnName = ((AbstractJdbcDatabase) database).quoteObject(columnName, Column.class);
                    }


                    insertStatement.addColumnValue(columnName, value);
                }
                statements.add(insertStatement);
            }

            return statements.toArray(new SqlStatement[statements.size()]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (UnexpectedLiquibaseException ule) {
                ExecutableChangeSet changeSet = (ExecutableChangeSet) getChangeSet();
                if (changeSet != null && changeSet.getFailOnError() != null && !changeSet.getFailOnError()) {
                    Logger log = LogFactory.getLogger();
                    log.info("Change set " + changeSet.toString(false) + " failed, but failOnError was false.  Error: " + ule.getMessage());
                    return new SqlStatement[0];
                } else {
                    throw ule;
                }
        } finally {
            if (null != reader) {
				try {
					reader.close();
				} catch (IOException e) {
					;
				}
			}
		}
    }

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return true;
    }

    protected InsertStatement createStatement(String catalogName, String schemaName, String tableName){
        return new InsertStatement(catalogName, schemaName,tableName);
    }

    protected ColumnConfig getColumnConfig(int index, String header) {
        for (LoadDataColumnConfig config : getColumns()) {
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
    public ChangeStatus checkStatus(Database database) {
        return new ChangeStatus().unknown("Cannot check loadData status");
    }

    @Override
    public String getConfirmationMessage() {
        return "Data loaded from "+getFile()+" into "+getTableName();
    }

    @Override
    public Warnings warn(Database database) {
        return null;
    }
}

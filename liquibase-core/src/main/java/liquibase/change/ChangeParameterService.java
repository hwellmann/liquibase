package liquibase.change;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import liquibase.change.core.LoadDataColumnConfig;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.DatabaseList;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;


public class ChangeParameterService {
    
    private ExecutableChange change;
    private Map<String, Object> exampleValues;
    private String parameterName;
    private String dataType;
    private ChangeParameterMetaData metaData;
    private Set<String> supportedDatabases;
    private Set<String> requiredForDatabase;
    
    
    public ChangeParameterService(ChangeParameterMetaData metaData) {
        this.metaData = metaData;
        this.parameterName = metaData.getParameterName();
        this.dataType = metaData.getDataType();
        this.change = (ExecutableChange) metaData.getChange();
        this.exampleValues = metaData.getExampleValues();
        analyze();
    }
    

    private void analyze() {
        supportedDatabases = analyzeSupportedDatabases(metaData.getSupportedDatabases());
        requiredForDatabase = analyzeRequiredDatabases(metaData.getRequiredForDatabase());
        
    }
    
    

    
    /**
     * @return the supportedDatabases
     */
    public Set<String> getSupportedDatabases() {
        return supportedDatabases;
    }


    

    
    /**
     * @return the requiredForDatabase
     */
    public Set<String> getRequiredForDatabase() {
        return requiredForDatabase;
    }


    protected Set<String> analyzeSupportedDatabases(Set<String> supportedDatabases) {
        if (supportedDatabases.isEmpty()) {
            supportedDatabases = Collections.singleton(ChangeParameterMetaData.COMPUTE);
        }

        Set<String> computedDatabases = new HashSet<String>();

        if (supportedDatabases.size() == 1 && supportedDatabases.iterator().next().equals(ChangeParameterMetaData.COMPUTE)) {
            int validDatabases = 0;
            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
                if (database.getShortName() == null || database.getShortName().equals("unsupported")) {
                    continue;
                }
                if (!change.supports(database)) {
                    continue;
                }
                try {
                    if (!change.generateStatementsVolatile(database)) {
                        ExecutableChange testChange = change.getClass().newInstance();
                        ValidationErrors originalErrors = getStatementErrors(testChange, database);
                        metaData.setValue(testChange, this.getExampleValue(database));
                        ValidationErrors finalErrors = getStatementErrors(testChange, database);
                        if (finalErrors.getUnsupportedErrorMessages().size() == 0 || finalErrors.getUnsupportedErrorMessages().size() == originalErrors.getUnsupportedErrorMessages().size()) {
                            computedDatabases.add(database.getShortName());
                        }
                        validDatabases++;
                    }
                } catch (Exception ignore) {
                }
            }

            if (validDatabases == 0) {
                return new HashSet<String>(Arrays.asList("all"));
            } else if (computedDatabases.size() == validDatabases) {
                computedDatabases = new HashSet<String>(Arrays.asList("all"));
            }

            computedDatabases.remove("none");

            return computedDatabases;
        } else {
            return new HashSet<String>(supportedDatabases);
        }
    }


    protected Set<String> analyzeRequiredDatabases(Set<String> requiredDatabases) {
        if (requiredDatabases.isEmpty()) {
            requiredDatabases = Collections.singleton(ChangeParameterMetaData.COMPUTE);
        }

        Set<String> computedDatabases = new HashSet<String>();

        if (requiredDatabases.size() == 1 && requiredDatabases.iterator().next().equals(ChangeParameterMetaData.COMPUTE)) {
            int validDatabases = 0;
            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
                try {
                    if (!change.generateStatementsVolatile(database)) {
                        ExecutableChange testChange = change.getClass().newInstance();
                        ValidationErrors originalErrors = getStatementErrors(testChange, database);
                        metaData.setValue(testChange, this.getExampleValue(database));
                        ValidationErrors finalErrors = getStatementErrors(testChange, database);
                        if (originalErrors.getRequiredErrorMessages().size() > 0 && finalErrors.getRequiredErrorMessages().size() < originalErrors.getRequiredErrorMessages().size()) {
                            computedDatabases.add(database.getShortName());
                        }
                        validDatabases++;
                    }
                } catch (Exception ignore) {
                }
            }

            if (validDatabases == 0) {
                return new HashSet<String>();
            } else if (computedDatabases.size() == validDatabases) {
                computedDatabases = new HashSet<String>(Arrays.asList("all"));
            }

            computedDatabases.remove("none");

        } else {
            computedDatabases = new HashSet<String>(requiredDatabases);
        }
        computedDatabases.remove("none");
        return computedDatabases;
    }

    private ValidationErrors getStatementErrors(ExecutableChange testChange, Database database) {
        ValidationErrors errors = new ValidationErrors();
        SqlStatement[] statements = testChange.generateStatements(database);
        for (SqlStatement statement : statements) {
            errors.addAll(SqlGeneratorFactory.getInstance().validate(statement, database));
        }
        return errors;
    }

    public Object getExampleValue(Database database) {
        if (exampleValues != null) {
            Object exampleValue = null;

            for (Map.Entry<String, Object> entry: exampleValues.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("all")) {
                    exampleValue = entry.getValue();
                } else if (DatabaseList.definitionMatches(entry.getKey(), database, false)) {
                    return entry.getValue();
                }
            }

            if (exampleValue != null) {
                return exampleValue;
            }
        }

        Map standardExamples = new HashMap();
        standardExamples.put("tableName", "person");
        standardExamples.put("schemaName", "public");
        standardExamples.put("tableSchemaName", "public");
        standardExamples.put("catalogName", "cat");
        standardExamples.put("tableCatalogName", "cat");
        standardExamples.put("columnName", "id");
        standardExamples.put("columnNames", "id, name");
        standardExamples.put("indexName", "idx_address");
        standardExamples.put("columnDataType", "int");
        standardExamples.put("dataType", "int");
        standardExamples.put("sequenceName", "seq_id");
        standardExamples.put("viewName", "v_person");
        standardExamples.put("constraintName", "const_name");
        standardExamples.put("primaryKey", "pk_id");



        if (standardExamples.containsKey(parameterName)) {
            return standardExamples.get(parameterName);
        }

        for (String prefix : new String[] {"base", "referenced", "new", "old"}) {
            if (parameterName.startsWith(prefix)) {
                String mainName = StringUtils.lowerCaseFirst(parameterName.replaceFirst("^"+prefix, ""));
                if (standardExamples.containsKey(mainName)) {
                    return standardExamples.get(mainName);
                }
            }
        }

        if (dataType.equals("string")) {
            return "A String";
        } else if (dataType.equals("integer")) {
            return 3;
        } else if (dataType.equals("boolean")) {
            return true;
        } else if (dataType.equals("bigInteger")) {
            return new BigInteger("371717");
        } else if (dataType.equals("list")) {
            return null; //"TODO";
        } else if (dataType.equals("sequenceNextValueFunction")) {
            return new SequenceNextValueFunction("seq_name");
        } else if (dataType.equals("databaseFunction")) {
            return new DatabaseFunction("now");
        } else if (dataType.equals("list of columnConfig")) {
            ArrayList<ColumnConfig> list = new ArrayList<ColumnConfig>();
            list.add(new ColumnConfig().setName("id").setType("int"));
            return list;
        } else if (dataType.equals("list of addColumnConfig")) {
            ArrayList<ColumnConfig> list = new ArrayList<ColumnConfig>();
            list.add(new AddColumnConfig().setName("id").setType("int"));
            return list;
        } else if (dataType.equals("list of loadDataColumnConfig")) {
            ArrayList<ColumnConfig> list = new ArrayList<ColumnConfig>();
            list.add(new LoadDataColumnConfig().setName("id").setType("int"));
            return list;
        } else {
            throw new UnexpectedLiquibaseException("Unknown dataType " + dataType + " for " + metaData.getParameterName());
        }
    }

    /**
     * A convenience method for testing the value returned by {@link #getRequiredForDatabase()} against a given database.
     * Returns true if the {@link Database#getShortName()} method is contained in the required databases or the required database list contains the string "all"
     */
    public boolean isRequiredFor(Database database) {
        return requiredForDatabase.contains("all") || requiredForDatabase.contains(database.getShortName());
    }

    public boolean supports(Database database) {
        return supportedDatabases.contains("all") || supportedDatabases.contains(database.getShortName());
    }

    
}

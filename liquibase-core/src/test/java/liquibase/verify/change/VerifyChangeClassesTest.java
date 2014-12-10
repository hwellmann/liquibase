package liquibase.verify.change;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeParameterMetaData;
import liquibase.change.ChangeParameterService;
import liquibase.change.ChangeService;
import liquibase.change.ExecutableChange;
import liquibase.change.ExecutableChangeFactory;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.ValidationErrors;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.core.string.StringChangeLogSerializer;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.test.JUnitResourceAccessor;
import liquibase.util.StringUtils;
import liquibase.verify.AbstractVerifyTest;

import org.junit.Ignore;
import org.junit.Test;

public class VerifyChangeClassesTest extends AbstractVerifyTest {

    @Ignore
    @Test
    public void minimumRequiredIsValidSql() throws Exception {
        ExecutableChangeFactory changeFactory = ExecutableChangeFactory.getInstance();
        for (String changeName : changeFactory.getDefinedChanges()) {
            if (changeName.equals("addDefaultValue")) {
                continue; //need to better handle strange "one of defaultValue* is required" logic
            }
            if (changeName.equals("changeWithNestedTags") || changeName.equals("sampleChange")) {
                continue; //not a real change
            }
            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
                if (database.getShortName() == null) {
                    continue;
                }

                TestState state = new TestState(name.getMethodName(), changeName, database.getShortName(), TestState.Type.SQL);
                state.addComment("Database: " + database.getShortName());

                ExecutableChange change = changeFactory.create(changeName);
                if (!change.supports(database)) {
                    continue;
                }
                if (change.generateStatementsVolatile(database)) {
                    continue;
                }
                ChangeMetaData changeMetaData = ExecutableChangeFactory.getInstance().getChangeMetaData(change);
                ChangeService changeService = new ChangeService(changeMetaData);

                change.setResourceAccessor(new JUnitResourceAccessor());

                for (String paramName : new TreeSet<String>(changeService.getRequiredParameters(database).keySet())) {
                    ChangeParameterMetaData param = changeMetaData.getParameters().get(paramName);
                    ChangeParameterService analyzer = new ChangeParameterService(param);
                    Object paramValue = analyzer.getExampleValue(database);
                    String serializedValue;
                    serializedValue = formatParameter(paramValue);
                    state.addComment("Change Parameter: " + param.getParameterName() + "=" + serializedValue);
                    param.setValue(change, paramValue);
                }

                ValidationErrors errors = change.validate(database);
                assertFalse("Validation errors for " + changeMetaData.getName() + " on " + database.getShortName() + ": " + errors.toString(), errors.hasErrors());

                SqlStatement[] sqlStatements = change.generateStatements(database);
                for (SqlStatement statement : sqlStatements) {
                    Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, database);
                    if (sql == null) {
                        System.out.println("Null sql for " + statement + " on " + database.getShortName());
                    } else {
                        for (Sql line : sql) {
                            String sqlLine = line.toSql();
                            assertFalse("Change "+changeMetaData.getName()+" contains 'null' for "+database.getShortName()+": "+sqlLine, sqlLine.contains(" null "));

                            state.addValue(sqlLine + ";");
                        }
                    }
                }
                state.test();
            }
        }
    }

    @Test
    public void lessThanMinimumFails() throws Exception {
        ExecutableChangeFactory changeFactory = ExecutableChangeFactory.getInstance();
        for (String changeName : changeFactory.getDefinedChanges()) {
            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
                if (database.getShortName() == null) {
                    continue;
                }

                ExecutableChange change = changeFactory.create(changeName);
                if (!change.supports(database)) {
                    continue;
                }
                if (change.generateStatementsVolatile(database)) {
                    continue;
                }
                ChangeMetaData changeMetaData = ExecutableChangeFactory.getInstance().getChangeMetaData(change);
                ChangeService changeService = new ChangeService(changeMetaData);

                change.setResourceAccessor(new JUnitResourceAccessor());
                System.out.println(change.getClass().getSimpleName());

                ArrayList<String> requiredParams = new ArrayList<String>(changeService.getRequiredParameters(database).keySet());
                for (String paramName : requiredParams) {
                    ChangeParameterMetaData param = changeMetaData.getParameters().get(paramName);
                    ChangeParameterService analyzer = new ChangeParameterService(param);
                    Object paramValue = analyzer.getExampleValue(database);
                    param.setValue(change, paramValue);
                }

                for (int i = 0; i < requiredParams.size(); i++) {
                    String paramToRemove = requiredParams.get(i);
                    ChangeParameterMetaData paramToRemoveMetadata = changeMetaData.getParameters().get(paramToRemove);
                    Object currentValue = paramToRemoveMetadata.getCurrentValue(change);
                    paramToRemoveMetadata.setValue(change, null);

                    assertTrue("No errors even with "+changeMetaData.getName()+" with a null "+paramToRemove+" on "+database.getShortName(), change.validate(database).hasErrors());
                    paramToRemoveMetadata.setValue(change, currentValue);
                }
            }
        }
    }

    @Ignore
    @Test
    public void extraParamsIsValidSql() throws Exception {
        ExecutableChangeFactory changeFactory = ExecutableChangeFactory.getInstance();
        for (String changeName : changeFactory.getDefinedChanges()) {
            if (changeName.equals("addDefaultValue")) {
                continue; //need to better handle strange "one of defaultValue* is required" logic
            }

            if (changeName.equals("createProcedure")) {
                continue; //need to better handle strange "one of path or body is required" logic
            }

            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
                if (database.getShortName() == null) {
                    continue;
                }

                TestState state = new TestState(name.getMethodName(), changeName, database.getShortName(), TestState.Type.SQL);
                state.addComment("Database: " + database.getShortName());

                ExecutableChange baseChange = changeFactory.create(changeName);
                if (!baseChange.supports(database)) {
                    continue;
                }
                if (baseChange.generateStatementsVolatile(database)) {
                    continue;
                }
                ChangeMetaData changeMetaData = ExecutableChangeFactory.getInstance().getChangeMetaData(baseChange);
                ChangeService changeService = new ChangeService(changeMetaData);
                ArrayList<String> optionalParameters = new ArrayList<String>(changeService.getOptionalParameters(database).keySet());
                Collections.sort(optionalParameters);

                List<List<String>> paramLists = powerSet(optionalParameters);
                Collections.sort(paramLists, new Comparator<List<String>>() {
                    @Override
                    public int compare(List<String> o1, List<String> o2) {
                        int comp = Integer.valueOf(o1.size()).compareTo(o2.size());
                        if (comp == 0) {
                            comp =  StringUtils.join(o1,",").compareTo(StringUtils.join(o2, ","));
                        }
                        return comp;
                    }
                });
                for (List<String> permutation : paramLists) {
                    ExecutableChange change = changeFactory.create(changeName);
                    change.setResourceAccessor(new JUnitResourceAccessor());
//
                    for (String paramName : new TreeSet<String>(changeService.getRequiredParameters(database).keySet())) {
                        ChangeParameterMetaData param = changeMetaData.getParameters().get(paramName);
                        ChangeParameterService analyzer = new ChangeParameterService(param);
                        Object paramValue = analyzer.getExampleValue(database);
                        String serializedValue;
                        serializedValue = formatParameter(paramValue);
                        state.addComment("Required Change Parameter: "+ param.getParameterName()+"="+ serializedValue);
                        param.setValue(change, paramValue);
                    }

                    for (String paramName : permutation) {
                        ChangeParameterMetaData param = changeMetaData.getParameters().get(paramName);
                        ChangeParameterService analyzer = new ChangeParameterService(param);
                        if (!analyzer.supports(database)) {
                            continue;
                        }
                        Object paramValue = analyzer.getExampleValue(database);
                        String serializedValue;
                        serializedValue = formatParameter(paramValue);
                        state.addComment("Optional Change Parameter: "+ param.getParameterName()+"="+ serializedValue);
                        param.setValue(change, paramValue);

                    }

                    ValidationErrors errors = change.validate(database);
                    assertFalse("Validation errors for " + changeMetaData.getName() + " on "+database.getShortName()+": " +errors.toString(), errors.hasErrors());
//
//                    SqlStatement[] sqlStatements = change.generateStatements(database);
//                    for (SqlStatement statement : sqlStatements) {
//                        Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, database);
//                        if (sql == null) {
//                            System.out.println("Null sql for "+statement+" on "+database.getShortName());
//                        } else {
//                            for (Sql line : sql) {
//                                state.addValue(line.toSql()+";");
//                            }
//                        }
//                    }
//                    state.test();
                }
            }
        }
    }

    private List<List<String>> powerSet(List<String> baseSet) {
        List<List<String>> returnList = new LinkedList<List<String>>();

        if (baseSet.isEmpty()) {
            returnList.add(new ArrayList<String>());
            return returnList;
        }
        List<String> list = new ArrayList<String>(baseSet);
        String head = list.get(0);
        List<String> rest = new ArrayList<String>(list.subList(1, list.size()));
        for (List<String> set : powerSet(rest)) {
            List<String> newSet = new ArrayList<String>();
            newSet.add(head);
            newSet.addAll(set);
            returnList.add(newSet);
            returnList.add(set);
        }
        return returnList;


    }

    private String formatParameter(Object paramValue) {
        String serializedValue;
        if (paramValue instanceof Collection) {
            serializedValue = "[";
            for (Object obj : (Collection) paramValue) {
                serializedValue += formatParameter(obj) + ", ";
            }
            serializedValue += "]";
        } else if (paramValue instanceof LiquibaseSerializable) {
            serializedValue = new StringChangeLogSerializer().serialize(((LiquibaseSerializable) paramValue), true);
        } else {
            serializedValue = paramValue.toString();
        }
        return serializedValue;
    }

//    @Test
//    public void volitileIsCorrect() {
//
//    }

}

package liquibase.action;

import java.util.SortedSet;

import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.custom.CustomChange;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.change.custom.CustomSqlChange;
import liquibase.change.custom.CustomSqlRollback;
import liquibase.change.custom.CustomTaskChange;
import liquibase.change.custom.CustomTaskRollback;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.statement.SqlStatement;
import liquibase.util.ObjectUtil;

import org.kohsuke.MetaInfServices;

/**
 * Adapts CustomChange implementations to the standard change system used by Liquibase.
 * Custom change implementations should implement CustomSqlChange or CustomTaskChange
 *
 * @see liquibase.change.custom.CustomSqlChange
 * @see liquibase.change.custom.CustomTaskChange
 */
@DatabaseChange(name="customChange",
        description = "Although Liquibase tries to provide a wide range of database refactorings, there are times you may want to create your own custom refactoring class.\n" +
                "\n" +
                "To create your own custom refactoring, simply create a class that implements the liquibase.change.custom.CustomSqlChange or liquibase.change.custom.CustomTaskChange interface and use the <custom> tag in your change set.\n" +
                "\n" +
                "If your change can be rolled back, implement the liquibase.change.custom.CustomSqlRollback interface as well.\n" +
                "\n" +
                "For a sample custom change class, see liquibase.change.custom.ExampleCustomSqlChange",
        priority = ChangeMetaData.PRIORITY_DEFAULT)
@MetaInfServices(ExecutableChange.class)
public class CustomChangeAction extends AbstractAction<CustomChangeWrapper> {

    public CustomChangeAction() {
        super(new CustomChangeWrapper());
    }

    public CustomChangeAction(CustomChangeWrapper change) {
        super(change);
    }

//    /**
//     * Non-private access only for testing.
//     */
//    CustomChange customChange;
//
//    private String className;
//
//    private SortedSet<String> params = new TreeSet<String>();
//
//    private Map<String, String> paramValues = new HashMap<String, String>();
//
//    private ClassLoader classLoader;

    private boolean configured = false;

    @Override
    public boolean generateStatementsVolatile(Database database) {
        return true;
    }

    /**
     * Return the CustomChange instance created by the call to {@link #setClass(String)}.
     */
    @DatabaseChangeProperty(isChangeProperty = false)
    public CustomChange getCustomChange() {
        return change.getCustomChange();
    }

    /**
     * Returns the classloader to use when creating the CustomChange instance in {@link #setClass(String)}.
     */
    @DatabaseChangeProperty(isChangeProperty = false)
    public ClassLoader getClassLoader() {
        return change.getClassLoader();
    }

    public void setClassLoader(ClassLoader classLoader) {
        change.setClassLoader(classLoader);
    }

//    /**
//     * Specify the name of the class to use as the CustomChange. This method instantiates the class using {@link #getClassLoader()} or fallback methods
//     * and assigns it to {@link #getCustomChange()}.
//     * {@link #setClassLoader(ClassLoader)} must be called before this method. The passed class is constructed, but no parameters are set. They are set in {@link liquibase.change.ExecutableChange#generateStatements(liquibase.database.Database)}
//     */
//    public CustomChangeAction setClass(String className) throws CustomChangeException {
//        change.setClass(className);
//        if (classLoader == null) {
//            throw new CustomChangeException("CustomChangeWrapper classLoader not set");
//        }
//        this.className = className;
//            try {
//                try {
//                    customChange = (CustomChange) Class.forName(className, true, classLoader).newInstance();
//                } catch (ClassCastException e) { //fails in Ant in particular
//                    try {
//                        customChange = (CustomChange) Thread.currentThread().getContextClassLoader().loadClass(className).newInstance();
//                    } catch (ClassNotFoundException e1) {
//                        customChange = (CustomChange) Class.forName(className).newInstance();
//                    }
//                }
//        } catch (Exception e) {
//            throw new CustomChangeException(e);
//        }
//
//        return this;
//    }

    /**
     * Return the name of the custom class set in {@link #setClass(String)}
     * @return
     */
    @DatabaseChangeProperty(description = "Name class that implements the custom change.")
    public String getClassName() {
        return change.getClassName();
    }

    /**
     * Specify a parameter on the CustomChange object to set before executing {@link liquibase.change.ExecutableChange#generateStatements(liquibase.database.Database)}  or {@link #generateRollbackStatements(liquibase.database.Database)} on it.
     * The CustomChange class must have a set method for the given parameter. For example, to call setParam("lastName", "X") you must have a method setLastName(String val) on your class.
     */
    public void setParam(String name, String value) {
        change.setParam(name, value);
    }

    /**
     * Returns the parameters set by {@link #setParam(String, String)}. If no parameters are set, an empty set will be returned
     */
    @DatabaseChangeProperty(isChangeProperty = false)
    public SortedSet<String> getParams() {
        return change.getParams();
    }

    /**
     * Get the value of a parameter set by {@link #setParam(String, String)}. If the parameter was not set, null will be returned.
     */
    public String getParamValue(String key) {
        return change.getParamValue(key);
    }

    /**
     * Call the {@link CustomChange#validate(liquibase.database.Database)} method and return the result.
     */
    @Override
    public ValidationErrors validate(Database database) {
        if (!configured) {
            try {
                configureCustomChange();
            } catch (CustomChangeException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }

        try {
            return new ValidationErrors();
            //return getCustomChange().validate(database);
        } catch (Throwable e) {
            return new ValidationErrors().addError("Exception thrown calling "+getClassName()+".validate():"+ e.getMessage());
        }
    }

    /**
     * Required for the Change interface, but not supported by CustomChanges. Returns an empty Warnings object.
     */
    @Override
    public Warnings warn(Database database) {
        //does not support warns
        return new Warnings();
    }

    /**
     * Finishes configuring the CustomChange based on the values passed to {@link #setParam(String, String)} then calls {@link CustomSqlChange#generateStatements(liquibase.database.Database)}
     * or {@link CustomTaskChange#execute(liquibase.database.Database)} depending on the CustomChange implementation.
     * <p></p>
     * If the CustomChange returns a null SqlStatement array, this method returns an empty array. If a CustomTaskChange is being used, this method will return an empty array.
     */
    @Override
    public SqlStatement[] generateStatements(Database database) {
        SqlStatement[] statements = null;
        try {
            if (!configured) {
                configureCustomChange();
            }
            CustomChange customChange = change.getCustomChange();
            if (customChange instanceof CustomSqlChange) {
                statements = ((CustomSqlChange) customChange).generateStatements(database);
            } else if (customChange instanceof CustomTaskChange) {
                ((CustomTaskChange) customChange).execute(database);
            } else {
                throw new UnexpectedLiquibaseException(customChange.getClass().getName() + " does not implement " + CustomSqlChange.class.getName() + " or " + CustomTaskChange.class.getName());
            }
        } catch (CustomChangeException e) {
            throw new UnexpectedLiquibaseException(e);
        }

        if (statements == null) {
            statements = new SqlStatement[0];
        }
        return statements;
    }

    /**
     * Finishes configuring the CustomChange based on the values passed to {@link #setParam(String, String)} then calls {@link CustomSqlRollback#generateRollbackStatements(liquibase.database.Database)}
     * or {@link CustomTaskRollback#rollback(liquibase.database.Database)} depending on the CustomChange implementation.
     * <p></p>
     * If the CustomChange returns a null SqlStatement array, this method returns an empty array. If a CustomTaskChange is being used, this method will return an empty array.
     * Any {@link RollbackImpossibleException} exceptions thrown by the CustomChange will thrown by this method.
     */
    @Override
    public SqlStatement[] generateRollbackStatements(Database database) throws RollbackImpossibleException {
        SqlStatement[] statements = null;
        try {
            if (!configured) {
                configureCustomChange();
            }
            CustomChange customChange = change.getCustomChange();
            if (customChange instanceof CustomSqlRollback) {
                statements = ((CustomSqlRollback) customChange).generateRollbackStatements(database);
            } else if (customChange instanceof CustomTaskRollback) {
                ((CustomTaskRollback) customChange).rollback(database);
            } else {
                throw new RollbackImpossibleException("Unknown rollback type: "+customChange.getClass().getName());
            }
        } catch (CustomChangeException e) {
            throw new UnexpectedLiquibaseException(e);
        }

        if (statements == null) {
            statements = new SqlStatement[0];
        }
        return statements;

    }


    /**
     * Returns true if the customChange supports rolling back.
     * {@link #generateRollbackStatements} may still trow a {@link RollbackImpossibleException} when it is actually exectued, even if this method returns true.
     * Currently only checks if the customChange implements {@link CustomSqlRollback}
     */
    @Override
    public boolean supportsRollback(Database database) {
        return change instanceof CustomSqlRollback || change instanceof CustomTaskRollback;
    }

    /**
     * Return the customChange's {@link CustomChange#getConfirmationMessage} message as the Change's message.
     */
    @Override
    public String getConfirmationMessage() {
        return change.getCustomChange().getConfirmationMessage();
    }

    private void configureCustomChange() throws CustomChangeException {
        try {
            CustomChange customChange = change.getCustomChange();
            for (String param : getParams()) {
                ObjectUtil.setProperty(customChange, param, change.getParamValue(param));
            }
            customChange.setFileOpener(change.getResourceAccessor());
            customChange.setUp();
        } catch (Exception e) {
            throw new CustomChangeException(e);
        }
    }

    public void setClass(String className) throws CustomChangeException {
        change.setClass(className);
    }
}

package liquibase.sdk.supplier.change;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import liquibase.change.ExecutableChange;
import liquibase.change.ExecutableChangeFactory;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.sdk.exception.UnexpectedLiquibaseSdkException;
import liquibase.servicelocator.ServiceLocator;

public class ChangeSupplierFactory {

    public Set<Class<? extends ExecutableChange>> getExtensionClasses() {
        Set<Class<? extends ExecutableChange>> classes = new HashSet<Class<? extends ExecutableChange>>(Arrays.asList(ServiceLocator.getInstance().findClasses(ExecutableChange.class)));
        return classes;
    }

    public Set<ExecutableChange> getExtensionChanges() {
        Set<ExecutableChange> returnSet = new HashSet<ExecutableChange>();
        for (String change : ExecutableChangeFactory.getInstance().getDefinedChanges()) {
            returnSet.add(ExecutableChangeFactory.getInstance().create(change));
        }
        return returnSet;
    }

    public Set<String> getExtensionChangeNames() {
        Set<String> returnSet = new HashSet<String>();
        for (ExecutableChange change : getExtensionChanges()) {
            returnSet.add(ExecutableChangeFactory.getInstance().getChangeMetaData(change).getName());
        }
        return returnSet;
    }

    public void prepareDatabase(ExecutableChange change, Database database) {
        ChangeSupplier supplier = getSupplier(change);

        try {
            Change[] changes = supplier.prepareDatabase(change);
            if (changes != null) {
                for (Change prepareChange : changes) {
                    ExecutorService.getInstance().getExecutor(database).execute((ExecutableChange) prepareChange);
                }
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseSdkException("Error executing change supplier prepareDatabase" + supplier.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    public void revertDatabase(ExecutableChange change, Database database) {
        ChangeSupplier supplier = getSupplier(change);

        try {
            Change[] changes = supplier.revertDatabase(change);
            if (changes != null) {
                for (Change revertChange : changes) {
                    ExecutorService.getInstance().getExecutor(database).execute((ExecutableChange) revertChange);
                }
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseSdkException("Error executing change supplier prepareDatabase" + supplier.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    protected ChangeSupplier getSupplier(Change change) {
        String supplierClassName = change.getClass().getName().replaceFirst("(.*)\\.(\\w+)", "$1\\.supplier\\.$2Supplier");
        try {
            Class supplierClass = Class.forName(supplierClassName);
            return (ChangeSupplier) supplierClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new UnexpectedLiquibaseSdkException("No change supplier class " + supplierClassName);
        } catch (InstantiationException e) {
            throw new UnexpectedLiquibaseSdkException("Error instantiating supplier class " + supplierClassName);
        } catch (IllegalAccessException e) {
            throw new UnexpectedLiquibaseSdkException("Error instantiating supplier class " + supplierClassName);
        }
    }

    public ChangeSupplier getSupplier(Class<? extends ExecutableChange> change) {
        try {
            return getSupplier(change.newInstance());
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public boolean isValid(ExecutableChange change, Database database) {
        return getSupplier(change).isValid(change, database);
    }
}

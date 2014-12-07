package liquibase.change;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory class for constructing the correct liquibase.change.Change implementation based on a command name.
 * For XML-based changelogs, the tag name is the command name.
 * Change implementations are looked up via the {@link ServiceLocator}.
 *
 * @see liquibase.change.ExecutableChange
 */
public class ExecutableChangeFactory {

    private static ExecutableChangeFactory instance;

    private Map<String, SortedSet<Class<? extends ExecutableChange>>> registry = new ConcurrentHashMap<String, SortedSet<Class<? extends ExecutableChange>>>();
    private Map<Class<? extends ExecutableChange>, ChangeMetaData> metaDataByClass = new ConcurrentHashMap<Class<? extends ExecutableChange>, ChangeMetaData>();

    private ExecutableChangeFactory() {
    }

    private void init() {
        Class<? extends ExecutableChange>[] classes;
        classes = ServiceLocator.getInstance().findClasses(ExecutableChange.class);

        for (Class<? extends ExecutableChange> clazz : classes) {
            //noinspection unchecked
            register(clazz);
        }
    }

    /**
     * Return the singleton ChangeFactory instance.
     */
    public static synchronized ExecutableChangeFactory getInstance() {
        if (instance == null) {
            instance = new ExecutableChangeFactory();
            instance.init();
        }
        return instance;
    }

    /**
     * Reset the ChangeFactory so it reloads the registry on the next call to @{link #getInstance()}. Mainly used in testing
     */
    public static void reset() {
        instance = null;
    }


    /**
     * Register a new Change class.
     * Normally called automatically by ChangeFactory on all Change implementations found by the ServiceLocator, but it can be called manually if needed.
     */
    public void register(Class<? extends ExecutableChange> changeClass) {
        try {
            ExecutableChange instance = changeClass.newInstance();
            ChangeMetaData metaData = getChangeMetaData(instance);
            String name = metaData.getName();
            if (registry.get(name) == null) {
                registry.put(name, new TreeSet<Class<? extends ExecutableChange>>(new Comparator<Class<? extends ExecutableChange>>() {
                    @Override
                    public int compare(Class<? extends ExecutableChange> o1, Class<? extends ExecutableChange> o2) {
                        try {
                            return -1 * new Integer(getChangeMetaData(o1.newInstance()).getPriority()).compareTo(getChangeMetaData(o2.newInstance()).getPriority());
                        } catch (Exception e) {
                            throw new UnexpectedLiquibaseException(e);
                        }
                    }
                }));
            }
            registry.get(name).add(changeClass);
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public ChangeMetaData getChangeMetaData(String change) {
        ExecutableChange changeObj = create(change);
        if (changeObj == null) {
            return null;
        }
        return getChangeMetaData(changeObj);
    }

    public ChangeMetaData getChangeMetaData(ExecutableChange change) {
        if (!metaDataByClass.containsKey(change.getClass())) {
            metaDataByClass.put(change.getClass(), change.createChangeMetaData());
        }
        return metaDataByClass.get(change.getClass());
    }

    /**
     * Unregister all instances of a given Change name. Normally used for testing, but can be called manually if needed.
     */
    public void unregister(String name) {
        registry.remove(name);
    }

    /**
     * Return the registry of all Changes found. Key is the change name and the values are a sorted set of implementations, ordered by Priority descending.
     * Normally used only for information/debugging purposes. The returned map is read only.
     */
    public Map<String, SortedSet<Class<? extends ExecutableChange>>> getRegistry() {
        return Collections.unmodifiableMap(registry);
    }


    /**
     * Returns all defined changes in the registry. Returned set is not modifiable.
     */
    public Set<String> getDefinedChanges() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    /**
     * Clear the registry of all Changes found. Normally used for testing.
     */
    public void clear() {
        registry.clear();
        metaDataByClass.clear();
    }

    /**
     * Create a new Change implementation for the given change name. The class of the constructed object will be the Change implementation with the highest priority.
     * Each call to create will return a new instance of the Change.
     */
    public ExecutableChange create(String name) {
        SortedSet<Class<? extends ExecutableChange>> classes = registry.get(name);

        if (classes == null) {
            return null;
        }

        try {
            return classes.iterator().next().newInstance();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public String[] getAllChangeNamespaces() {
        Set<String> namespaces = new HashSet<String>();
        for (String changeName : getDefinedChanges()) {
            ExecutableChange change = create(changeName);
            namespaces.add(change.getSerializedObjectNamespace());
        }

        return namespaces.toArray(new String[namespaces.size()]);
    }

    public Map<String, Object> getParameters(ExecutableChange change) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        ChangeMetaData changeMetaData = getChangeMetaData(change);
        for (ChangeParameterMetaData param : changeMetaData.getParameters().values()) {
            Object currentValue = param.getCurrentValue(change);
            if (currentValue != null) {
                returnMap.put(param.getParameterName(), currentValue);
            }
        }

        return returnMap;
    }
}

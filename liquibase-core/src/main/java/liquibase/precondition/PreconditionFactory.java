package liquibase.precondition;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.HashMap;
import java.util.Map;

public class PreconditionFactory {
    @SuppressWarnings("unchecked")
    private final Map<String, Class<? extends ExecutablePrecondition>> preconditions;

    private static PreconditionFactory instance;

    @SuppressWarnings("unchecked")
    private PreconditionFactory() {
        preconditions = new HashMap<String, Class<? extends ExecutablePrecondition>>();
        Class[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(ExecutablePrecondition.class);

            for (Class<? extends ExecutablePrecondition> clazz : classes) {
                    register(clazz);
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public static PreconditionFactory getInstance() {
        if (instance == null) {
             instance = new PreconditionFactory();
        }
        return instance;
    }

    public static void reset() {
        instance = new PreconditionFactory();
    }

    public Map<String, Class<? extends ExecutablePrecondition>> getPreconditions() {
        return preconditions;
    }

    public void register(Class<? extends ExecutablePrecondition> clazz) {
        try {
            preconditions.put(clazz.newInstance().getName(), clazz);
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public void unregister(String name) {
        preconditions.remove(name);
    }

    /**
     * Create a new Precondition subclass based on the given tag name.
     */
    public ExecutablePrecondition create(String tagName) {
        Class<?> aClass = preconditions.get(tagName);
        if (aClass == null) {
            return null;
        }
        try {
            return (ExecutablePrecondition) aClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

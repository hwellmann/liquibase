package liquibase.servicelocator;

import liquibase.exception.ServiceNotFoundException;

public abstract class ServiceLocator {

    private static ServiceLocator instance;

    static {
        try {
            Class<?> scanner = Class.forName("Liquibase.ServiceLocator.ClrServiceLocator, Liquibase");
            instance = (ServiceLocator) scanner.newInstance();
        } catch (Exception e) {
            instance = new MetaInfServiceLocator();
        }
    }


    public static ServiceLocator getInstance() {
        return instance;
    }

    public static void setInstance(ServiceLocator newInstance) {
        instance = newInstance;
    }


    public abstract Class findClass(Class requiredInterface) throws ServiceNotFoundException;

    public abstract <T> Class<? extends T>[] findClasses(Class<T> requiredInterface) throws ServiceNotFoundException;

    public abstract Object newInstance(Class requiredInterface) throws ServiceNotFoundException;


    public static void reset() {
        instance = new MetaInfServiceLocator();
    }
}

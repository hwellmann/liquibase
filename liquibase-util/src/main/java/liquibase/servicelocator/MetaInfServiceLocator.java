package liquibase.servicelocator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import liquibase.exception.ServiceNotFoundException;
import liquibase.util.PrioritizedService;

public class MetaInfServiceLocator extends ServiceLocator {

    @Override
    public Object newInstance(Class requiredInterface) throws ServiceNotFoundException {
            ServiceLoader<Object> loader = ServiceLoader.load(requiredInterface);

            if (PrioritizedService.class.isAssignableFrom(requiredInterface)) {
                PrioritizedService returnObject = null;
                for (Object service : loader) {
                    PrioritizedService newInstance = (PrioritizedService) service;
                    if (returnObject == null
                        || newInstance.getPriority() > returnObject.getPriority()) {
                        returnObject = newInstance;
                    }
                }

                if (returnObject == null) {
                    throw new ServiceNotFoundException("Could not find implementation of "
                        + requiredInterface.getName());
                }
                return returnObject;
            }

            Iterator<Object> it = loader.iterator();
            if (it.hasNext()) {
                Object result = it.next();
                if (it.hasNext()) {
                    throw new ServiceNotFoundException("Could not find unique implementation of "
                        + requiredInterface.getName());
                }
                return result;
            }
        throw new ServiceNotFoundException("Could not find any implementation of "
            + requiredInterface.getName());

    }

    @Override
    public Class findClass(Class requiredInterface) throws ServiceNotFoundException {
        return newInstance(requiredInterface).getClass();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <T> Class<? extends T>[] findClasses(Class<T> requiredInterface)
        throws ServiceNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        ServiceLoader<T> loader = ServiceLoader.load(requiredInterface);
        for (T service : loader) {
            classes.add(service.getClass());
        }
        return classes.toArray(new Class[classes.size()]);
    }

}

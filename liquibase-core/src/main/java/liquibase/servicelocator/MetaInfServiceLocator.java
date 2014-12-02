package liquibase.servicelocator;

import java.util.Iterator;
import java.util.ServiceLoader;

import liquibase.exception.ServiceNotFoundException;
import liquibase.executor.Executor;
import liquibase.logging.Logger;
import liquibase.parser.ChangeLogParser;

public class MetaInfServiceLocator extends ServiceLocator {

    @Override
    public Object newInstance(Class requiredInterface) throws ServiceNotFoundException {
        if (requiredInterface == Executor.class || requiredInterface == Logger.class
            || requiredInterface == ChangeLogParser.class) {
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
        return super.newInstance(requiredInterface);
    }

}

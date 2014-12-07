package liquibase.change;

import java.util.HashMap;
import java.util.Map;

import liquibase.database.Database;


public class ChangeService {
    
    
    private ChangeMetaData metaData;

    public ChangeService(ChangeMetaData metaData) {
        this.metaData = metaData;
    }

    /**
     *  Returns the required parameters for this change for the given database. Will never return a null map, only an empty or populated map.
     */
    public Map<String, ChangeParameterMetaData> getRequiredParameters(Database database) {
        Map<String, ChangeParameterMetaData> returnMap = new HashMap<String, ChangeParameterMetaData>();

        for (ChangeParameterMetaData metaData : metaData.getParameters().values()) {
            ChangeParameterService analyzer = new ChangeParameterService(metaData);
            if (analyzer.isRequiredFor(database)) {
                returnMap.put(metaData.getParameterName(), metaData);
            }
        }
        return returnMap;
    }

    /**
     *  Returns the optional parameters for this change for the given database. Will never return a null map, only an empty or populated map.
     */
    public Map<String, ChangeParameterMetaData> getOptionalParameters(Database database) {
        Map<String, ChangeParameterMetaData> returnMap = new HashMap<String, ChangeParameterMetaData>();

        for (ChangeParameterMetaData metaData : metaData.getParameters().values()) {
            ChangeParameterService analyzer = new ChangeParameterService(metaData);
            if (!analyzer.isRequiredFor(database)) {
                returnMap.put(metaData.getParameterName(), metaData);
            }
        }
        return returnMap;
    }

}

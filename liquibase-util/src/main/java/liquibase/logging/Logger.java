package liquibase.logging;

import liquibase.util.PrioritizedService;

public interface Logger extends PrioritizedService {

    void setName(String name);

    void setLogLevel(String level);

    void setLogLevel(LogLevel level);

    public void setLogLevel(String logLevel, String logFile);

    void severe(String message);

    void severe(String message, Throwable e);

    void warning(String message);

    void warning(String message, Throwable e);

    void info(String message);

    void info(String message, Throwable e);

    void debug(String message);

    LogLevel getLogLevel();

    void debug(String message, Throwable e);

    void setChangeLog(/*DatabaseChangeLog*/ Object databaseChangeLog);

    void setChangeSet(/*ChangeSet*/ Object changeSet);

}

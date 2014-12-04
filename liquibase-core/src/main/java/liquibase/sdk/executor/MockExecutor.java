package liquibase.sdk.executor;

import java.io.StringWriter;

import liquibase.executor.LoggingExecutor;
import liquibase.sdk.database.MockDatabase;
import liquibase.util.LiquibaseService;

@LiquibaseService(skip=true)
public class MockExecutor extends LoggingExecutor {

    public MockExecutor() {
        super(null, new StringWriter(), new MockDatabase());
    }

    public String getRanSql() {
        return getOutput().toString();
    }
}

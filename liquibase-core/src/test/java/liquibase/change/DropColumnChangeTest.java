package liquibase.change;

import liquibase.action.DropColumnAction;
import liquibase.change.core.DropColumnChange;
import liquibase.sdk.database.MockDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropColumnStatement;

import org.junit.Assert;
import org.junit.Test;

public class DropColumnChangeTest {

    @Test
    public void generateStatements_multipleColumns() {
        DropColumnAction action = new DropColumnAction();
        DropColumnChange change = action.getChange();
        ColumnConfig column1 = new ColumnConfig();
        column1.setName("column1");
        change.addColumn(column1);
        ColumnConfig column2 = new ColumnConfig();
        column2.setName("column2");
        change.addColumn(column2);

        SqlStatement[] statements = action.generateStatements(new MockDatabase());
        Assert.assertEquals(1, statements.length);
        Assert.assertTrue(statements[0] instanceof DropColumnStatement);
        DropColumnStatement stmt = (DropColumnStatement)statements[0];
        Assert.assertTrue(stmt.isMultiple());
        Assert.assertEquals(2, stmt.getColumns().size());
    }
}

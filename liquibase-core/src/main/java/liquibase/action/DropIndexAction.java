package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.DropIndexChange;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropIndexStatement;
import liquibase.structure.core.Index;

import org.kohsuke.MetaInfServices;

/**
 * Drops an existing index.
 */
@DatabaseChange(name="dropIndex", description = "Drops an existing index", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "index")
@MetaInfServices(ExecutableChange.class)
public class DropIndexAction extends AbstractAction<DropIndexChange> {

    public DropIndexAction() {
        super(new DropIndexChange());
    }

    public DropIndexAction(DropIndexChange change) {
        super(change);
    }

    @DatabaseChangeProperty(mustEqualExisting ="index.schema")
    public String getSchemaName() {
        return change.getSchemaName();
    }

    public void setSchemaName(String schemaName) {
        change.setSchemaName(schemaName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "index", description = "Name of the index to drop")
    public String getIndexName() {
        return change.getIndexName();
    }

    public void setIndexName(String indexName) {
        change.setIndexName(indexName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "index.table", description = "Name fo the indexed table.")
    public String getTableName() {
        return change.getTableName();
    }

    public void setTableName(String tableName) {
        change.setTableName(tableName);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] {
            new DropIndexStatement(getIndexName(), getCatalogName(), getSchemaName(), getTableName(), getAssociatedWith())
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        try {
            return new ChangeStatus().assertComplete(!SnapshotGeneratorFactory.getInstance().has(new Index(getIndexName(), getCatalogName(), getSchemaName(), getTableName()), database), "Index exists");
        } catch (Exception e) {
            return new ChangeStatus().unknown(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Index " + getIndexName() + " dropped from table " + getTableName();
    }

    @DatabaseChangeProperty(isChangeProperty = false)
    public String getAssociatedWith() {
        return change.getAssociatedWith();
    }

    public void setAssociatedWith(String associatedWith) {
        change.setAssociatedWith(associatedWith);
    }

    @DatabaseChangeProperty(mustEqualExisting = "index.catalog", since = "3.0")
    public String getCatalogName() {
        return change.getCatalogName();
    }

    public void setCatalogName(String catalogName) {
        change.setCatalogName(catalogName);
    }
}

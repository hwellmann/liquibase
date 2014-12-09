package liquibase.action;

import java.util.List;

import liquibase.change.AddColumnConfig;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.CreateIndexChange;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateIndexStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;

import org.kohsuke.MetaInfServices;

/**
 * Creates an index on an existing column.
 */
@DatabaseChange(name = "createIndex", description = "Creates an index on an existing column or set of columns.", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "index")
@MetaInfServices(ExecutableChange.class)
public class CreateIndexAction extends AbstractAction<CreateIndexChange> implements
    ChangeWithColumns<AddColumnConfig> {

    public CreateIndexAction() {
        super(new CreateIndexChange());
    }

    public CreateIndexAction(CreateIndexChange change) {
        super(change);
    }

    @DatabaseChangeProperty(mustEqualExisting = "index", description = "Name of the index to create")
    public String getIndexName() {
        return change.getIndexName();
    }

    public void setIndexName(String indexName) {
        change.setIndexName(indexName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "index.schema")
    public String getSchemaName() {
        return change.getSchemaName();
    }

    public void setSchemaName(String schemaName) {
        change.setSchemaName(schemaName);
    }

    @DatabaseChangeProperty(mustEqualExisting = "index.table", description = "Name of the table to add the index to", exampleValue = "person")
    public String getTableName() {
        return change.getTableName();
    }

    public void setTableName(String tableName) {
        change.setTableName(tableName);
    }

    @Override
    @DatabaseChangeProperty(mustEqualExisting = "index.column", description = "Column(s) to add to the index", requiredForDatabase = "all")
    public List<AddColumnConfig> getColumns() {
        return change.getColumns();
    }

    @Override
    public void setColumns(List<AddColumnConfig> columns) {
        change.setColumns(columns);
    }

    @Override
    public void addColumn(AddColumnConfig column) {
        change.addColumn(column);
    }

    @DatabaseChangeProperty(description = "Tablepace to create the index in.")
    public String getTablespace() {
        return change.getTablespace();
    }

    public void setTablespace(String tablespace) {
        change.setTablespace(tablespace);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] { new CreateIndexStatement(getIndexName(), getCatalogName(),
            getSchemaName(), getTableName(), this.isUnique(), getAssociatedWith(), getColumns()
                .toArray(new AddColumnConfig[getColumns().size()])).setTablespace(getTablespace())
            .setClustered(getClustered()) };
    }

    @Override
    protected ExecutableChange[] createInverses() {
        DropIndexAction inverse = new DropIndexAction();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setIndexName(getIndexName());

        return new ExecutableChange[] { inverse };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            Index example = new Index(getIndexName(), getCatalogName(), getSchemaName(),
                getTableName());
            if (getColumns() != null) {
                for (ColumnConfig column : getColumns()) {
                    example.addColumn(new Column(column));
                }
            }

            Index snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(example,
                database);
            result.assertComplete(snapshot != null, "Index does not exist");

            if (snapshot != null) {
                if (isUnique() != null) {
                    result.assertCorrect(isUnique().equals(snapshot.isUnique()),
                        "Unique does not match");
                }
            }

            return result;

        }
        catch (Exception e) {
            return result.unknown(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Index " + getIndexName() + " created";
    }

    /**
     * @param isUnique
     *            the isUnique to set
     */
    public void setUnique(Boolean isUnique) {
        change.setUnique(isUnique);
    }

    @DatabaseChangeProperty(description = "Unique values index", since = "1.8")
    public Boolean isUnique() {
        return change.isUnique();
    }

    /**
     * @return Index associations. Valid values:<br>
     *         <li>primaryKey</li> <li>foreignKey</li> <li>uniqueConstraint</li> <li>none</li>
     * */
    @DatabaseChangeProperty(isChangeProperty = false)
    public String getAssociatedWith() {
        return change.getAssociatedWith();
    }

    public void setAssociatedWith(String associatedWith) {
        change.setAssociatedWith(associatedWith);
    }

    @DatabaseChangeProperty(since = "3.0")
    public String getCatalogName() {
        return change.getCatalogName();
    }

    public void setCatalogName(String catalogName) {
        change.setCatalogName(catalogName);
    }

    public Boolean getClustered() {
        return change.getClustered();
    }

    public void setClustered(Boolean clustered) {
        change.setClustered(clustered);
    }
}

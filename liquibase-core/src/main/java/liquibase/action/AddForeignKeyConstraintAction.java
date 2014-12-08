package liquibase.action;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.ColumnConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.ExecutableChange;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.change.core.DropForeignKeyConstraintChange;
import liquibase.database.Database;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddForeignKeyConstraintStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.ForeignKeyConstraintType;
import liquibase.structure.core.Table;

import org.kohsuke.MetaInfServices;

/**
 * Adds a foreign key constraint to an existing column.
 */
@DatabaseChange(name="addForeignKeyConstraint", description = "Adds a foreign key constraint to an existing column", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
@MetaInfServices(ExecutableChange.class)
public class AddForeignKeyConstraintAction extends AbstractAction<AddForeignKeyConstraintChange> {

    public AddForeignKeyConstraintAction() {
        super(new AddForeignKeyConstraintChange());
    }

    public AddForeignKeyConstraintAction(AddForeignKeyConstraintChange change) {
        super(change);
    }

    // FIXME where to place this method?

//    @Override
//    protected String[] createSupportedDatabasesMetaData(String parameterName, DatabaseChangeProperty changePropertyAnnotation) {
//        if (parameterName.equals("deferrable") || parameterName.equals("initiallyDeferred")) {
//            List<String> supported = new ArrayList<String>();
//            for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
//                if (database.supportsInitiallyDeferrableColumns()) {
//                    supported.add(database.getShortName());
//                }
//            }
//            return supported.toArray(new String[supported.size()]);
//
//        } else {
//            return super.createSupportedDatabasesMetaData(parameterName, changePropertyAnnotation);
//        }
//    }

    @DatabaseChangeProperty(mustEqualExisting ="column.relation.catalog", since = "3.0")
    public String getBaseTableCatalogName() {
        return change.getBaseTableCatalogName();
    }

    public void setBaseTableCatalogName(String baseTableCatalogName) {
        change.setBaseTableCatalogName(baseTableCatalogName);
    }

    public String getBaseTableSchemaName() {
        return change.getBaseTableSchemaName();
    }

    public void setBaseTableSchemaName(String baseTableSchemaName) {
        change.setBaseTableSchemaName(baseTableSchemaName);
    }

    public String getBaseTableName() {
        return change.getBaseTableName();
    }

    public void setBaseTableName(String baseTableName) {
        change.setBaseTableName(baseTableName);
    }

    public String getBaseColumnNames() {
        return change.getBaseColumnNames();
    }

    public void setBaseColumnNames(String baseColumnNames) {
        change.setBaseColumnNames(baseColumnNames);
    }

    @DatabaseChangeProperty(since = "3.0", mustEqualExisting = "column")
    public String getReferencedTableCatalogName() {
        return change.getReferencedTableCatalogName();
    }

    public void setReferencedTableCatalogName(String referencedTableCatalogName) {
        change.setReferencedTableCatalogName(referencedTableCatalogName);
    }

    public String getReferencedTableSchemaName() {
        return change.getReferencedTableSchemaName();
    }

    public void setReferencedTableSchemaName(String referencedTableSchemaName) {
        change.setReferencedTableSchemaName(referencedTableSchemaName);
    }

    @DatabaseChangeProperty(description = "Name of the table the foreign key points to", exampleValue = "person")
    public String getReferencedTableName() {
        return change.getReferencedTableName();
    }

    public void setReferencedTableName(String referencedTableName) {
        change.setReferencedTableName(referencedTableName);
    }

    @DatabaseChangeProperty(description = "Column(s) the foreign key points to. Comma-separate if multiple", exampleValue = "id")
    public String getReferencedColumnNames() {
        return change.getReferencedColumnNames();
    }

    public void setReferencedColumnNames(String referencedColumnNames) {
        change.setReferencedColumnNames(referencedColumnNames);
    }

    @DatabaseChangeProperty(description = "Name of the new foreign key constraint", exampleValue = "fk_address_person")
    public String getConstraintName() {
        return change.getConstraintName();
    }

    public void setConstraintName(String constraintName) {
        change.setConstraintName(constraintName);
    }

    @DatabaseChangeProperty(description = "Is the foreign key deferrable")
    public Boolean getDeferrable() {
        return change.getDeferrable();
    }

    public void setDeferrable(Boolean deferrable) {
        change.setDeferrable(deferrable);
    }

    @DatabaseChangeProperty(description = "Is the foreign key initially deferred")
    public Boolean getInitiallyDeferred() {
        return change.getInitiallyDeferred();
    }

    public void setInitiallyDeferred(Boolean initiallyDeferred) {
        change.setInitiallyDeferred(initiallyDeferred);
    }

    public void setDeleteCascade(Boolean deleteCascade) {
        change.setDeleteCascade(deleteCascade);
    }

    public void setOnUpdate(String rule) {
        change.setOnUpdate(rule);
    }

    @DatabaseChangeProperty(description = "ON UPDATE functionality. Possible values: 'CASCADE', 'SET NULL', 'SET DEFAULT', 'RESTRICT', 'NO ACTION'", exampleValue = "RESTRICT")
    public String getOnUpdate() {
        return change.getOnUpdate();
    }

    public void setOnDelete(String onDelete) {
        change.setOnDelete(onDelete);
    }

    @DatabaseChangeProperty(description = "ON DELETE functionality. Possible values: 'CASCADE', 'SET NULL', 'SET DEFAULT', 'RESTRICT', 'NO ACTION'", exampleValue = "CASCADE")
    public String getOnDelete() {
        return change.getOnDelete();
    }

    public void setOnDelete(ForeignKeyConstraintType rule) {
        change.setOnDelete(rule);
    }

    public void setOnUpdate(ForeignKeyConstraintType rule) {
        change.setOnUpdate(rule);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

        boolean deferrable = false;
        if (getDeferrable() != null) {
            deferrable = getDeferrable();
        }

        boolean initiallyDeferred = false;
        if (getInitiallyDeferred() != null) {
            initiallyDeferred = getInitiallyDeferred();
        }

        return new SqlStatement[]{
                new AddForeignKeyConstraintStatement(getConstraintName(),
                        getBaseTableCatalogName(),
                        getBaseTableSchemaName(),
                        getBaseTableName(),
                        ColumnConfig.arrayFromNames(getBaseColumnNames()),
                        getReferencedTableCatalogName(),
                        getReferencedTableSchemaName(),
                        getReferencedTableName(),
                        ColumnConfig.arrayFromNames(getReferencedColumnNames()))
                        .setDeferrable(deferrable)
                        .setInitiallyDeferred(initiallyDeferred)
                        .setOnUpdate(getOnUpdate())
                        .setOnDelete(getOnDelete())
        };
    }

    @Override
    protected ExecutableChange[] createInverses() {
        DropForeignKeyConstraintChange inverse = new DropForeignKeyConstraintChange();
        inverse.setBaseTableSchemaName(getBaseTableSchemaName());
        inverse.setBaseTableName(getBaseTableName());
        inverse.setConstraintName(getConstraintName());

        return new ExecutableChange[]{
                inverse
        };
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            ForeignKey example = new ForeignKey(getConstraintName(), getBaseTableCatalogName(), getBaseTableSchemaName(), getBaseTableName());
            example.setPrimaryKeyTable(new Table(getReferencedTableCatalogName(), getReferencedTableSchemaName(), getReferencedTableName()));
            example.setForeignKeyColumns(Column.listFromNames(getBaseColumnNames()));
            example.setPrimaryKeyColumns(Column.listFromNames(getReferencedColumnNames()));

            ForeignKey snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
            result.assertComplete(snapshot != null, "Foreign key does not exist");

            if (snapshot != null) {
                if (getInitiallyDeferred() != null) {
                    result.assertCorrect(getInitiallyDeferred().equals(snapshot.isInitiallyDeferred()), "Initially deferred incorrect");
                }
                if (getDeferrable() != null) {
                    result.assertCorrect(getDeferrable().equals(snapshot.isDeferrable()), "Initially deferred incorrect");
                }
            }

            return result;

        } catch (Exception e) {
            return result.unknown(e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "Foreign key contraint added to " + getBaseTableName() + " (" + getBaseColumnNames() + ")";
    }
}

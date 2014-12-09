package liquibase.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import liquibase.change.AddColumnConfig;
import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.ChangeWithColumns;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.DropColumnChange;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.ForeignKeyConstraint;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.PrimaryKeyConstraint;
import liquibase.statement.SqlStatement;
import liquibase.statement.UniqueConstraint;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.statement.core.SetColumnRemarksStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

import org.kohsuke.MetaInfServices;

/**
 * Adds a column to an existing table.
 */
@DatabaseChange(name="addColumn", description = "Adds a new column to an existing table", priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table")
@MetaInfServices(ExecutableChange.class)
public class AddColumnAction extends AbstractAction<AddColumnChange> implements ChangeWithColumns<AddColumnConfig> {

    public AddColumnAction() {
        this(new AddColumnChange());
    }

    public AddColumnAction(AddColumnChange change) {
        super(change);
    }

    public String getCatalogName() {
        return change.getCatalogName();
    }

    public void setCatalogName(String catalogName) {
        change.setCatalogName(catalogName);
    }

    public String getSchemaName() {
        return change.getSchemaName();
    }

    public void setSchemaName(String schemaName) {
        change.setSchemaName(schemaName);
    }

    public String getTableName() {
        return change.getTableName();
    }

    public void setTableName(String tableName) {
        change.setTableName(tableName);
    }

    @Override
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

    public void removeColumn(ColumnConfig column) {
        change.removeColumn(column);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {

        List<SqlStatement> sql = new ArrayList<SqlStatement>();
        List<AddColumnStatement> addColumnStatements = new ArrayList<AddColumnStatement>();

        if (getColumns().size() == 0) {
            return new SqlStatement[] {
                    new AddColumnStatement(getCatalogName(), getSchemaName(), getTableName(), null, null, null)
            };
        }

        for (AddColumnConfig column : getColumns()) {
            Set<ColumnConstraint> constraints = new HashSet<ColumnConstraint>();
            ConstraintsConfig constraintsConfig =column.getConstraints();
            if (constraintsConfig != null) {
                if (constraintsConfig.isNullable() != null && !constraintsConfig.isNullable()) {
                    constraints.add(new NotNullConstraint());
                }
                if (constraintsConfig.isUnique() != null && constraintsConfig.isUnique()) {
                    constraints.add(new UniqueConstraint());
                }
                if (constraintsConfig.isPrimaryKey() != null && constraintsConfig.isPrimaryKey()) {
                    constraints.add(new PrimaryKeyConstraint(constraintsConfig.getPrimaryKeyName()));
                }

                if (constraintsConfig.getReferences() != null ||
                        (constraintsConfig.getReferencedColumnNames() != null && constraintsConfig.getReferencedTableName() != null)) {
                    constraints.add(new ForeignKeyConstraint(constraintsConfig.getForeignKeyName(), constraintsConfig.getReferences()
                            , constraintsConfig.getReferencedTableName(), constraintsConfig.getReferencedColumnNames()));
                }
            }

            if (column.isAutoIncrement() != null && column.isAutoIncrement()) {
                constraints.add(new AutoIncrementConstraint(column.getName(), column.getStartWith(), column.getIncrementBy()));
            }

            AddColumnStatement addColumnStatement = new AddColumnStatement(getCatalogName(), getSchemaName(),
                    getTableName(),
                    column.getName(),
                    column.getType(),
                    column.getDefaultValueObject(),
                    column.getRemarks(),
                    constraints.toArray(new ColumnConstraint[constraints.size()]));

            if ((database instanceof MySQLDatabase) && (column.getAfterColumn() != null)) {
                addColumnStatement.setAddAfterColumn(column.getAfterColumn());
            } else if (((database instanceof HsqlDatabase) || (database instanceof H2Database))
                       && (column.getBeforeColumn() != null)) {
                addColumnStatement.setAddBeforeColumn(column.getBeforeColumn());
            } else if ((database instanceof FirebirdDatabase) && (column.getPosition() != null)) {
                addColumnStatement.setAddAtPosition(column.getPosition());
            }

            addColumnStatements.add(addColumnStatement);

            if (database instanceof DB2Database) {
                sql.add(new ReorganizeTableStatement(getCatalogName(), getSchemaName(), getTableName()));
            }

            if (column.getValueObject() != null) {
                UpdateStatement updateStatement = new UpdateStatement(getCatalogName(), getSchemaName(), getTableName());
                updateStatement.addNewColumnValue(column.getName(), column.getValueObject());
                sql.add(updateStatement);
            }
        }

      if (addColumnStatements.size() == 1) {
          sql.add(0, addColumnStatements.get(0));
      } else {
          sql.add(0, new AddColumnStatement(addColumnStatements));
      }

      for (ColumnConfig column : getColumns()) {
          String columnRemarks = StringUtils.trimToNull(column.getRemarks());
          if (columnRemarks != null) {
              SetColumnRemarksStatement remarksStatement = new SetColumnRemarksStatement(getCatalogName(), getSchemaName(), getTableName(), column.getName(), columnRemarks);
              if (SqlGeneratorFactory.getInstance().supports(remarksStatement, database)) {
                  sql.add(remarksStatement);
              }
          }
      }

      return sql.toArray(new SqlStatement[sql.size()]);
    }

    @Override
    protected ExecutableChange[] createInverses() {
        List<ExecutableChange> inverses = new ArrayList<ExecutableChange>();

        DropColumnAction inverse = new DropColumnAction();
        DropColumnChange change = inverse.getChange();
        change.setSchemaName(getSchemaName());
        change.setTableName(getTableName());

        for (ColumnConfig aColumn : getColumns()) {
            if (aColumn.hasDefaultValue()) {
                DropDefaultValueAction dropAction = new DropDefaultValueAction();
                dropAction.setTableName(getTableName());
                dropAction.setColumnName(aColumn.getName());
                dropAction.setSchemaName(getSchemaName());

                inverses.add(dropAction);
            }

            change.addColumn(aColumn);
        }
        // FIXME
        //inverses.add(inverse);
        return inverses.toArray(new ExecutableChange[inverses.size()]);
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            for (AddColumnConfig column : getColumns()) {
                Column snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), column.getName()), database);
                result.assertComplete(snapshot != null, "Column "+column.getName()+" does not exist");

                if (snapshot != null) {
                    PrimaryKey snapshotPK = ((Table) snapshot.getRelation()).getPrimaryKey();

                    ConstraintsConfig constraints = column.getConstraints();
                    if (constraints != null) {
                        result.assertComplete(constraints.isPrimaryKey() == (snapshotPK != null && snapshotPK.getColumnNames().contains(column.getName())), "Column " + column.getName() + " not set as primary key");
                    }
                }
            }
        } catch (Exception e) {
            return result.unknown(e);
        }

        return result;
    }

    @Override
    public String getConfirmationMessage() {
        List<String> names = new ArrayList<String>(getColumns().size());
        for (ColumnConfig col : getColumns()) {
            names.add(col.getName() + "(" + col.getType() + ")");
        }

        return "Columns " + StringUtils.join(names, ",") + " added to " + getTableName();
    }
}

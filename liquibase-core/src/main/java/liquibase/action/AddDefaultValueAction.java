package liquibase.action;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import liquibase.change.ChangeMetaData;
import liquibase.change.ChangeStatus;
import liquibase.change.DatabaseChange;
import liquibase.change.ExecutableChange;
import liquibase.change.core.AddDefaultValueChange;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddDefaultValueStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.util.ISODateFormat;

import org.kohsuke.MetaInfServices;

/**
 * Sets a new default value to an existing column.
 */
@DatabaseChange(name = "addDefaultValue",
        description = "Adds a default value to the database definition for the specified column.\n" +
                "One of defaultValue, defaultValueNumeric, defaultValueBoolean or defaultValueDate must be set",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "column")
@MetaInfServices(ExecutableChange.class)
public class AddDefaultValueAction extends AbstractAction<AddDefaultValueChange> {

    public AddDefaultValueAction() {
        super(new AddDefaultValueChange());
    }

    public AddDefaultValueAction(AddDefaultValueChange change) {
        super(change);
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validate = new ValidationErrors();

        int nonNullValues = 0;
        if (getDefaultValue() != null) {
            nonNullValues++;
        }
        if (getDefaultValueNumeric() != null) {
            nonNullValues++;
        }
        if (getDefaultValueBoolean() != null) {
            nonNullValues++;
        }
        if (getDefaultValueDate() != null) {
            nonNullValues++;
        }
        if (getDefaultValueComputed() != null) {
            nonNullValues++;
        }
        if (getDefaultValueSequenceNext() != null) {
            nonNullValues++;
        }

        if (nonNullValues > 1) {
            validate.addError("Only one defaultValue* value can be specified");
        } else {
            validate.addAll(super.validate(database));
        }

        return validate;
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

    public String getColumnName() {
        return change.getColumnName();
    }

    public void setColumnName(String columnName) {
        change.setColumnName(columnName);
    }

    public String getColumnDataType() {
        return change.getColumnDataType();
    }

    public void setColumnDataType(String columnDataType) {
        change.setColumnDataType(columnDataType);
    }

    public String getDefaultValue() {
        return change.getDefaultValue();
    }

    public void setDefaultValue(String defaultValue) {
        change.setDefaultValue(defaultValue);
    }


    public String getDefaultValueNumeric() {
        return change.getDefaultValueNumeric();
    }

    public void setDefaultValueNumeric(String defaultValueNumeric) {
        change.setDefaultValueNumeric(defaultValueNumeric);
    }

    public String getDefaultValueDate() {
        return change.getDefaultValueDate();
    }

    public void setDefaultValueDate(String defaultValueDate) {
        change.setDefaultValueDate(defaultValueDate);
    }


    public Boolean getDefaultValueBoolean() {
        return change.getDefaultValueBoolean();
    }

    public void setDefaultValueBoolean(Boolean defaultValueBoolean) {
        change.setDefaultValueBoolean(defaultValueBoolean);
    }

    public DatabaseFunction getDefaultValueComputed() {
        return change.getDefaultValueComputed();
    }

    public void setDefaultValueComputed(DatabaseFunction defaultValueComputed) {
        change.setDefaultValueComputed(defaultValueComputed);
    }

    public SequenceNextValueFunction getDefaultValueSequenceNext() {
        return change.getDefaultValueSequenceNext();
    }

    public void setDefaultValueSequenceNext(SequenceNextValueFunction defaultValueSequenceNext) {
        change.setDefaultValueSequenceNext(defaultValueSequenceNext);
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        Object defaultValue = null;

        if (getDefaultValue() != null) {
            defaultValue = getDefaultValue();
        } else if (getDefaultValueBoolean() != null) {
            defaultValue = getDefaultValueBoolean();
        } else if (getDefaultValueNumeric() != null) {
            try {
                defaultValue = NumberFormat.getInstance(Locale.US).parse(getDefaultValueNumeric());
            } catch (ParseException e) {
                defaultValue = new DatabaseFunction(getDefaultValueNumeric());
            }
        } else if (getDefaultValueDate() != null) {
            try {
                defaultValue = new ISODateFormat().parse(getDefaultValueDate());
            } catch (ParseException e) {
                defaultValue = new DatabaseFunction(getDefaultValueDate());
            }
        } else if (getDefaultValueComputed() != null) {
            defaultValue = getDefaultValueComputed();
        } else if (getDefaultValueSequenceNext() != null) {
            defaultValue = getDefaultValueSequenceNext();
        }

        return new SqlStatement[]{
                new AddDefaultValueStatement(getCatalogName(), getSchemaName(), getTableName(), getColumnName(), getColumnDataType(), defaultValue)
        };
    }

    @Override
    protected ExecutableChange[] createInverses() {
        DropDefaultValueAction inverse = new DropDefaultValueAction();
        inverse.setSchemaName(getSchemaName());
        inverse.setTableName(getTableName());
        inverse.setColumnName(getColumnName());
        inverse.setColumnDataType(getColumnDataType());

        return new ExecutableChange[]{
                inverse
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Default value added to " + getTableName() + "." + getColumnName();
    }

    @Override
    public ChangeStatus checkStatus(Database database) {
        ChangeStatus result = new ChangeStatus();
        try {
            Column column = SnapshotGeneratorFactory.getInstance().createSnapshot(new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), getColumnName()), database);
            if (column == null) {
                return result.unknown("Column " + getColumnName() + " does not exist");
            }

            result.assertComplete(column.getDefaultValue() != null, "Column "+getColumnName()+" has no default value");
            if (column.getDefaultValue() == null) {
                return result;
            }

            if (getDefaultValue() != null) {
                return result.assertCorrect(getDefaultValue().equals(column.getDefaultValue()), "Default value was "+column.getDefaultValue());
            } else if (getDefaultValueDate() != null) {
                return result.assertCorrect(getDefaultValueDate().equals(new ISODateFormat().format((Date) column.getDefaultValue())), "Default value was "+column.getDefaultValue());
            } else if (getDefaultValueNumeric() != null) {
                return result.assertCorrect(getDefaultValueNumeric().equals(column.getDefaultValue().toString()), "Default value was "+column.getDefaultValue());
            } else if (getDefaultValueBoolean() != null) {
                return result.assertCorrect(getDefaultValueBoolean().equals(column.getDefaultValue()), "Default value was "+column.getDefaultValue());
            } else if (getDefaultValueComputed() != null) {
                return result.assertCorrect(getDefaultValueComputed().equals(column.getDefaultValue()), "Default value was "+column.getDefaultValue());
            } else if (getDefaultValueSequenceNext() != null) {
                return result.assertCorrect(getDefaultValueSequenceNext().equals(column.getDefaultValue()), "Default value was "+column.getDefaultValue());
            } else {
                return result.unknown("Unknown default value type");
            }
        } catch (Exception e) {
            return result.unknown(e);
        }
    }
}

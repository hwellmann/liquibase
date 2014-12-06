package liquibase.precondition.core;

import liquibase.precondition.AbstractPrecondition;
import liquibase.precondition.Precondition;
import liquibase.util.StringUtils;

import org.kohsuke.MetaInfServices;

@MetaInfServices(Precondition.class)
public class RowCountPrecondition extends AbstractPrecondition {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private Integer expectedRows;

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getExpectedRows() {
        return expectedRows;
    }

    public void setExpectedRows(Integer expectedRows) {
        this.expectedRows = expectedRows;
    }

//    public ValidationErrors validate(Database database) {
//        ValidationErrors validationErrors = new ValidationErrors();
//        validationErrors.checkRequiredField("tableName", tableName);
//        validationErrors.checkRequiredField("expectedRows", expectedRows);
//
//        return validationErrors;
//    }

    protected String getFailureMessage(int result) {
        return "Table "+tableName+" is not empty. Contains "+result+" rows";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "rowCount";
    }

}

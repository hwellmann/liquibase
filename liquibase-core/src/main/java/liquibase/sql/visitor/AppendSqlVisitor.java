package liquibase.sql.visitor;


public class AppendSqlVisitor extends AbstractSqlVisitor{
    private String value;


    @Override
    public String getName() {
        return "append";
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String modifySql(String sql) {
        return sql + value;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

}
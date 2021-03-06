package liquibase.change.core;

import liquibase.change.BaseChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;

import org.kohsuke.MetaInfServices;

/**
 * Creates a new view.
 */
@DatabaseChange(name="createView", description = "Create a new database view", priority = ChangeMetaData.PRIORITY_DEFAULT)
@MetaInfServices(Change.class)
public class CreateViewChange extends BaseChange {

    private String catalogName;
	private String schemaName;
	private String viewName;
	private String selectQuery;
	private Boolean replaceIfExists;
    private Boolean fullDefinition;


    @DatabaseChangeProperty(since = "3.0")
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
		this.schemaName = schemaName;
	}

    @DatabaseChangeProperty(description = "Name of the view to create")
	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

    @DatabaseChangeProperty(serializationType = SerializationType.DIRECT_VALUE, description = "SQL for generating the view", exampleValue = "select id, name from person where id > 10")
	public String getSelectQuery() {
		return selectQuery;
	}

	public void setSelectQuery(String selectQuery) {
		this.selectQuery = selectQuery;
	}

    @DatabaseChangeProperty(description = "Use 'create or replace' syntax", since = "1.5")
	public Boolean getReplaceIfExists() {
		return replaceIfExists;
	}

	public void setReplaceIfExists(Boolean replaceIfExists) {
		this.replaceIfExists = replaceIfExists;
	}

    @DatabaseChangeProperty(description = "Set to true if selectQuery is the entire view definition. False if the CREATE VIEW header should be added", since = "3.3")
    public Boolean getFullDefinition() {
        return fullDefinition;
    }

    public void setFullDefinition(Boolean fullDefinition) {
        this.fullDefinition = fullDefinition;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    protected void customLoadLogic(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        Object value = parsedNode.getValue();
        if (value instanceof String) {
            this.setSelectQuery((String) value);
        }
    }
}

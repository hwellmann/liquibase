package liquibase.change.core;

import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.exception.LiquibaseException;

import org.kohsuke.MetaInfServices;

@DatabaseChange(name="loadUpdateData",
        description = "Loads or updates data from a CSV file into an existing table. Differs from loadData by issuing a SQL batch that checks for the existence of a record. If found, the record is UPDATEd, else the record is INSERTed. Also, generates DELETE statements for a rollback.\n" +
                "\n" +
                "A value of NULL in a cell will be converted to a database NULL rather than the string 'NULL'",
        priority = ChangeMetaData.PRIORITY_DEFAULT, appliesTo = "table", since = "2.0")
@MetaInfServices(Change.class)
public class LoadUpdateDataChange extends LoadDataChange {
    private String primaryKey;
    private Boolean onlyUpdate = Boolean.FALSE;

    @Override
    @DatabaseChangeProperty(description = "Name of the table to insert or update data in", requiredForDatabase = "all")
    public String getTableName() {
        return super.getTableName();
    }

    public void setPrimaryKey(String primaryKey) throws LiquibaseException {
        this.primaryKey = primaryKey;
    }

    @DatabaseChangeProperty(description = "Comma delimited list of the columns for the primary key", requiredForDatabase = "all")
    public String getPrimaryKey() {
        return primaryKey;
    }

    @DatabaseChangeProperty(description = "If true, records with no matching database record should be ignored", since = "3.3" )
    public Boolean getOnlyUpdate() {
    	if ( onlyUpdate == null ) {
    		return false;
    	}
		return onlyUpdate;
	}

	public void setOnlyUpdate(Boolean onlyUpdate) {
		this.onlyUpdate = (onlyUpdate == null ? Boolean.FALSE : onlyUpdate) ;
	}

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}

package liquibase.structure.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import org.kohsuke.MetaInfServices;

@MetaInfServices(DatabaseObject.class)
public class Table extends Relation {


    public Table() {
        setAttribute("outgoingForeignKeys", new ArrayList<ForeignKey>());
        setAttribute("indexes", new ArrayList<Index>());
        setAttribute("uniqueConstraints", new ArrayList<UniqueConstraint>());
    }

    public Table(String catalogName, String schemaName, String tableName) {
        this.setSchema(new Schema(catalogName, schemaName));
        setName(tableName);
    }

    public PrimaryKey getPrimaryKey() {
        return getAttribute("primaryKey", PrimaryKey.class);
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.setAttribute("primaryKey", primaryKey);
    }

    public List<ForeignKey> getOutgoingForeignKeys() {
        return getAttribute("outgoingForeignKeys", List.class);
    }

    public List<Index> getIndexes() {
        return getAttribute("indexes", List.class);
    }

    public List<UniqueConstraint> getUniqueConstraints() {
        return getAttribute("uniqueConstraints", List.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Table that = (Table) o;

        return getName().equalsIgnoreCase(that.getName());

    }

    @Override
    public int hashCode() {
        return StringUtils.trimToEmpty(getName()).toUpperCase().hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Table setName(String name) {
        return (Table) super.setName(name);
    }

}

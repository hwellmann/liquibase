package liquibase.precondition.core;

import liquibase.precondition.ExecutablePrecondition;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ExecutablePrecondition.class)
public class TableIsEmptyPrecondition extends RowCountPrecondition {

    public TableIsEmptyPrecondition() {
        this.setExpectedRows(0);
    }

    @Override
    protected String getFailureMessage(int result) {
        return "Table "+getTableName()+" is not empty. Contains "+result+" rows";
    }

    @Override
    public String getName() {
        return "tableIsEmpty";
    }

}

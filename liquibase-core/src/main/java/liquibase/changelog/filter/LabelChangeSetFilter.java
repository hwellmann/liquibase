package liquibase.changelog.filter;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.changelog.ExecutableChangeSet;
import liquibase.sql.visitor.SqlVisitor;

import java.util.ArrayList;
import java.util.List;

public class LabelChangeSetFilter implements ChangeSetFilter {
    private LabelExpression labelExpression;

    public LabelChangeSetFilter() {
        this(new LabelExpression());
    }

    public LabelChangeSetFilter(LabelExpression labels) {
        this.labelExpression = labels;
    }

    @Override
    public ChangeSetFilterResult accepts(ExecutableChangeSet changeSet) {
        List<SqlVisitor> visitorsToRemove = new ArrayList<SqlVisitor>();
        for (SqlVisitor visitor : changeSet.getSqlVisitors()) {
            if (visitor.getLabels() != null && !labelExpression.matches(visitor.getLabels())) {
                visitorsToRemove.add(visitor);
            }
        }
        changeSet.getSqlVisitors().removeAll(visitorsToRemove);

        if (labelExpression == null || labelExpression.isEmpty()) {
            return new ChangeSetFilterResult(true, "No runtime labels specified, all labels will run", this.getClass());
        }

        if (changeSet.getLabels() == null || changeSet.getLabels().isEmpty()) {
            return new ChangeSetFilterResult(true, "Change set runs under all labels", this.getClass());
        }

        if (labelExpression.matches(changeSet.getLabels())) {
            return new ChangeSetFilterResult(true, "Labels matches '"+labelExpression.toString()+"'", this.getClass());
        } else {
            return new ChangeSetFilterResult(false, "Labels does not match '"+labelExpression.toString()+"'", this.getClass());
        }
    }
}

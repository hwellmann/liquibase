package liquibase.changelog;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.RuntimeEnvironment;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.filter.LabelChangeSetFilter;
import liquibase.changelog.visitor.ValidatingVisitor;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationFailedException;
import liquibase.logging.LogFactory;


public class ChangeLogValidator {


    private DatabaseChangeLogImpl changeLog;

    public ChangeLogValidator(DatabaseChangeLogImpl changeLog) {
        this.changeLog = changeLog;
    }
    public void validate(Database database, String... contexts) throws LiquibaseException {
        this.validate(database, new Contexts(contexts), new LabelExpression());
    }

    /**
     * @deprecated Use LabelExpression version
     */
    @Deprecated
    public void validate(Database database, Contexts contexts) throws LiquibaseException {
        this.validate(database, contexts, new LabelExpression());
    }

    public void validate(Database database, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {

        ChangeLogIterator logIterator = new ChangeLogIterator(changeLog, new DbmsChangeSetFilter(database), new ContextChangeSetFilter(contexts), new LabelChangeSetFilter(labelExpression));

        ValidatingVisitor validatingVisitor = new ValidatingVisitor(database.getRanChangeSetList());
        validatingVisitor.validate(database, changeLog);
        logIterator.run(validatingVisitor, new RuntimeEnvironment(database, contexts, labelExpression));

        for (String message : validatingVisitor.getWarnings().getMessages()) {
            LogFactory.getLogger().warning(message);
        }

        if (!validatingVisitor.validationPassed()) {
            throw new ValidationFailedException(validatingVisitor);
        }
    }

}

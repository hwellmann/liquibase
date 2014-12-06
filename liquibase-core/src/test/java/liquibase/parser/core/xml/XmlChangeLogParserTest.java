package liquibase.parser.core.xml;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import liquibase.changelog.ChangeLogParametersImpl;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.test.JUnitResourceAccessor;

import org.junit.Test;


public class XmlChangeLogParserTest {

    @Test
    public void shouldParsePrecondition() throws Exception {
        String path = "liquibase/parser/core/xml/precondition.xml";
        DatabaseChangeLog changeLog = new XMLChangeLogSAXParser().parse(path, new ChangeLogParametersImpl(), new JUnitResourceAccessor());


        ChangeSet changeSet = changeLog.getChangeSet(path, "nvoxland", "precondition attributes 1");
        assertThat(changeSet.getPreconditions().getOnSqlOutput(), is(PreconditionContainer.OnSqlOutputOption.FAIL));

    }

}

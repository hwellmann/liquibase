package liquibase.change

import liquibase.change.core.DropTableChange
import liquibase.util.StringUtils
import spock.lang.Specification

class ChangeFactorySpec extends Specification {

    def "getParameters with parameters set"() {
        when:
        def change = new DropTableChange()
        change.tableName = "tab"
        change.schemaName = "schem"

        then:
        ExecutableChangeFactory.instance.getParameters(change) == [tableName: "tab", schemaName: "schem"]
    }

    def "getParameters with no parameters set"() {
        when:
        def change = new DropTableChange()

        then:
        StringUtils.join(ExecutableChangeFactory.instance.getParameters(change), ",") == ""
    }
}

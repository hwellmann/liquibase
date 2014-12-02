package liquibase.parser.core.json;

import liquibase.parser.ChangeLogParser;
import liquibase.parser.core.yaml.YamlChangeLogParser;

import org.kohsuke.MetaInfServices;

@MetaInfServices(ChangeLogParser.class)
public class JsonChangeLogParser extends YamlChangeLogParser {

    @Override
    protected String[] getSupportedFileExtensions() {
        return new String[] {"json"};
    }
}
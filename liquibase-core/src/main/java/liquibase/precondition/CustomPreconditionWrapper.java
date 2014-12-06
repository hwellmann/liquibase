package liquibase.precondition;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;

import org.kohsuke.MetaInfServices;

@MetaInfServices(Precondition.class)
public class CustomPreconditionWrapper extends AbstractPrecondition {

    private String className;
    private ClassLoader classLoader;

    private SortedSet<String> params = new TreeSet<String>();
    private Map<String, String> paramValues = new HashMap<String, String>();

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public String getParamValue(String key) {
        return paramValues.get(key);
    }

    public void setParam(String name, String value) {
        this.params.add(name);
        this.paramValues.put(name, value);
    }




    /**
     * @return the params
     */
    public SortedSet<String> getParams() {
        return params;
    }


    /**
     * @return the paramValues
     */
    public Map<String, String> getParamValues() {
        return paramValues;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "customPrecondition";
    }

    @Override
    protected boolean shouldAutoLoad(ParsedNode node) {
        if (node.getName().equals("params")) {
            return false;
        }
        return super.shouldAutoLoad(node);
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        setClassLoader(resourceAccessor.toClassLoader());
        setClassName(parsedNode.getChildValue(null, "className", String.class));

        ParsedNode paramsNode = parsedNode.getChild(null, "params");
        if (paramsNode == null) {
            paramsNode = parsedNode;
        }

        for (ParsedNode child : paramsNode.getChildren(null, "param")) {
            Object value = child.getValue();
            if (value == null) {
                value = child.getChildValue(null, "value");
            }
            if (value != null) {
                value = value.toString();
            }
            this.setParam(child.getChildValue(null, "name", String.class), (String) value);
        }
        super.load(parsedNode, resourceAccessor);

    }
}

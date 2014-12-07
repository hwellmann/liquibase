package liquibase.change;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import liquibase.changelog.ChangeSet;
import liquibase.exception.SetupException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.ChangeLogSerializerFactory;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.util.StringUtils;

/**
 * Standard superclass to simplify {@link ExecutableChange } implementations. You can implement Change directly, this class is purely for convenience but recommended.
 * <p></p>
 * By default, this base class relies on annotations such as {@link DatabaseChange} and {@link DatabaseChangeProperty}
 * and delegating logic to the {@link liquibase.sqlgenerator.SqlGenerator} objects created to do the actual change work.
 * Place the @DatabaseChangeProperty annotations on the read "get" methods to control property metadata.
 */
public abstract class BaseChange implements Change {

    private ResourceAccessor resourceAccessor;

    private ChangeSet changeSet;

    public BaseChange() {
    }

    /**
     * Default implementation is a no-op
     */
    @Override
    public void finishInitialization() throws SetupException {

    }

    /**
     * Generate the ChangeMetaData for this class. Default implementation reads from the @{@link DatabaseChange } annotation
     * and calls out to {@link #createChangeParameterMetadata(String)} for each property.
     *
     * @throws UnexpectedLiquibaseException if no @DatabaseChange annotation on this Change class
     */
    @Override
    public ChangeMetaData createChangeMetaData() {
        try {
            DatabaseChange databaseChange = this.getClass().getAnnotation(DatabaseChange.class);

            if (databaseChange == null) {
                throw new UnexpectedLiquibaseException("No @DatabaseChange annotation for " + getClass().getName());
            }

            Set<ChangeParameterMetaData> params = new HashSet<ChangeParameterMetaData>();
            for (PropertyDescriptor property : Introspector.getBeanInfo(this.getClass()).getPropertyDescriptors()) {
                if (isInvalidProperty(property)) {
                    continue;
                }
                Method readMethod = property.getReadMethod();
                Method writeMethod = property.getWriteMethod();
                if (readMethod == null) {
                    try {
                        readMethod = this.getClass().getMethod("is" + StringUtils.upperCaseFirst(property.getName()));
                    } catch (Exception ignore) {
                        //it was worth a try
                    }
                }
                if (readMethod != null && writeMethod != null) {
                    DatabaseChangeProperty annotation = readMethod.getAnnotation(DatabaseChangeProperty.class);
                    if (annotation == null || annotation.isChangeProperty()) {
                        params.add(createChangeParameterMetadata(property.getDisplayName()));
                    }
                }

            }

            Map<String, String> notes = new HashMap<String, String>();
            for (DatabaseChangeNote note : databaseChange.databaseNotes()) {
                notes.put(note.database(), note.notes());
            }

            return new ChangeMetaData(databaseChange.name(), databaseChange.description(), databaseChange.priority(), databaseChange.appliesTo(), notes, params);
        } catch (Throwable e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    protected boolean isInvalidProperty(PropertyDescriptor property) {
        return property.getDisplayName().equals("metaClass");
    }

    /**
     * Called by {@link #createChangeMetaData()} to create metadata for a given parameter. It finds the method that corresponds to the parameter
     * and calls the corresponding create*MetaData methods such as {@link #createRequiredDatabasesMetaData(String, DatabaseChangeProperty)} to determine the
     * correct values for the ChangeParameterMetaData fields.
     *
     * @throws UnexpectedLiquibaseException if the passed parameter does not exist
     */
    protected ChangeParameterMetaData createChangeParameterMetadata(String parameterName) {

        try {
            String displayName = parameterName.replaceAll("([A-Z])", " $1");
            displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);

            PropertyDescriptor property = null;
            for (PropertyDescriptor prop : Introspector.getBeanInfo(this.getClass()).getPropertyDescriptors()) {
                if (prop.getDisplayName().equals(parameterName)) {
                    property = prop;
                    break;
                }
            }
            if (property == null) {
                throw new UnexpectedLiquibaseException("Could not find property " + parameterName);
            }

            Method readMethod = property.getReadMethod();
            if (readMethod == null) {
                readMethod = getClass().getMethod("is" + StringUtils.upperCaseFirst(property.getName()));
            }
            Type type = readMethod.getGenericReturnType();

            DatabaseChangeProperty changePropertyAnnotation = readMethod.getAnnotation(DatabaseChangeProperty.class);

            String mustEqualExisting = createMustEqualExistingMetaData(parameterName, changePropertyAnnotation);
            String description = createDescriptionMetaData(parameterName, changePropertyAnnotation);
            Map<String, Object> examples = createExampleValueMetaData(parameterName, changePropertyAnnotation);
            String since = createSinceMetaData(parameterName, changePropertyAnnotation);
            SerializationType serializationType = createSerializationTypeMetaData(parameterName, changePropertyAnnotation);
            String[] requiredForDatabase = createRequiredDatabasesMetaData(parameterName, changePropertyAnnotation);
            String[] supportsDatabase = createSupportedDatabasesMetaData(parameterName, changePropertyAnnotation);


            return new ChangeParameterMetaData(this, parameterName, displayName, description, examples, since, type, requiredForDatabase, supportsDatabase, mustEqualExisting, serializationType);
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    /**
     * Create the {@link ChangeParameterMetaData} "since" value. Uses the value on the DatabaseChangeProperty annotation or returns null as a default.
     */
    @SuppressWarnings("UnusedParameters")
    protected String createSinceMetaData(String parameterName, DatabaseChangeProperty changePropertyAnnotation) {
        if (changePropertyAnnotation == null) {
            return null;
        }
        return StringUtils.trimToNull(changePropertyAnnotation.since());
    }

    /**
     * Create the {@link ChangeParameterMetaData} "description" value. Uses the value on the DatabaseChangeProperty annotation or returns null as a default.
     */
    @SuppressWarnings("UnusedParameters")
    protected String createDescriptionMetaData(String parameterName, DatabaseChangeProperty changePropertyAnnotation) {
        if (changePropertyAnnotation == null) {
            return null;
        }
        return StringUtils.trimToNull(changePropertyAnnotation.description());
    }

    /**
     * Create the {@link ChangeParameterMetaData} "serializationType" value. Uses the value on the DatabaseChangeProperty annotation or returns {@link SerializationType}.NAMED_FIELD as a default.
     */
    @SuppressWarnings("UnusedParameters")
    protected SerializationType createSerializationTypeMetaData(String parameterName, DatabaseChangeProperty changePropertyAnnotation) {
        if (changePropertyAnnotation == null) {
            return SerializationType.NAMED_FIELD;
        }
        return changePropertyAnnotation.serializationType();
    }

    /**
     * Create the {@link ChangeParameterMetaData} "mustEqual" value. Uses the value on the DatabaseChangeProperty annotation or returns null as a default.
     */
    @SuppressWarnings("UnusedParameters")
    protected String createMustEqualExistingMetaData(String parameterName, DatabaseChangeProperty changePropertyAnnotation) {
        if (changePropertyAnnotation == null) {
            return null;
        }

        return changePropertyAnnotation.mustEqualExisting();
    }

    /**
     * Create the {@link ChangeParameterMetaData} "example" value. Uses the value on the DatabaseChangeProperty annotation or returns null as a default.
     * Returns map with key=database short name, value=example. Use short-name "all" as the fallback.
     */
    @SuppressWarnings("UnusedParameters")
    protected Map<String, Object> createExampleValueMetaData(String parameterName, DatabaseChangeProperty changePropertyAnnotation) {
        if (changePropertyAnnotation == null) {
            return null;
        }

        Map<String, Object> examples = new HashMap<String, Object>();
        examples.put("all", StringUtils.trimToNull(changePropertyAnnotation.exampleValue()));

        return examples;
    }

    /**
     * Create the {@link ChangeParameterMetaData} "requiredDatabases" value.
     * Uses the value on the DatabaseChangeProperty annotation or returns an array containing the string "COMPUTE" as a default.
     * "COMPUTE" will cause ChangeParameterMetaData to attempt to determine the required databases based on the generated Statements
     */
    @SuppressWarnings("UnusedParameters")
    protected String[] createRequiredDatabasesMetaData(String parameterName, DatabaseChangeProperty changePropertyAnnotation) {
        if (changePropertyAnnotation == null) {
            return new String[]{ChangeParameterMetaData.COMPUTE};
        } else {
            return changePropertyAnnotation.requiredForDatabase();
        }
    }

    /**
     * Create the {@link ChangeParameterMetaData} "supportedDatabase" value.
     * Uses the value on the DatabaseChangeProperty annotation or returns an array containing the string "COMPUTE" as a default.
     * "COMPUTE" will cause ChangeParameterMetaData to attempt to determine the required databases based on the generated Statements
     */
    @SuppressWarnings("UnusedParameters")
    protected String[] createSupportedDatabasesMetaData(String parameterName, DatabaseChangeProperty changePropertyAnnotation) {
        if (changePropertyAnnotation == null) {
            return new String[]{ChangeParameterMetaData.COMPUTE};
        } else {
            return changePropertyAnnotation.supportsDatabase();
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DatabaseChangeProperty(isChangeProperty = false)
    public ChangeSet getChangeSet() {
        return changeSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChangeSet(ChangeSet changeSet) {
        this.changeSet = (ChangeSet) changeSet;
    }

    /**
     * Implementation generates checksum by serializing the change with {@link StringChangeLogSerializer}
     */
    @Override
    public CheckSum generateCheckSum() {
        ChangeLogSerializer serializer = ChangeLogSerializerFactory.getInstance().getSerializer("txt");
        return CheckSum.compute(serializer.serialize(this, false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResourceAccessor(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    /**
     * @{inheritDoc}
     */
    @DatabaseChangeProperty(isChangeProperty = false)
    public ResourceAccessor getResourceAccessor() {
        return resourceAccessor;
    }

    /**
     * Returns the fields on this change that are serializable.
     */
    @Override
    public Set<String> getSerializableFields() {
        return ChangeFactory.getInstance().getChangeMetaData(this).getParameters().keySet();
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        return ChangeFactory.getInstance().getChangeMetaData(this).getParameters().get(field).getCurrentValue(this);
    }

    @Override
    public String getSerializedObjectName() {
        return ChangeFactory.getInstance().getChangeMetaData(this).getName();
    }

    @Override
    public SerializationType getSerializableFieldType(String field) {
        return ChangeFactory.getInstance().getChangeMetaData(this).getParameters().get(field).getSerializationType();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return GENERIC_CHANGELOG_EXTENSION_NAMESPACE;
    }

    @Override
    public String getSerializableFieldNamespace(String field) {
        return getSerializedObjectNamespace();
    }

    @Override
    public String toString() {
        return ChangeFactory.getInstance().getChangeMetaData(this).getName();
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        ChangeMetaData metaData = ChangeFactory.getInstance().getChangeMetaData(this);
        this.setResourceAccessor(resourceAccessor);
        try {
            for (ChangeParameterMetaData param : metaData.getParameters().values()) {
                if (Collection.class.isAssignableFrom(param.getDataTypeClass())) {
                    Class collectionType = (Class) param.getDataTypeClassParameters()[0];
                    if (param.getDataTypeClassParameters().length == 1) {
                        if (ColumnConfig.class.isAssignableFrom(collectionType)) {
                            List<ParsedNode> columnNodes = new ArrayList<ParsedNode>(parsedNode.getChildren(null, param.getParameterName()));
                            columnNodes.addAll(parsedNode.getChildren(null, "column"));

                            Object nodeValue = parsedNode.getValue();
                            if (nodeValue instanceof ParsedNode) {
                                columnNodes.add((ParsedNode) nodeValue);
                            } else if (nodeValue instanceof Collection) {
                                for (Object nodeValueChild : ((Collection) nodeValue)) {
                                    if (nodeValueChild instanceof ParsedNode) {
                                        columnNodes.add((ParsedNode) nodeValueChild);
                                    }
                                }
                            }


                            for (ParsedNode child : columnNodes) {
                                if (child.getName().equals("column") || child.getName().equals("columns")) {
                                    List<ParsedNode> columnChildren = child.getChildren(null, "column");
                                    if (columnChildren != null && columnChildren.size() > 0) {
                                        for (ParsedNode columnChild : columnChildren) {
                                            ColumnConfig columnConfig = (ColumnConfig) collectionType.newInstance();
                                            columnConfig.load(columnChild, resourceAccessor);
                                            ((ChangeWithColumns) this).addColumn(columnConfig);
                                        }
                                    } else {
                                        ColumnConfig columnConfig = (ColumnConfig) collectionType.newInstance();
                                        columnConfig.load(child, resourceAccessor);
                                        ((ChangeWithColumns) this).addColumn(columnConfig);
                                    }
                                }
                            }
                        } else if (LiquibaseSerializable.class.isAssignableFrom(collectionType)) {
                            List<ParsedNode> childNodes = new ArrayList<ParsedNode>(parsedNode.getChildren(null, param.getParameterName()));
                            for (ParsedNode childNode : childNodes) {
                                LiquibaseSerializable childObject = (LiquibaseSerializable) collectionType.newInstance();
                                childObject.load(childNode, resourceAccessor);

                                ((Collection) param.getCurrentValue(this)).add(childObject);

                            }
                        }
                    }
                } else if (LiquibaseSerializable.class.isAssignableFrom(param.getDataTypeClass())) {
                    try {
                        ParsedNode child = parsedNode.getChild(null, param.getParameterName());
                        if (child != null) {
                            LiquibaseSerializable serializableChild = (LiquibaseSerializable) param.getDataTypeClass().newInstance();
                            serializableChild.load(child, resourceAccessor);
                            param.setValue(this, serializableChild);
                        }
                    } catch (InstantiationException e) {
                        throw new UnexpectedLiquibaseException(e);
                    } catch (IllegalAccessException e) {
                        throw new UnexpectedLiquibaseException(e);
                    }
                } else {
                    Object childValue = parsedNode.getChildValue(null, param.getParameterName(), param.getDataTypeClass());
                    if (childValue == null && param.getSerializationType() == SerializationType.DIRECT_VALUE) {
                        childValue = parsedNode.getValue();
                    }
                    param.setValue(this, childValue);
                }
            }
        } catch (InstantiationException e) {
            throw new UnexpectedLiquibaseException(e);
        } catch (IllegalAccessException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        customLoadLogic(parsedNode, resourceAccessor);
        try {
            this.finishInitialization();
        } catch (SetupException e) {
            throw new ParsedNodeException(e);
        }
    }

    protected void customLoadLogic(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {

    }

    @Override
    public ParsedNode serialize() throws ParsedNodeException {
        ParsedNode node = new ParsedNode(null, getSerializedObjectName());
        ChangeMetaData metaData = ChangeFactory.getInstance().getChangeMetaData(this);
        for (ChangeParameterMetaData param : metaData.getSetParameters(this).values()) {
            Object currentValue = param.getCurrentValue(this);
            currentValue = serializeValue(currentValue);
            if (currentValue != null) {
                node.addChild(null, param.getParameterName(), currentValue);
            }
        }

        return node;
    }

    protected Object serializeValue(Object value) throws ParsedNodeException {
        if (value instanceof Collection) {
            List returnList = new ArrayList();
            for (Object obj : (Collection) value) {
                Object objValue = serializeValue(obj);
                if (objValue != null) {
                    returnList.add(objValue);
                }
            }
            if (((Collection) value).size() == 0) {
                return null;
            } else {
                return returnList;
            }
        } else if (value instanceof LiquibaseSerializable) {
            return ((LiquibaseSerializable) value).serialize();
        } else {
            return value;
        }
    }
}

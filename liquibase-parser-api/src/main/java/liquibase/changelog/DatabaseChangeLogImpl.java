package liquibase.changelog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.LiquibaseException;
import liquibase.exception.SetupException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.UnknownChangelogFormatException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.precondition.Conditional;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StreamUtil;
import liquibase.util.file.FilenameUtils;

/**
 * Encapsulates the information stored in the change log XML file.
 */
public class DatabaseChangeLogImpl implements Conditional, DatabaseChangeLog {
    private PreconditionContainer preconditionContainer = new PreconditionContainer();
    private String physicalFilePath;
    private String logicalFilePath;
    private ObjectQuotingStrategy objectQuotingStrategy;

    private List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
    private ChangeLogParameters changeLogParameters;

    private boolean ignoreClasspathPrefix = false;

    public DatabaseChangeLogImpl() {
    }

    public DatabaseChangeLogImpl(String physicalFilePath) {
        this.physicalFilePath = physicalFilePath;
    }

    /* (non-Javadoc)
     * @see liquibase.changelog.IDatabaseChangeLog#getPreconditions()
     */
    @Override
    public PreconditionContainer getPreconditions() {
        return preconditionContainer;
    }

    /* (non-Javadoc)
     * @see liquibase.changelog.IDatabaseChangeLog#setPreconditions(liquibase.precondition.Precondition)
     */
    @Override
    public void setPreconditions(PreconditionContainer precond) {
        if (precond == null) {
            this.preconditionContainer = new PreconditionContainer();
        } else {
            preconditionContainer = precond;
        }
    }


    /* (non-Javadoc)
     * @see liquibase.changelog.IDatabaseChangeLog#getChangeLogParameters()
     */
    @Override
    public ChangeLogParameters getChangeLogParameters() {
        return changeLogParameters;
    }

    /* (non-Javadoc)
     * @see liquibase.changelog.IDatabaseChangeLog#setChangeLogParameters(liquibase.changelog.ChangeLogParameters)
     */
    @Override
    public void setChangeLogParameters(ChangeLogParameters changeLogParameters) {
        this.changeLogParameters = changeLogParameters;
    }

    /* (non-Javadoc)
     * @see liquibase.changelog.IDatabaseChangeLog#getPhysicalFilePath()
     */
    @Override
    public String getPhysicalFilePath() {
        return physicalFilePath;
    }

    /* (non-Javadoc)
     * @see liquibase.changelog.IDatabaseChangeLog#setPhysicalFilePath(java.lang.String)
     */
    @Override
    public void setPhysicalFilePath(String physicalFilePath) {
        this.physicalFilePath = physicalFilePath;
    }

    /* (non-Javadoc)
     * @see liquibase.changelog.IDatabaseChangeLog#getLogicalFilePath()
     */
    @Override
    public String getLogicalFilePath() {
        String returnPath = logicalFilePath;
        if (logicalFilePath == null) {
            returnPath = physicalFilePath;
        }
        return returnPath.replaceAll("\\\\", "/");
    }

    /* (non-Javadoc)
     * @see liquibase.changelog.IDatabaseChangeLog#setLogicalFilePath(java.lang.String)
     */
    @Override
    public void setLogicalFilePath(String logicalFilePath) {
        this.logicalFilePath = logicalFilePath;
    }

    /* (non-Javadoc)
     * @see liquibase.changelog.IDatabaseChangeLog#getFilePath()
     */
    @Override
    public String getFilePath() {
        if (logicalFilePath == null) {
            return physicalFilePath;
        } else {
            return logicalFilePath;
        }
    }

    /* (non-Javadoc)
     * @see liquibase.changelog.IDatabaseChangeLog#getObjectQuotingStrategy()
     */
    @Override
    public ObjectQuotingStrategy getObjectQuotingStrategy() {
        return objectQuotingStrategy;
    }

    /* (non-Javadoc)
     * @see liquibase.changelog.IDatabaseChangeLog#setObjectQuotingStrategy(liquibase.database.ObjectQuotingStrategy)
     */
    @Override
    public void setObjectQuotingStrategy(ObjectQuotingStrategy objectQuotingStrategy) {
        this.objectQuotingStrategy = objectQuotingStrategy;
    }

    @Override
    public String toString() {
        return getFilePath();
    }

    @Override
    public int compareTo(DatabaseChangeLog o) {
        return getFilePath().compareTo(o.getFilePath());
    }


    /* (non-Javadoc)
     * @see liquibase.changelog.IDatabaseChangeLog#getChangeSet(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ChangeSet getChangeSet(String path, String author, String id) {
        for (ChangeSet changeSet : changeSets) {
            if (normalizePath(changeSet.getFilePath()).equalsIgnoreCase(normalizePath(path))
                    && changeSet.getAuthor().equalsIgnoreCase(author)
                    && changeSet.getId().equalsIgnoreCase(id)
                    && (changeSet.getDbmsSet() == null
                    || changeLogParameters == null
                    || changeLogParameters.getValue("database.typeName") == null
                    || changeSet.getDbmsSet().isEmpty()
                    || changeSet.getDbmsSet().contains(changeLogParameters.getValue("database.typeName").toString()))) {
                return changeSet;
            }
        }

        return null;
    }

    /* (non-Javadoc)
     * @see liquibase.changelog.IDatabaseChangeLog#getChangeSets()
     */
    @Override
    public List<ChangeSet> getChangeSets() {
        return changeSets;
    }

    /* (non-Javadoc)
     * @see liquibase.changelog.IDatabaseChangeLog#addChangeSet(liquibase.changelog.ChangeSet)
     */
    @Override
    public void addChangeSet(ChangeSet changeSet) {
        this.changeSets.add(changeSet);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseChangeLog that = (DatabaseChangeLog) o;

        return getFilePath().equals(that.getFilePath());

    }

    @Override
    public int hashCode() {
        return getFilePath().hashCode();
    }

    public ChangeSet getChangeSet(RanChangeSet ranChangeSet) {
        return getChangeSet(ranChangeSet.getChangeLog(), ranChangeSet.getAuthor(), ranChangeSet.getId());
    }

    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException, SetupException {
        setLogicalFilePath(parsedNode.getChildValue(null, "logicalFilePath", String.class));
        String objectQuotingStrategy = parsedNode.getChildValue(null, "objectQuotingStrategy", String.class);
        if (objectQuotingStrategy != null) {
            setObjectQuotingStrategy(ObjectQuotingStrategy.valueOf(objectQuotingStrategy));
        }
        for (ParsedNode childNode : parsedNode.getChildren()) {
            handleChildNode(childNode, resourceAccessor);
        }
    }

    protected void expandExpressions(ParsedNode parsedNode) {
        if (changeLogParameters == null) {
            return;
        }
        try {
            Object value = parsedNode.getValue();
            if (value != null && value instanceof String) {
                parsedNode.setValue(changeLogParameters.expandExpressions(parsedNode.getValue(String.class)));
            }

            List<ParsedNode> children = parsedNode.getChildren();
            if (children != null) {
                for (ParsedNode child : children) {
                    expandExpressions(child);
                }
            }
        } catch (ParsedNodeException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    protected void handleChildNode(ParsedNode node, ResourceAccessor resourceAccessor) throws ParsedNodeException, SetupException {
        expandExpressions(node);
        String nodeName = node.getName();
        if (nodeName.equals("changeSet")) {
            this.addChangeSet(createChangeSet(node, resourceAccessor));
        } else if (nodeName.equals("include")) {
            String path = node.getChildValue(null, "file", String.class);
            path = path.replace('\\', '/');
            try {
                include(path, node.getChildValue(null, "relativeToChangelogFile", false), resourceAccessor);
            } catch (LiquibaseException e) {
                throw new SetupException(e);
            }
        } else if (nodeName.equals("includeAll")) {
            String path = node.getChildValue(null, "path", String.class);
            String resourceFilterDef = node.getChildValue(null, "resourceFilter", String.class);
            IncludeAllFilter resourceFilter = null;
            if (resourceFilterDef != null) {
                try {
                    resourceFilter = (IncludeAllFilter) Class.forName(resourceFilterDef).newInstance();
                } catch (Exception e) {
                    throw new SetupException(e);
                }
            }

            includeAll(path, node.getChildValue(null, "relativeToChangelogFile", false), resourceFilter, getStandardChangeLogComparator(), resourceAccessor);
        } else if (nodeName.equals("preConditions")) {
            this.preconditionContainer = new PreconditionContainer();
            try {
                this.preconditionContainer.load(node, resourceAccessor);
            } catch (ParsedNodeException e) {
                e.printStackTrace();
            }
        } else if (nodeName.equals("property")) {
            try {
                String context = node.getChildValue(null, "context", String.class);
                String dbms = node.getChildValue(null, "dbms", String.class);
                String labels = node.getChildValue(null, "labels", String.class);

                if (node.getChildValue(null, "file", String.class) == null) {
                    this.changeLogParameters.set(node.getChildValue(null, "name", String.class), node.getChildValue(null, "value", String.class), context, labels, dbms);
                } else {
                    Properties props = new Properties();
                    InputStream propertiesStream = StreamUtil.singleInputStream(node.getChildValue(null, "file", String.class), resourceAccessor);
                    if (propertiesStream == null) {
                        LogFactory.getInstance().getLog().info("Could not open properties file " + node.getChildValue(null, "file", String.class));
                    } else {
                        props.load(propertiesStream);

                        for (Map.Entry entry : props.entrySet()) {
                            this.changeLogParameters.set(entry.getKey().toString(), entry.getValue().toString(), context, labels, dbms);
                        }
                    }
                }
            } catch (IOException e) {
                throw new ParsedNodeException(e);
            }

        }
    }

    public void includeAll(String pathName, boolean isRelativeToChangelogFile, IncludeAllFilter resourceFilter, Comparator<String> resourceComparator, ResourceAccessor resourceAccessor) throws SetupException {
        try {
            pathName = pathName.replace('\\', '/');

            if (!(pathName.endsWith("/"))) {
                pathName = pathName + '/';
            }
            Logger log = LogFactory.getInstance().getLog();
            log.debug("includeAll for " + pathName);
            log.debug("Using file opener for includeAll: " + resourceAccessor.toString());

            String relativeTo = null;
            if (isRelativeToChangelogFile) {
                relativeTo = this.getPhysicalFilePath();
            }

            Set<String> unsortedResources = resourceAccessor.list(relativeTo, pathName, true, false, true);
            SortedSet<String> resources = new TreeSet<String>(resourceComparator);
            if (unsortedResources != null) {
                for (String resourcePath : unsortedResources) {
                    if (resourceFilter == null || resourceFilter.include(resourcePath)) {
                        resources.add(resourcePath);
                    }
                }
            }

            if (resources.size() == 0) {
                throw new SetupException("Could not find directory or directory was empty for includeAll '" + pathName + "'");
            }

            for (String path : resources) {
                include(path, false, resourceAccessor);
            }
        } catch (Exception e) {
            throw new SetupException(e);
        }
    }

    protected boolean include(String fileName, boolean isRelativePath, ResourceAccessor resourceAccessor) throws LiquibaseException {

        if (fileName.equalsIgnoreCase(".svn") || fileName.equalsIgnoreCase("cvs")) {
            return false;
        }

        String relativeBaseFileName = this.getPhysicalFilePath();
        if (isRelativePath) {
            // workaround for FilenameUtils.normalize() returning null for relative paths like ../conf/liquibase.xml
            String tempFile = FilenameUtils.concat(FilenameUtils.getFullPath(relativeBaseFileName), fileName);
            if (tempFile != null && new File(tempFile).exists() == true) {
                fileName = tempFile;
            } else {
                fileName = FilenameUtils.getFullPath(relativeBaseFileName) + fileName;
            }
        }
        DatabaseChangeLogImpl changeLog;
        try {
            changeLog = (DatabaseChangeLogImpl) ChangeLogParserFactory.getInstance().getParser(fileName, resourceAccessor).parse(fileName, changeLogParameters, resourceAccessor);
        } catch (UnknownChangelogFormatException e) {
            LogFactory.getInstance().getLog().warning("included file " + relativeBaseFileName + "/" + fileName + " is not a recognized file type");
            return false;
        }
        PreconditionContainer preconditions = changeLog.getPreconditions();
        if (preconditions != null) {
            if (null == this.getPreconditions()) {
                this.setPreconditions(new PreconditionContainer());
            }
            this.getPreconditions().addNestedPrecondition(preconditions);
        }
        for (ChangeSet changeSet : changeLog.getChangeSets()) {
            this.changeSets.add(changeSet);
        }

        return true;
    }

    protected ChangeSet createChangeSet(ParsedNode node, ResourceAccessor resourceAccessor) throws ParsedNodeException, SetupException {
        ChangeSetImpl changeSet = new ChangeSetImpl(this);
        changeSet.setChangeLogParameters(this.getChangeLogParameters());
        try {
            changeSet.load(node, resourceAccessor);
        } catch (ParsedNodeException e) {
            e.printStackTrace();
        }
        return changeSet;
    }

    protected Comparator<String> getStandardChangeLogComparator() {
        return new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1. compareTo(o2);
            }
        };
    }

    /* (non-Javadoc)
     * @see liquibase.changelog.IDatabaseChangeLog#setIgnoreClasspathPrefix(boolean)
     */
    @Override
    public void setIgnoreClasspathPrefix(boolean ignoreClasspathPrefix) {
        this.ignoreClasspathPrefix = ignoreClasspathPrefix;
    }

    /* (non-Javadoc)
     * @see liquibase.changelog.IDatabaseChangeLog#ignoreClasspathPrefix()
     */
    @Override
    public boolean ignoreClasspathPrefix() {
        return ignoreClasspathPrefix;
    }

    protected String normalizePath(String filePath) {
        if (ignoreClasspathPrefix) {
            return filePath.replaceFirst("^classpath:", "");
        }
        return filePath;
    }
}

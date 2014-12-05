package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Labels;

public interface ChangeLogParameters {

    void setContexts(Contexts contexts);

    Contexts getContexts();

    void set(String paramter, Object value);

    void set(String key, String value, String contexts, String labels, String databases);

    void set(String key, String value, ContextExpression contexts, Labels labels, String databases);

    /**
     * Return the value of a parameter
     *
     * @param key Name of the parameter
     * @return The parameter value or null if not found. (Note that null can also be return if it is the parameter value. For
     *         strict parameter existence use {@link #hasValue(String)))
     */
    Object getValue(String key);

    boolean hasValue(String key);

    String expandExpressions(String string);

    void setLabels(LabelExpression labels);

    LabelExpression getLabels();

}

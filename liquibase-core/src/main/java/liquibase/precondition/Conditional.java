package liquibase.precondition;


public interface Conditional {
    public Precondition getPreconditions();

    public void setPreconditions(Precondition precond);

}

package plus.justice.workers;

public interface Worker {
    /**
     * concatenate codes and save to disk
     */
    void concatenate();


    /**
     * compile codes, for languages like python just return
     */
    void compile();


    /**
     * run in sandbox
     * @return String
     */
    String run();
}
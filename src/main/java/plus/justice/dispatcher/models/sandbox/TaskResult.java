package plus.justice.dispatcher.models.sandbox;

public class TaskResult {
    private Long runtime;
    private Long memory;
    private Integer status;
    private String error;
    private String input;
    private String output;
    private String expected;

    public Long getRuntime() {
        return runtime;
    }

    public void setRuntime(Long runtime) {
        this.runtime = runtime;
    }

    public Long getMemory() {
        return memory;
    }

    public void setMemory(Long memory) {
        this.memory = memory;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getExpected() {
        return expected;
    }

    public void setExpected(String expected) {
        this.expected = expected;
    }

    @Override
    public String toString() {
        return "TaskResult{" +
                "runtime=" + runtime +
                ", memory=" + memory +
                ", status=" + status +
                ", error='" + error + '\'' +
                ", input='" + input + '\'' +
                ", output='" + output + '\'' +
                ", expected='" + expected + '\'' +
                '}';
    }
}

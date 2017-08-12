package plus.justice.dispatcher.models.sandbox;

public class TaskResult {
    private Integer runtime;
    private Integer memory;
    private Integer status;
    private String error;

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer rumtime) {
        this.runtime = rumtime;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
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

    @Override
    public String toString() {
        return "TaskResult{" +
                "rumtime=" + runtime +
                ", memory=" + memory +
                ", status=" + status +
                ", error='" + error + '\'' +
                '}';
    }
}

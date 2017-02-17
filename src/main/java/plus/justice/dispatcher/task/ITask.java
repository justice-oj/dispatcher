package plus.justice.dispatcher.task;

import plus.justice.dispatcher.model.CompileResult;
import plus.justice.dispatcher.model.RunResult;

public interface ITask {
    public void save(String code);

    public CompileResult compile();

    public RunResult run();

    public void remove();
}
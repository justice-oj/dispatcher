package plus.justice.dispatcher.service;

import plus.justice.dispatcher.model.RunResult;
import plus.justice.dispatcher.task.impl.JavaTask;

public interface IRunTaskService {
    RunResult run(JavaTask task);
}

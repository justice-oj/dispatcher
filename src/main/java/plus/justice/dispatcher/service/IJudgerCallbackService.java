package plus.justice.dispatcher.service;

public interface IJudgerCallbackService {
    public void call(long sid, long uid, long pid, int status, int runtime, int memory);
}

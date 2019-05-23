package mymou.task.backend;

public interface TaskInterface {
    void resetTimer_();
    void trialEnded_(String outcome);
    void logEvent_(String event);
}

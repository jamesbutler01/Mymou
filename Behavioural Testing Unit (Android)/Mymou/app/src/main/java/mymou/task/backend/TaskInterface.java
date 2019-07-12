package mymou.task.backend;

public interface TaskInterface {
    void resetTimer_();
    void trialEnded_(String outcome, int rew_scalar);
    void logEvent_(String event);
}

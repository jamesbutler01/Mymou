package mymou.task.backend;

public interface TaskInterface {
    void resetTimer_();
    void trialEnded_(String outcome, double rew_scalar);
    void logEvent_(String event);
    void giveRewardFromTask_(int amount);
    void takePhotoFromTask_();
    void  commitTrialDataFromTask_(String overallTrialOutcome);
    void setBrightnessFromTask_(boolean bool);
}

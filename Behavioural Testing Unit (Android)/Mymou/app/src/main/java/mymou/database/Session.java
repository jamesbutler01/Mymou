package mymou.database;
import android.text.format.Time;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
public class Session {
    @PrimaryKey(autoGenerate=true)
    public int uid;
//
//    @ColumnInfo
//    public Task task;
//
//    @ColumnInfo
//    public Monkey monkey;
//
//    @ColumnInfo
//    public Date date;
//
//    @ColumnInfo
//    public Date startTime;
//
//    @ColumnInfo
//    public SimpleDateFormat stopTime;

    @ColumnInfo
    public int total_reward_given;

    @ColumnInfo
    public int total_corr_trials;

    @ColumnInfo
    public int total_incorr_trials;

    @ColumnInfo
    public int num_incorr_trials;
//
//    @ColumnInfo
//    public int[] reward_by_type;
//
//    @ColumnInfo
//    public int[] corr_trials_over_time;
//
//    @ColumnInfo
//    public int[] incorr_trials_over_time;


}
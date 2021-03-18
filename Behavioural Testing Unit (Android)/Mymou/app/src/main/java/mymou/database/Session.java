package mymou.database;
import android.text.format.Time;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
public class Session {

    @PrimaryKey  // We only want one entry per date, so use this as primary key
    @NonNull
    public String date;

    @ColumnInfo
    public int ms_reward_given;

    @ColumnInfo
    public int num_corr_trials;

    @ColumnInfo
    public int num_trials;

    @ColumnInfo
    public int task;


}
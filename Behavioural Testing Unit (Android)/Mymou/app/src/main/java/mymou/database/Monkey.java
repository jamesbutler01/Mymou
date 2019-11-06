package mymou.database;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
public class Monkey {

    @PrimaryKey(autoGenerate=true)
    public int uid;

    @ColumnInfo
    public String name;

    @ColumnInfo
    public Date dob;




}
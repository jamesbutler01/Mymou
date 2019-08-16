package mymou.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {User.class, Session.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class MymouDatabase extends RoomDatabase {

    public abstract UserDao userDao();

}
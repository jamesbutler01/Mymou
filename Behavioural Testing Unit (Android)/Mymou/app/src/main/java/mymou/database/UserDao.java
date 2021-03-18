package mymou.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM session")
    List<Session> getAllSessions();

//    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
//    List<User> loadAllByIds(int[] userIds);

//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    User findByName(String first, String last);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertSession(Session session);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    void updateSession(Session session);

//    @Transaction
//    public void upsertSession(Session session) {
//        long id = insertSession(session);
//        if (id == -1) {
//            updateSession(session);
//        }
//    }

    @Delete
    void deleteSession(Session session);
}
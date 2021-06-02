package mymou.database;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public final class UserDao_Impl implements UserDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter __insertionAdapterOfSession;

  private final EntityDeletionOrUpdateAdapter __deletionAdapterOfSession;

  private final EntityDeletionOrUpdateAdapter __updateAdapterOfSession;

  public UserDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSession = new EntityInsertionAdapter<Session>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR IGNORE INTO `Session`(`date`,`ms_reward_given`,`num_corr_trials`,`num_trials`,`task`) VALUES (?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Session value) {
        if (value.date == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.date);
        }
        stmt.bindLong(2, value.ms_reward_given);
        stmt.bindLong(3, value.num_corr_trials);
        stmt.bindLong(4, value.num_trials);
        stmt.bindLong(5, value.task);
      }
    };
    this.__deletionAdapterOfSession = new EntityDeletionOrUpdateAdapter<Session>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `Session` WHERE `date` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Session value) {
        if (value.date == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.date);
        }
      }
    };
    this.__updateAdapterOfSession = new EntityDeletionOrUpdateAdapter<Session>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR IGNORE `Session` SET `date` = ?,`ms_reward_given` = ?,`num_corr_trials` = ?,`num_trials` = ?,`task` = ? WHERE `date` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Session value) {
        if (value.date == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.date);
        }
        stmt.bindLong(2, value.ms_reward_given);
        stmt.bindLong(3, value.num_corr_trials);
        stmt.bindLong(4, value.num_trials);
        stmt.bindLong(5, value.task);
        if (value.date == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.date);
        }
      }
    };
  }

  @Override
  public long insertSession(Session session) {
    __db.beginTransaction();
    try {
      long _result = __insertionAdapterOfSession.insertAndReturnId(session);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteSession(Session session) {
    __db.beginTransaction();
    try {
      __deletionAdapterOfSession.handle(session);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updateSession(Session session) {
    __db.beginTransaction();
    try {
      __updateAdapterOfSession.handle(session);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<Session> getAllSessions() {
    final String _sql = "SELECT * FROM session";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final Cursor _cursor = __db.query(_statement);
    try {
      final int _cursorIndexOfDate = _cursor.getColumnIndexOrThrow("date");
      final int _cursorIndexOfMsRewardGiven = _cursor.getColumnIndexOrThrow("ms_reward_given");
      final int _cursorIndexOfNumCorrTrials = _cursor.getColumnIndexOrThrow("num_corr_trials");
      final int _cursorIndexOfNumTrials = _cursor.getColumnIndexOrThrow("num_trials");
      final int _cursorIndexOfTask = _cursor.getColumnIndexOrThrow("task");
      final List<Session> _result = new ArrayList<Session>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final Session _item;
        _item = new Session();
        _item.date = _cursor.getString(_cursorIndexOfDate);
        _item.ms_reward_given = _cursor.getInt(_cursorIndexOfMsRewardGiven);
        _item.num_corr_trials = _cursor.getInt(_cursorIndexOfNumCorrTrials);
        _item.num_trials = _cursor.getInt(_cursorIndexOfNumTrials);
        _item.task = _cursor.getInt(_cursorIndexOfTask);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }
}

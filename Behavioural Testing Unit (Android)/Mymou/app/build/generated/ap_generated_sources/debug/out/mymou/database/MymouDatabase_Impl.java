package mymou.database;

import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenHelper;
import androidx.room.RoomOpenHelper.Delegate;
import androidx.room.util.TableInfo;
import androidx.room.util.TableInfo.Column;
import androidx.room.util.TableInfo.ForeignKey;
import androidx.room.util.TableInfo.Index;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration;
import java.lang.IllegalStateException;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.HashMap;
import java.util.HashSet;

@SuppressWarnings("unchecked")
public final class MymouDatabase_Impl extends MymouDatabase {
  private volatile UserDao _userDao;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `Session` (`date` TEXT NOT NULL, `ms_reward_given` INTEGER NOT NULL, `num_corr_trials` INTEGER NOT NULL, `num_trials` INTEGER NOT NULL, `task` INTEGER NOT NULL, PRIMARY KEY(`date`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, \"3a06aad0365f912b16c95f0027af6e1a\")");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `Session`");
      }

      @Override
      protected void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      @Override
      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      @Override
      protected void validateMigration(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsSession = new HashMap<String, TableInfo.Column>(5);
        _columnsSession.put("date", new TableInfo.Column("date", "TEXT", true, 1));
        _columnsSession.put("ms_reward_given", new TableInfo.Column("ms_reward_given", "INTEGER", true, 0));
        _columnsSession.put("num_corr_trials", new TableInfo.Column("num_corr_trials", "INTEGER", true, 0));
        _columnsSession.put("num_trials", new TableInfo.Column("num_trials", "INTEGER", true, 0));
        _columnsSession.put("task", new TableInfo.Column("task", "INTEGER", true, 0));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSession = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSession = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSession = new TableInfo("Session", _columnsSession, _foreignKeysSession, _indicesSession);
        final TableInfo _existingSession = TableInfo.read(_db, "Session");
        if (! _infoSession.equals(_existingSession)) {
          throw new IllegalStateException("Migration didn't properly handle Session(mymou.database.Session).\n"
                  + " Expected:\n" + _infoSession + "\n"
                  + " Found:\n" + _existingSession);
        }
      }
    }, "3a06aad0365f912b16c95f0027af6e1a", "25ca09b21bf93dce391f40300c679040");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    return new InvalidationTracker(this, "Session");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `Session`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  public UserDao userDao() {
    if (_userDao != null) {
      return _userDao;
    } else {
      synchronized(this) {
        if(_userDao == null) {
          _userDao = new UserDao_Impl(this);
        }
        return _userDao;
      }
    }
  }
}

package com.example.youtubedownloader.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class DownloadDao_Impl implements DownloadDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DownloadHistory> __insertionAdapterOfDownloadHistory;

  private final EntityDeletionOrUpdateAdapter<DownloadHistory> __deletionAdapterOfDownloadHistory;

  public DownloadDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDownloadHistory = new EntityInsertionAdapter<DownloadHistory>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `DownloadHistory` (`id`,`url`,`filePath`,`date`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final DownloadHistory entity) {
        statement.bindLong(1, entity.id);
        if (entity.url == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.url);
        }
        if (entity.filePath == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.filePath);
        }
        statement.bindLong(4, entity.date);
      }
    };
    this.__deletionAdapterOfDownloadHistory = new EntityDeletionOrUpdateAdapter<DownloadHistory>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `DownloadHistory` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final DownloadHistory entity) {
        statement.bindLong(1, entity.id);
      }
    };
  }

  @Override
  public void insert(final DownloadHistory history) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfDownloadHistory.insert(history);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final DownloadHistory history) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfDownloadHistory.handle(history);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<DownloadHistory> getAll() {
    final String _sql = "SELECT * FROM DownloadHistory ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
      final int _cursorIndexOfFilePath = CursorUtil.getColumnIndexOrThrow(_cursor, "filePath");
      final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
      final List<DownloadHistory> _result = new ArrayList<DownloadHistory>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final DownloadHistory _item;
        _item = new DownloadHistory();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfUrl)) {
          _item.url = null;
        } else {
          _item.url = _cursor.getString(_cursorIndexOfUrl);
        }
        if (_cursor.isNull(_cursorIndexOfFilePath)) {
          _item.filePath = null;
        } else {
          _item.filePath = _cursor.getString(_cursorIndexOfFilePath);
        }
        _item.date = _cursor.getLong(_cursorIndexOfDate);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

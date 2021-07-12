package net.gauso001.shinigami_chanaiclient;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Alarm.class}, version = 2, exportSchema = false)
public abstract class alarm_DB extends RoomDatabase {
    public abstract Alarm_DaoAccess Alarm_daoAccess() ;
}

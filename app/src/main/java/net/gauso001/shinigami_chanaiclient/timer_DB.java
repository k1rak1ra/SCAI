package net.gauso001.shinigami_chanaiclient;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Timer.class}, version = 1, exportSchema = false)
public abstract class timer_DB extends RoomDatabase {
    public abstract Timer_DaoAccess Timer_daoAccess() ;
}

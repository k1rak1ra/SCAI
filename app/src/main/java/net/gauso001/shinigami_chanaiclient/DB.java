package net.gauso001.shinigami_chanaiclient;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Message.class}, version = 1, exportSchema = false)
public abstract class DB extends RoomDatabase {
    public abstract DaoAccess daoAccess() ;
}

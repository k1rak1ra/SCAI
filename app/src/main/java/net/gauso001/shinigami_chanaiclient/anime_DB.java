package net.gauso001.shinigami_chanaiclient;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Anime.class}, version = 5, exportSchema = false)
public abstract class anime_DB extends RoomDatabase {
    public abstract Anime_DaoAccess Anime_daoAccess() ;
}

package net.gauso001.shinigami_chanaiclient;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface Anime_DaoAccess {

    @Insert
    void insert_single_anime(Anime anime);
    @Insert
    void insert_all(List<Anime> anime);
    @Query("SELECT * FROM Anime WHERE id = :m_id")
    Anime fetch(int m_id);
    @Query("SELECT * FROM Anime")
    List<Anime> fetch_list();
    @Query("DELETE FROM Anime WHERE id = :m_id")
    void delete(int m_id);
    @Query("DELETE FROM Anime")
    void nukeTable();

}

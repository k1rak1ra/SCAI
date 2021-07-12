package net.gauso001.shinigami_chanaiclient;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface Character_DaoAccess {

    @Insert
    void insert_single(Character character);
    @Insert
    void insert_all(List<Character> character);
    @Query("SELECT * FROM Character WHERE id = :m_id")
    Character fetch(int m_id);
    @Query("SELECT * FROM Character")
    List<Character> fetch_list();
    @Query("DELETE FROM Character WHERE id = :m_id")
    void delete(int m_id);
    @Query("DELETE FROM Character")
    void nukeTable();

}

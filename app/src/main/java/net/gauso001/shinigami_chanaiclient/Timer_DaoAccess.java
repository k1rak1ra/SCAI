package net.gauso001.shinigami_chanaiclient;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface Timer_DaoAccess {

    @Insert
    void insert_single_timer(Timer timer);
    @Query("SELECT * FROM Timer WHERE id = :m_id")
    Timer fetch(int m_id);
    @Query("SELECT * FROM Timer")
    List<Timer> fetch_timers();
    @Query("DELETE FROM Timer WHERE id = :m_id")
    void delete(int m_id);

}

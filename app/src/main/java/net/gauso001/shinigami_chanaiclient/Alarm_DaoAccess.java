package net.gauso001.shinigami_chanaiclient;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface Alarm_DaoAccess {

    @Insert
    void insert_single_alarm(Alarm alarm);
    @Query("SELECT * FROM Alarm WHERE id = :m_id")
    Alarm fetch(int m_id);
    @Query("SELECT * FROM Alarm")
    List<Alarm> fetch_alarms();
    @Query("SELECT * FROM Alarm WHERE time > :before AND time <= :after")
    List<Alarm> fetch_range (long before, long after);
    @Query("SELECT * FROM Alarm WHERE override = 1")
    List<Alarm> fetch_overriding_alarms();
    @Query("DELETE FROM Alarm WHERE id = :m_id")
    void delete(int m_id);
    @Query("UPDATE Alarm SET override = 1 WHERE id = :m_id")
    void set_to_override(int m_id);

}

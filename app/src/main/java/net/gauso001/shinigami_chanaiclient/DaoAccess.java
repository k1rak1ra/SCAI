package net.gauso001.shinigami_chanaiclient;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface DaoAccess {

    @Insert
    void insert_single_message (Message message);
    @Insert
    void insert_multiple_message (List<Message> messageList);
    @Query("SELECT * FROM Message WHERE id = :m_id")
    Message fetch_message_by_id (int m_id);
    @Query("SELECT * FROM (SELECT * FROM Message ORDER BY ID DESC LIMIT 200) ORDER BY ID ASC")
    List<Message> fetch_messages ();
    @Query("UPDATE Message set stage = :s WHERE id = (SELECT MAX(id) FROM Message)")
    void update_last_stage(int s);

}

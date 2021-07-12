package net.gauso001.shinigami_chanaiclient;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.RoomDatabase;
import android.support.annotation.NonNull;

@Entity
public class Message {
    @PrimaryKey(autoGenerate = true)
    protected int id;
    protected String msg;
    protected int stage;

    public Message() {
    }

    public int get_id() { return id; }
    public void set_id(int id) { this.id = id; }
    public String get_msg() { return msg; }
    public void set_msg (String msg) { this.msg = msg; }
    public int get_stage() { return stage; }
    public void set_stage(int stage) { this.stage = stage; }
}

package net.gauso001.shinigami_chanaiclient;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.support.annotation.NonNull;

import java.util.Date;

@Entity
public class Timer {
    @PrimaryKey
    @NonNull
    protected int id;
    protected long end;



    public Timer() {
    }

    public int get_id() { return id; }
    public void set_id(int id) { this.id = id; }
    public long get_end() { return end; }
    public void set_end (Date end) { this.end = end.getTime(); }

}

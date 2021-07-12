package net.gauso001.shinigami_chanaiclient;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

@Entity
public class Alarm {
    @PrimaryKey
    @NonNull
    protected int id;
    protected long time;
    int override = 0;


    public Alarm() {
    }

    public int get_id() { return id; }
    public void set_id(int id) { this.id = id; }
    public long get_time() { return time; }
    public void set_time (Date time) { this.time = time.getTime(); }
    public int get_override() { return override; }
    public void set_override (int o) { this.override = o; }

}

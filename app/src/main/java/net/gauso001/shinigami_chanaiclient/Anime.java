package net.gauso001.shinigami_chanaiclient;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Anime {
    @PrimaryKey(autoGenerate = true)
    protected int id;
    String MALid;
    String image_URL;
    String name;
    String type;
    String score;
    String numEpisodes;
    String description;
    String AirDate;
    boolean current = false;
    long new_ep_day = 0;
    int streamType = 0;
    String streamLink = "";

    public Anime(String MALid, String image_URL, String name, String type, String score, String numEpisodes, String description, String AirDate)
    {
        this.MALid = MALid;
        this.image_URL = image_URL;
        this.name = name;
        this.type = type;
        this.score = score;
        this.numEpisodes = numEpisodes;
        this.description = description;
        this.AirDate = AirDate;
    }
}

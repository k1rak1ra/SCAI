package net.gauso001.shinigami_chanaiclient;

import android.app.Activity;
import android.app.Dialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class screen_main_adapter extends BaseAdapter {
    List<Message> msg;

    String tutor_number_local;
    Context context;
    private static LayoutInflater inflater=null;
    public screen_main_adapter(screen_main screen_main, List<Message> m_pass) {
        // Auto-generated constructor stub
        context=screen_main;
        msg = m_pass;



        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // Auto-generated method stub
        return msg.size();
    }

    @Override
    public Object getItem(int position) {
        // Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // Auto-generated method stub
        return position;
    }

    private class Holder
    {

        TextView msg_text;
        TextView stage;
    }

    String hour_string = "";
    String minute_string = "";
    String second_string = "";
    int delay = 1; //milliseconds

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        // Auto-generated method stub
        final Holder holder = new Holder();
        View rowView;

        //negative stage number = message is response
        //-1 = generic text message
        if (msg.get(position).get_stage()==-1) {

            rowView = inflater.inflate(R.layout.msg_left, null);
            holder.msg_text= rowView.findViewById(R.id.msg_text);
            holder.msg_text.setBackground(context.getResources().getDrawable(R.drawable.b_black));
            holder.msg_text.setText(msg.get(position).get_msg());
            holder.msg_text.setTextColor(Color.WHITE);
        }
        //-2 = anime details in JSON
        else if(msg.get(position).get_stage() == -2)
        {
            rowView = inflater.inflate(R.layout.conversation_anime_card, null);
            ImageView m_a_img = rowView.findViewById(R.id.m_a_img);
            TextView m_a_title = rowView.findViewById(R.id.m_a_title);
            TextView m_a_score = rowView.findViewById(R.id.m_a_score);
            TextView m_a_dates = rowView.findViewById(R.id.m_a_dates);
            TextView m_a_ep_count = rowView.findViewById(R.id.m_a_ep_count);
            TextView m_a_desc = rowView.findViewById(R.id.m_a_desc);
            TextView m_a_studio = rowView.findViewById(R.id.m_a_studio);
            TextView m_a_genre = rowView.findViewById(R.id.m_a_genre);
            TextView m_a_prequel = rowView.findViewById(R.id.m_a_prequel);
            TextView m_a_sequel = rowView.findViewById(R.id.m_a_sequel);
            TextView m_a_tags = rowView.findViewById(R.id.m_a_tags);
            ListView m_a_list = rowView.findViewById(R.id.m_a_charalist);
            //TextView m_a_OP = rowView.findViewById(R.id.m_a_OP);
            //TextView m_a_ED = rowView.findViewById(R.id.m_a_ED);

            try {
                JSONObject obj = new JSONObject(msg.get(position).get_msg()).getJSONObject("Media");

                Picasso.get().load(obj.getJSONObject("coverImage").getString("large")).into(m_a_img);
                m_a_title.setText(obj.getJSONObject("title").getString("romaji"));
                if (!obj.getString("averageScore").equals("null")) {
                    m_a_score.setText(obj.getString("averageScore"));
                }
                else
                {
                    m_a_score.setText("No score data");
                }
                if (obj.getString("status").equals("NOT_YET_RELEASED"))
                {
                    String ad = "";
                    if (!obj.getJSONObject("startDate").getString("day").equals("null")){ ad += obj.getJSONObject("startDate").getString("day")+"/"; }
                    if (!obj.getJSONObject("startDate").getString("month").equals("null")){ ad += obj.getJSONObject("startDate").getString("month")+"/"; }
                    if (!obj.getJSONObject("startDate").getString("year").equals("null")){ ad += obj.getJSONObject("startDate").getString("year"); }
                    m_a_dates.setText("Will start airing "+ad);
                }
                else if (obj.getString("status").equals("RELEASING"))
                {
                    m_a_dates.setText("Currently airing");
                }
                else {
                    String ad = "";
                    if (!obj.getJSONObject("startDate").getString("day").equals("null")){ ad += obj.getJSONObject("startDate").getString("day")+"/"; }
                    if (!obj.getJSONObject("startDate").getString("month").equals("null")){ ad += obj.getJSONObject("startDate").getString("month")+"/"; }
                    if (!obj.getJSONObject("startDate").getString("year").equals("null")){ ad += obj.getJSONObject("startDate").getString("year"); }
                    m_a_dates.setText("Aired " + ad);
                }
                if (obj.getString("episodes").equals("null"))
                {
                    m_a_ep_count.setText(obj.getString("format"));
                }
                else {
                    m_a_ep_count.setText(obj.getString("format") + " - " + obj.getString("episodes") + " episodes");
                }
                try {
                    m_a_desc.setText(obj.getString("description").replace("<br>", "\n"));
                } catch (JSONException e) { m_a_desc.setText("No description"); }
                JSONArray studios = obj.getJSONObject("studios").getJSONArray("nodes");
                String studio_string = "";
                for (int i = 0; i < studios.length(); i++) {
                    JSONObject studio = studios.getJSONObject(i);
                    studio_string += studio.getString("name")+"\n";
                }
                m_a_studio.setText(studio_string);
                JSONArray genres = obj.getJSONArray("genres");
                String genre_string = genres.getString(0);
                for (int i = 1; i < genres.length(); i++) {
                    genre_string += ", "+genres.getString(i);
                }
                m_a_genre.setText(genre_string);

                JSONArray tags = obj.getJSONArray("tags");
                String tag_string = "";
                for (int i = 0; i < tags.length(); i++)
                {
                    if (tags.getJSONObject(i).getString("isGeneralSpoiler").equals("false") && tags.getJSONObject(i).getString("isMediaSpoiler").equals("false"))
                    {
                        tag_string += tags.getJSONObject(i).getString("name")+" - "+tags.getJSONObject(i).getString("rank")+"% \n";
                    }
                }
                m_a_tags.setText(tag_string);

                try
                {
                    String prequel_text = "Prequel: \n";
                    int prequel_count = 0;
                    String sequel_text = "Sequel: \n";
                    int sequel_count = 0;
                    JSONArray nodes = obj.getJSONObject("relations").getJSONArray("nodes");
                    JSONArray types = obj.getJSONObject("relations").getJSONArray("edges");
                    for (int i = 0; i < types.length(); i++)
                    {
                        if (types.getJSONObject(i).getString("relationType").equals("PREQUEL"))
                        {
                            prequel_count++;
                            prequel_text += "["+prequel_count+"] "+nodes.getJSONObject(i).getJSONObject("title").getString("romaji");
                        }
                        else if (types.getJSONObject(i).getString("relationType").equals("SEQUEL"))
                        {
                            sequel_count++;
                            sequel_text += "["+sequel_count+"] "+nodes.getJSONObject(i).getJSONObject("title").getString("romaji");
                        }
                    }
                    if (prequel_count > 0) { m_a_prequel.setText(prequel_text); } else { m_a_prequel.setText("No prequel"); }
                    if (sequel_count > 0) { m_a_sequel.setText(sequel_text); } else { m_a_sequel.setText("No sequel"); }
                }
                catch (JSONException e)
                {
                    m_a_prequel.setText("No prequel");
                    m_a_sequel.setText("No sequel");
                }

                m_a_list.setAdapter(new chara_list_adapter((screen_main)context, obj.getJSONObject("characters").getJSONArray("nodes"),obj.getJSONObject("characters").getJSONArray("edges")));
                setListViewHeightBasedOnItems(m_a_list);

/*
                try {
                    JSONArray OPs = obj.getJSONArray("opening_theme");
                    String OP_string = "[1] " + OPs.getJSONObject(0).getString("track") + " by " + OPs.getJSONObject(0).getString("by");
                    for (int i = 1; i < OPs.length(); i++) {
                        JSONObject OP = OPs.getJSONObject(i);
                        int n = i + 1;
                        OP_string += "\n[" + n + "] " + OP.getString("track") + " by " + OP.getString("by");
                    }
                    m_a_OP.setText("OP: \n" + OP_string);
                } catch (JSONException e) { m_a_OP.setText("No OP found"); }

                try
                {
                JSONArray EDs = obj.getJSONArray("ending_theme");
                String ED_string = "[1] "+EDs.getJSONObject(0).getString("track")+" by "+EDs.getJSONObject(0).getString("by");
                for (int i = 1; i < EDs.length(); i++) {
                    JSONObject ED = EDs.getJSONObject(i);
                    int n = i+1;
                    ED_string += "\n["+n+"] " + ED.getString("track")+" by "+ ED.getString("by");
                }
                m_a_ED.setText("ED: \n"+ED_string);
                } catch (JSONException e) { m_a_ED.setText("No ED found"); }
*/
            }
                catch (JSONException e) { System.out.println(e); }
        }
        //23 = stage 2/3, sent for processing
        else if (msg.get(position).get_stage() == 23)
        {
            rowView = inflater.inflate(R.layout.msg_right, null);
            holder.msg_text= rowView.findViewById(R.id.msg_text_r);
            holder.msg_text.setBackground(context.getResources().getDrawable(R.drawable.b_red));
            holder.msg_text.setText(msg.get(position).get_msg());
            holder.msg_text.setTextColor(Color.WHITE);

            holder.stage = rowView.findViewById(R.id.msg_stage);
            holder.stage.setTextColor(Color.WHITE);
            holder.stage.setText("「stage 2/3」");
            ProgressBar pending = (ProgressBar)rowView.findViewById(R.id.msg_stage_pending);
            pending.setVisibility(View.VISIBLE);
        }
        //-3 = reminder
        else if (msg.get(position).get_stage() == -3)
        {
            rowView = inflater.inflate(R.layout.reminder_dialog_card, null);

            final TextView m_r_thing = rowView.findViewById(R.id.m_r_thing);
            final TextView m_r_time = rowView.findViewById(R.id.m_r_time);
            final TextView m_r_location = rowView.findViewById(R.id.m_r_location);

            String[] data = msg.get(position).get_msg().split("\\|");

            m_r_thing.setText(data[1]);
            m_r_time.setText(data[2]);
            m_r_location.setText("Location: "+data[3]);
        }
        //-4 = timer
        else if (msg.get(position).get_stage() == -4) {
            rowView = inflater.inflate(R.layout.timer_dialog_card, null);
            final String[] data = msg.get(position).get_msg().split("\\|");

            final TextView m_t_expired = rowView.findViewById(R.id.m_t_expired);
            final TextView m_t_s = rowView.findViewById(R.id.m_t_s);
            final TextView m_t_m = rowView.findViewById(R.id.m_t_m);
            final TextView m_t_h = rowView.findViewById(R.id.m_t_h);
            final TextView m_t_mid = rowView.findViewById(R.id.m_t_mid);
            final TextView m_t_mid2 = rowView.findViewById(R.id.m_t_mid2);

            final timer_DB timer_DB = Room.databaseBuilder(context,
                    timer_DB.class, "timer_db")
                    .build();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();

                    final Timer timer = timer_DB.Timer_daoAccess().fetch(Integer.parseInt(data[1]));

                    if (timer != null) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                        final Handler h = new Handler();

                        h.post(new Runnable() {
                            public void run() {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                            delay = 100;
                                            long total_left = (timer.get_end() - new Date().getTime());

                                            if (total_left <= 0) {
                                                timer_DB.close();
                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        m_t_s.setVisibility(View.INVISIBLE);
                                                        m_t_m.setVisibility(View.INVISIBLE);
                                                        m_t_h.setVisibility(View.INVISIBLE);
                                                        m_t_mid.setVisibility(View.INVISIBLE);
                                                        m_t_mid2.setVisibility(View.INVISIBLE);
                                                        m_t_expired.setVisibility(View.VISIBLE);
                                                    }
                                                });
                                            }
                                            else {

                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        m_t_s.setVisibility(View.VISIBLE);
                                                        m_t_m.setVisibility(View.VISIBLE);
                                                        m_t_h.setVisibility(View.VISIBLE);
                                                        m_t_mid.setVisibility(View.VISIBLE);
                                                        m_t_mid2.setVisibility(View.VISIBLE);
                                                        m_t_expired.setVisibility(View.INVISIBLE);
                                                    }
                                                });

                                                long hours_left = total_left / 3600000;
                                                total_left -= (hours_left) * 3600000;

                                                long minutes_left = total_left / 60000;
                                                total_left -= (minutes_left) * 60000;

                                                long seconds_left = total_left / 1000;


                                                if (hours_left < 10) {
                                                    hour_string = "0" + String.valueOf(hours_left);
                                                } else {
                                                    hour_string = String.valueOf(hours_left);
                                                }

                                                if (minutes_left < 10) {
                                                    minute_string = "0" + String.valueOf(minutes_left);
                                                } else {
                                                    minute_string = String.valueOf(minutes_left);
                                                }

                                                if (seconds_left < 10) {
                                                    second_string = "0" + String.valueOf(seconds_left);
                                                } else {
                                                    second_string = String.valueOf(seconds_left);
                                                }

                                                ((Activity) context).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        m_t_h.setText(hour_string);
                                                        m_t_m.setText(minute_string);
                                                        m_t_s.setText(second_string);
                                                    }
                                                });
                                            }

                                    }
                                }).start();
                                h.postDelayed(this, delay);
                            }
                        });
                            }
                        });
                    }
                    else
                    {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                m_t_s.setVisibility(View.INVISIBLE);
                                m_t_m.setVisibility(View.INVISIBLE);
                                m_t_h.setVisibility(View.INVISIBLE);
                                m_t_mid.setVisibility(View.INVISIBLE);
                                m_t_mid2.setVisibility(View.INVISIBLE);
                                m_t_expired.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
        }) .start();
        }
        // -5 = setting alarm
        else if (msg.get(position).get_stage() == -5) {
            final String[] data = msg.get(position).get_msg().split("\\|");

            rowView = inflater.inflate(R.layout.msg_left, null);
            holder.msg_text= rowView.findViewById(R.id.msg_text);
            holder.msg_text.setBackground(context.getResources().getDrawable(R.drawable.b_black));
            holder.msg_text.setText(data[0]);
            holder.msg_text.setTextColor(Color.WHITE);

        }
        // -6 = character details
        else if (msg.get(position).get_stage() == -6) {
            rowView = inflater.inflate(R.layout.conversation_chara_card, null);
            ImageView chara_img = rowView.findViewById(R.id.chara_img);
            TextView chara_name = rowView.findViewById(R.id.chara_name);
            TextView chara_desc = rowView.findViewById(R.id.chara_desc);
            ListView chara_a_list = rowView.findViewById(R.id.chara_a_l);

            try {
                JSONObject obj = new JSONObject(msg.get(position).get_msg());

                Picasso.get().load(obj.getJSONObject("image").getString("large")).into(chara_img);

                String name_text = "";
                if (!obj.getJSONObject("name").getString("first").equals("null"))
                {
                    name_text = obj.getJSONObject("name").getString("first");
                }
                if (!obj.getJSONObject("name").getString("last").equals("null"))
                {
                    name_text += " "+obj.getJSONObject("name").getString("last");
                }
                chara_name.setText(name_text);
                chara_desc.setText(obj.getString("description").replace("<br>", "\n"));

                chara_a_list.setAdapter(new anime_list_adapter((screen_main)context, obj.getJSONObject("media").getJSONArray("nodes")));
                setListViewHeightBasedOnItems(chara_a_list);

            }
            catch (JSONException e) {}
        }
        // -7 = fanart image
        else if (msg.get(position).get_stage() == -7) {
            rowView = inflater.inflate(R.layout.conversation_image_card, null);
            ImageView img = rowView.findViewById(R.id.fa_img);
                Picasso.get().load(msg.get(position).get_msg()) .resize(1500, 1500)
                        .onlyScaleDown().centerCrop().into(img);
        }
        else
        {
            rowView = inflater.inflate(R.layout.msg_right, null);
            holder.msg_text= rowView.findViewById(R.id.msg_text_r);
            holder.msg_text.setBackground(context.getResources().getDrawable(R.drawable.b_red));
            holder.msg_text.setText(msg.get(position).get_msg());
            holder.msg_text.setTextColor(Color.WHITE);

            holder.stage = rowView.findViewById(R.id.msg_stage);
            holder.stage.setTextColor(Color.WHITE);
            holder.stage.setText("「stage "+msg.get(position).get_stage()+"」");

        }






        return rowView;
    }




    //adapter for embedded chara-list
    public class chara_list_adapter extends BaseAdapter {
        JSONArray nodes;
        JSONArray tags;


        JSONParser jsonParser = new JSONParser();
        private String AL = "https://graphql.anilist.co";

        Context context;
        private LayoutInflater inflater = null;

        public chara_list_adapter(screen_main screen_main, JSONArray n, JSONArray t) {
            // Auto-generated constructor stub
            context = screen_main;
            nodes = n;
            tags=t;

            inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            // Auto-generated method stub
            System.out.println("LENGTH: "+tags.length());
            return tags.length();
        }

        @Override
        public Object getItem(int position) {
            // Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // Auto-generated method stub
            return position;
        }

        private class Holder {
            Button btn;
            ImageView img;
            TextView name;
        }



        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {


            // Auto-generated method stub
            final Holder holder = new Holder();
            View rowView = inflater.inflate(R.layout.conversation_anime_card_charaprofile, null);

            try {
                holder.btn = rowView.findViewById(R.id.chara_button);
                holder.img = rowView.findViewById(R.id.chara_image);
                holder.name = rowView.findViewById(R.id.chara_name);

                String name_text = "";
                if (!nodes.getJSONObject(position).getJSONObject("name").getString("first").equals("null"))
                {
                    name_text = nodes.getJSONObject(position).getJSONObject("name").getString("first");
                }
                if (!nodes.getJSONObject(position).getJSONObject("name").getString("last").equals("null"))
                {
                    name_text += " "+nodes.getJSONObject(position).getJSONObject("name").getString("last");
                }

                holder.btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Dialog Loading = new Dialog(context);
                        Loading.setContentView(R.layout.loading);
                        Loading.setCancelable(false);
                        Loading.show();
                        Thread worker = new Thread() {
                            public void run() {
                                Looper.prepare();
                        try
                        {
                        // Building Parameters
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        String json_out = "query { \n" +
                                "Character(id:"+nodes.getJSONObject(position).getString("id")+")\n" +
                                "  {id\n" +
                                "    name {\n" +
                                "      first\n" +
                                "      last\n" +
                                "    }\n" +
                                "    description(asHtml: false)\n" +
                                "    \n" +
                                "    image\n" +
                                "    {\n" +
                                "      large\n" +
                                "    }\n" +
                                "    \n" +
                                "    media(type:ANIME)\n" +
                                "    {\n" +
                                "      nodes\n" +
                                "      {\n" +
                                "        id\n" +
                                "        title\n" +
                                "        {\n" +
                                "          romaji\n" +
                                "        }\n" +
                                "        \n" +
                                "        coverImage\n" +
                                "        {\n" +
                                "          large\n" +
                                "        }\n" +
                                "        averageScore\n" +
                                "        description(asHtml: false)\n" +
                                "        episodes\n" +
                                "        format\n" +
                                "        startDate {\n" +
                                "          year\n" +
                                "          month\n" +
                                "          day\n" +
                                "        }\n" +
                                "        \n" +
                                "      }\n" +
                                "    }\n" +
                                "    \n" +
                                "    \n" +
                                "  }\n" +
                                "}";
                        params.add(new BasicNameValuePair("query", json_out));


                        JSONObject json = null;
                        json = jsonParser.makeHttpRequest(AL,
                                "POST", params);
                        System.out.println(AL);
                        // check log cat for response

                        try {
                            Log.d("getting chara...", json.toString());
                        } catch (java.lang.NullPointerException e) {
                        }


                        String output = json.getJSONObject("data").getJSONObject("Character").toString();

                            final DB DB = Room.databaseBuilder(context,
                                    DB.class, "msg_db")
                                    .build();

                            //send chara details message
                            Message msg3 = new Message();
                            msg3.set_msg(output);
                            msg3.set_stage(-6);
                            DB.daoAccess().insert_single_message(msg3);

                            DB.close();

                            ((Activity)context).runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    Loading.dismiss();
                                    context.sendBroadcast(new Intent("net.gauso001.SC_AI_update_msg"));
                                }
                            });


                        } catch (JSONException e) {
                            e.printStackTrace();
                            ((Activity)context).runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    Toast.makeText(context, "No internet",
                                            Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                            }
                        };
                        worker.start();

                    }
                });

                holder.name.setText(name_text+" ("+tags.getJSONObject(position).getString("role")+")");
                Picasso.get().load(nodes.getJSONObject(position).getJSONObject("image").getString("large")).into(holder.img);

            } catch (JSONException e) {}
            return rowView;
        }
    }


    //adapter for embedded anime-list
    public class anime_list_adapter extends BaseAdapter {
        JSONArray nodes;


        JSONParser jsonParser = new JSONParser();

        Context context;
        private LayoutInflater inflater = null;

        public anime_list_adapter(screen_main screen_main, JSONArray n) {
            // Auto-generated constructor stub
            context = screen_main;
            nodes = n;

            inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            // Auto-generated method stub
            return nodes.length();
        }

        @Override
        public Object getItem(int position) {
            // Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // Auto-generated method stub
            return position;
        }

        private class Holder {
            CardView cv;
            TextView desc;
            TextView name;
            ImageView img;
            TextView air_rating;
        }



        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {


            // Auto-generated method stub
            final Holder holder = new Holder();
            View rowView = inflater.inflate(R.layout.anime_list_dialog_card, null);

            holder.cv = (CardView)rowView.findViewById(R.id.a_card_simple);
            holder.desc = (TextView)rowView.findViewById(R.id.a_desc);
            holder.name = (TextView)rowView.findViewById(R.id.a_name);
            holder.img = (ImageView)rowView.findViewById(R.id.a_img);
            holder.air_rating = (TextView)rowView.findViewById(R.id.a_air_rating);

            try {
                holder.name.setText(nodes.getJSONObject(position).getJSONObject("title").getString("romaji"));
                Picasso.get().load(nodes.getJSONObject(position).getJSONObject("coverImage").getString("large")).into(holder.img);

                String airString = "";
                if (!nodes.getJSONObject(position).getJSONObject("startDate").getString("day").equals("null"))
                {
                    airString += nodes.getJSONObject(position).getJSONObject("startDate").getString("day")+"/";
                }
                if (!nodes.getJSONObject(position).getJSONObject("startDate").getString("month").equals("null"))
                {
                    airString += nodes.getJSONObject(position).getJSONObject("startDate").getString("month")+"/";
                }
                if (!nodes.getJSONObject(position).getJSONObject("startDate").getString("year").equals("null"))
                {
                    airString += nodes.getJSONObject(position).getJSONObject("startDate").getString("year");
                }
                if (nodes.getJSONObject(position).getString("averageScore").equals("null"))
                {
                    holder.air_rating.setText(airString);
                }
                else {
                    holder.air_rating.setText("Rating: " + nodes.getJSONObject(position).getString("averageScore") + " - " + airString);
                }

                holder.desc.setText(nodes.getJSONObject(position).getString("description").replace("<br>", ""));

                holder.cv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Intent intent = new Intent("net.gauso001.SC_AI_SWITCH");
                            intent.putExtra("state", 0);
                            intent.putExtra("s_i", nodes.getJSONObject(position).getInt("id"));
                            context.sendBroadcast(intent);
                        }
                        catch (JSONException e) {}
                    }
                });


            } catch (JSONException e) {}
            return rowView;
        }
    }



    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }




    }


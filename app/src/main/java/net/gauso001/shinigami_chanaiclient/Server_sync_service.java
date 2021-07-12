package net.gauso001.shinigami_chanaiclient;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.arch.persistence.room.Room;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Server_sync_service extends JobService {
    private static final String TAG = "SyncService";

    JSONParser jsonParser = new JSONParser();

    private static String get = "https://gausnet-yserver.gauso001.net:85/SC_AI/get.php";
    private static String get_timer = "https://gausnet-yserver.gauso001.net:85/SC_AI/get_timer.php";
    private static String get_alarm = "https://gausnet-yserver.gauso001.net:85/SC_AI/get_alarm.php";
    private static String get_anime = "https://gausnet-yserver.gauso001.net:85/SC_AI/get_anime.php";
    private static String get_waifus = "https://gausnet-yserver.gauso001.net:85/SC_AI/get_waifus.php";
    private static String AL = "https://graphql.anilist.co";
    private static String get_regular_alarms = "https://gausnet-yserver.gauso001.net:85/SC_AI/get_regular_alarms.php";


    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ERROR = "error";

    //global scope/buffer vars
    String text = "";
    int stage_s;
    int stage_r;
    Date timer_end;
    int timer_id;
    String[] MAL_id;
    String[] waifu_AL_id;
    String buffer;

    int network_delay = 2000;
    int anime_update_delay = 3600*1000;

    @Override
    public boolean onStartJob(JobParameters params) {

        //reschedule immediately so it runs again even if it fails
        ComponentName serviceComponent = new ComponentName(this, Server_sync_service.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(200); //interval for timer watchdog
        builder.setOverrideDeadline(300); //maximum delay
        JobScheduler jobScheduler = (JobScheduler) getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());

        final SharedPreferences sharedPref = getSharedPreferences("net.gauso001.SC_AI", Context.MODE_PRIVATE);

        //get new messages from server
        if (new Date().getTime() > sharedPref.getLong("n_n", 0)) {

            final DB DB = Room.databaseBuilder(Server_sync_service.this,
                    DB.class, "msg_db")
                    .build();

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong("n_n", new Date().getTime()+network_delay);
            editor.apply();

            //check for new messages
            Thread worker = new Thread() {
                public void run() {
                    Looper.prepare();
                    GET s = new GET();

                    try {
                        String ret = s.execute().get();

                        if (ret != null && ret.equals("success")) {
                            //update previous with stage
                            DB.daoAccess().update_last_stage(stage_s);

                            if (stage_r == -3) {
                                //acknowledge reminder creation
                                Message msg = new Message();
                                msg.set_msg(getString(R.string.ACK) + "\n" + getString(R.string.made_reminder));
                                msg.set_stage(-1);
                                DB.daoAccess().insert_single_message(msg);
                            } else if (stage_r == -4) {
                                //acknowledge timer creation and fetch details
                                Message msg = new Message();
                                msg.set_msg(getString(R.string.ACK) + "\n" + getString(R.string.made_timer));
                                msg.set_stage(-1);
                                DB.daoAccess().insert_single_message(msg);

                                final String[] data = text.split("\\|");

                                GET_timer s2 = new GET_timer();
                                try {
                                    String ret2 = s2.execute(data[1]).get();
                                    if (ret != null && ret2.equals("success")) {
                                        //insert timer into DB
                                        final timer_DB timer_DB = Room.databaseBuilder(Server_sync_service.this,
                                                timer_DB.class, "timer_db")
                                                .build();

                                        Timer timer = new Timer();
                                        timer.set_id(timer_id);
                                        timer.set_end(timer_end);
                                        timer_DB.Timer_daoAccess().insert_single_timer(timer);
                                        timer_DB.close();
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }

                            } else if (stage_r == -5) {

                                final String[] data = text.split("\\|");

                                List<NameValuePair> params = new ArrayList<NameValuePair>();
                                params.add(new BasicNameValuePair("K1", "7rKKRJVrT-#CzKBRPP4=WA^UFCUdEbhmzUD??ZBGJz7w9GaRAd^t+56B7SV?B66?^jWv9V=RM9rDuy64G_+JkhH_tTb&CpnUdJG6eF6&BbL&Pv+?B4HYYZ@aqPss!zhckrf?haMzaaZ*LM4P%B%7rYu^U6SxZ#=?Cvrw!fAw3A5uKYyzjz8y2B_b#J-FNaCj!g!Hkk4hg5mYB&BE+3gzZVufZqhkFbvetjCdT-Y@+y=X8%AqSY3Gedq8r+^Sw6P"));
                                params.add(new BasicNameValuePair("K2", "p4#eP4AZ9+UVxGgeaA^eUbvNqXhjfADP@hrJ%B47W?CBAQZaMp*_-?hmtfTu?2JqCXfdksQ%qUaZ9PQ3GXt@Un9zJr%W77YkrWkE%NQ=zWE3LYAH=?RPe^p^MYr?UwR@gYWNh5kC8-@+9rbxaZ?sUTEkt?t^Lu8mdGNy!pWF+aS+Sub*4q&Zq6dFem3!pUu-hPL2nfjjfe6zxZwT8!4*SqN^TayEw^R5YxLqA-VhzkU&_-bqJ&Wz!zn6Xx"));
                                params.add(new BasicNameValuePair("K3", "RD=F*+nEuXzvK6#zzxdpppSWT*m_QVbA4yBrL@LT3p=-G8zxD!SWd#9zMcsTHBehhKUta6#YzSh7sR?tx?WnxedQk4VPUBZP!qMj=vT#T=E?TXMdpP!Fn+kGfFV!CNzw2*gUQ4@TrLh#WQVdYs2nfPjU5cPh42vS8g9Tzp2VtHkhUpuHUt4feSYYE@Jj5R+5qWXMN_WHJ*Jda9kLg6%ZL_Hq=c26MRf!MyyCgxQ2jR7XTD^Xdyp*h#asfEWKm6"));
                                params.add(new BasicNameValuePair("K4", "T35YKZBYFY9tK#PRP#%NscQY8uuMPtyMGgR_^s-V-g^97zcTmx4A6mt5XEt3Ms=Q%+4_7?-LzXm2w#95?Um4BdFy4sDDN_Pq=N%?LYBtC9mYGT2DtWvr3Cv%W!bUXvwfj^4wTuxdhJK!ruP#YseH5nj62M57e9!C-d=-CfwA2=f&yNYE7Jz%sd&UAbf@yzCFzCmDN8=G-wxnCKL3#Lgqq-!%FRxqJk_5T@3mm4tymQ73-^Cr4tBc=Q4a^V4"));
                                params.add(new BasicNameValuePair("id", data[1]));


                                JSONObject json = null;
                                json = jsonParser.makeHttpRequest(get_alarm,
                                        "POST", params);
                                System.out.println(get_alarm);

                                try {
                                    Log.d("getting alarm...", json.toString());
                                } catch (java.lang.NullPointerException e) {
                                }

                                final alarm_DB alarm_DB = Room.databaseBuilder(Server_sync_service.this,
                                        alarm_DB.class, "alarm_db")
                                        .build();

                                // check for success tag
                                try
                                {
                                    if (json.getInt(TAG_SUCCESS) == 1) {
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        SimpleDateFormat out_time = new SimpleDateFormat("hh:mm:ss aa");
                                        SimpleDateFormat out_day = new SimpleDateFormat("EEEE, MMM dd");
                                        Alarm alarm = new Alarm();
                                        try {
                                            alarm.set_id(Integer.parseInt(data[1]));
                                            alarm.set_time(sdf.parse(json.getString("time")));
                                            alarm_DB.Alarm_daoAccess().insert_single_alarm(alarm);
                                        }catch (ParseException e){}

                                        text = getString(R.string.ACK)+"\n"+getString(R.string.made_alarm)+"\n"+out_time.format(new Date(alarm.get_time()))+" on "+out_day.format(new Date(alarm.get_time()))+". \n"+getString(R.string.should_override_alarm)+"|"+data[1];
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }



                                alarm_DB.close();
                            }

                            //insert what was just recieved
                            Message msg2 = new Message();
                            msg2.set_msg(text);
                            msg2.set_stage(stage_r);
                            DB.daoAccess().insert_single_message(msg2);

                            //TODO maybe send a notification but maybe not

                            //broadcast to prompt update
                            sendBroadcast(new Intent("net.gauso001.SC_AI_update_msg"));
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    DB.close();

                }
            };
            worker.start();
        }

        //update anime list and regular alarm
        if (new Date().getTime() > sharedPref.getLong("l_u", 0)) {

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong("l_u", new Date().getTime()+anime_update_delay);
            editor.apply();

            //update anime list
            Thread worker = new Thread() {
                public void run() {
                    Looper.prepare();

                    final anime_DB anime_DB = Room.databaseBuilder(Server_sync_service.this,
                            anime_DB.class, "anime_db").fallbackToDestructiveMigration()
                            .build();

                    GET_anime s = new GET_anime();

                    try {
                        String ret = s.execute().get();
                        List<Anime> anime_list = new ArrayList<Anime>();

                        if (ret != null && ret.equals("success")) {

                            for (int i = 0; i < MAL_id.length; i++)
                            {

                                fetch_AL s2 = new fetch_AL();

                                try {
                                    String ret2 = s2.execute(MAL_id[i]).get();


                                    if (ret2 == null || ret2.equals("error0")) {

                                    } else if (ret2.equals("success")) {
                                        try {
                                            JSONObject obj = new JSONObject(buffer);

                                            String airString = "";
                                            if (obj.getString("status").equals("NOT_YET_RELEASED"))
                                            {
                                                airString += "Will start airing ";
                                            }
                                            else
                                            {
                                                airString += "Aired ";
                                            }

                                            if (!obj.getJSONObject("startDate").getString("day").equals("null"))
                                            {
                                                airString += obj.getJSONObject("startDate").getString("day")+"/";
                                            }
                                            if (!obj.getJSONObject("startDate").getString("month").equals("null"))
                                            {
                                                airString += obj.getJSONObject("startDate").getString("month")+"/";
                                            }
                                            if (!obj.getJSONObject("startDate").getString("year").equals("null"))
                                            {
                                                airString += obj.getJSONObject("startDate").getString("year");
                                            }


                                            //save all details as anime object extended edition in PTW list DB
                                            Anime anime = new Anime(obj.getString("id"), obj.getJSONObject("coverImage").getString("large"), obj.getJSONObject("title").getString("romaji"), obj.getString("format"), obj.getString("averageScore"), obj.getString("episodes"), obj.getString("description"), airString);
                                            anime.current = obj.getString("status").equals("RELEASING");
                                            if (anime.current) {
                                                try {
                                                    anime.new_ep_day = obj.getJSONObject("nextAiringEpisode").getLong("airingAt");
                                                } catch (JSONException e) { }

                                            }

                                            JSONArray links = obj.getJSONArray("externalLinks");
                                            for (int j = 0; j < links.length(); j++)
                                            {
                                                if (links.getJSONObject(j).getString("site").equals("Crunchyroll"))
                                                {
                                                    anime.streamType = 1;
                                                    JSONArray EPs = obj.getJSONArray("streamingEpisodes");
                                                    if (EPs.length() > 0) {
                                                        anime.streamLink = EPs.getJSONObject(EPs.length()-1).getString("url");
                                                    } else { anime.streamLink = "NOEP"; }
                                                    break;
                                                }
                                            }

                                            anime_list.add(anime);


                                        } catch (JSONException | NullPointerException e) {}


                                    } else if (ret2.equals("error5")) {
                                        i--;
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }

                            }

                            anime_DB.Anime_daoAccess().nukeTable();

                            anime_DB.Anime_daoAccess().insert_all(anime_list);

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    anime_DB.close();

                }
            };
            worker.start();

            //update regular alarm list
            Thread worker2 = new Thread() {
                public void run() {
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("K1", "7rKKRJVrT-#CzKBRPP4=WA^UFCUdEbhmzUD??ZBGJz7w9GaRAd^t+56B7SV?B66?^jWv9V=RM9rDuy64G_+JkhH_tTb&CpnUdJG6eF6&BbL&Pv+?B4HYYZ@aqPss!zhckrf?haMzaaZ*LM4P%B%7rYu^U6SxZ#=?Cvrw!fAw3A5uKYyzjz8y2B_b#J-FNaCj!g!Hkk4hg5mYB&BE+3gzZVufZqhkFbvetjCdT-Y@+y=X8%AqSY3Gedq8r+^Sw6P"));
                    params.add(new BasicNameValuePair("K2", "p4#eP4AZ9+UVxGgeaA^eUbvNqXhjfADP@hrJ%B47W?CBAQZaMp*_-?hmtfTu?2JqCXfdksQ%qUaZ9PQ3GXt@Un9zJr%W77YkrWkE%NQ=zWE3LYAH=?RPe^p^MYr?UwR@gYWNh5kC8-@+9rbxaZ?sUTEkt?t^Lu8mdGNy!pWF+aS+Sub*4q&Zq6dFem3!pUu-hPL2nfjjfe6zxZwT8!4*SqN^TayEw^R5YxLqA-VhzkU&_-bqJ&Wz!zn6Xx"));
                    params.add(new BasicNameValuePair("K3", "RD=F*+nEuXzvK6#zzxdpppSWT*m_QVbA4yBrL@LT3p=-G8zxD!SWd#9zMcsTHBehhKUta6#YzSh7sR?tx?WnxedQk4VPUBZP!qMj=vT#T=E?TXMdpP!Fn+kGfFV!CNzw2*gUQ4@TrLh#WQVdYs2nfPjU5cPh42vS8g9Tzp2VtHkhUpuHUt4feSYYE@Jj5R+5qWXMN_WHJ*Jda9kLg6%ZL_Hq=c26MRf!MyyCgxQ2jR7XTD^Xdyp*h#asfEWKm6"));
                    params.add(new BasicNameValuePair("K4", "T35YKZBYFY9tK#PRP#%NscQY8uuMPtyMGgR_^s-V-g^97zcTmx4A6mt5XEt3Ms=Q%+4_7?-LzXm2w#95?Um4BdFy4sDDN_Pq=N%?LYBtC9mYGT2DtWvr3Cv%W!bUXvwfj^4wTuxdhJK!ruP#YseH5nj62M57e9!C-d=-CfwA2=f&yNYE7Jz%sd&UAbf@yzCFzCmDN8=G-wxnCKL3#Lgqq-!%FRxqJk_5T@3mm4tymQ73-^Cr4tBc=Q4a^V4"));


                    JSONObject json = null;
                    json = jsonParser.makeHttpRequest(get_regular_alarms,
                            "POST", params);
                    System.out.println(get_regular_alarms);

                    try {
                        Log.d("getting regalarms...", json.toString());
                    } catch (java.lang.NullPointerException e) { }


                    try
                    {
                        if (json.getInt(TAG_SUCCESS) == 1) {

                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("1", json.getString("mon"));
                            editor.putString("2", json.getString("tues"));
                            editor.putString("3", json.getString("wed"));
                            editor.putString("4", json.getString("thurs"));
                            editor.putString("5", json.getString("fri"));
                            editor.putString("6", json.getString("sat"));
                            editor.putString("7", json.getString("sun"));
                            editor.apply();

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            };
            worker2.start();

            //update waifu list
            Thread worker3 = new Thread() {
                public void run() {
                    Looper.prepare();

                    final character_DB character_DB = Room.databaseBuilder(Server_sync_service.this,
                            character_DB.class, "character_db").fallbackToDestructiveMigration()
                            .build();

                    GET_waifus s = new GET_waifus();

                    try {
                        String ret = s.execute().get();
                        List<Character> waifu_list = new ArrayList<Character>();

                        if (ret != null && ret.equals("success")) {

                            for (int i = 0; i < waifu_AL_id.length; i++)
                            {
                                try {
                                    // Building Parameters
                                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                                    String json_out = "query { \n" +
                                            "Character(id:" + waifu_AL_id[i] + ")\n" +
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

                                    String name = "";

                                    if (!json.getJSONObject("data").getJSONObject("Character").getJSONObject("name").getString("first").equals("null")) {
                                        name += json.getJSONObject("data").getJSONObject("Character").getJSONObject("name").getString("first") + " ";
                                    }
                                    if (!json.getJSONObject("data").getJSONObject("Character").getJSONObject("name").getString("last").equals("null")) {
                                        name += json.getJSONObject("data").getJSONObject("Character").getJSONObject("name").getString("last");
                                    }

                                    waifu_list.add(new Character(json.getJSONObject("data").getJSONObject("Character").getString("id"),json.getJSONObject("data").getJSONObject("Character").getJSONObject("image").getString("large"),name,json.getJSONObject("data").getJSONObject("Character").getString("description"),json.getJSONObject("data").getJSONObject("Character").getJSONObject("media").toString()));

                                } catch (JSONException e) { i--; }

                            }

                            character_DB.Character_daoAccess().nukeTable();
                            character_DB.Character_daoAccess().insert_all(waifu_list);

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    character_DB.close();

                }
            };
            worker3.start();

        }

        timers();

        return true;
    }

    void timers()
    {
        Thread worker = new Thread() {
            public void run() {

                final timer_DB timer_DB = Room.databaseBuilder(Server_sync_service.this,
                        timer_DB.class, "timer_db")
                        .build();
                List<Timer> timer_list = timer_DB.Timer_daoAccess().fetch_timers();

                for (int i = 0; i < timer_list.size(); i++)
                {
                    if (new Date().getTime() >= timer_list.get(i).get_end())
                    {
                        timer_DB.Timer_daoAccess().delete(timer_list.get(i).get_id());

                        Intent intent = new Intent(Server_sync_service.this, screen_main.class);
                        intent.putExtra("state", 1);
                        intent.putExtra("s_i", 1);
                        startActivity(intent);
                    }
                }

                timer_DB.close();
            }
        };
        worker.start();
    }


    private class GET extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("K1", "7rKKRJVrT-#CzKBRPP4=WA^UFCUdEbhmzUD??ZBGJz7w9GaRAd^t+56B7SV?B66?^jWv9V=RM9rDuy64G_+JkhH_tTb&CpnUdJG6eF6&BbL&Pv+?B4HYYZ@aqPss!zhckrf?haMzaaZ*LM4P%B%7rYu^U6SxZ#=?Cvrw!fAw3A5uKYyzjz8y2B_b#J-FNaCj!g!Hkk4hg5mYB&BE+3gzZVufZqhkFbvetjCdT-Y@+y=X8%AqSY3Gedq8r+^Sw6P"));
            params.add(new BasicNameValuePair("K2", "p4#eP4AZ9+UVxGgeaA^eUbvNqXhjfADP@hrJ%B47W?CBAQZaMp*_-?hmtfTu?2JqCXfdksQ%qUaZ9PQ3GXt@Un9zJr%W77YkrWkE%NQ=zWE3LYAH=?RPe^p^MYr?UwR@gYWNh5kC8-@+9rbxaZ?sUTEkt?t^Lu8mdGNy!pWF+aS+Sub*4q&Zq6dFem3!pUu-hPL2nfjjfe6zxZwT8!4*SqN^TayEw^R5YxLqA-VhzkU&_-bqJ&Wz!zn6Xx"));
            params.add(new BasicNameValuePair("K3", "RD=F*+nEuXzvK6#zzxdpppSWT*m_QVbA4yBrL@LT3p=-G8zxD!SWd#9zMcsTHBehhKUta6#YzSh7sR?tx?WnxedQk4VPUBZP!qMj=vT#T=E?TXMdpP!Fn+kGfFV!CNzw2*gUQ4@TrLh#WQVdYs2nfPjU5cPh42vS8g9Tzp2VtHkhUpuHUt4feSYYE@Jj5R+5qWXMN_WHJ*Jda9kLg6%ZL_Hq=c26MRf!MyyCgxQ2jR7XTD^Xdyp*h#asfEWKm6"));
            params.add(new BasicNameValuePair("K4", "T35YKZBYFY9tK#PRP#%NscQY8uuMPtyMGgR_^s-V-g^97zcTmx4A6mt5XEt3Ms=Q%+4_7?-LzXm2w#95?Um4BdFy4sDDN_Pq=N%?LYBtC9mYGT2DtWvr3Cv%W!bUXvwfj^4wTuxdhJK!ruP#YseH5nj62M57e9!C-d=-CfwA2=f&yNYE7Jz%sd&UAbf@yzCFzCmDN8=G-wxnCKL3#Lgqq-!%FRxqJk_5T@3mm4tymQ73-^Cr4tBc=Q4a^V4"));




            JSONObject json = null;
            json = jsonParser.makeHttpRequest(get,
                    "POST", params);
            System.out.println(get);
            // check log cat for response

            try {
                Log.d("getting reply...", json.toString());
            } catch (java.lang.NullPointerException e) {
                return "error0";
            }

            // check for success tag
            try
            {
                if (json.getInt(TAG_SUCCESS) == 1) {
                    text = json.getString("text");
                    stage_r = json.getInt("stage_r");
                    stage_s = json.getInt("stage_s");
                    return "success";
                }
                else if (json.getInt(TAG_SUCCESS) == 0) {
                    return "no_new";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

    }


    private class GET_timer extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("K1", "7rKKRJVrT-#CzKBRPP4=WA^UFCUdEbhmzUD??ZBGJz7w9GaRAd^t+56B7SV?B66?^jWv9V=RM9rDuy64G_+JkhH_tTb&CpnUdJG6eF6&BbL&Pv+?B4HYYZ@aqPss!zhckrf?haMzaaZ*LM4P%B%7rYu^U6SxZ#=?Cvrw!fAw3A5uKYyzjz8y2B_b#J-FNaCj!g!Hkk4hg5mYB&BE+3gzZVufZqhkFbvetjCdT-Y@+y=X8%AqSY3Gedq8r+^Sw6P"));
            params.add(new BasicNameValuePair("K2", "p4#eP4AZ9+UVxGgeaA^eUbvNqXhjfADP@hrJ%B47W?CBAQZaMp*_-?hmtfTu?2JqCXfdksQ%qUaZ9PQ3GXt@Un9zJr%W77YkrWkE%NQ=zWE3LYAH=?RPe^p^MYr?UwR@gYWNh5kC8-@+9rbxaZ?sUTEkt?t^Lu8mdGNy!pWF+aS+Sub*4q&Zq6dFem3!pUu-hPL2nfjjfe6zxZwT8!4*SqN^TayEw^R5YxLqA-VhzkU&_-bqJ&Wz!zn6Xx"));
            params.add(new BasicNameValuePair("K3", "RD=F*+nEuXzvK6#zzxdpppSWT*m_QVbA4yBrL@LT3p=-G8zxD!SWd#9zMcsTHBehhKUta6#YzSh7sR?tx?WnxedQk4VPUBZP!qMj=vT#T=E?TXMdpP!Fn+kGfFV!CNzw2*gUQ4@TrLh#WQVdYs2nfPjU5cPh42vS8g9Tzp2VtHkhUpuHUt4feSYYE@Jj5R+5qWXMN_WHJ*Jda9kLg6%ZL_Hq=c26MRf!MyyCgxQ2jR7XTD^Xdyp*h#asfEWKm6"));
            params.add(new BasicNameValuePair("K4", "T35YKZBYFY9tK#PRP#%NscQY8uuMPtyMGgR_^s-V-g^97zcTmx4A6mt5XEt3Ms=Q%+4_7?-LzXm2w#95?Um4BdFy4sDDN_Pq=N%?LYBtC9mYGT2DtWvr3Cv%W!bUXvwfj^4wTuxdhJK!ruP#YseH5nj62M57e9!C-d=-CfwA2=f&yNYE7Jz%sd&UAbf@yzCFzCmDN8=G-wxnCKL3#Lgqq-!%FRxqJk_5T@3mm4tymQ73-^Cr4tBc=Q4a^V4"));
            params.add(new BasicNameValuePair("id", args[0]));


            JSONObject json = null;
            json = jsonParser.makeHttpRequest(get_timer,
                    "POST", params);
            System.out.println(get_timer);
            // check log cat for response

            try {
                Log.d("getting timer...", json.toString());
            } catch (java.lang.NullPointerException e) {
                return "error0";
            }

            // check for success tag
            try
            {
                if (json.getInt(TAG_SUCCESS) == 1) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                    timer_end = sdf.parse(json.getString("end"));
                    timer_id = Integer.parseInt(args[0]);
                    }catch (ParseException e){}
                    return "success";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }


    }

    private class GET_anime extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("K1", "7rKKRJVrT-#CzKBRPP4=WA^UFCUdEbhmzUD??ZBGJz7w9GaRAd^t+56B7SV?B66?^jWv9V=RM9rDuy64G_+JkhH_tTb&CpnUdJG6eF6&BbL&Pv+?B4HYYZ@aqPss!zhckrf?haMzaaZ*LM4P%B%7rYu^U6SxZ#=?Cvrw!fAw3A5uKYyzjz8y2B_b#J-FNaCj!g!Hkk4hg5mYB&BE+3gzZVufZqhkFbvetjCdT-Y@+y=X8%AqSY3Gedq8r+^Sw6P"));
            params.add(new BasicNameValuePair("K2", "p4#eP4AZ9+UVxGgeaA^eUbvNqXhjfADP@hrJ%B47W?CBAQZaMp*_-?hmtfTu?2JqCXfdksQ%qUaZ9PQ3GXt@Un9zJr%W77YkrWkE%NQ=zWE3LYAH=?RPe^p^MYr?UwR@gYWNh5kC8-@+9rbxaZ?sUTEkt?t^Lu8mdGNy!pWF+aS+Sub*4q&Zq6dFem3!pUu-hPL2nfjjfe6zxZwT8!4*SqN^TayEw^R5YxLqA-VhzkU&_-bqJ&Wz!zn6Xx"));
            params.add(new BasicNameValuePair("K3", "RD=F*+nEuXzvK6#zzxdpppSWT*m_QVbA4yBrL@LT3p=-G8zxD!SWd#9zMcsTHBehhKUta6#YzSh7sR?tx?WnxedQk4VPUBZP!qMj=vT#T=E?TXMdpP!Fn+kGfFV!CNzw2*gUQ4@TrLh#WQVdYs2nfPjU5cPh42vS8g9Tzp2VtHkhUpuHUt4feSYYE@Jj5R+5qWXMN_WHJ*Jda9kLg6%ZL_Hq=c26MRf!MyyCgxQ2jR7XTD^Xdyp*h#asfEWKm6"));
            params.add(new BasicNameValuePair("K4", "T35YKZBYFY9tK#PRP#%NscQY8uuMPtyMGgR_^s-V-g^97zcTmx4A6mt5XEt3Ms=Q%+4_7?-LzXm2w#95?Um4BdFy4sDDN_Pq=N%?LYBtC9mYGT2DtWvr3Cv%W!bUXvwfj^4wTuxdhJK!ruP#YseH5nj62M57e9!C-d=-CfwA2=f&yNYE7Jz%sd&UAbf@yzCFzCmDN8=G-wxnCKL3#Lgqq-!%FRxqJk_5T@3mm4tymQ73-^Cr4tBc=Q4a^V4"));


            JSONObject json = null;
            json = jsonParser.makeHttpRequest(get_anime,
                    "POST", params);
            System.out.println(get_anime);

            try {
                Log.d("getting anime list...", json.toString());
            } catch (java.lang.NullPointerException e) {
                return "error0";
            }


            try
            {
                if (json.getInt(TAG_SUCCESS) == 1) {

                    MAL_id = json.getString("data").split("\\|");

                    return "success";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }


    }

    private class GET_waifus extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("K1", "7rKKRJVrT-#CzKBRPP4=WA^UFCUdEbhmzUD??ZBGJz7w9GaRAd^t+56B7SV?B66?^jWv9V=RM9rDuy64G_+JkhH_tTb&CpnUdJG6eF6&BbL&Pv+?B4HYYZ@aqPss!zhckrf?haMzaaZ*LM4P%B%7rYu^U6SxZ#=?Cvrw!fAw3A5uKYyzjz8y2B_b#J-FNaCj!g!Hkk4hg5mYB&BE+3gzZVufZqhkFbvetjCdT-Y@+y=X8%AqSY3Gedq8r+^Sw6P"));
            params.add(new BasicNameValuePair("K2", "p4#eP4AZ9+UVxGgeaA^eUbvNqXhjfADP@hrJ%B47W?CBAQZaMp*_-?hmtfTu?2JqCXfdksQ%qUaZ9PQ3GXt@Un9zJr%W77YkrWkE%NQ=zWE3LYAH=?RPe^p^MYr?UwR@gYWNh5kC8-@+9rbxaZ?sUTEkt?t^Lu8mdGNy!pWF+aS+Sub*4q&Zq6dFem3!pUu-hPL2nfjjfe6zxZwT8!4*SqN^TayEw^R5YxLqA-VhzkU&_-bqJ&Wz!zn6Xx"));
            params.add(new BasicNameValuePair("K3", "RD=F*+nEuXzvK6#zzxdpppSWT*m_QVbA4yBrL@LT3p=-G8zxD!SWd#9zMcsTHBehhKUta6#YzSh7sR?tx?WnxedQk4VPUBZP!qMj=vT#T=E?TXMdpP!Fn+kGfFV!CNzw2*gUQ4@TrLh#WQVdYs2nfPjU5cPh42vS8g9Tzp2VtHkhUpuHUt4feSYYE@Jj5R+5qWXMN_WHJ*Jda9kLg6%ZL_Hq=c26MRf!MyyCgxQ2jR7XTD^Xdyp*h#asfEWKm6"));
            params.add(new BasicNameValuePair("K4", "T35YKZBYFY9tK#PRP#%NscQY8uuMPtyMGgR_^s-V-g^97zcTmx4A6mt5XEt3Ms=Q%+4_7?-LzXm2w#95?Um4BdFy4sDDN_Pq=N%?LYBtC9mYGT2DtWvr3Cv%W!bUXvwfj^4wTuxdhJK!ruP#YseH5nj62M57e9!C-d=-CfwA2=f&yNYE7Jz%sd&UAbf@yzCFzCmDN8=G-wxnCKL3#Lgqq-!%FRxqJk_5T@3mm4tymQ73-^Cr4tBc=Q4a^V4"));


            JSONObject json = null;
            json = jsonParser.makeHttpRequest(get_waifus,
                    "POST", params);
            System.out.println(get_waifus);

            try {
                Log.d("getting waifu list...", json.toString());
            } catch (java.lang.NullPointerException e) {
                return "error0";
            }


            try
            {
                if (json.getInt(TAG_SUCCESS) == 1) {

                    waifu_AL_id = json.getString("data").split("\\|");

                    return "success";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }


    }

    private class fetch_AL extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            String json_out = "query { \n" +
                    "  Media (id: "+args[0]+") { \n" +
                    "    id\n" +
                    "    title {\n" +
                    "      romaji\n" +
                    "    }\n" +
                    "    coverImage\n" +
                    "    {\n" +
                    "      large\n" +
                    "    }\n" +
                    "    format\n" +
                    "    averageScore\n" +
                    "    description(asHtml: false)\n" +
                    "    episodes\n" +
                    "    status\n" +
                    "\t\tnextAiringEpisode {\n" +
                    "\t\t  id\n" +
                    "      episode\n" +
                    "      airingAt\n" +
                    "\t\t}\n" +
                    "    startDate {\n" +
                    "      year\n" +
                    "      month\n" +
                    "      day\n" +
                    "    }\n" +
                    "    endDate {\n" +
                    "      year\n" +
                    "      month\n" +
                    "      day\n" +
                    "    }\n" +
                    "    studios {\n" +
                    "      nodes\n" +
                    "      {\n" +
                    "        name\n" +
                    "      }\n" +
                    "    }\n" +
                    "streamingEpisodes {\n" +
                    "      url\n" +
                    "      site\n" +
                    "    }"+
                    "    relations\n" +
                    "    {\n" +
                    "      nodes{\n" +
                    "        id\n" +
                    "        title{\n" +
                    "          romaji\n" +
                    "        }\n" +
                    "      }\n" +
                    "      edges {\n" +
                    "        relationType\n" +
                    "      }\n" +
                    "    }\n" +
                    "    \n" +
                    " externalLinks {\n" +
                    "      id\n" +
                    "      url\n" +
                    "      site\n" +
                    "    }"+
                    "    tags {\n" +
                    "      id\n" +
                    "      name\n" +
                    "      isGeneralSpoiler\n" +
                    "      isMediaSpoiler\n" +
                    "      rank\n" +
                    "    }\n" +
                    "    \n" +
                    "    genres\n" +
                    "    \n" +
                    "    characters(sort:ROLE) {\n" +
                    "      nodes{\n" +
                    "        id\n" +
                    "        image {\n" +
                    "          large\n" +
                    "        }\n" +
                    "        name {\n" +
                    "          first\n" +
                    "          last\n" +
                    "        }\n" +
                    "      }\n" +
                    "      edges\n" +
                    "      {\n" +
                    "        role\n" +
                    "      }\n" +
                    "\n" +
                    "      \n" +
                    "     \n" +
                    "    }\n" +
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
                Log.d("Fetching AL...", json.toString());
            } catch (java.lang.NullPointerException e) {
                return "error0";
            }

            try {
                buffer = json.getJSONObject("data").getJSONObject("Media").toString();
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            try {
                if (json.getString("error").equals("5")) {
                    return "error5";
                }
            } catch (JSONException e) {
                return "success";
            }



            return null;
        }

    }



    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
    }

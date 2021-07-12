package net.gauso001.shinigami_chanaiclient;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static java.lang.Math.abs;

public class screen_main extends AppCompatActivity {


    JSONParser jsonParser = new JSONParser();


    private static String CR_login = "https://api.crunchyroll.com/login.0.json";
    private static String CR_ls = "https://api.crunchyroll.com/info.0.json";
    private static String CR_batch = "https://api.crunchyroll.com/batch.0.json";
    private static String CR_log = "https://api.crunchyroll.com/log.0.json";
    private static String CR_sess = "https://api.crunchyroll.com/start_session.0.json";
    private static String AL = "https://graphql.anilist.co";
    private static String stage_2_3_post = "https://gausnet-yserver.gauso001.net:85/SC_AI/post.php";
    private static String add_l = "https://gausnet-yserver.gauso001.net:85/SC_AI/add.php";
    private static String delete_l = "https://gausnet-yserver.gauso001.net:85/SC_AI/delete.php";
    private static String alarm_set_over = "https://gausnet-yserver.gauso001.net:85/SC_AI/alarm_set_over.php";
    private static String alarm_delete_l = "https://gausnet-yserver.gauso001.net:85/SC_AI/alarm_delete.php";


    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ERROR = "error";

    Anime anime_mentioned = null;
    Character character_mentioned = null;
    int alarm_mentioned = 0;

    View current = null;

    //global scope vars
    List<Anime> MAL_search_results = new ArrayList<Anime>();
    List<Character> chara_search_results = new ArrayList<Character>();
    EditText new_msg;
    String buffer = "";
    int state = 0;
    int SupplementaryIndex = 0;

    private BroadcastReceiver listener = null;
    private BroadcastReceiver playhead_listener = null;
    private BroadcastReceiver switch_listener;

    AnimationView anim_view;

    abstract static class CR_params {
        static String access_token = "QWjz212GspMHH9h";
        static String device_type = "com.crunchyroll.iphone";
        static String device_id = "FFFFbaka-SCAI-asdf-1234-yunogibapi00";
        static String version = "2313.8";
        static String locale = "enUS";
    }


    final DB DB = Room.databaseBuilder(screen_main.this,
            DB.class, "msg_db")
            .build();


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(listener);
        unregisterReceiver(switch_listener);
        DB.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_main_3d);
        SchedService();

        //get animationView
        anim_view = (AnimationView)findViewById(R.id.anim_view);
        //load idle animation
        anim_view.loadAnimation("idle", 1,  11);
        anim_view.playAnimation();


        //spawn a thread to manage animations
        Thread worker = new Thread() {
            public void run() {

                //load additional animations
                anim_view.loadAdditional();

                boolean run = true;

                while(run) {
                    Random rand = new Random();
                    int  n = rand.nextInt(6);
                    if (n == 0) { anim_view.blinkNext = true; }
                    else if (n == 1) { anim_view.head_turn_left_next = true; }
                    else if (n == 2) { anim_view.head_turn_right_next = true; }
                    else if (n == 3) { anim_view.bend_left_next = true; }
                    else if (n == 4) { anim_view.bend_right_next = true; }

                    SystemClock.sleep(1500);
                }
            }
        };
        worker.start();

        //rory face
        Button rory_face = (Button)findViewById(R.id.rory_face);
        rory_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anim_view.blushNext = true;
            }
        });


        state = getIntent().getIntExtra("state", 0);
        SupplementaryIndex = getIntent().getIntExtra("s_i", 0);

        System.out.println(state+" - "+SupplementaryIndex);

        final ConstraintLayout cl = (ConstraintLayout)findViewById(R.id.cl_3d);



        switch_listener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent ) {
                state = intent.getIntExtra("state", 0);
                if (SupplementaryIndex == 0) {SupplementaryIndex = intent.getIntExtra("s_i", 0); }

                if (current != null) {cl.removeView(current); }

                // state 0 is the convo state
                if (state == 0) { current = LayoutInflater.from(context).inflate(R.layout.overlay_convo, cl, false); cl.addView(current); State0(); }
                else if (state == 1) { current = LayoutInflater.from(context).inflate(R.layout.overlay_timer, cl, false); cl.addView(current); State1(); }
                else if (state == 2) { current = LayoutInflater.from(context).inflate(R.layout.overlay_timer, cl, false); cl.addView(current); State2(); }
                else if (state == 3) { current = LayoutInflater.from(context).inflate(R.layout.overlay_alarm, cl, false); cl.addView(current); State3(); }
                else if (state == 4) { current = LayoutInflater.from(context).inflate(R.layout.overlay_timer, cl, false); cl.addView(current); State4(); }
            }
        };

        registerReceiver(switch_listener,new IntentFilter("net.gauso001.SC_AI_SWITCH"));

        Intent intent = new Intent("net.gauso001.SC_AI_SWITCH");
        intent.putExtra("state", state);
        sendBroadcast(intent);
    }

    //waifu list view
    void State4()
    {
        final character_DB character_DB = Room.databaseBuilder(screen_main.this,
                character_DB.class, "character_db").fallbackToDestructiveMigration()
                .build();

        //button to change view
        FloatingActionButton back = (FloatingActionButton)findViewById(R.id.overlay_timer_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //switch to state 0
                character_DB.close();
                onBackPressed();
            }
        });


        final TextView none = (TextView)findViewById(R.id.timer_none);

        none.setText("Your waifu list is empty. NORMIE.");

        Loading = new Dialog(screen_main.this);
        Loading.setContentView(R.layout.loading);
        Loading.setCancelable(false);
        Loading.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                final List<Character> waifu_list = character_DB.Character_daoAccess().fetch_list();

                character_DB.close();

                if (waifu_list.size() != 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            none.setVisibility(View.INVISIBLE);

                            RecyclerView list = (RecyclerView) findViewById(R.id.timer_list);
                            LinearLayoutManager llm = new LinearLayoutManager(screen_main.this);
                            list.setLayoutManager(llm);

                            final waifu_list_adapter adapter = new waifu_list_adapter();
                            adapter.waifuList = waifu_list;
                            list.setAdapter(adapter);

                            Loading.dismiss();

                            ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                                @Override
                                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                                    return false;
                                }

                                @Override
                                public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                                    final int position = viewHolder.getAdapterPosition(); //get position which is swipe

                                    Thread worker = new Thread() {
                                        public void run() {

                                            final character_DB character_DB = Room.databaseBuilder(screen_main.this,
                                                    character_DB.class, "character_db").fallbackToDestructiveMigration()
                                                    .build();

                                            Thread worker = new Thread() {
                                                public void run() {

                                                    boolean run = true;

                                                    while(run) {
                                                        delete s = new delete();

                                                        try {
                                                            String ret = s.execute(waifu_list.get(position).ALid, "1").get();
                                                            Loading.dismiss();

                                                            if (ret == null || ret.equals("error0")) {


                                                            } else if (ret.equals("success")) {

                                                                run = false;
                                                            }
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        } catch (ExecutionException e) {
                                                            e.printStackTrace();
                                                        }


                                                    }

                                                    SystemClock.sleep(5000);

                                                }
                                            };
                                            worker.start();

                                            character_DB.Character_daoAccess().delete(waifu_list.get(position).id);

                                            character_DB.close();

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    RecyclerView list = (RecyclerView) findViewById(R.id.timer_list);
                                                    if (waifu_list.size() == 1)
                                                    {
                                                        none.setVisibility(View.VISIBLE);
                                                    }
                                                    adapter.remove(position);
                                                }
                                            });

                                        }
                                    };
                                    worker.start();

                                }
                            };
                            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                            itemTouchHelper.attachToRecyclerView(list);
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Loading.dismiss();
                            none.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }) .start();

    }



    class waifu_list_adapter extends RecyclerView.Adapter<waifu_list_adapter.WaifuViewHolder>{

        List<Character> waifuList;

        class WaifuViewHolder extends RecyclerView.ViewHolder {
            ImageView img;
            TextView name;
            //int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1;

            WaifuViewHolder(View itemView) {
                super(itemView);
                img = (ImageView)itemView.findViewById(R.id.w_l_img);
                name = (TextView)itemView.findViewById(R.id.w_l_name);
            }
        }
        @Override
        public int getItemCount() {
            return waifuList.size();
        }
        @Override
        public WaifuViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.waifu_list_card, viewGroup, false);
            WaifuViewHolder vh = new WaifuViewHolder(v);
            return vh;
        }
        @Override
        public void onBindViewHolder(final WaifuViewHolder vh, final int i) {
            Picasso.get().load(waifuList.get(i).image_URL).into(vh.img);
            vh.name.setText(waifuList.get(i).name);
            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog chooser = new Dialog(screen_main.this);
                    chooser.setContentView(R.layout.anime_list_inst_dialog);
                    chooser.setCancelable(true);
                    chooser.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                    final Button stream = chooser.findViewById(R.id.ptw_d_stream);
                    final Button details = chooser.findViewById(R.id.ptw_d_details);

                    //TODO add option to just search google images or something

                    stream.setText("fanart");
                    stream.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            final Dialog fanart_chooser = new Dialog(screen_main.this);
                            fanart_chooser.setContentView(R.layout.fanart_dialog);
                            fanart_chooser.setCancelable(true);
                            fanart_chooser.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                            final Button gelbooru = fanart_chooser.findViewById(R.id.fa_gel);
                            final EditText tags = fanart_chooser.findViewById(R.id.fa_tags);
                            final EditText num = fanart_chooser.findViewById(R.id.fa_count);
                            final RadioButton safe = fanart_chooser.findViewById(R.id.fa_safe);
                            final RadioButton questionable = fanart_chooser.findViewById(R.id.fa_questionable);
                            final RadioButton explicit = fanart_chooser.findViewById(R.id.fa_explicit);
                            fanart_chooser.show();

                            final Dialog Loading2 = new Dialog(screen_main.this);
                            Loading2.setContentView(R.layout.loading);
                            Loading2.setCancelable(false);
                            Loading2.show();
                            Thread worker = new Thread() {
                                public void run() {
                                    Looper.prepare();

                                    final String tag = AL_name_to_gelbooru_tag.convert(waifuList.get(i).name, waifuList.get(i).media);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Loading2.dismiss();
                                            if (tag.equals("Error: no internet")) {
                                                Toast.makeText(screen_main.this, tag, Toast.LENGTH_SHORT).show();
                                                fanart_chooser.dismiss();
                                            }
                                            else {
                                                tags.setText(tag);
                                            }
                                        }
                                    });

                                }
                            };
                            worker.start();

                            final SharedPreferences sharedPref = getSharedPreferences("net.gauso001.SC_AI", Context.MODE_PRIVATE);

                            gelbooru.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Loading = new Dialog(screen_main.this);
                                    Loading.setContentView(R.layout.loading);
                                    Loading.setCancelable(false);
                                    Loading.show();
                                    Thread worker = new Thread() {
                                        public void run() {
                                            Looper.prepare();
                                            try
                                            {
                                                String rating = "safe";
                                                if (questionable.isChecked()){ rating = "questionable"; }
                                                else if (explicit.isChecked()) { rating = "explicit"; }

                                                // Building Parameters
                                                List<NameValuePair> params_i = new ArrayList<NameValuePair>();
                                                params_i.add(new BasicNameValuePair("limit", "1"));
                                                params_i.add(new BasicNameValuePair("page", "dapi"));
                                                params_i.add(new BasicNameValuePair("s", "post"));
                                                params_i.add(new BasicNameValuePair("q", "index"));
                                                params_i.add(new BasicNameValuePair("tags", tags.getText().toString()+" rating:"+rating)); //TODO also make global excluded tags

                                                String xml_i = jsonParser.makeHttpRequest_out_s("https://gelbooru.com/index.php",
                                                        "GET", params_i);

                                                try {
                                                    Log.d("getting gelbooru...", xml_i);
                                                } catch (java.lang.NullPointerException e) {
                                                }

                                                Document output_i = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(xml_i.getBytes("utf-8"))));
                                                output_i.getDocumentElement().normalize();

                                                Element meta = (Element)output_i.getElementsByTagName("posts").item(0);

                                                int total_posts = Integer.parseInt(meta.getAttribute("count"));
                                                if (total_posts > 0) {
                                                    int posts_per_page = 100;
                                                    int total_pages = total_posts / posts_per_page;
                                                    int posts_last_page = total_posts - (total_pages * posts_per_page);
                                                    if (posts_last_page > 0) {
                                                        total_pages++;
                                                    }

                                                    //send tag details message
                                                    Message msg = new Message();
                                                    msg.set_msg("Rating: " + rating + "\nTags used: " + tags.getText().toString());
                                                    msg.set_stage(-1);
                                                    DB.daoAccess().insert_single_message(msg);

                                                    for (int i = 0; i < Integer.parseInt(num.getText().toString().replace(" ", "")); i++) {
                                                        int page = new Random().nextInt(total_pages);

                                                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                                                        params.add(new BasicNameValuePair("limit", "100"));
                                                        params.add(new BasicNameValuePair("pid", String.valueOf(page)));
                                                        params.add(new BasicNameValuePair("page", "dapi"));
                                                        params.add(new BasicNameValuePair("s", "post"));
                                                        params.add(new BasicNameValuePair("q", "index"));
                                                        params.add(new BasicNameValuePair("tags", tags.getText().toString() + " rating:" + rating)); //TODO again, include global excluded tags

                                                        String xml = jsonParser.makeHttpRequest_out_s("https://gelbooru.com/index.php",
                                                                "GET", params);

                                                        try {
                                                            Log.d("getting gelbooru...", xml);
                                                        } catch (java.lang.NullPointerException e) {
                                                        }

                                                        Document output = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));
                                                        output.getDocumentElement().normalize();

                                                        boolean unique = false;
                                                        NodeList items = output.getElementsByTagName("post");
                                                        Element post = null;

                                                        while (!unique) {
                                                            int selected = new Random().nextInt(posts_per_page);
                                                            if (page + 1 == total_pages) {
                                                                selected = new Random().nextInt(posts_last_page);
                                                            }
                                                            post = (Element) items.item(selected);
                                                            int post_id = Integer.parseInt(post.getAttribute("id"));
                                                            unique = sharedPref.getInt(tags.getText().toString(), -1) != post_id || page + 1 == total_pages && posts_last_page == 1;
                                                        }

                                                        //send fanart message
                                                        Message msg3 = new Message();
                                                        msg3.set_msg(post.getAttribute("file_url"));
                                                        msg3.set_stage(-7);
                                                        DB.daoAccess().insert_single_message(msg3);
                                                    }

                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Loading.dismiss();
                                                            fanart_chooser.dismiss();
                                                            chooser.dismiss();
                                                            Intent intent = new Intent("net.gauso001.SC_AI_SWITCH");
                                                            intent.putExtra("state", 0);
                                                            sendBroadcast(intent);
                                                        }
                                                    });
                                                }
                                                else
                                                {
                                                    runOnUiThread(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            Loading.dismiss();
                                                            Toast.makeText(screen_main.this, "No posts",
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }


                                            } catch (ParserConfigurationException | IOException | SAXException e) {
                                                e.printStackTrace();
                                                runOnUiThread(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        Loading.dismiss();
                                                        Toast.makeText(screen_main.this, "No internet",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                            }
                                        }
                                    };
                                    worker.start();

                                }
                            });
                        }
                    });

                    details.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            final Dialog Loading = new Dialog(screen_main.this);
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
                                                "Character(id:"+waifuList.get(i).ALid+")\n" +
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

                                        final DB DB = Room.databaseBuilder(screen_main.this,
                                                DB.class, "msg_db")
                                                .build();

                                        //send chara details message
                                        Message msg3 = new Message();
                                        msg3.set_msg(output);
                                        msg3.set_stage(-6);
                                        DB.daoAccess().insert_single_message(msg3);

                                        DB.close();

                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                Loading.dismiss();
                                                chooser.dismiss();
                                                Intent intent = new Intent("net.gauso001.SC_AI_SWITCH");
                                                intent.putExtra("state", 0);
                                                sendBroadcast(intent);
                                            }
                                        });


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        runOnUiThread(new Runnable() {

                                            @Override
                                            public void run() {
                                                Loading.dismiss();
                                                Toast.makeText(screen_main.this, "No internet",
                                                        Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                    }
                                }
                            };
                            worker.start();
                        }
                    });


                    chooser.show();
                }
            });
        }
        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }


        public void remove(int i) {
            waifuList.remove(i);
            notifyItemRemoved(i);
        }
    }


    //alarm list view
    void State3()
    {
        final alarm_DB alarm_DB = Room.databaseBuilder(screen_main.this,
                alarm_DB.class, "alarm_db")
                .build();

        //button to change view
        FloatingActionButton back = (FloatingActionButton)findViewById(R.id.overlay_alarm_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //switch to state 0
                alarm_DB.close();
                onBackPressed();
            }
        });

        alarmPanel(alarm_DB);


            //deal with recyclerview
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                final List<Alarm> alarm_list = alarm_DB.Alarm_daoAccess().fetch_alarms();


                if (alarm_list.size() != 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            RecyclerView list = (RecyclerView) findViewById(R.id.alarm_list);
                            LinearLayoutManager llm = new LinearLayoutManager(screen_main.this);
                            list.setLayoutManager(llm);

                            final alarm_list_adapter adapter = new alarm_list_adapter();
                            adapter.alarmList = alarm_list;
                            list.setAdapter(adapter);


                            ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                                @Override
                                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                                    return false;
                                }

                                @Override
                                public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                                    final int position = viewHolder.getAdapterPosition(); //get position which is swipe

                                    Thread worker = new Thread() {
                                        public void run() {


                                            Thread worker = new Thread() {
                                                public void run() {

                                                    boolean run = true;

                                                    while(run) {
                                                        alarm_delete s = new alarm_delete();

                                                        try {
                                                            String ret = s.execute(String.valueOf(alarm_list.get(position).get_id())).get();

                                                            if (ret == null || ret.equals("error0")) {


                                                            } else if (ret.equals("success")) {

                                                                run = false;
                                                            }
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        } catch (ExecutionException e) {
                                                            e.printStackTrace();
                                                        }


                                                    }

                                                    SystemClock.sleep(5000);

                                                }
                                            };
                                            worker.start();

                                            alarm_DB.Alarm_daoAccess().delete(alarm_list.get(position).id);

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    adapter.remove(position);
                                                    alarmPanel(alarm_DB);
                                                }
                                            });


                                        }
                                    };
                                    worker.start();

                                }
                            };
                            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                            itemTouchHelper.attachToRecyclerView(list);
                        }
                    });
                }

            }
        }) .start();


    }

    int today;

    void alarmPanel(final alarm_DB alarm_DB)
    {
        final TextView mon = findViewById(R.id.al_mon);
        final TextView tues = findViewById(R.id.al_tues);
        final TextView wed = findViewById(R.id.al_wed);
        final TextView thurs = findViewById(R.id.al_thurs);
        final TextView fri = findViewById(R.id.al_fri);
        final TextView sat = findViewById(R.id.al_sat);
        final TextView sun = findViewById(R.id.al_sun);

        final TextView[] days = new TextView[8];

        today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1;

        if (today == 0) { today = 7; }

        switch(today)
        {
            case 1:
                days[0] = mon;
                days[1] = tues;
                days[2] = wed;
                days[3] = thurs;
                days[4] = fri;
                days[5] = sat;
                days[6] = sun;
                days[7] = mon;
                break;
            case 2:
                days[7] = tues;
                days[0] = tues;
                days[1] = wed;
                days[2] = thurs;
                days[3] = fri;
                days[4] = sat;
                days[5] = sun;
                days[6] = mon;
                break;
            case 3:
                days[6] = tues;
                days[7] = wed;
                days[0] = wed;
                days[1] = thurs;
                days[2] = fri;
                days[3] = sat;
                days[4] = sun;
                days[5] = mon;
                break;
            case 4:
                days[5] = tues;
                days[6] = wed;
                days[7] = thurs;
                days[0] = thurs;
                days[1] = fri;
                days[2] = sat;
                days[3] = sun;
                days[4] = mon;
                break;
            case 5:
                days[4] = tues;
                days[5] = wed;
                days[6] = thurs;
                days[7] = fri;
                days[0] = fri;
                days[1] = sat;
                days[2] = sun;
                days[3] = mon;
                break;
            case 6:
                days[3] = tues;
                days[4] = wed;
                days[5] = thurs;
                days[6] = fri;
                days[7] = sat;
                days[0] = sat;
                days[1] = sun;
                days[2] = mon;
                break;
            case 7:
                days[2] = tues;
                days[3] = wed;
                days[4] = thurs;
                days[5] = fri;
                days[6] = sat;
                days[7] = sun;
                days[0] = sun;
                days[1] = mon;
                break;
        }

        final SharedPreferences sharedPref = getSharedPreferences("net.gauso001.SC_AI", Context.MODE_PRIVATE);

        final SimpleDateFormat hr24 = new SimpleDateFormat("kk:mm");

        final String[] day_alarms = new String[8];
        final boolean[] override = new boolean[8];

        //initialise day_alarms
        for (int i = 0; i < 8; i++) { day_alarms[i] = ""; }

        //now deal with irregular alarms in next week and make list all below
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                Calendar[] cals = new GregorianCalendar[9];

                for (int i = 0; i < 9; i++)
                {
                    cals[i] = new GregorianCalendar();
                    cals[i].set(Calendar.HOUR_OF_DAY, 0);
                    cals[i].set(Calendar.MINUTE, 0);
                    cals[i].set(Calendar.SECOND, 0);
                    cals[i].set(Calendar.MILLISECOND, 0);
                    cals[i].add(Calendar.DAY_OF_MONTH, i);
                }

                List<List<Alarm>> day = new ArrayList<>();

                for (int i = 0; i < 8; i++)
                {
                    day.add(alarm_DB.Alarm_daoAccess().fetch_range(cals[i].getTimeInMillis(), cals[i+1].getTimeInMillis()));
                    for (int j = 0; j < day.get(i).size(); j++)
                    {
                        day_alarms[i] += hr24.format(new Date(day.get(i).get(j).get_time()))+"|";
                        override[i] = (!override[i] && day.get(i).get(j).get_override() == 1);
                    }
                }


                final String[] combined = new String[8];


                for (int i = 0; i < 8; i++)
                {
                    if (!override[i])
                    {
                        combined[i] = day_alarms[i]+sharedPref.getString(String.valueOf(((i+today)%7)+1), "");
                    }
                    else
                    {
                        combined[i] = day_alarms[i];
                    }
                }




                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < days.length; i++)
                        {
                            days[i].setTypeface(null, Typeface.NORMAL);
                            days[i].setTextColor(Color.GRAY);
                        }

                        for (int i = 1; i < 7; i++)
                        {
                            days[i].setText(list_out(combined[i]));
                        }

                        int now = Integer.parseInt(hr24.format(new Date()).replace(":",""));
                        String[] unsorted_string = combined[0].replace(":","").split("\\|");
                        int[] values = new int[unsorted_string.length];
                        for (int i = 0; i < values.length; i++)
                        {
                            values[i] = Integer.parseInt(unsorted_string[i]);
                        }
                        boolean alarmTodayFuture = false;
                        for (int i = 0; i < values.length; i++)
                        {
                            if (now < values[i]) { alarmTodayFuture = true; break; }
                        }
                        if (alarmTodayFuture)
                        {
                            days[0].setText(list_out(combined[0]));
                            days[0].setTypeface(null, Typeface.BOLD);
                            days[0].setTextColor(Color.BLACK);
                        }
                        else
                        {
                            days[0].setText(list_out(combined[7]));
                            days[1].setTypeface(null, Typeface.BOLD);
                            days[1].setTextColor(Color.BLACK);
                        }

                    }
                });



            }
        }).start();
    }


    class alarm_list_adapter extends RecyclerView.Adapter<alarm_list_adapter.AlarmViewHolder>{

        List<Alarm> alarmList;

        class AlarmViewHolder extends RecyclerView.ViewHolder {
            TextView time;
            TextView date;
            TextView override;

            AlarmViewHolder(View itemView) {
                super(itemView);
                time = (TextView)itemView.findViewById(R.id.a_time);
                date = (TextView)itemView.findViewById(R.id.a_date);
                override = (TextView)itemView.findViewById(R.id.a_override);
            }
        }
        @Override
        public int getItemCount() {
            return alarmList.size();
        }
        @Override
        public AlarmViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.alarm_list_card, viewGroup, false);
            AlarmViewHolder vh = new AlarmViewHolder(v);
            return vh;
        }
        @Override
        public void onBindViewHolder(final AlarmViewHolder vh, final int i) {
            if (alarmList.get(i).get_override() != 1) { vh.override.setVisibility(View.INVISIBLE); }
            SimpleDateFormat out_time = new SimpleDateFormat("hh:mm:ss aa");
            SimpleDateFormat out_day = new SimpleDateFormat("EEEE, MMM dd");
            vh.time.setText(out_time.format(new Date(alarmList.get(i).get_time())));
            vh.date.setText(out_day.format(new Date(alarmList.get(i).get_time())));
        }
        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }


        public void remove(int i) {
            alarmList.remove(i);
            notifyItemRemoved(i);
        }
    }


    String list_out(String in)
    {
        String[] unsorted_string = in.replace(":","").split("\\|");
        int[] sorted = new int[unsorted_string.length];
        for (int i = 0; i < sorted.length; i++)
        {
            sorted[i] = Integer.parseInt(unsorted_string[i]);
        }
        //first sort
        for (int i = 0; i < sorted.length; i++)
        {
            for (int j = sorted.length-1; j > i; j--)
            {
                if (sorted[i] > sorted[j]) { int b = sorted[i]; sorted[i] = sorted[j]; sorted[j] = b;}
            }
        }
        //then add ":" and make single string with newlines
        String[] next = new String[sorted.length];
        for (int i = 0; i< next.length; i++)
        {
            if (sorted[i] >= 1000)
            {
                next[i] = String.valueOf(sorted[i]).substring(0, 2)+":"+String.valueOf(sorted[i]).substring(2, 4);
            }
            else
            {
                next[i] = String.valueOf(sorted[i]).substring(0, 1)+":"+String.valueOf(sorted[i]).substring(1, 3);
            }
        }


        String out = next[0];
        for (int i = 1; i < sorted.length; i++)
        {
            out += "\n"+next[i];
        }
        return out;
    }

    //timer view can be reused since it's just a recyclerview. It might just become a general layout and be renamed
    void State2() {

        final anime_DB anime_DB = Room.databaseBuilder(screen_main.this,
                anime_DB.class, "anime_db").fallbackToDestructiveMigration()
                .build();

        //button to change view
        FloatingActionButton back = (FloatingActionButton)findViewById(R.id.overlay_timer_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //switch to state 0
                anime_DB.close();
                onBackPressed();
            }
        });


        final TextView none = (TextView)findViewById(R.id.timer_none);

        none.setText("There is nothing on your watch list");


        Loading = new Dialog(screen_main.this);
        Loading.setContentView(R.layout.loading);
        Loading.setCancelable(false);
        Loading.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                final List<Anime> anime_list = anime_DB.Anime_daoAccess().fetch_list();

                anime_DB.close();

                if (anime_list.size() != 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            none.setVisibility(View.INVISIBLE);

                            RecyclerView list = (RecyclerView) findViewById(R.id.timer_list);
                            LinearLayoutManager llm = new LinearLayoutManager(screen_main.this);
                            list.setLayoutManager(llm);

                            final anime_list_adapter adapter = new anime_list_adapter();
                            adapter.animeList = anime_list;
                            list.setAdapter(adapter);

                            Loading.dismiss();

                            ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                                @Override
                                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                                    return false;
                                }

                                @Override
                                public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                                    final int position = viewHolder.getAdapterPosition(); //get position which is swipe

                                    Thread worker = new Thread() {
                                        public void run() {

                                            final anime_DB anime_DB = Room.databaseBuilder(screen_main.this,
                                                    anime_DB.class, "anime_db")
                                                    .build();

                                            Thread worker = new Thread() {
                                                public void run() {

                                                    boolean run = true;

                                                    while(run) {
                                                        delete s = new delete();

                                                        try {
                                                            String ret = s.execute(anime_list.get(position).MALid, "0").get();
                                                            Loading.dismiss();

                                                            if (ret == null || ret.equals("error0")) {


                                                            } else if (ret.equals("success")) {

                                                                run = false;
                                                            }
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        } catch (ExecutionException e) {
                                                            e.printStackTrace();
                                                        }


                                                    }

                                                    SystemClock.sleep(5000);

                                                }
                                            };
                                            worker.start();

                                            anime_DB.Anime_daoAccess().delete(anime_list.get(position).id);

                                            anime_DB.close();

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    RecyclerView list = (RecyclerView) findViewById(R.id.timer_list);
                                                    if (anime_list.size() == 1)
                                                    {
                                                        none.setVisibility(View.VISIBLE);
                                                    }
                                                    adapter.remove(position);
                                                }
                                            });

                                        }
                                    };
                                    worker.start();

                                }
                            };
                            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                            itemTouchHelper.attachToRecyclerView(list);
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Loading.dismiss();
                            none.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }) .start();


    }



    class anime_list_adapter extends RecyclerView.Adapter<anime_list_adapter.AnimeViewHolder>{

        List<Anime> animeList;

        class AnimeViewHolder extends RecyclerView.ViewHolder {
            ImageView img;
            TextView title;
            TextView ar;
            TextView ne;
            TextView tn;
            //int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1;

            AnimeViewHolder(View itemView) {
                super(itemView);
                img = (ImageView)itemView.findViewById(R.id.a_l_img);
                title = (TextView)itemView.findViewById(R.id.a_l_name);
                ar = (TextView)itemView.findViewById(R.id.a_l_air_rating);
                ne = (TextView)itemView.findViewById(R.id.a_l_new_ep_day_count);
                tn = (TextView)itemView.findViewById(R.id.a_l_type_num);
            }
        }
        @Override
        public int getItemCount() {
            return animeList.size();
        }
        @Override
        public AnimeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.anime_list_card, viewGroup, false);
            AnimeViewHolder vh = new AnimeViewHolder(v);
            return vh;
        }
        @Override
        public void onBindViewHolder(final AnimeViewHolder vh, final int i) {
            Picasso.get().load(animeList.get(i).image_URL).into(vh.img);
            vh.title.setText(animeList.get(i).name);
            vh.ar.setText(animeList.get(i).score+" - "+animeList.get(i).AirDate);
            if (animeList.get(i).current) {
                vh.ne.setVisibility(View.VISIBLE);
                long diff_millis = (animeList.get(i).new_ep_day*1000) - new Date().getTime();
                int days = (int)(diff_millis/86400000);
                diff_millis -= days*86400000;
                //int hours = (int)(diff_millis/3600000);
                //System.out.println("SMD HOURS: "+hours);

                Calendar today_begin = new GregorianCalendar();
                today_begin.set(Calendar.HOUR_OF_DAY, 0);
                today_begin.set(Calendar.MINUTE, 0);
                today_begin.set(Calendar.SECOND, 0);
                today_begin.set(Calendar.MILLISECOND, 0);

                if  ((new Date().getTime() - today_begin.getTimeInMillis())+diff_millis > 86400000)
                {
                    days++;
                }

                if (days == 0 || days == 7) { vh.ne.setText("New episode today"); }
                else if (days == 1) { vh.ne.setText("New episode tomorrow"); }
                else
                {
                    vh.ne.setText("New episode in "+days+" days");
                }

            } else { vh.ne.setVisibility(View.INVISIBLE); }
            vh.tn.setText(animeList.get(i).type+" - "+animeList.get(i).numEpisodes+" episode(s)");
            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog chooser = new Dialog(screen_main.this);
                    chooser.setContentView(R.layout.anime_list_inst_dialog);
                    chooser.setCancelable(true);
                    chooser.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                    Button stream = chooser.findViewById(R.id.ptw_d_stream);
                    Button details = chooser.findViewById(R.id.ptw_d_details);

                    details.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            chooser.dismiss();
                            Intent intent = new Intent("net.gauso001.SC_AI_SWITCH");
                            intent.putExtra("state", 0);
                            intent.putExtra("s_i", Integer.parseInt(animeList.get(i).MALid));
                            sendBroadcast(intent);
                        }
                    });

                    if (animeList.get(i).streamType == 1 && !animeList.get(i).streamLink.equals("NOEP"))
                    {
                        //crunchyroll link
                        stream.setText("Crunchyroll stream");
                        stream.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final Dialog loading = new Dialog(screen_main.this);
                                loading.setContentView(R.layout.loading);
                                loading.setCancelable(false);
                                loading.show();
                                Thread worker = new Thread() {
                                    public void run() {
                                        Looper.prepare();
                                        //TODO login and session creation may be done over US proxy in the future
                                        List<NameValuePair> login_params = new ArrayList<NameValuePair>();
                                        login_params.add(new BasicNameValuePair("account", "gauso001@gauso001.net"));
                                        login_params.add(new BasicNameValuePair("password", "babcia212345678"));
                                        login_params.add(new BasicNameValuePair("access_token", CR_params.access_token));
                                        login_params.add(new BasicNameValuePair("device_type", CR_params.device_type));
                                        login_params.add(new BasicNameValuePair("device_id", CR_params.device_id));
                                        login_params.add(new BasicNameValuePair("version", CR_params.version));
                                        login_params.add(new BasicNameValuePair("locale", CR_params.locale));


                                        JSONObject json_login = null;
                                        json_login = jsonParser.makeHttpRequest(CR_login,
                                                "POST", login_params);
                                        System.out.println(CR_login);
                                        // check log cat for response

                                        try {
                                            Log.d("CR login...", json_login.toString());
                                        } catch (java.lang.NullPointerException e) {
                                        }


                                        // check for success tag
                                        try
                                        {

                                            List<NameValuePair> ss_params = new ArrayList<NameValuePair>();
                                            ss_params.add(new BasicNameValuePair("auth", json_login.getJSONObject("data").getString("auth")));
                                            ss_params.add(new BasicNameValuePair("access_token", CR_params.access_token));
                                            ss_params.add(new BasicNameValuePair("device_type", CR_params.device_type));
                                            ss_params.add(new BasicNameValuePair("device_id", CR_params.device_id));
                                            ss_params.add(new BasicNameValuePair("version", CR_params.version));
                                            ss_params.add(new BasicNameValuePair("locale", CR_params.locale));


                                            JSONObject ss_json = null;
                                            ss_json = jsonParser.makeHttpRequest(CR_sess,
                                                    "POST", ss_params);
                                            System.out.println(CR_sess);
                                            // check log cat for response

                                            try {
                                                Log.d("CR sess...", ss_json.toString());
                                            } catch (java.lang.NullPointerException e) {
                                            }

                                                final String session_id = ss_json.getJSONObject("data").getString("session_id");

                                            List<NameValuePair> params = new ArrayList<NameValuePair>();
                                            params.add(new BasicNameValuePair("query", "query { \n" +
                                                    "  Media (id: "+animeList.get(i).MALid+") { duration \n" +
                                                    "    streamingEpisodes {\n" +
                                                    "      title\n" +
                                                    "      thumbnail\n" +
                                                    "      url\n" +
                                                    "      site\n" +
                                                    "    }\n" +
                                                    "  }\n" +
                                                    "}"));

                                            JSONObject json = null;
                                            json = jsonParser.makeHttpRequest(AL,
                                                    "POST", params);
                                            System.out.println(AL);
                                            // check log cat for response

                                            try {
                                                Log.d("Fetching AL...", json.toString());
                                            } catch (java.lang.NullPointerException e) {
                                            }

                                            try {
                                                final JSONArray episodes_a = json.getJSONObject("data").getJSONObject("Media").getJSONArray("streamingEpisodes");


                                                //json.getJSONObject("data").getJSONObject("Media").getInt("duration")*60

                                                final int seconds = 24*60;

                                                final int[] playheads = new int[episodes_a.length()];

                                                String requests = "[{\"method_version\":\"0\",\"api_method\":\"info\",\"params\":{\"media_id\":\""+episodes_a.getJSONObject(0).getString("url").split("\\-")[episodes_a.getJSONObject(0).getString("url").split("\\-").length - 1]+"\",\"fields\":\"media.playhead\"}}";

                                                for (int i = 1; i < episodes_a.length(); i++)
                                                {
                                                    requests +=",{\"method_version\":\"0\",\"api_method\":\"info\",\"params\":{\"media_id\":\""+episodes_a.getJSONObject(i).getString("url").split("\\-")[episodes_a.getJSONObject(i).getString("url").split("\\-").length - 1]+"\",\"fields\":\"media.playhead\"}}";
                                                }
                                                requests += "]";

                                                List<NameValuePair> ls_params = new ArrayList<NameValuePair>();
                                                ls_params.add(new BasicNameValuePair("requests", requests));
                                                ls_params.add(new BasicNameValuePair("access_token", CR_params.access_token));
                                                ls_params.add(new BasicNameValuePair("device_type", CR_params.device_type));
                                                ls_params.add(new BasicNameValuePair("device_id", CR_params.device_id));
                                                ls_params.add(new BasicNameValuePair("version", CR_params.version));
                                                ls_params.add(new BasicNameValuePair("locale", CR_params.locale));

                                                JSONObject ls_json = null;
                                                ls_json = jsonParser.makeHttpRequest(CR_batch + "?&session_id=" + session_id,
                                                        "POST", ls_params);
                                                System.out.println(CR_batch + "?&session_id=" + session_id);
                                                // check log cat for response

                                                try {
                                                    Log.d("CR batch...", ls_json.toString());
                                                } catch (java.lang.NullPointerException e) {
                                                }

                                                JSONArray data = ls_json.getJSONArray("data");

                                                for (int i = 0; i < data.length(); i++)
                                                {
                                                    playheads[i] = data.getJSONObject(i).getJSONObject("body").getJSONObject("data").getInt("playhead");
                                                }



                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        loading.dismiss();

                                                        Dialog episodes = new Dialog(screen_main.this);
                                                        episodes.setContentView(R.layout.episode_list_dialog);
                                                        episodes.setCancelable(true);
                                                        episodes.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                                                        RecyclerView ep_list = episodes.findViewById(R.id.episode_list);
                                                        LinearLayoutManager llm = new LinearLayoutManager(screen_main.this);
                                                        ep_list.setLayoutManager(llm);

                                                        final episode_list_adapter adapter = new episode_list_adapter();
                                                        adapter.episodes = episodes_a;
                                                        adapter.session_id = session_id;
                                                        adapter.playheads = new int[playheads.length];
                                                        System.arraycopy(playheads, 0, adapter.playheads, 0, playheads.length);
                                                        adapter.seconds = seconds;
                                                        ep_list.setAdapter(adapter);

                                                        episodes.show();


                                                        playhead_listener = new BroadcastReceiver() {
                                                            @Override
                                                            public void onReceive(Context context, final Intent intent ) {
                                                                adapter.playheads[intent.getIntExtra("index",0)] = (int)intent.getLongExtra("playhead",0);
                                                                adapter.notifyItemChanged(intent.getIntExtra("index",0));

                                                                Thread worker = new Thread() {
                                                                    public void run() {
                                                                        Looper.prepare();

                                                                        try {
                                                                            List<NameValuePair> params = new ArrayList<NameValuePair>();
                                                                            params.add(new BasicNameValuePair("media_id", adapter.episodes.getJSONObject(intent.getIntExtra("index",0)).getString("url").split("\\-")[adapter.episodes.getJSONObject(intent.getIntExtra("index",0)).getString("url").split("\\-").length - 1]));
                                                                            params.add(new BasicNameValuePair("event", "playback_status"));
                                                                            params.add(new BasicNameValuePair("playhead", String.valueOf(intent.getLongExtra("playhead",0))));
                                                                            params.add(new BasicNameValuePair("access_token", CR_params.access_token));
                                                                            params.add(new BasicNameValuePair("device_type", CR_params.device_type));
                                                                            params.add(new BasicNameValuePair("device_id", CR_params.device_id));
                                                                            params.add(new BasicNameValuePair("version", CR_params.version));
                                                                            params.add(new BasicNameValuePair("locale", CR_params.locale));

                                                                            JSONObject ls_json = null;
                                                                            ls_json = jsonParser.makeHttpRequest(CR_log + "?&session_id=" + session_id,
                                                                                    "POST", params);
                                                                            System.out.println(CR_log + "?&session_id=" + session_id);
                                                                            // check log cat for response

                                                                            try {
                                                                                Log.d("CR log...", ls_json.toString());
                                                                            } catch (java.lang.NullPointerException e) {
                                                                            }
                                                                        }
                                                                        catch (JSONException e) {}

                                                                    }
                                                                };
                                                                worker.start();

                                                            }
                                                        };

                                                        registerReceiver(playhead_listener,new IntentFilter("net.gauso001.SC_AI_PLAYHEAD"));


                                                    }
                                                });
                                            }
                                            catch (JSONException e)
                                            {
                                                e.printStackTrace();
                                            }

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }



                                    }
                                };
                                worker.start();
                            }
                        });
                    }
                    else if (animeList.get(i).streamLink.equals("NOEP"))
                    {
                        //error, no episodes

                    }
                    else
                    {
                        stream.setVisibility(View.INVISIBLE);
                    }

                    chooser.show();
                }
            });
        }
        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }


        public void remove(int i) {
            animeList.remove(i);
            notifyItemRemoved(i);
        }
    }


    class episode_list_adapter extends RecyclerView.Adapter<episode_list_adapter.EpisodeViewHolder>{

        JSONArray episodes;
        String session_id;
        int[] playheads;
        int seconds;

        class EpisodeViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            ImageView img;
            ProgressBar bar;

            EpisodeViewHolder(View itemView) {
                super(itemView);
                name = (TextView)itemView.findViewById(R.id.episode_name);
                img = itemView.findViewById(R.id.episode_image);
                bar = itemView.findViewById(R.id.episode_bar);
            }
        }
        @Override
        public int getItemCount() {
            return episodes.length();
        }
        @Override
        public EpisodeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.episode_list_card, viewGroup, false);
            EpisodeViewHolder vh = new EpisodeViewHolder(v);
            return vh;
        }
        @Override
        public void onBindViewHolder(final EpisodeViewHolder vh, final int i) {
            try {

            vh.name.setText(episodes.getJSONObject(i).getString("title"));

            float progress = (float)playheads[i]/(float)seconds;
            progress *= 100;

            vh.bar.setProgress((int)progress);
            Picasso.get().load(episodes.getJSONObject(i).getString("thumbnail")).into(vh.img);

                vh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Dialog loading = new Dialog(screen_main.this);
                        loading.setContentView(R.layout.loading);
                        loading.setCancelable(false);
                        loading.show();
                        Thread worker = new Thread() {
                            public void run() {
                                Looper.prepare();

                                try {
                                    List<NameValuePair> ls_params = new ArrayList<NameValuePair>();
                                    ls_params.add(new BasicNameValuePair("media_id", episodes.getJSONObject(i).getString("url").split("\\-")[episodes.getJSONObject(i).getString("url").split("\\-").length - 1]));
                                    ls_params.add(new BasicNameValuePair("fields", "media.episode_number,media.playhead,media.url,media.stream_data"));
                                    ls_params.add(new BasicNameValuePair("access_token", CR_params.access_token));
                                    ls_params.add(new BasicNameValuePair("device_type", CR_params.device_type));
                                    ls_params.add(new BasicNameValuePair("device_id", CR_params.device_id));
                                    ls_params.add(new BasicNameValuePair("version", CR_params.version));
                                    ls_params.add(new BasicNameValuePair("locale", CR_params.locale));


                                    JSONObject ls_json = null;
                                    ls_json = jsonParser.makeHttpRequest(CR_ls + "?&session_id=" + session_id,
                                            "POST", ls_params);
                                    System.out.println(CR_ls + "?&session_id=" + session_id);
                                    // check log cat for response

                                    try {
                                        Log.d("CR ep sel...", ls_json.toString());
                                    } catch (java.lang.NullPointerException e) {
                                    }

                                    loading.dismiss();


                                    Intent intent = new Intent(screen_main.this, fullscreen_video.class);
                                    intent.putExtra("stream", ls_json.getJSONObject("data").getJSONObject("stream_data").getJSONArray("streams").getJSONObject(0).getString("url"));
                                    intent.putExtra("playhead",playheads[i]);
                                    intent.putExtra("index",i);
                                    startActivity(intent);


                                }
                                catch (JSONException e) {}

                            }
                        };
                        worker.start();
                    }
                });

            } catch (JSONException e) {}
        }
        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

    }


    boolean run_vibrate_pattern = false;
    void State1()
    {
        final timer_DB timer_DB = Room.databaseBuilder(screen_main.this,
                timer_DB.class, "timer_db")
                .build();

        //button to change view
        FloatingActionButton back = (FloatingActionButton)findViewById(R.id.overlay_timer_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //switch to state 0
                timer_DB.close();
                onBackPressed();
            }
        });



        final TextView none = (TextView)findViewById(R.id.timer_none);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                final List<Timer> timer_list = timer_DB.Timer_daoAccess().fetch_timers();

                timer_DB.close();

                if (timer_list.size() != 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            none.setVisibility(View.INVISIBLE);

                            RecyclerView list = (RecyclerView) findViewById(R.id.timer_list);
                            LinearLayoutManager llm = new LinearLayoutManager(screen_main.this);
                            list.setLayoutManager(llm);

                            final timer_list_adapter adapter = new timer_list_adapter();
                            adapter.timerList = timer_list;
                            list.setAdapter(adapter);

                            ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                                @Override
                                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                                    return false;
                                }

                                @Override
                                public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                                    final int position = viewHolder.getAdapterPosition(); //get position which is swipe

                                    Thread worker = new Thread() {
                                        public void run() {

                                            final timer_DB timer_DB = Room.databaseBuilder(screen_main.this,
                                                    timer_DB.class, "timer_db")
                                                    .build();

                                                    timer_DB.Timer_daoAccess().delete(timer_list.get(position).get_id());

                                            timer_DB.close();

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    RecyclerView list = (RecyclerView) findViewById(R.id.timer_list);
                                                    if (timer_list.size() == 1)
                                                    {
                                                        none.setVisibility(View.VISIBLE);
                                                    }
                                                        adapter.remove(position);
                                                }
                                            });

                                        }
                                    };
                                    worker.start();

                                }
                            };
                            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
                            itemTouchHelper.attachToRecyclerView(list);
                        }
                    });
                }
                else
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            none.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }) .start();




        //SI == 1 means timer alert. This is the timer up routine
        if (SupplementaryIndex == 1)
        {
            SupplementaryIndex = 0;
            run_vibrate_pattern = true;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(run_vibrate_pattern) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                        }

                        android.os.SystemClock.sleep(500);
                    }
                }
            }) .start();


            AlertDialog.Builder builder = new AlertDialog.Builder(screen_main.this).setTitle("Time is up!")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        run_vibrate_pattern = false;
                    }
                });

                builder.show();
        }

    }

    String hour_string = "";
    String minute_string = "";
    String second_string = "";
    boolean f_r = true;

    class timer_list_adapter extends RecyclerView.Adapter<timer_list_adapter.TimerViewHolder>{

        List<Timer> timerList;

        class TimerViewHolder extends RecyclerView.ViewHolder {
            TextView text;

            TimerViewHolder(View itemView) {
                super(itemView);
                text = (TextView)itemView.findViewById(R.id.t_time);
            }
        }
        @Override
        public int getItemCount() {
            return timerList.size();
        }
        @Override
        public TimerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.timer_list_card, viewGroup, false);
            TimerViewHolder vh = new TimerViewHolder(v);
            return vh;
        }
        @Override
        public void onBindViewHolder(final TimerViewHolder vh, final int i) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                while (true) {
                                    long total_left = (timerList.get(i).get_end() - new Date().getTime());

                                    if (!f_r) {
                                        android.os.SystemClock.sleep(100);
                                    } else {
                                        f_r = false;
                                    }

                                    if (total_left >= 0) {
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

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                vh.text.setText(hour_string + ":" + minute_string + ":" + second_string);
                                            }
                                        });
                                    }


                                }
                            }
                            catch (IndexOutOfBoundsException e) {}

                        }
                    }).start();



        }
        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        public void remove(int i) {
            timerList.remove(i);
            notifyItemRemoved(i);
        }
    }



    @Override
    public void onBackPressed() {
        if (playhead_listener != null)
        {
            unregisterReceiver(playhead_listener);
            playhead_listener = null;
        }
        if (state != 0)
        {
            //switch to state 0
            Intent intent = new Intent("net.gauso001.SC_AI_SWITCH");
            intent.putExtra("state", 0);
            sendBroadcast(intent);
        }
    }

    void State0()
    {
        //buttons to change views
        FloatingActionButton timer_list = (FloatingActionButton)findViewById(R.id.button_timer_list);
        timer_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //switch to state 1
                unregisterReceiver(listener);
                Intent intent = new Intent("net.gauso001.SC_AI_SWITCH");
                intent.putExtra("state", 1);
                sendBroadcast(intent);
            }
        });
        FloatingActionButton PTW_list = (FloatingActionButton)findViewById(R.id.button_anime_list);
        PTW_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //switch to state 2
                unregisterReceiver(listener);
                Intent intent = new Intent("net.gauso001.SC_AI_SWITCH");
                intent.putExtra("state", 2);
                sendBroadcast(intent);
            }
        });
        FloatingActionButton alarm_list = (FloatingActionButton)findViewById(R.id.button_alarm_list);
        alarm_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //switch to state 3
                unregisterReceiver(listener);
                Intent intent = new Intent("net.gauso001.SC_AI_SWITCH");
                intent.putExtra("state", 3);
                sendBroadcast(intent);
            }
        });
        FloatingActionButton waifu_list = (FloatingActionButton)findViewById(R.id.button_waifu_list);
        waifu_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //switch to state 4
                unregisterReceiver(listener);
                Intent intent = new Intent("net.gauso001.SC_AI_SWITCH");
                intent.putExtra("state", 4);
                sendBroadcast(intent);
            }
        });



        getMessages(true);

        listener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent ) {
                getMessages();
            }
        };
        registerReceiver(listener,new IntentFilter("net.gauso001.SC_AI_update_msg"));

        new_msg = (EditText)findViewById(R.id.new_msg);
        final ImageButton new_msg_send = (ImageButton)findViewById(R.id.new_msg_send);

        if (SupplementaryIndex != 0)
        {
            AnimeDetails(new Anime(String.valueOf(SupplementaryIndex),null, null, null, null, null, null, null), null, 5);
            SupplementaryIndex = 0;
        }

        new_msg_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();

                        //first try stage 1
                        //MAL search format 1
                        if (new_msg.getText().toString().length() > 12 && new_msg.getText().toString().substring(0, 11).equals("search for "))
                        {
                            MALsearch(new_msg.getText().toString().substring(11));
                        }
                        //MAL search format 2
                        else if (new_msg.getText().toString().length() > 9 && new_msg.getText().toString().substring(0, 8).equals("look up "))
                        {
                            MALsearch(new_msg.getText().toString().substring(8));
                        }
                        //character search
                        else if (new_msg.getText().toString().length() > 11 && new_msg.getText().toString().substring(0, 10).equals("character "))
                        {
                            chara_search(new_msg.getText().toString().substring(10));
                        }
                        //MAL prequel display
                        else if (new_msg.getText().toString().equals("prequel"))
                        {
                            if (anime_mentioned != null)
                            {
                                AnimeDetails(anime_mentioned, null, 2);
                            }
                            else
                            { no_a_m(); }
                        }
                        //MAL sequel display
                        else if (new_msg.getText().toString().equals("sequel"))
                        {
                            if (anime_mentioned != null)
                            {
                                AnimeDetails(anime_mentioned, null, 3);
                            }
                            else
                            { no_a_m(); }
                        }
                        //specific waifu add command
                        else if (new_msg.getText().toString().equals("waifu"))
                        {
                            if (character_mentioned != null)
                            {
                                add_to_waifu_list(character_mentioned);
                            }
                            else
                            {
                                no_c_m();
                            }
                        }
                        //add to PTW command
                        else if (new_msg.getText().toString().toLowerCase().equals("add to ptw") || new_msg.getText().toString().toLowerCase().equals("add to watch list"))
                        {
                            if (anime_mentioned != null)
                            {
                                AnimeDetails(anime_mentioned, null, 4);
                            }
                            else
                            { no_a_m(); }
                        }
                        //generic add to list command depends on context
                        else if(new_msg.getText().toString().toLowerCase().equals("add to list"))
                        {
                            if (anime_mentioned != null)
                            {
                                AnimeDetails(anime_mentioned, null, 4);
                            }
                            else if (character_mentioned != null)
                            {
                                add_to_waifu_list(character_mentioned);
                            }
                            else
                            {
                                no_m();
                            }
                        }
                        //force list update command
                        else if (new_msg.getText().toString().equals("update list"))
                        {
                            anime_list_refresh();
                        }
                        //yes to alarm override, need context
                        else if (new_msg.getText().toString().toLowerCase().equals("hai") || new_msg.getText().toString().toLowerCase().equals("yes"))
                        {
                            if (alarm_mentioned != 0)
                            {
                                set_alarm_override();
                            }
                            else
                            {
                                Stage2and3(new_msg.getText().toString());
                            }
                        }
                        //no to alarm override, need context
                        else if (new_msg.getText().toString().toLowerCase().equals("no"))
                        {
                            if (alarm_mentioned != 0)
                            {
                                no_alarm_override();
                            }
                            else
                            {
                                Stage2and3(new_msg.getText().toString());
                            }
                        }
                        //do nothing if message is blank
                        else if (new_msg.getText().toString().equals("")){}
                        //stage 2 desu, sets anime mentioned to null
                        else
                        {
                            Stage2and3(new_msg.getText().toString());
                        }

                    }
                }) .start();
            }
        });
    }

    Dialog Loading2 = null;
    void add_to_waifu_list(final Character waifu)
    {
        Thread worker = new Thread() {
            public void run() {
                try
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Loading2 = new Dialog(screen_main.this);
                            Loading2.setContentView(R.layout.loading);
                            Loading2.setCancelable(false);
                            Loading2.show();
                        }
                    });

                    // Building Parameters
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    String json_out = "query { \n" +
                            "Character(id:"+waifu.ALid+")\n" +
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

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                        new_msg.setText("");
                        }
                    });



                    if (!json.getJSONObject("data").getJSONObject("Character").getJSONObject("name").getString("first").equals("null")){name +=json.getJSONObject("data").getJSONObject("Character").getJSONObject("name").getString("first")+" ";}
                    if (!json.getJSONObject("data").getJSONObject("Character").getJSONObject("name").getString("last").equals("null")){name +=json.getJSONObject("data").getJSONObject("Character").getJSONObject("name").getString("last");}

                    //first set msg at stage1 and save
                    Message msg = new Message();
                    msg.set_msg(new_msg.getText().toString());
                    msg.set_stage(1);
                    DB.daoAccess().insert_single_message(msg);



                    //send acknowledgement
                    Message msg3 = new Message();
                    msg3.set_msg(getString(R.string.ACK) + "\n" + getString(R.string.ptw_added_1)+" "+name+" "+getString(R.string.waifu_added_2));
                    msg3.set_stage(-1);
                    DB.daoAccess().insert_single_message(msg3);


                    final character_DB character_DB = Room.databaseBuilder(screen_main.this,
                            character_DB.class, "character_db").fallbackToDestructiveMigration()
                            .build();
                    character_DB.Character_daoAccess().insert_single(new Character(json.getJSONObject("data").getJSONObject("Character").getString("id"),json.getJSONObject("data").getJSONObject("Character").getJSONObject("image").getString("large"),name,json.getJSONObject("data").getJSONObject("Character").getString("description"),json.getJSONObject("data").getJSONObject("Character").getJSONObject("media").toString()));
                    character_DB.close();

                    final String id = json.getJSONObject("data").getJSONObject("Character").getString("id");


                    Thread worker = new Thread() {
                        public void run() {

                            boolean run = true;

                            while(run) {
                                try {
                                add s = new add();
                                    String ret = s.execute(id,"1").get();

                                    if (ret == null || ret.equals("error0")) {


                                    } else if (ret.equals("success")) {

                                        run = false;
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();}



                            }

                            android.os.SystemClock.sleep(5000);

                        }
                    };
                    worker.start();


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Loading2.dismiss();
                            getMessages();
                        }
                    });


                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            Toast.makeText(screen_main.this, "No internet",
                                    Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }
        };
        worker.start();

    }

    void no_alarm_override()
    {
        Thread worker = new Thread() {
            public void run() {
                Looper.prepare();


                //first set msg at stage1 and save
                Message msg = new Message();
                msg.set_msg(new_msg.getText().toString());
                msg.set_stage(1);
                DB.daoAccess().insert_single_message(msg);

                new_msg.setText("");

                //first set msg at stage1 and save
                Message msg2 = new Message();
                msg2.set_msg(getString(R.string.understood));
                msg2.set_stage(-1);
                DB.daoAccess().insert_single_message(msg2);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //and refresh all messages
                        getMessages();

                    }
                });

            }
        };
        worker.start();
    }

    void set_alarm_override()
    {
        Thread worker = new Thread() {
            public void run() {
                Looper.prepare();
        final alarm_DB alarm_DB = Room.databaseBuilder(screen_main.this,
                alarm_DB.class, "alarm_db")
                .build();

        alarm_DB.Alarm_daoAccess().set_to_override(alarm_mentioned);



                //first set msg at stage1 and save
                Message msg = new Message();
                msg.set_msg(new_msg.getText().toString());
                msg.set_stage(1);
                DB.daoAccess().insert_single_message(msg);

                new_msg.setText("");

                //first set msg at stage1 and save
                Message msg2 = new Message();
                msg2.set_msg(getString(R.string.understood));
                msg2.set_stage(-1);
                DB.daoAccess().insert_single_message(msg2);

                alarm_DB.close();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //and refresh all messages
                        getMessages();

                    }
                });


                Thread worker = new Thread() {
                    public void run() {

                        boolean run = true;

                        while(run) {
                            alarm_setover s = new alarm_setover();

                            try {
                                String ret = s.execute(String.valueOf(alarm_mentioned)).get();
                                Loading.dismiss();

                                if (ret == null || ret.equals("error0")) {


                                } else if (ret.equals("success")) {

                                    run = false;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }


                        }

                        SystemClock.sleep(5000);

                    }
                };
                worker.start();

            }
        };
        worker.start();
    }

    void anime_list_refresh()
    {
        SharedPreferences sharedPref = getSharedPreferences("net.gauso001.SC_AI", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong("l_u", 0);
        editor.apply();

        Thread worker = new Thread() {
            public void run() {
                Looper.prepare();

                //first set msg at stage1 and save
                Message msg = new Message();
                msg.set_msg(new_msg.getText().toString());
                msg.set_stage(1);
                DB.daoAccess().insert_single_message(msg);

                new_msg.setText("");

                //first set msg at stage1 and save
                Message msg2 = new Message();
                msg2.set_msg(getString(R.string.ACK)+"\n"+getString(R.string.force_refresh));
                msg2.set_stage(-1);
                DB.daoAccess().insert_single_message(msg2);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //and refresh all messages
                        getMessages();

                    }
                });

            }
        };
        worker.start();
    }


    void SchedService()
    {
        ComponentName serviceComponent = new ComponentName(this, Server_sync_service.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(10000); // wait at least 10 seconds, consider making 5
        builder.setOverrideDeadline(300000); // maximum delay
        JobScheduler jobScheduler = (JobScheduler) getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

    void no_a_m()
    {
        Thread worker = new Thread() {
            public void run() {
                Looper.prepare();

                //first set msg at stage1 and save
                Message msg = new Message();
                msg.set_msg(new_msg.getText().toString());
                msg.set_stage(1);
                DB.daoAccess().insert_single_message(msg);

                new_msg.setText("");

                //first set msg at stage1 and save
                Message msg2 = new Message();
                msg2.set_msg(getString(R.string.sorry)+"\n"+getString(R.string.no_a_m));
                msg2.set_stage(-1);
                DB.daoAccess().insert_single_message(msg2);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //and refresh all messages
                        getMessages();

                    }
                });

            }
        };
        worker.start();
    }

    void no_m()
    {
        Thread worker = new Thread() {
            public void run() {
                Looper.prepare();

                //first set msg at stage1 and save
                Message msg = new Message();
                msg.set_msg(new_msg.getText().toString());
                msg.set_stage(1);
                DB.daoAccess().insert_single_message(msg);

                new_msg.setText("");

                //first set msg at stage1 and save
                Message msg2 = new Message();
                msg2.set_msg(getString(R.string.sorry)+"\n"+getString(R.string.no_m));
                msg2.set_stage(-1);
                DB.daoAccess().insert_single_message(msg2);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //and refresh all messages
                        getMessages();

                    }
                });

            }
        };
        worker.start();
    }

    void no_c_m()
    {
        Thread worker = new Thread() {
            public void run() {
                Looper.prepare();

                //first set msg at stage1 and save
                Message msg = new Message();
                msg.set_msg(new_msg.getText().toString());
                msg.set_stage(1);
                DB.daoAccess().insert_single_message(msg);

                new_msg.setText("");

                //first set msg at stage1 and save
                Message msg2 = new Message();
                msg2.set_msg(getString(R.string.sorry)+"\n"+getString(R.string.no_c_m));
                msg2.set_stage(-1);
                DB.daoAccess().insert_single_message(msg2);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //and refresh all messages
                        getMessages();

                    }
                });

            }
        };
        worker.start();
    }

    void Stage2and3(final String msg)
    {
        anime_mentioned = null;

        //proceed with stages2/3
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Loading = new Dialog(screen_main.this);
                Loading.setContentView(R.layout.loading);
                Loading.setCancelable(false);
                Loading.show();
            }
        });
        Thread worker = new Thread() {
            public void run() {
                Looper.prepare();
                Post_Stage_2_3 s = new Post_Stage_2_3();

                try {
                    String ret = s.execute(new_msg.getText().toString()).get();
                    Loading.dismiss();

                    if (ret == null || ret.equals("error0")) {

                        //no internet
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                Toast.makeText(screen_main.this, "No internet",
                                        Toast.LENGTH_SHORT).show();

                            }
                        });

                    } else if (ret.equals("success")) {

                        Message msg = new Message();
                        msg.set_msg(new_msg.getText().toString());
                        msg.set_stage(23);
                        DB.daoAccess().insert_single_message(msg);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                new_msg.setText("");
                                getMessages();
                            }
                        });



                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }
        };
        worker.start();

    }

    Dialog Loading;

    void MALsearch(final String search)
    {


        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Loading = new Dialog(screen_main.this);
                Loading.setContentView(R.layout.loading);
                Loading.setCancelable(false);
                Loading.show();
            }
        });
        Thread worker = new Thread() {
            public void run() {
                Looper.prepare();
                search_AL s = new search_AL();

                try {
                    String ret = s.execute(search).get();
                    Loading.dismiss();

                    if (ret == null || ret.equals("error0")) {

                        //no internet
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                Toast.makeText(screen_main.this, "No internet",
                                        Toast.LENGTH_SHORT).show();

                            }
                        });

                    } else if (ret.equals("success")) {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                final Dialog selector = new Dialog(screen_main.this);
                                selector.setContentView(R.layout.anime_list_dialog);
                                selector.setCancelable(true);

                                RecyclerView list = (RecyclerView) selector.findViewById(R.id.rec_list);
                                LinearLayoutManager llm = new LinearLayoutManager(screen_main.this);
                                list.setLayoutManager(llm);

                                MAL_list_adapter adapter = new MAL_list_adapter();
                                adapter.d = selector;
                                list.setAdapter(adapter);

                                selector.show();
                            }
                        });



                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }
        };
        worker.start();
    }


    void chara_search(final String search)
    {


        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Loading = new Dialog(screen_main.this);
                Loading.setContentView(R.layout.loading);
                Loading.setCancelable(false);
                Loading.show();
            }
        });
        Thread worker = new Thread() {
            public void run() {
                Looper.prepare();
                search_AL_chara s = new search_AL_chara();

                try {
                    String ret = s.execute(search).get();
                    Loading.dismiss();

                    if (ret == null || ret.equals("error0")) {

                        //no internet
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                Toast.makeText(screen_main.this, "No internet",
                                        Toast.LENGTH_SHORT).show();

                            }
                        });

                    } else if (ret.equals("success")) {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                final Dialog selector = new Dialog(screen_main.this);
                                selector.setContentView(R.layout.anime_list_dialog);
                                selector.setCancelable(true);

                                RecyclerView list = (RecyclerView) selector.findViewById(R.id.rec_list);
                                LinearLayoutManager llm = new LinearLayoutManager(screen_main.this);
                                list.setLayoutManager(llm);


                                MAL_list_adapter adapter = new MAL_list_adapter();
                                adapter.d = selector;
                                adapter.chara = true;
                                list.setAdapter(adapter);

                                selector.show();
                            }
                        });



                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }
        };
        worker.start();
    }


    void getMessages(final boolean... firstLaunch)
    {
        final Dialog loading = new Dialog(screen_main.this);
        loading.setContentView(R.layout.loading);
        loading.setCancelable(false);
        loading.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Message> msgs = DB.daoAccess().fetch_messages();

                if (firstLaunch != null)
                {
                    if (msgs.get(msgs.size()-1).get_stage() == -5)
                    {
                        final String[] data = msgs.get(msgs.size()-1).get_msg().split("\\|");
                        if (data.length > 1) {
                            alarm_mentioned = Integer.parseInt(data[1]);
                        }
                    } else { alarm_mentioned = 0; }

                    try
                    {
                        JSONObject obj = new JSONObject(msgs.get(msgs.size()-1).get_msg());
                        anime_mentioned = new Anime(obj.getJSONObject("Media").getString("id"), null, null, null, null, null, null, null);
                    }
                    catch (JSONException e) {}
                    try
                    {
                        JSONObject obj = new JSONObject(msgs.get(msgs.size()-1).get_msg());
                        character_mentioned = new Character(obj.getString("id"), null, null, null, null);
                    }
                    catch (JSONException e) {}
                }

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                final ListView msg_list = (ListView)findViewById(R.id.msg_list);
                msg_list.setDivider(null);
                msg_list.setDividerHeight(0);
                msg_list.setAdapter(new screen_main_adapter(screen_main.this, msgs));

                EditText new_msg = (EditText)findViewById(R.id.new_msg);
                new_msg.setTextColor(Color.WHITE);

                msg_list.setSelection(msgs.size()-1);

                        //on long click listener to download fanart
                        msg_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                                if (msgs.get(i).get_stage() == -7)
                                {
                                    final Dialog download = new Dialog(screen_main.this);
                                    download.setContentView(R.layout.download_dialog);
                                    download.setCancelable(true);
                                    download.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

                                    Button dl = download.findViewById(R.id.fa_download);
                                    dl.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Picasso.get()
                                                    .load(msgs.get(i).get_msg())
                                                    .into(getTarget("SCAI/scai_gelimg_"+msgs.get(i).get_msg().split("\\/")[msgs.get(i).get_msg().split("\\/").length-1].split("\\.")[0]+".png"));

                                            download.dismiss();
                                        }
                                    });

                                    download.show();
                                    return true;
                                }
                                else
                                { return false; }
                            }
                        });

                loading.dismiss();
                    }
                });

            }
        }) .start();
    }

    //target to save
    private static Target getTarget(final String url){
        Target target = new Target(){

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        System.out.println("LOC: "+url);

                        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + url);
                        try {
                            if (file.createNewFile()) {
                                FileOutputStream ostream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
                                ostream.flush();
                                ostream.close();

                                System.out.println("DONE");
                            }
                            else
                            {
                                System.out.println("ERROR");
                            }
                        } catch (IOException e) {
                            Log.e("IOException", e.getLocalizedMessage());
                        }
                    }
                }).start();

            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        return target;
    }


    void AnimeDetails(final Anime a, final Dialog d, final int mode)
    {
        character_mentioned = null;
        anime_mentioned = a;

        Thread worker = new Thread() {
            public void run() {
                Looper.prepare();

                if (mode == 1 || mode == 0) {
                    //first set msg at stage1 and save
                    Message msg = new Message();
                    msg.set_msg(new_msg.getText().toString());
                    msg.set_stage(1);
                    DB.daoAccess().insert_single_message(msg);
                }

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                //then dismiss list dialog and clear text field
                if (mode == 1) {d.dismiss();}
                if (mode == 1 || mode == 0) {new_msg.setText("");}

                //fetch anime details and print

                Loading = new Dialog(screen_main.this);
                Loading.setContentView(R.layout.loading);
                Loading.setCancelable(false);
                Loading.show();
            }
        });

                fetch_AL s = new fetch_AL();

                try {
                    String ret = s.execute(a.MALid).get();


                    if (ret == null || ret.equals("error0")) {

                        //no internet
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                Loading.dismiss();
                                Toast.makeText(screen_main.this, "No internet",
                                        Toast.LENGTH_SHORT).show();

                            }
                        });

                    } else if (ret.equals("success")) {

                        //normal anime search
                        if (mode == 1 || mode == 0 || mode == 5) {
                            if (mode != 5) {
                                //send acknowledgement
                                Message msg3 = new Message();
                                msg3.set_msg(getString(R.string.ACK) + "\n" + getString(R.string.here));
                                msg3.set_stage(-1);
                                DB.daoAccess().insert_single_message(msg3);
                            }

                            //save JSON output
                            Message msg2 = new Message();
                            msg2.set_msg(buffer);
                            msg2.set_stage(-2);
                            DB.daoAccess().insert_single_message(msg2);


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    //and refresh all messages
                                    getMessages();

                                    //dismiss only at very end
                                    Loading.dismiss();
                                }
                            });
                        }
                        //get if other request
                        else
                        {
                            Loading.dismiss();
                            try {
                                final JSONObject obj = new JSONObject(buffer).getJSONObject("Media");

                                if (mode == 2)
                                {
                                    try {

                                        JSONArray nodes = obj.getJSONObject("relations").getJSONArray("nodes");
                                        JSONArray types = obj.getJSONObject("relations").getJSONArray("edges");
                                        for (int i = 0; i < types.length(); i++)
                                        {
                                            if (types.getJSONObject(i).getString("relationType").equals("PREQUEL"))
                                            {
                                                AnimeDetails(new Anime(nodes.getJSONObject(i).getString("id"),null, null, null, null, null, null, null), null, 0);
                                            }
                                        }
                                    }
                                    catch (JSONException e) {
                                        //first set msg at stage1 and save
                                        Message msg = new Message();
                                        msg.set_msg(new_msg.getText().toString());
                                        msg.set_stage(1);
                                        DB.daoAccess().insert_single_message(msg);

                                        new_msg.setText("");

                                        //send acknowledgement
                                        Message msg3 = new Message();
                                        msg3.set_msg(getString(R.string.sorry) + "\n" + getString(R.string.no_prequel));
                                        msg3.set_stage(-1);
                                        DB.daoAccess().insert_single_message(msg3);

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                getMessages();
                                            }
                                        });
                                    }
                                }
                                else if (mode == 3)
                                {
                                    try {
                                        JSONArray nodes = obj.getJSONObject("relations").getJSONArray("nodes");
                                        JSONArray types = obj.getJSONObject("relations").getJSONArray("edges");
                                        for (int i = 0; i < types.length(); i++)
                                        {
                                            if (types.getJSONObject(i).getString("relationType").equals("SEQUEL"))
                                            {
                                                AnimeDetails(new Anime(nodes.getJSONObject(i).getString("id"),null, null, null, null, null, null, null), null, 0);
                                            }
                                        }
                                    }
                                    catch (JSONException e) {
                                        //first set msg at stage1 and save
                                        Message msg = new Message();
                                        msg.set_msg(new_msg.getText().toString());
                                        msg.set_stage(1);
                                        DB.daoAccess().insert_single_message(msg);

                                        new_msg.setText("");

                                        //send acknowledgement
                                        Message msg3 = new Message();
                                        msg3.set_msg(getString(R.string.sorry) + "\n" + getString(R.string.no_sequel));
                                        msg3.set_stage(-1);
                                        DB.daoAccess().insert_single_message(msg3);

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                getMessages();
                                            }
                                        });
                                    }
                                }
                                else if (mode == 4)
                                {
                                    final anime_DB anime_DB = Room.databaseBuilder(screen_main.this,
                                            anime_DB.class, "anime_db")
                                            .build();

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

                                    if (obj.getString("status").equals("RELEASING"))
                                    {
                                        airString = "Currently airing";
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
                                    for (int i = 0; i < links.length(); i++)
                                    {
                                        if (links.getJSONObject(i).getString("site").equals("Crunchyroll"))
                                        {
                                            anime.streamType = 1;

                                            JSONArray EPs = obj.getJSONArray("streamingEpisodes");
                                            if (EPs.length() > 0) {
                                                anime.streamLink = EPs.getJSONObject(EPs.length()-1).getString("url");
                                            } else { anime.streamLink = "NOEP"; }
                                            break;
                                        }
                                    }

                                    anime_DB.Anime_daoAccess().insert_single_anime(anime);
                                    anime_DB.close();

                                    //first set msg at stage1 and save
                                    Message msg = new Message();
                                    msg.set_msg(new_msg.getText().toString());
                                    msg.set_stage(1);
                                    DB.daoAccess().insert_single_message(msg);

                                    new_msg.setText("");

                                    //send acknowledgement
                                    Message msg3 = new Message();
                                    msg3.set_msg(getString(R.string.ACK) + "\n" + getString(R.string.ptw_added_1)+" "+obj.getJSONObject("title").getString("romaji")+" "+getString(R.string.ptw_added_2));
                                    msg3.set_stage(-1);
                                    DB.daoAccess().insert_single_message(msg3);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            getMessages();
                                        }
                                    });


                                    Thread worker = new Thread() {
                                        public void run() {

                                            boolean run = true;

                                            while(run) {
                                                add s = new add();

                                                try {
                                                    String ret = s.execute(obj.getString("id"),"0").get();
                                                    Loading.dismiss();

                                                    if (ret == null || ret.equals("error0")) {


                                                    } else if (ret.equals("success")) {

                                                       run = false;
                                                    }
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                } catch (ExecutionException e) {
                                                    e.printStackTrace();
                                                } catch (JSONException e) {}


                                            }

                                            android.os.SystemClock.sleep(5000);

                                        }
                                    };
                                    worker.start();

                                }


                            } catch (JSONException e) {
                                System.out.println(e);
                            }
                        }



                    } else if (ret.equals("error5")) {
                        //server error
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                Loading.dismiss();
                                Toast.makeText(screen_main.this, ".....something went wrong with the server. again.",
                                        Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }
        };
        worker.start();

    }


     class MAL_list_adapter extends RecyclerView.Adapter<MAL_list_adapter.AnimeViewHolder>{

        Dialog d;
        boolean chara = false;

        class AnimeViewHolder extends RecyclerView.ViewHolder {
            CardView cv;
            TextView desc;
            TextView name;
            ImageView img;
            TextView air_rating;

            AnimeViewHolder(View itemView) {
                super(itemView);
                cv = (CardView)itemView.findViewById(R.id.a_card_simple);
                desc = (TextView)itemView.findViewById(R.id.a_desc);
                name = (TextView)itemView.findViewById(R.id.a_name);
                img = (ImageView)itemView.findViewById(R.id.a_img);
                air_rating = (TextView)itemView.findViewById(R.id.a_air_rating);
            }
        }
        @Override
        public int getItemCount() {
            if (!chara) {
                return MAL_search_results.size();
            }
            else
            {
                return chara_search_results.size();
            }
        }
        @Override
        public AnimeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.anime_list_dialog_card, viewGroup, false);
            AnimeViewHolder avh = new AnimeViewHolder(v);
            return avh;
        }
        @Override
        public void onBindViewHolder(AnimeViewHolder avh, final int i) {
            if (!chara) {
                avh.name.setText(MAL_search_results.get(i).name);
                avh.desc.setText(MAL_search_results.get(i).description.replace("<br>", ""));
                if (MAL_search_results.get(i).score.equals("null")) {
                    avh.air_rating.setText(MAL_search_results.get(i).AirDate);
                } else {
                    avh.air_rating.setText("Rating: " + MAL_search_results.get(i).score + " - " + MAL_search_results.get(i).AirDate);
                }
                Picasso.get().load(MAL_search_results.get(i).image_URL).into(avh.img);
                avh.cv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AnimeDetails(MAL_search_results.get(i), d, 1);
                    }
                });
            }
            else
            {
                avh.name.setText(chara_search_results.get(i).name);
                Picasso.get().load(chara_search_results.get(i).image_URL).into(avh.img);
                avh.air_rating.setText("");
                avh.desc.setText(chara_search_results.get(i).description.replace("<br>", ""));
                avh.cv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Dialog Loading = new Dialog(screen_main.this);
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
                                            "Character(id:"+chara_search_results.get(i).ALid+")\n" +
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

                                    anime_mentioned = null;
                                    character_mentioned = chara_search_results.get(i);

                                    //first set msg at stage1 and save
                                    Message msg = new Message();
                                    msg.set_msg(new_msg.getText().toString());
                                    msg.set_stage(1);
                                    DB.daoAccess().insert_single_message(msg);

                                    new_msg.setText("");

                                    //send chara details message
                                    Message msg3 = new Message();
                                    msg3.set_msg(output);
                                    msg3.set_stage(-6);
                                    DB.daoAccess().insert_single_message(msg3);


                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            Loading.dismiss();
                                            d.dismiss();
                                            getMessages();
                                        }
                                    });


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {

                                            Toast.makeText(screen_main.this, "No internet",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                    });
                                }
                            }
                        };
                        worker.start();
                    }
                });
            }
        }
         @Override
         public void onAttachedToRecyclerView(RecyclerView recyclerView) {
             super.onAttachedToRecyclerView(recyclerView);
         }
    }



    private class search_AL extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            String json_out = "query ($page: Int, $perPage: Int) {\n" +
                    "  Page (page: $page, perPage: $perPage) {\n" +
                    "    pageInfo {\n" +
                    "      total\n" +
                    "      currentPage\n" +
                    "      lastPage\n" +
                    "      hasNextPage\n" +
                    "      perPage\n" +
                    "    }\n" +
                    "    media (search: \""+args[0]+"\", type: ANIME) {\n" +
                    "      id\n" +
                    "      title {\n" +
                    "        romaji\n" +
                    "      }\n" +
                    "      coverImage\n" +
                    "      {\n" +
                    "        large\n" +
                    "      }\n" +
                    "      averageScore\n" +
                    "      description(asHtml: false)\n" +
                    "      episodes \n" +
                    "      format\n" +
                    "      startDate {\n" +
                    "        year\n" +
                    "        month\n" +
                    "        day\n" +
                    "      }\n" +
                    "              \n" +
                    "      \n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
            params.add(new BasicNameValuePair("query", json_out));


                JSONObject json = null;
                json = jsonParser.makeHttpRequest(AL,
                        "POST", params);
                System.out.println(AL);
                // check log cat for response

                try {
                    Log.d("Searching AL...", json.toString());
                } catch (java.lang.NullPointerException e) {
                    return "error0";
                }

                // check for success tag
                try
                {
                        MAL_search_results.clear();
                        JSONArray results = json.getJSONObject("data").getJSONObject("Page").getJSONArray("media");

                        for (int i = 0; i < results.length(); i++)
                        {
                            JSONObject obj = results.getJSONObject(i);

                            String airString = "";
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

                            MAL_search_results.add(new Anime(obj.getString("id"), obj.getJSONObject("coverImage").getString("large"),obj.getJSONObject("title").getString("romaji"), obj.getString("format"),obj.getString("averageScore"), obj.getString("episodes"),obj.getString("description"), airString));
                        }


                        return "success";


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            return null;
        }

    }

    private class search_AL_chara extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            String json_out = "query ($page: Int, $perPage: Int) {\n" +
                    "  Page (page: $page, perPage: $perPage) {\n" +
                    "    pageInfo {\n" +
                    "      total\n" +
                    "      currentPage\n" +
                    "      lastPage\n" +
                    "      hasNextPage\n" +
                    "      perPage\n" +
                    "    }\n" +
                    "    characters (search:\""+args[0]+"\") {\n" +
                    "      id\n" +
                    "      name\n" +
                    "      {\n" +
                    "        first\n" +
                    "        last\n" +
                    "      }\n" +
                    "      description(asHtml:false)\n" +
                    "      image {\n" +
                    "        large\n" +
                    "      }\n" +
                    "      media(type:ANIME)\n" +
                    "      {\n" +
                    "        nodes\n" +
                    "        {\n" +
                    "          id\n" +
                    "          title\n" +
                    "          {romaji}\n" +
                    "          coverImage\n" +
                    "        {large}\n" +
                    "          averageScore\n" +
                    "          description(asHtml:false)\n" +
                    "          episodes\n" +
                    "          format\n" +
                    "          startDate\n" +
                    "          {\n" +
                    "            year\n" +
                    "            day\n" +
                    "            month\n" +
                    "          }\n" +
                    "        }\n" +
                    "      }\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
            params.add(new BasicNameValuePair("query", json_out));


            JSONObject json = null;
            json = jsonParser.makeHttpRequest(AL,
                    "POST", params);
            System.out.println(AL);
            // check log cat for response

            try {
                Log.d("Searching AL...", json.toString());
            } catch (java.lang.NullPointerException e) {
                return "error0";
            }

            // check for success tag
            try
            {
                chara_search_results.clear();
                JSONArray results = json.getJSONObject("data").getJSONObject("Page").getJSONArray("characters");

                for (int i = 0; i < results.length(); i++)
                {
                    JSONObject obj = results.getJSONObject(i);

                    String name = "";

                    if (!obj.getJSONObject("name").getString("first").equals("null")){name +=obj.getJSONObject("name").getString("first")+" ";}
                    if (!obj.getJSONObject("name").getString("last").equals("null")){name +=obj.getJSONObject("name").getString("last");}

                    chara_search_results.add(new Character(obj.getString("id"),obj.getJSONObject("image").getString("large"),name, obj.getString("description"),obj.getJSONObject("media").toString()));
                }


                return "success";


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
                        buffer = json.getJSONObject("data").toString();
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

    private class Post_Stage_2_3 extends AsyncTask<String, String, String> {

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
            params.add(new BasicNameValuePair("msg", args[0]));



                JSONObject json = null;
                json = jsonParser.makeHttpRequest(stage_2_3_post,
                        "POST", params);
                System.out.println(stage_2_3_post);
                // check log cat for response

                try {
                    Log.d("sending for stage2/3...", json.toString());
                } catch (java.lang.NullPointerException e) {
                    return "error0";
                }

                // check for success tag
                try
                {
                    if (json.getInt(TAG_SUCCESS) == 1) {
                        return "success";
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            return null;
        }

    }

    private class add extends AsyncTask<String, String, String> {

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
            params.add(new BasicNameValuePair("cmd", args[1]));



            JSONObject json = null;
            json = jsonParser.makeHttpRequest(add_l,
                    "POST", params);
            System.out.println(add_l);
            // check log cat for response

            try {
                Log.d("addting to PTW...", json.toString());
            } catch (java.lang.NullPointerException e) {
                return "error0";
            }

            // check for success tag
            try
            {
                if (json.getInt(TAG_SUCCESS) == 1) {
                    return "success";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }
    }

    private class delete extends AsyncTask<String, String, String> {

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
            params.add(new BasicNameValuePair("cmd", args[1]));



            JSONObject json = null;
            json = jsonParser.makeHttpRequest(delete_l,
                    "POST", params);
            System.out.println(delete_l);
            // check log cat for response

            try {
                Log.d("deleteing from PTW...", json.toString());
            } catch (java.lang.NullPointerException e) {
                return "error0";
            }

            // check for success tag
            try
            {
                if (json.getInt(TAG_SUCCESS) == 1) {
                    return "success";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }
    }

    private class alarm_delete extends AsyncTask<String, String, String> {

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
            json = jsonParser.makeHttpRequest(alarm_delete_l,
                    "POST", params);
            System.out.println(alarm_delete_l);
            // check log cat for response

            try {
                Log.d("deleteing alarm...", json.toString());
            } catch (java.lang.NullPointerException e) {
                return "error0";
            }

            // check for success tag
            try
            {
                if (json.getInt(TAG_SUCCESS) == 1) {
                    return "success";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }
    }

    private class alarm_setover extends AsyncTask<String, String, String> {

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
            json = jsonParser.makeHttpRequest(alarm_set_over,
                    "POST", params);
            System.out.println(alarm_set_over);
            // check log cat for response

            try {
                Log.d("setting alarm OVR...", json.toString());
            } catch (java.lang.NullPointerException e) {
                return "error0";
            }

            // check for success tag
            try
            {
                if (json.getInt(TAG_SUCCESS) == 1) {
                    return "success";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }
    }



}


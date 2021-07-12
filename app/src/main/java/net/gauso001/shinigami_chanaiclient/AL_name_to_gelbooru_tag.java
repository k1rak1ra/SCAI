package net.gauso001.shinigami_chanaiclient;


import android.util.Log;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public abstract class AL_name_to_gelbooru_tag {
    static JSONParser jsonParser = new JSONParser();
    static String tag = "";
    static String convert(final String name, final String media)
    {
        //first take care of manual overrides, for name-to-tag transitions which seemingly make no bloody sense
        if (name.equals("Yataorishino Igsem")) { return "yatorishino_xam"; }
        //really really ghetto but should work desu
        else if (name.equals("Nemesis ") && media.substring(0,22).equals("{\"nodes\":[{\"id\":13663,")) { return "master_nemesis"; }

        //try and convert character name to tag with the help of danbooru's API (since both boorus use the same tags)
                try
                {
                    //stage 1 preparation. More complicated but prevents issues from having double spaces (which was encountered once with anilist data)
                    String[] pieces = name.split("\\s+");
                    tag = pieces[0];
                    for (int i = 1; i < pieces.length; i++) { tag+= "_"+pieces[i]; }

                    //3 stages of trying to get a tag:
                    //stage 1/ideal case: adding an underscore to 2-part names or straight single-part name is the tag
                    //stage 2: reversing the order of first/last name
                    //stage 3/pain: single name given, and it's very common and so anime name is included in tag (for example: Emilia-chan's tag is emilia_(re:zero))
                    for (int i = 0; i < 4; i++)
                    { System.out.println(tag);
                        List<NameValuePair> params_i = new ArrayList<NameValuePair>();
                        params_i.add(new BasicNameValuePair("commit", "Search"));
                        params_i.add(new BasicNameValuePair("page", "0"));
                        params_i.add(new BasicNameValuePair("search[hide_empty]", "yes"));
                        params_i.add(new BasicNameValuePair("search[order]", "count"));
                        params_i.add(new BasicNameValuePair("search[category]", "4"));
                        params_i.add(new BasicNameValuePair("search[name_matches]", tag));

                        String j_arr = jsonParser.makeHttpRequest_out_s("https://danbooru.donmai.us/tags.json", "GET", params_i);

                        try {
                            Log.d("checking tag...", j_arr);
                        } catch (java.lang.NullPointerException e) {
                        }

                        JSONArray tags = new JSONArray(j_arr);

                        //if returned array isn't empty,
                        if (tags.length() != 0 && i != 2) {
                            break;
                        }
                        else
                        {
                            switch(i) {
                                //just finished stage 1, reverse first/last names and do stage 2. This will not work with characters with middle names but it hasn't been a problem yet
                                case 0:
                                try {
                                    tag = name.split("\\s+")[1] + "_" + name.split("\\s+")[0];
                                    break;
                                    //could be a single-name as well. If so, skip this stage
                                } catch (ArrayIndexOutOfBoundsException e) { i = 1; }
                                //the anime name might be included. We'll get the character's anime name possibilities by appending a wildcard to the request and then trying to match
                                case 1:
                                    tag +="*";
                                    break;
                                //now we try and match. First, we extract the content of the brackets. For now, we'll assume the form will be around the lines of "name_(anime)" and will remove all colons/spaces, etc and do a direct letter-by-letter comparison
                                case 2:
                                    List<String> anime_given = new ArrayList<String>();
                                    for (int j = 0; j < tags.length(); j++)
                                    {
                                        try { anime_given.add(tags.getJSONObject(j).getString("name").split("\\(")[1].split("\\)")[0].replace("_","").replace(":","").replace("!","").replace("?","")); } catch (JSONException | ArrayIndexOutOfBoundsException e) {} //there will be items not of the form (other character names) and they'll generate an exception. ignore them and move on
                                    }

                                    //for now we'll select the first anime in the list. set to lower case since tags are lower case
                                    String anime_name = new JSONObject(media).getJSONArray("nodes").getJSONObject(0).getJSONObject("title").getString("romaji").toLowerCase().replace(" ","").replace(":","").replace("-","").replace("!","").replace("?","");

                                    //We're assuming at least the first letter matches. Otherwise, we're screwed desu. We'll comtinue matching letter-by-letter until one option remains, scoring by matching letter.
                                    int[] matching_score = new int[anime_given.size()];

                                    for (int j = 0; j < anime_given.size(); j++)
                                    {
                                        try
                                        {
                                            for (int q = 0; q < anime_given.get(j).length(); q++)
                                            {
                                                if (anime_given.get(j).substring(q).equals(anime_name.substring(q))) { matching_score[j]++; }
                                            }
                                        } catch (ArrayIndexOutOfBoundsException e) {}
                                    }

                                    //Now the option with the highest score is our tag
                                    int max_index = 0;
                                    for (int j = 0; j < matching_score.length; j++)
                                    {
                                        if (matching_score[max_index] < matching_score[j]) { max_index = j; }
                                    }
                                    try { tag = tags.getJSONObject(max_index).getString("name"); } catch (JSONException e) {}
                                    break;
                                //all hope is lost, or I'm just shit at writing algorithms
                                case 3:
                                    tag = "Error: nothing found";
                                    break;
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    tag = "Error: no internet";
                }

        return tag;
    }
}

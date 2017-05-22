package com.example.cheriehuang.referee;

/**
 * Created by cheriehuang on 5/19/17.
 */
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import android.widget.ProgressBar;
import java.io.IOException;
import java.io.File;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;


public class BaseballActivity  extends BaseActivity{
    private ListView mylistview;
    private android.widget.ProgressBar progressBar;
    private static String url = "https://www.mysportsfeeds.com/api/feed/pull/mlb/2017-regular/daily_game_schedule.json?fordate=";
    ArrayList<HashMap<String, String>> gameList;
    Context thecontext;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baseball);
        gameList = new ArrayList<>();
        mylistview = (ListView)findViewById(R.id.baseball_list);
        thecontext = this;
            new GetGameSchedule().execute();
    }

    /*
    Shouldn't use savedInstanceState to save feed data such as grabbed from URL
    But can use it to save form data probably like which games the player has selected
     */

    //the class for async task calling the url to get json
    //basically does it in the backgroundthread asynchonously
    private class GetGameSchedule extends AsyncTask<Void, Void, Void>
    {

        private SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.US);

        private File getFile(String fname)
        {
            return getBaseContext().getFileStreamPath(fname);

        }

        private boolean fileExistance(String fname){
            File file = getFile(fname);
            return file.exists();
        }

        private Date fileLastModified(String fname)
        {
            //if the file exists check the last modified date
            File file = getFile(fname);
            Date lastModified = null;
            if (file.exists())
            {
                lastModified = new Date(file.lastModified());
            }
            return lastModified;
        }

        private Boolean compareDates()
        {
            //new Date() gives us today's date
            return fmt.format(new Date()).equals(fmt.format(fileLastModified("gameList")));
        }



        //invoked on the UI thread before the task is executed. Used to setup the task
        //such as setting up the progress bar
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = (android.widget.ProgressBar) findViewById(R.id.progress2);
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        // invoked on the background thread immediately after onPreExecute() finishes executing.
        // This step is used to perform background computation that can take a long time
        @Override
        protected Void doInBackground(Void... arg0) {
            if (fileExistance("gameList") && compareDates())
            {
                //the file exists already and is updated, just load from internal storage
                    try {
                        gameList = InternalStorage.readObject(thecontext, "gameList");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

            }
            //file exists but the flag is false or if file does not exist
            else {

                //will automatically overwrite the file if it is old or does not exist
                //first create instance of handler from class we just made to connect to URL
                HttpHandler myhp = new HttpHandler();
                String todayurl = url + fmt.format(new Date());
                // Making a request to url and getting response, calling function in HttpHandler class that we wrote
                String jsonStr = myhp.makeServiceCall(todayurl);
                //if we actually grabbed something back as a string
                if (jsonStr != null) {
                    try {
                        //first create a JSONObject of the whole json response
                        JSONObject jsonObj = new JSONObject(jsonStr);
                        //these steps depend on the structure of the json
                        JSONObject gameScheduleObj = jsonObj.getJSONObject("dailygameschedule");
                        //this is the array of games basically. Each game is a separate object
                        JSONArray gameEntryArr = gameScheduleObj.getJSONArray("gameentry");

                        // looping through All the game objects in the game schedule
                        for (int i = 0; i < gameEntryArr.length(); i++) {
                            //grabs one game object at a time
                            JSONObject c = gameEntryArr.getJSONObject(i);

                            String gameTime = c.getString("time");
                            //grab awayteam info first
                            JSONObject awayTeamObj = c.getJSONObject("awayTeam");
                            String awayCity = awayTeamObj.getString("City");
                            String awayName = awayTeamObj.getString("Name");

                            //now grab hometeam info
                            JSONObject homeTeamObj = c.getJSONObject("homeTeam");
                            String homeCity = homeTeamObj.getString("City");
                            String homeName = homeTeamObj.getString("Name");
                            String gameName = awayCity + " " + awayName + " " + "@ " + homeCity + " " + homeName;
                            String timeET = gameTime + " ET";

                            // tmp hash map for single game
                            HashMap<String, String> game = new HashMap<>();

                            // adding each child node to HashMap key => value
                            game.put("thename", gameName);
                            game.put("thetime", timeET);

                            // adding game to the game list
                            gameList.add(game);
                            //saving the list in internal android storage
                            try {
                                InternalStorage.writeObject(thecontext, "gameList", gameList);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }
                    } catch (final JSONException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Json parsing error: " + e.getMessage(),
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });

                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Couldn't get json from server",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            }

            return null;
        }

       // invoked on the UI thread after the background computation finishes.
       // The result of the background computation is passed to this step as a parameter.
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (progressBar != null)
                progressBar.setVisibility(android.widget.ProgressBar.INVISIBLE);
             // Updating parsed JSON data into ListView
            ListAdapter adapter = new SimpleAdapter(
                   BaseballActivity.this, gameList,
                    R.layout.list_item, new String[]{"thename", "thetime"}, new int[]{R.id.game_name,
                    R.id.game_time});

            mylistview.setAdapter(adapter);
        }


    }
}

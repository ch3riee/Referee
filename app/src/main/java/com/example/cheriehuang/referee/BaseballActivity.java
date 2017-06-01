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
import android.content.Intent;
import android.widget.Button;
import android.view.View;
import android.support.design.widget.FloatingActionButton;

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
import android.util.SparseBooleanArray;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.MutableData;
import java.util.Calendar;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.util.Log;

public class BaseballActivity  extends BaseActivity{
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ListView mylistview;
    private String myusername ;
    private long numMembers = 0;
    private String mygroup;
    private android.widget.ProgressBar progressBar;
    private static String url = "https://www.mysportsfeeds.com/api/feed/pull/mlb/2017-regular/daily_game_schedule.json?fordate=";
    ArrayList<HashMap<String, String>> gameList;
    ArrayList<String> user_names;
    ArrayList<String> user_recorded_games;
    Context thecontext;
    android.support.design.widget.FloatingActionButton saveGames;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baseball);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //new GetRequestsFromGroup().execute();
        FirebaseUser user = mAuth.getCurrentUser();
        myusername = user.getDisplayName();
        gameList = new ArrayList<>();
        mylistview = (ListView)findViewById(R.id.baseball_list);
        thecontext = this;
        saveGames = (android.support.design.widget.FloatingActionButton)findViewById(R.id.saveGamesButton);
        new GetGameSchedule().execute();
        //now we have listview created but want to enable it to be checked off
        saveGames.setOnClickListener(new Button.OnClickListener(){
            @Override

            public void onClick(View v) {
                String selected = "";
                int cntChoice = mylistview.getCount();

                SparseBooleanArray sparseBooleanArray = mylistview.getCheckedItemPositions();

                for(int i = 0; i < cntChoice; i++){

                    if(sparseBooleanArray.get(i)) {
                        HashMap<String, String> onegame = (HashMap<String,String>) mylistview.getItemAtPosition(i);
                        selected += onegame.get("thename");
                        //if it is not the last one
                        if (i != (cntChoice - 1))
                        {

                            selected += ", ";

                        }

                    }

                }
                //Toast.makeText(BaseballActivity.this, selected, Toast.LENGTH_LONG).show();
                getGroupName(selected);

            }});
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 3); // For 1 PM or 2 PM, 22 is 10pm
        calendar.set(Calendar.MINUTE, 5);
        calendar.set(Calendar.SECOND, 0);
        if(Calendar.getInstance().after(calendar)){
            // Move to tomorrow
            calendar.add(Calendar.DATE, 1);
        }
        PendingIntent pi = PendingIntent.getBroadcast(thecontext, 0,
                new Intent(thecontext, AlarmReceiver.class),PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) thecontext.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);
        Log.d("Baseball Activity", "Alarm has been set");
    }



    private void getGroupName(final String selected) {
        mDatabase.child("usergroup").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //snapshot.exists() checks if the DataSnapshot was empty or not
                if (snapshot.exists()) {
                    mygroup = snapshot.getValue(String.class);
                    saveDB(selected);
                }
                else
                {
                    //TODO does not exist?? GROUP
                    Toast.makeText(BaseballActivity.this, "Not part of a group yet!!", Toast.LENGTH_LONG).show();


                    //mygroup stays null

                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    public void saveDB(final String selected_games)
    {
        mDatabase.child("group_recorded").child(mygroup).child(myusername).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                    //if existing data is not already there then this is a new request
                    mutableData.setValue(selected_games);
                    return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError firebaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    Toast.makeText(getBaseContext(), "Recording games saved!", Toast.LENGTH_LONG).show();
                    //checkMemberNum(); TODO uncomment to send trigger by number of people in group
                } else {
                }
            }
        });

    }


    public void checkMemberNum()
    {
        mDatabase.child("groups").child(mygroup).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    numMembers = dataSnapshot.getChildrenCount();
                    checkChildren();

                } else {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void checkChildren()
    {
        mDatabase.child("group_recorded").child(mygroup).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == numMembers) {
                  //ready to make a post to the database trigger path
                    prepareDBTrigger();

                } else {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void sendDBTriggerHelper()
    {
        HashMap<String, String> theMembers = new HashMap<String,String>();
        for (int i = 0; i < user_names.size(); i++) {
            String tempusername = user_names.get(i);
            String games = user_recorded_games.get(i);
           // sendDBTrigger(tempusername, games);
            theMembers.put(tempusername, games);
        }
        sendDBTrigger(theMembers);
    }

    public void sendDBTrigger(final HashMap<String,String> theList)
    {
        mDatabase.child("notifications").child(mygroup).child("recorded").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                    //if existing data is not already there then this is a new request
                    mutableData.setValue(theList);
                    return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError firebaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    Toast.makeText(getBaseContext(), "Notifications updated!", Toast.LENGTH_LONG).show();
                    sendDBName();

                } else {
                }
            }
        });
    }


    public void sendDBName()
    {
        mDatabase.child("notifications").child(mygroup).child("groupname").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    //if existing data is not already there then this is a new request
                    mutableData.setValue(mygroup);
                    return Transaction.success(mutableData);
                }

                return Transaction.abort();
            }

            @Override
            public void onComplete(DatabaseError firebaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    Toast.makeText(getBaseContext(), "Group name added!", Toast.LENGTH_LONG).show();
                } else {
                }
            }
        });
    }



    public void prepareDBTrigger()
    {
        user_names = new ArrayList<String>();
        user_recorded_games = new ArrayList<String>();
        mDatabase.child("group_recorded").child(mygroup).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //ready to make a post to the database trigger path
                    HashMap<String, String> records = (HashMap<String, String>) dataSnapshot.getValue();
                    for (String theUsername : records.keySet())
                    {
                        if(!user_names.contains(theUsername))
                        {
                            user_names.add(theUsername);
                        }

                    }
                    for (String theGames : records.values())
                    {
                        if(!user_recorded_games.contains(theGames))
                        {
                            user_recorded_games.add(theGames);
                        }

                    }
                    //now put this data into the database
                    sendDBTriggerHelper();

                } else {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        mylistview.setItemChecked(position, true);
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
            mylistview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        }


    }
}

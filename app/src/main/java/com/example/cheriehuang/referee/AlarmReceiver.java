package com.example.cheriehuang.referee;

/**
 * Created by cheriehuang on 5/30/17.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import android.util.Log;


public class AlarmReceiver extends BroadcastReceiver {
    private FirebaseAuth mAuth;
    private ArrayList<String> user_names;
    private ArrayList<String> user_recorded_games;
    private String mygroup;
    private DatabaseReference mDatabase;
    private HashMap<String, String> theMembers;
    private String myusername;
    @Override
    public void onReceive(Context context, Intent intent)
    {
        // TODO Auto-generated method stub
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = mAuth.getCurrentUser();
        myusername = user.getDisplayName();
        getGroupName();
    }

    private void getGroupName() {
        mDatabase.child("usergroup").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //snapshot.exists() checks if the DataSnapshot was empty or not
                if (snapshot.exists()) {
                    mygroup = snapshot.getValue(String.class);
                    Log.d("Alarm Receiver", "my group is not null");
                    prepareDBTrigger();
                }
                else
                {
                    Log.d("Alarm Receiver", "mygroup is null");
                    //mygroup stays null
                    mygroup = null;
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }

    public void prepareDBTrigger() {
        if (mygroup != null)
        {
            user_names = new ArrayList<String>();
            user_recorded_games = new ArrayList<String>();
            mDatabase.child("group_recorded").child(mygroup).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        //ready to make a post to the database trigger path
                        HashMap<String, String> records = (HashMap<String, String>) dataSnapshot.getValue();
                        for (String theUsername : records.keySet()) {
                            if (!user_names.contains(theUsername)) {
                                user_names.add(theUsername);
                            }

                        }
                        for (String theGames : records.values()) {
                            if (!user_recorded_games.contains(theGames)) {
                                user_recorded_games.add(theGames);
                            }

                        }
                        //now put this data into the database
                        sendDBTriggerHelper();

                    } else {
                        //don't send a notification no games recorded
                        //or maybe send one? Saying no games recorded
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        }

    }

    public void sendDBTriggerHelper()
    {
         theMembers = new HashMap<String,String>();
        for (int i = 0, x = 0; i < user_names.size() && x<user_recorded_games.size(); i++, x++) {
            String tempusername = user_names.get(i);
            String games = user_recorded_games.get(x);
            // sendDBTrigger(tempusername, games);
            theMembers.put(tempusername, games);
        }
        sendDBTrigger(theMembers); //which would mean members is null
    }

    public void sendDBTrigger(final HashMap<String,String> theList)
    {
        mDatabase.child("notifications").child(mygroup).child("recorded").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                //if existing data is not already there then this is a new request
                mutableData.setValue(theList); //setting data as null here i think for some reason
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError firebaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
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
                    Log.d("Alarm Receiver", "Notifications DB set");
                } else {
                }
            }
        });
    }


}

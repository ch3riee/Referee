package com.example.cheriehuang.referee;

/**
 * Created by cheriehuang on 5/27/17.
 */

import android.content.Context;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupActivity extends BaseActivity{
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String myusername ;
    private String mygroup;
    private ArrayList<String> groupRequestList;
    private ArrayList<String> requestedIDList;
    private ArrayList<String> requestedNamesList;
    private ArrayList<String> acceptedNamesList;
    private ArrayList<String> friendids;


    @BindView(R.id.input_friendusername) EditText _requestText;
    @BindView(R.id.btn_sendrequest)android.support.v7.widget.AppCompatButton _requestButton;
    @BindView(R.id.btn_acceptRequest) android.support.v7.widget.AppCompatButton _acceptButton;
    @BindView(R.id.btn_creategroup) android.support.v7.widget.AppCompatButton _createButton;
    @BindView(R.id.requests_list) ListView mylistview;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //new GetRequestsFromGroup().execute();
        FirebaseUser user = mAuth.getCurrentUser();
        myusername = user.getDisplayName();
        getGroupName();
        _requestButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String name_group =  _requestText.getText().toString(); //grabs the name in the same box
                groupRequestCheck(name_group);
            }
        });

        _createButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String name_group =  _requestText.getText().toString(); //grabs the name in the same box
                checkIfGroupExists(name_group);
            }
        });

        _acceptButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                acceptedNamesList = new ArrayList<String>();
                friendids = new ArrayList<String>();
                int cntChoice = mylistview.getCount();

                SparseBooleanArray sparseBooleanArray = mylistview.getCheckedItemPositions();

                for (int i = 0; i < cntChoice; i++) {

                    if (sparseBooleanArray.get(i)) {

                        acceptedNamesList.add(mylistview.getItemAtPosition(i).toString());
                        friendids.add(requestedIDList.get(i));
                    }

                }

                AcceptRequestsHelper();
                //now update the list view
                //ArrayAdapter<String> myAdapter = (ArrayAdapter<String>) mylistview.getAdapter();
                for (int x = 0; x < acceptedNamesList.size(); x++) {
                    String temp = acceptedNamesList.get(x);
                    requestedNamesList.remove(temp);
                }
                mylistview.invalidateViews();


            }
        });
    }

            public void AcceptRequestsHelper() {

                for (int i = 0; i < acceptedNamesList.size(); i++) {
                    String tempusername = acceptedNamesList.get(i);
                    String tempuid = friendids.get(i);
                    AcceptRequests(tempusername, tempuid);
                }

            }

            public void AcceptRequests(String oneusername, String frienduid)
            {
                String registered = Calendar.getInstance().getTime().toString();
                mDatabase.child("groups").child(mygroup).child(frienduid).setValue(registered);
                mDatabase.child("usergroup").child(frienduid).setValue(mygroup);
                mDatabase.child("groupRequests").child(mygroup).child(oneusername).removeValue();

                Toast.makeText(GroupActivity.this,oneusername + " accepted into group", Toast.LENGTH_LONG).show();
            }



            @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    //request to join a group, first check if group exists and if already in the group
    public void groupRequestCheck(final String groupname) {
        mDatabase.child("groups").child(groupname).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //a valid group
                    checkNotInGroup(groupname);

                } else {
                    // not a group name yet so notify user to create the group instead by pressing create group button
                    Toast.makeText(getBaseContext(), groupname + " does not exist, press Create button to create group", Toast.LENGTH_LONG ).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void checkNotInGroup(final String groupname)
    {
        mDatabase.child("groups").child(groupname).child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //already in the group
                    Toast.makeText(getBaseContext(), groupname + " is already your group", Toast.LENGTH_LONG ).show();

                } else {
                    sendRequestGroup(groupname);
                    // not a group name yet so notify user to create the group instead by pressing create group button

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    //function to send a request to all the people in the group, that we now know exists
    public void sendRequestGroup(final String groupname)
    {
        //requesting persons username: userid
        //don't set it if it already exists
        mDatabase.child("groupRequests").child(groupname).child(myusername).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    //if existing data is not already there then this is a new request
                    mutableData.setValue(mAuth.getCurrentUser().getUid());
                    return Transaction.success(mutableData);
                }

                return Transaction.abort();
            }

            @Override
            public void onComplete(DatabaseError firebaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    Toast.makeText(getBaseContext(), "Request Sent Successfully!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getBaseContext(), "Duplicate Request not sent", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

   /* //helper function to grab all ids of members in the group
    public void grabGroupIDS(final String groupname)
    {
        peopleInGroupList = new ArrayList<String>();
        mDatabase.child("groups").child(groupname).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //snapshot.exists() checks if the DataSnapshot was empty or not
                if (snapshot.exists()) {
                    HashMap<String, String> groupids = (HashMap<String, String>) snapshot.getValue();
                    for (String id : groupids.keySet()) { //only grab keys b/c don't need date registered
                        if (!peopleInGroupList.contains(id)) {
                            peopleInGroupList.add(id);
                        }
                    }
                } else {

                }
            }//end of onDataChange

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });
    }*/

    //for creating groups! Called by on button click for create a group
    public void checkIfGroupExists(final String groupname) {
        mDatabase.child("groups").child(groupname).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //already friends!
                    Toast.makeText(getBaseContext(), groupname + " exists already, pick another", Toast.LENGTH_LONG ).show();
                } else {
                    // not a group name yet so create the group
                    createGroup(groupname);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //creates group and adds current users id to the group plus time they joined the group
    //then also sets group name under user info

    public void createGroup(final String groupname)
    {
        mDatabase.child("groups").child(groupname).child(mAuth.getCurrentUser().getUid()).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    String registered = Calendar.getInstance().getTime().toString();
                    mutableData.setValue(registered); //date joined the group
                    return Transaction.success(mutableData);
                }

                return Transaction.abort();
            }

            @Override
            public void onComplete(DatabaseError firebaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    mDatabase.child("usergroup").child(mAuth.getCurrentUser().getUid()).setValue(groupname);
                    Log.w("AUTH", "group saved");
                    Toast.makeText(getBaseContext(), "Group created and user added to group!", Toast.LENGTH_LONG).show();
                } else {
                    Log.w("AUTH", "group already exists");
                    Toast.makeText(getBaseContext(), "Group: " + groupname +" already exists, pick another name", Toast.LENGTH_LONG).show();
                }


        }

    });
}//end of createGroup
    private void getGroupName() {
            mDatabase.child("usergroup").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    //snapshot.exists() checks if the DataSnapshot was empty or not
                    if (snapshot.exists()) {
                        mygroup = snapshot.getValue(String.class);
                        populateRequestList();
                    }
                    else
                    {
                        //TODO does not exist?? GROUP

                        //mygroup stays null

                    }
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                }
            });
        }

        private void populateRequestsListView() {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(GroupActivity.this,
                    R.layout.list_item_requests, requestedNamesList);
            mylistview.setAdapter(arrayAdapter);
            mylistview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);


        }

        /*private void emptyList()
        {
            mylistview.setEmptyView(findViewById(R.id.empty));
        }*/



        private void populateRequestList() {
            requestedNamesList = new ArrayList<String>();
            requestedIDList = new ArrayList<String>();
            if(mygroup == null )
            {
                populateRequestsListView();
                return;
                //no group yet so no requests
                //print please choose a group or something


            }
            mDatabase.child("groupRequests").child(mygroup).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    //snapshot.exists() checks if the DataSnapshot was empty or not
                    if (snapshot.exists()) {
                        HashMap<String, String> requests = (HashMap<String, String>) snapshot.getValue();
                        for (String request : requests.keySet()) {
                            if (!requestedNamesList.contains(request)) {
                                requestedNamesList.add(request);
                            }
                        }
                        for (String request : requests.values()) {
                            if (!requestedIDList.contains(request)) {
                                requestedIDList.add(request);
                            }
                        }
                        populateRequestsListView();

                    }
                    else{
                        requestedNamesList.add("dummy snapshot does not exist");

                    }


                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                }
            });}







}//end of BaseActivity




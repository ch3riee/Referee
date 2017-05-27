package com.example.cheriehuang.referee;

/**
 * Created by cheriehuang on 5/23/17.
 */


import android.content.Context;

import android.os.AsyncTask;
import android.os.Bundle;
        import android.util.SparseBooleanArray;
        import android.view.MotionEvent;
        import android.view.View;
        import android.view.inputmethod.InputMethodManager;
        import android.widget.EditText;
        import android.util.Log;

        import java.util.ArrayList;
        import java.util.HashMap;

        import android.widget.ArrayAdapter;
        import android.widget.ListView;
        import android.widget.Toast;

        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.Transaction;
        import com.google.firebase.database.ValueEventListener;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.MutableData;

        import butterknife.BindView;
        import butterknife.ButterKnife;
//import android.support.v7.widget.RecyclerView;
        //import android.support.v7.widget.LinearLayoutManager;

public class FriendActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String myusername ;
    private ArrayList<String> friendrequestList;
    private ArrayList<String> friendidsrequestList;
   /* private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;*/
    private ArrayList<String> friendids;
    private ArrayList<String> acceptedUsernames;


     @BindView(R.id.input_friendusername) EditText _requestText;
     @BindView(R.id.btn_sendrequest)android.support.v7.widget.AppCompatButton _requestButton;
    @BindView(R.id.btn_acceptRequest) android.support.v7.widget.AppCompatButton _acceptButton;
    @BindView(R.id.requests_list) ListView mylistview;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        new GetRequestsFromFriends().execute();
        FirebaseUser user = mAuth.getCurrentUser();
        myusername = user.getDisplayName();

        _requestButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                checkRequests();
            }
        });

        _acceptButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                    acceptedUsernames = new ArrayList<String>();
                friendids = new ArrayList<String>();
                    int cntChoice = mylistview.getCount();

                    SparseBooleanArray sparseBooleanArray = mylistview.getCheckedItemPositions();

                    for(int i = 0; i < cntChoice; i++){

                        if(sparseBooleanArray.get(i)) {

                            acceptedUsernames.add(mylistview.getItemAtPosition(i).toString());
                            friendids.add(friendidsrequestList.get(i));
                        }

                    }

                AcceptFriendsHelper();
                //now update the list view
                ArrayAdapter<String> myAdapter = (ArrayAdapter<String>) mylistview.getAdapter();
                for (int x = 0; x < acceptedUsernames.size();x++)
                {
                    String temp = acceptedUsernames.get(x);
                    friendrequestList.remove(temp);
                }
                mylistview.invalidateViews();

            }
        });
    }

    public void AcceptFriendsHelper() {

        for (int i = 0; i < acceptedUsernames.size(); i++) {
            String tempusername = acceptedUsernames.get(i);
            String tempuid = friendids.get(i);
            AcceptFriends(tempusername, tempuid);
        }

    }

    public void AcceptFriends(String oneusername, String frienduid)
    {
        mDatabase.child("friendships").child(mAuth.getCurrentUser().getUid()).child(oneusername).setValue(true);
        mDatabase.child("friendships").child(frienduid).child(myusername).setValue(true);
        //now remove the requests from their sent requests
        mDatabase.child("sentRequests").child(frienduid).child(myusername).removeValue();
        //and my received requests
        mDatabase.child("receivedRequests").child(mAuth.getCurrentUser().getUid()).child(oneusername).removeValue();
        Toast.makeText(FriendActivity.this,oneusername + "DONE", Toast.LENGTH_LONG).show();
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

    public void onListItemClick(ListView l, View v, int position, long id) {
        ListView listView = (ListView) findViewById(R.id.requests_list);
        listView.setItemChecked(position, true);
    }

    public void checkIfFriends(final String friendid, final String friendusername) {
        mDatabase.child("friendships").child(mAuth.getCurrentUser().getUid()).child(friendusername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                   //already friends!
                    Toast.makeText(getBaseContext(), friendusername + " is already your friend", Toast.LENGTH_LONG ).show();
                } else {
                    // not currently friends yet
                    //sendCheckRequests(friendid, friendusername);
                    checkOutstandingRequests(friendid, friendusername);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void checkOutstandingRequests(final String friendid, final String friendusername)
    {
        mDatabase.child("receivedRequests").child(mAuth.getCurrentUser().getUid()).child(friendusername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //already friends!
                    Toast.makeText(getBaseContext(), "Please accept " + friendusername + "'s friend request ", Toast.LENGTH_LONG ).show();
                } else {
                    // all checks check out
                    sendCheckRequests(friendid, friendusername);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }


    public void sendCheckRequests(final String friendid, final String frienduser) {
        final String uid = mAuth.getCurrentUser().getUid();
        grabUsername(uid);

        //checking the ones that we have sent. Meaning UID is the one who is sending for toRequests
        mDatabase.child("sentRequests").child(uid).child(frienduser).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    mutableData.setValue(friendid);
                    return Transaction.success(mutableData);
                }

                return Transaction.abort();
            }

            @Override
            public void onComplete(DatabaseError firebaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    mDatabase.child("receivedRequests").child(friendid).child(myusername).setValue(uid);
                    Toast.makeText(getBaseContext(), "Request Sent Successfully!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getBaseContext(), "Duplicate Request not sent", Toast.LENGTH_LONG).show();
                }
            }
        });
    }



    public void checkRequests() {
        final String friendusername = _requestText.getText().toString();
        if (friendusername.isEmpty())
        {
            _requestText.setError("please enter a friend's username");
        }
        mDatabase.child("usernames").child(friendusername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // use "username" already exists so we can continue with sending the request
                    String fuid = dataSnapshot.getValue(String.class);
                    checkIfFriends(fuid, friendusername); //check to see if a request has already been sent so no duplicates

                } else {
                    // User does not exist. NOW call createUserWithEmailAndPassword
                    Toast.makeText(getBaseContext(), "Username: " + friendusername + " does not exist", Toast.LENGTH_LONG).show();
                    // Your previous code here.

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }




    public void grabUsername(String uid) {

        mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // use "username" already exists so we can continue with sending the request
                    User myuser = dataSnapshot.getValue(User.class);
                    myusername = myuser.getUsername();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



   //the class for async task calling the url to get json
   //basically does it in the backgroundthread asynchonously
   private class GetRequestsFromFriends extends AsyncTask<Void, Void, Void>
   {
       //invoked on the UI thread before the task is executed. Used to setup the task
       //such as setting up the progress bar
       @Override
       protected void onPreExecute() {
           super.onPreExecute();
       }

       private void populateRequestsListView() {

               ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(FriendActivity.this,
                       R.layout.list_item_requests, friendrequestList);
               mylistview.setAdapter(arrayAdapter);
               mylistview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
           /*recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
           layoutManager = new LinearLayoutManager(FriendActivity.this);
           recyclerView.setLayoutManager(layoutManager);

           adapter = new RequestsAdapter(friendrequestList,getApplicationContext());
           recyclerView.setAdapter(adapter);*/



       }


       private void populateRequestList() {
           friendrequestList = new ArrayList<String>();
           friendidsrequestList = new ArrayList<String>();
           mDatabase.child("receivedRequests").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(DataSnapshot snapshot) {
                   //snapshot.exists() checks if the DataSnapshot was empty or not
                   if (snapshot.exists()) {
                       HashMap<String, String> requests = (HashMap<String, String>) snapshot.getValue();
                       for (String request : requests.keySet()) {
                           if (!friendrequestList.contains(request)) {
                               friendrequestList.add(request);
                           }
                       }
                       for (String request : requests.values()) {
                           if (!friendidsrequestList.contains(request)) {
                               friendidsrequestList.add(request);
                           }
                       }

                   }
                   else{

                   }

               }

               @Override
               public void onCancelled(DatabaseError firebaseError) {
               }
           });}

       // invoked on the background thread immediately after onPreExecute() finishes executing.
       // This step is used to perform background computation that can take a long time
       @Override
       protected Void doInBackground(Void... arg0) {
               populateRequestList();
           return null;
       }

       // invoked on the UI thread after the background computation finishes.
       // The result of the background computation is passed to this step as a parameter.
       @Override
       protected void onPostExecute(Void result) {
           super.onPostExecute(result);
               populateRequestsListView();
       }


   }
}


 //end of FriendActivity
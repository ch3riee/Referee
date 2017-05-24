package com.example.cheriehuang.referee;

/**
 * Created by cheriehuang on 5/23/17.
 */

        import android.app.ProgressDialog;
        import android.content.Context;
        import android.os.Bundle;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.Toolbar;
        import android.view.MotionEvent;
        import android.view.View;
        import android.view.inputmethod.InputMethodManager;
        import android.widget.Button;
        import android.widget.EditText;
        import android.util.Log;

        import java.util.ArrayList;
        import java.util.Calendar;
        import java.util.HashMap;

        import android.content.Intent;
        import android.widget.TextView;
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

public class FriendActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String myusername;


    @BindView(R.id.input_friendusername)
    EditText _requestText;
    @BindView(R.id.btn_sendrequest)
    android.support.v7.widget.AppCompatButton _requestButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        _requestButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                checkRequests();
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
        mDatabase.child("usernames").child(friendusername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // use "username" already exists so we can continue with sending the request
                    String fuid = dataSnapshot.getValue(String.class);
                    sendCheckRequests(fuid, friendusername); //check to see if a request has already been sent so no duplicates

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
}
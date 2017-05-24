package com.example.cheriehuang.referee;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import java.util.Calendar;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.MutableData;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by cheriehuang on 5/24/17.
 */

public class UsernameActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @BindView(R.id.input_username) EditText _usernameText;
    @BindView(R.id.btn_username) Button _usernameButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);
        ButterKnife.bind(this);
        Intent startingIntent = getIntent();
        final String name  = startingIntent.getStringExtra("name");
        final String email  = startingIntent.getStringExtra("email");
        final String uid  = startingIntent.getStringExtra("uid");
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        _usernameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = _usernameText.getText().toString();
                addUserToDatabase(username,name, email, uid);
            }
        });

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

    public void addUserToDatabase(final String username,final String name, final String email,  final String uid){
            //add the user
            mDatabase.child("usernames").child(username).runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    if (mutableData.getValue() == null) {
                        mutableData.setValue(uid);
                        return Transaction.success(mutableData);
                    }

                    return Transaction.abort();
                }

                @Override
                public void onComplete(DatabaseError firebaseError, boolean committed, DataSnapshot dataSnapshot) {
                    if (committed) {
                        String registered = Calendar.getInstance().getTime().toString();
                        mDatabase.child("users").child(uid).setValue(new User(username, name, email, registered));
                        Log.w("AUTH", "username saved");
                        Toast.makeText(getBaseContext(), "User created successfully!", Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK, null);
                        finish();
                    } else {
                        Log.w("AUTH", "username exists");
                        Toast.makeText(getBaseContext(), "Username: " + username +" already exists, pick another", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }




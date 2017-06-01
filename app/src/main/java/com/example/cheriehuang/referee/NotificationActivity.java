package com.example.cheriehuang.referee;

/**
 * Created by cheriehuang on 5/30/17.
 */
import android.os.Bundle;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import android.content.Intent;
import android.content.Context;
import android.widget.ListView;

import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;

public class NotificationActivity extends BaseActivity {

    private ArrayList<String> theData;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);
        Intent intent = getIntent();
        HashMap<String, String> hashMap = (HashMap<String, String>)intent.getSerializableExtra("map");

        NotifyAdapter adapter = new NotifyAdapter(hashMap);
        ListView mylist = (ListView) findViewById(R.id.recorded_list);
        mylist.setAdapter(adapter);
        //now we have to send this to the RestrictedActivity where it will be saved so it can be viewed
        Intent newIntent = new Intent(this, RestrictedActivity.class);
        newIntent.putExtra("restrictedgames", hashMap);
        startActivity(newIntent);
    }






}

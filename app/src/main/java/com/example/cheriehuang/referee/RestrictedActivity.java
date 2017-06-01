package com.example.cheriehuang.referee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.content.Context;

/**
 * Created by cheriehuang on 5/31/17.
 */

public class RestrictedActivity extends BaseActivity {

    private ArrayList<String> theData;
    private Context mycontext;
    private HashMap<String,String> theList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restricted);
        mycontext = this;
        //will give us the intent that started this, the one with a bundle will be
        //the notification activity, the one with null will be the toolbar menu base activity
        Intent intent = getIntent();
        HashMap<String, String> restrictedgames = (HashMap<String, String>)intent.getSerializableExtra("restrictedgames");
        if (restrictedgames != null)
        {
            //we are accessing through notification so need to save this
            saveGames(restrictedgames);
        }
        else
        {
            //accessing through the toolbar menu should have one previously saved
            //or if not then put up empty view or something
            checkGamesExist();
        }
    }

    public void saveGames(HashMap<String, String> restrictedgames)
    {
        try {
            InternalStorage.writeObjectHashMap(mycontext, "restrictedgames", restrictedgames);
        } catch (IOException e) {
            e.printStackTrace();
        }
        theList = restrictedgames;
        populateNew();
    }

    public void populateNew()
    {
        NotifyAdapter adapter = new NotifyAdapter(theList);
        ListView mylist = (ListView) findViewById(R.id.restricted_list);
        mylist.setAdapter(adapter);
        mylist.setVisibility(View.VISIBLE);
        //TextView thetext = (TextView) findViewById(R.id.notavailable);
        //thetext.setVisibility(View.GONE);
        //now we have to send this to the RestrictedActivity where it will be saved so it can be viewed
    }


    private SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.US);
    public void checkGamesExist()
    {
        //see if there is one previously saved
        //or if we need to set up empty view
        if ((fileExistance("restrictedgames") == true) && compareDates())
        {
            //we have one saved
            populateListView();
        }
        else
        {
            populateEmptyView();
        }
    }

    public void populateListView()
    {
            try {
                theList = InternalStorage.readObjectHashMap(mycontext, "restrictedgames");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        NotifyAdapter adapter = new NotifyAdapter(theList);
        ListView mylist = (ListView) findViewById(R.id.restricted_list);
        mylist.setAdapter(adapter);
        mylist.setVisibility(View.VISIBLE);
        //TextView thetext = (TextView) findViewById(R.id.notavailable);
        //thetext.setVisibility(View.GONE);
        //now we have to send this to the RestrictedActivity where it will be saved so it can be viewed

    }

    public void populateEmptyView()
    {
        ListView mylist = (ListView) findViewById(R.id.restricted_list);
       // mylist.setVisibility(View.GONE); //hides the listview
        //TextView thetext = (TextView) findViewById(R.id.notavailable);
        //thetext.setVisibility(View.VISIBLE);

    }

    private Boolean compareDates()
    {
        //new Date() gives us today's date
        return fmt.format(new Date()).equals(fmt.format(fileLastModified("gameList")));
    }

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






}

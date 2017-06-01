package com.example.cheriehuang.referee;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.content.Intent;

import java.util.Calendar;
import android.content.SharedPreferences;



public class MainActivity extends BaseActivity {
    private Toolbar mToolbar;
    private Boolean myflag = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn = (Button)findViewById(R.id.baseball_button);

        btn.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                       startActivity(new Intent(MainActivity.this, BaseballActivity.class));
                                   }
                               }

                                   );


           /* Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 23); // For 1 PM or 2 PM, 22 is 10pm
            calendar.set(Calendar.MINUTE, 36);
            calendar.set(Calendar.SECOND, 0);
            if(Calendar.getInstance().after(calendar)){
                // Move to tomorrow
                calendar.add(Calendar.DATE, 1);
            }
            PendingIntent pi = PendingIntent.getBroadcast(this, 0,
                    new Intent(this, AlarmReceiver.class),PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pi);
            Log.d("Main Activity", "Alarm has been set");*/

    }



    /*private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        //setTitle(getString(R.string.app_name));
       // mToolbar.setTitleTextColor(android.graphics.Color.WHITE);
        //these are to enable home back up functionality
        //getSupportActionBar().setHomeButtonEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        tb.inflateMenu(R.menu.toolbar_menu);
        tb.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return onOptionsItemSelected(item);
                    }
                });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home_button:
                //just testing our buttons with Toast for now
                Toast.makeText(this, "Home button selected", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.notifications:
                Toast.makeText(this, "Notifications selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.profile:
                Toast.makeText(this, "Profile selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.settings:
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
*/
}

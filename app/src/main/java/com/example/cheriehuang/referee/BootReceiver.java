package com.example.cheriehuang.referee;

/**
 * Created by cheriehuang on 5/30/17.
 */
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;

import java.util.Calendar;


public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set alarms
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 3); //
            calendar.set(Calendar.MINUTE, 5);
            calendar.set(Calendar.SECOND, 0);
            if(Calendar.getInstance().after(calendar)){
                // Move to tomorrow
                calendar.add(Calendar.DATE, 1);
            }
            PendingIntent pi = PendingIntent.getBroadcast(context, 0,
                    new Intent(context, AlarmReceiver.class),PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pi);
        }
    }
}
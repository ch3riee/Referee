package com.example.cheriehuang.referee;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
/**
 * Created by cheriehuang on 5/18/17.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
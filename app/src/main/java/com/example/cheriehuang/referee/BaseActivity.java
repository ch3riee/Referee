package com.example.cheriehuang.referee;

/**
 * Created by cheriehuang on 5/19/17.
 */

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.Toast;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public abstract class BaseActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initToolbar();
    }
    private void initToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        assert myToolbar != null;
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
                //now adding in the code to return to main activity
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                //if the activity being launched is already running in the current task, then instead
                //of launching new instance of activity, all other activities on top of it will be closed
                //and intent will be delivered to the now top main activity.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;

            case R.id.notifications:
                Toast.makeText(this, "Notifications selected", Toast.LENGTH_SHORT).show();
                Intent intent4 = new Intent(this, RestrictedActivity.class);
                intent4.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent4);
                return true;
            case R.id.friends:
                Toast.makeText(this, "Groups selected", Toast.LENGTH_SHORT).show();
                Intent intent3 = new Intent(this, GroupActivity.class);
                startActivity(intent3);
                return true;
            case R.id.settings:
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.signout:
                Toast.makeText(this, "Signout selected", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                //now bring them back to the login page
                Intent intent2 = new Intent(this, LoginActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent2);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}

package chalmers.ip_radio;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.*;
import android.view.MenuItem;
import android.widget.Button;

import chalmers.ip_radio.VoIP.TalkActivity;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void map1(View view){
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }
    public void map2(View view){
        Intent intent = new Intent(this, TalkActivity.class);
        startActivity(intent);
    }
}

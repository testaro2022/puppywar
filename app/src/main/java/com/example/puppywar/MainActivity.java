package com.example.puppywar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //Options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options_menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean resultVal = true;
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menuListOptionRanking:
                Intent intent_ranking = new Intent(MainActivity.this,RankingActivity.class);
                startActivity(intent_ranking);
                break;//TODO 画面遷移
            case R.id.menuListOptionCamera:
                Intent intent_camera = new Intent(MainActivity.this,CameraActivity.class);
                startActivity(intent_camera);
                break;
            case R.id.menuListOptionHTTPtest:
                Intent intent_HTTPtest = new Intent(MainActivity.this,HTTPtestActivity.class);
                startActivity(intent_HTTPtest);
                break;
            default:
                resultVal = super.onOptionsItemSelected(item);
                break;
        }
        return resultVal;
    }

}
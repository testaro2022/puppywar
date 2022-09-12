package com.example.puppywar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class RankingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        Intent intent = getIntent();
        ListView lvRanking = findViewById(R.id.lvRanking);
        List<String> RankingList = new ArrayList<>();
        RankingList.add("dog1");
        RankingList.add("dog2");
        RankingList.add("dog3");
        RankingList.add("dog4");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(RankingActivity.this,
                android.R.layout.simple_list_item_1,RankingList);
        lvRanking.setAdapter(adapter);

    }

//    public void onBackButtonClick(View view) {
//        finish();
//    }
}
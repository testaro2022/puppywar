package com.example.puppywar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HTTPtestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_httptest);

        Button btGet = findViewById(R.id.btGet);
        Button btPost = findViewById(R.id.btPost);
        Button btClear = findViewById(R.id.btClear);
        ButtonListener listener = new ButtonListener();
        btGet.setOnClickListener(listener);
        btPost.setOnClickListener(listener);
        btClear.setOnClickListener(listener);

    }

    private class ButtonListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            TextView response = findViewById(R.id.tvResponse);
            int id = view.getId();
            switch (id) {
                case R.id.btGet:
                    response.setText("Get");
                    break;
                case R.id.btPost:
                    response.setText("Post");
                    break;
                case R.id.btClear:
                    response.setText("");
                    break;
            }

        }
    }

}
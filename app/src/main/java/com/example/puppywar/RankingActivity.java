package com.example.puppywar;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RankingActivity extends AppCompatActivity {
    String url = "http://flask-env-eb-3.eba-kndu7tft.ap-northeast-1.elasticbeanstalk.com/";
    int Ranking_num = 5;
    String DEBUG_TAG = "ranking_activity";
    String rank0,rank1,rank2,rank3,rank4;
    List<String> RankingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        Intent intent = getIntent();

        init_ranking();

    }

    public void init_ranking(){
        RankingList = new ArrayList<>();//初期化
        receiveHttp(url+"ranking","GET");
    }

    @UiThread
    private void receiveHttp(final String urlFull,String order){
        Looper mainlooper = Looper.getMainLooper();
        Handler handler = HandlerCompat.createAsync(mainlooper);
        RankingActivity.HttpBackgroundReceiver backgroundReceiver = new RankingActivity.HttpBackgroundReceiver(handler,urlFull,order);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(backgroundReceiver);
    }

    private class HttpBackgroundReceiver implements Runnable {

        private final Handler _handler;
        private final String _urlFull;
        private final String _order;

        public HttpBackgroundReceiver(Handler handler, String urlFull, String order) {
            _handler = handler;
            _urlFull = urlFull;
            _order = order;
        }

        @WorkerThread
        @Override
        public void run() {
            HttpURLConnection con = null;
            InputStream is = null;
            String result = "";
            Bitmap bmp = null;
            try {
                URL url = new URL(_urlFull);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(2000);
                con.setReadTimeout(2000);
                if (_order == "GET" ) {
                    con.setRequestMethod("GET");
                    con.connect();
                    is = con.getInputStream();
                    result = is2String(is);
                } else if (_order == "GETIMG") {
                    con.setRequestMethod("GET");
                    con.connect();
                    is = con.getInputStream();
                    bmp = BitmapFactory.decodeStream(is);
                }
                else{
                    con.setRequestMethod("GET");
                }
            } catch (MalformedURLException ex) {
                Log.e(DEBUG_TAG, "URL変換失敗", ex);
            } catch (SocketTimeoutException ex) {
                Log.w(DEBUG_TAG, "通信タイム", ex);
            } catch (IOException ex) {
                Log.e(DEBUG_TAG, "通信失敗", ex);
            } finally {
                if (con != null) {
                    con.disconnect();
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        Log.e(DEBUG_TAG, "InputStream解放失敗");
                    }
                }
            }

            if (_order == "GET") {
                RankingActivity.HttpPostExecutor postExecutor = new RankingActivity.HttpPostExecutor(result,_order);
                _handler.post(postExecutor);
            }
            else if(_order =="GETIMG") {
                RankingActivity.HTTPPOSTImgExecutor postImgExecutor = new RankingActivity.HTTPPOSTImgExecutor(bmp,_order);
                _handler.post(postImgExecutor);
            }
        }
    }
    private class HttpPostExecutor implements Runnable{
        private final String _result;
        private String _order;

        public HttpPostExecutor(String result,String order){
            _result = result;
            _order = order;
        }

        @UiThread
        @Override
        public void run (){
            //UIスレッドの記述
            int id=-1,win=-1,lose=-1;
            String title="default_title";
            Log.e(DEBUG_TAG,"テスト＝＝＝＝＝＝＝＝");
                try{
                    JSONObject rootJSON = new JSONObject(_result);
                    rank0 = rootJSON.getString("rank0");
                    rank1 = rootJSON.getString("rank1");
                    rank2 = rootJSON.getString("rank2");
                    rank3 = rootJSON.getString("rank3");
                    rank4 = rootJSON.getString("rank4");
                    RankingList.add("1st: "+rank0);
                    RankingList.add("2nd: "+rank1);
                    RankingList.add("3rd: "+rank2);
                    RankingList.add("4th: "+rank3);
                    RankingList.add("5th: "+rank4);
                    ListView lvRanking = findViewById(R.id.lvRanking);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(RankingActivity.this, android.R.layout.simple_list_item_1,RankingList);
                    lvRanking.setAdapter(adapter);
                }
                catch (JSONException ex){
                    Log.e(DEBUG_TAG,"JSON解析失敗",ex);
                }
        }
    }

    private  class  HTTPPOSTImgExecutor implements  Runnable {
        private Bitmap _bmp = null;
        private String _order = null;

        public HTTPPOSTImgExecutor(Bitmap bmp,String order) {
            _bmp = bmp;
            _order = order;
        }

        @UiThread
        @Override
        public void run() {
            //UIスレッドの記述
//
            Log.e(DEBUG_TAG, "テストIMG＝＝＝＝＝＝＝＝");
            //tvをgetして書き換え
            if(_order == "GETIMG"){
                ImageView imageView = findViewById(R.id.imgbt_r);
                _bmp = Bitmap.createScaledBitmap(_bmp, 400, 300, true);
                imageView.setImageBitmap(_bmp);
            }
            else{
                ImageView imageView = findViewById(R.id.imgbt_l);
                _bmp = Bitmap.createScaledBitmap(_bmp, 400, 300, true);
                imageView.setImageBitmap(_bmp);
            }

        }
    }

    private String is2String(InputStream is)throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
        StringBuffer sb = new StringBuffer();
        char[] b = new char[1024];
        int line;
        while(0 <= (line = reader.read(b))){
            sb.append(b,0,line);
        }
        return sb.toString();
    }

}
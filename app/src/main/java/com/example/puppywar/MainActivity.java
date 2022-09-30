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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String DEBUG_TAG = "Main activity";
    private String ramen_name_r;
    private String ramen_name_l;
    String url = "http://flask-env-eb-3.eba-kndu7tft.ap-northeast-1.elasticbeanstalk.com/";
    String url_json = url+"getjson/";
    String url_img = url+"static/images/";
    int current_id_r;
    int current_id_l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton Imgbt_r = findViewById(R.id.imgbt_r);
        ImageButton Imgbt_l = findViewById(R.id.imgbt_l);
        TextView Tv_r = findViewById(R.id.tv_r);
        TextView Tv_l = findViewById(R.id.tv_l);
        ButtonListener listener = new ButtonListener();
        Imgbt_r.setOnClickListener(listener);
        Imgbt_l.setOnClickListener(listener);
        init_imgandjson(Imgbt_r,Imgbt_l,Tv_r,Tv_l);
    }

    public void init_imgandjson(ImageButton imgbt_r,ImageButton imgbt_l,TextView Tv_r,TextView Tv_l){

        Random rand = new Random();
        List<Integer> arraylist = new ArrayList<>(Arrays.asList(32,35,37,38,39,40,41,42,43,44));
        int random_num_r = rand.nextInt(10);
        int random_num_l = rand.nextInt(10);
        while(random_num_r == random_num_l){
            random_num_l = rand.nextInt(10);
        }
        current_id_r = arraylist.get(random_num_r);
        current_id_l = arraylist.get(random_num_l);
        receiveHttp(url_json+Integer.valueOf(current_id_r).toString(),"GET_R");
        receiveHttp(url_json+Integer.valueOf(current_id_l).toString(),"GET_L");
    }


    private class ButtonListener implements View.OnClickListener{
        ImageButton Imgbt_r = findViewById(R.id.imgbt_r);
        ImageButton Imgbt_l = findViewById(R.id.imgbt_l);
        TextView Tv_r = findViewById(R.id.tv_r);
        TextView Tv_l = findViewById(R.id.tv_l);
        @Override
        public void onClick(View view){
            int id = view.getId();
            switch (id){
                case R.id.imgbt_r:
                    Log.e(DEBUG_TAG,"current_id_r"+Integer.valueOf(current_id_r).toString());
                    Log.e(DEBUG_TAG,"current_id_l"+Integer.valueOf(current_id_l).toString());
                    receiveHttp(url+"updatewin/"+Integer.valueOf(current_id_r).toString(),"updatewinlose");
                    receiveHttp(url+"updatelose/"+Integer.valueOf(current_id_l).toString(),"updatewinlose");
                    init_imgandjson(Imgbt_r,Imgbt_l,Tv_r,Tv_l);
                    break;
                case R.id.imgbt_l:
                    Log.e(DEBUG_TAG,"current_id_r"+Integer.valueOf(current_id_r).toString());
                    Log.e(DEBUG_TAG,"current_id_l"+Integer.valueOf(current_id_l).toString());
                    receiveHttp(url+"updatelose/"+Integer.valueOf(current_id_r).toString(),"updatewinlose");
                    receiveHttp(url+"updatewin/"+Integer.valueOf(current_id_l).toString(),"updatewinlose");
                    init_imgandjson(Imgbt_r,Imgbt_l,Tv_r,Tv_l);
                    break;
            }
        }
    }

    @UiThread
    private void receiveHttp(final String urlFull,String order){
        Looper mainlooper = Looper.getMainLooper();
        Handler handler = HandlerCompat.createAsync(mainlooper);
        MainActivity.HttpBackgroundReceiver backgroundReceiver = new MainActivity.HttpBackgroundReceiver(handler,urlFull,order);
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
                if (_order == "GET_R" || _order == "GET_L" ) {
                    con.setRequestMethod("GET");
                    con.connect();
                    is = con.getInputStream();
                    result = is2String(is);
                } else if (_order == "GETIMG_R" || _order == "GETIMG_L" ) {
                    con.setRequestMethod("GET");
                    con.connect();
                    is = con.getInputStream();
                    bmp = BitmapFactory.decodeStream(is);
                } else if(_order =="updatewinlose"){
                    con.setRequestMethod("GET");
                    con.connect();
                    int responseCode = con.getResponseCode();
                    Log.e(DEBUG_TAG, "通信開始");
                    if(responseCode == HttpURLConnection.HTTP_OK){
                        Log.e(DEBUG_TAG, "通信成功");
//                        con.disconnect();
                    }
                    con.disconnect();
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

            if (_order == "GET_R" || _order == "POST" || _order == "GET_L") {
                MainActivity.HttpPostExecutor postExecutor = new MainActivity.HttpPostExecutor(result,_order);
                _handler.post(postExecutor);
            }
            else if(_order == "updatewinlose"){
                MainActivity.HttpPostExecutor postExecutor = new MainActivity.HttpPostExecutor(result,_order);
                _handler.post(postExecutor);
            }
            else {
                MainActivity.HTTPPOSTImgExecutor postImgExecutor = new MainActivity.HTTPPOSTImgExecutor(bmp,_order);
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
                if(_order != "updatewinlose"){
                try{
                    JSONObject rootJSON = new JSONObject(_result);
                    id = rootJSON.getInt("id");
                    win = rootJSON.getInt("win");
                    lose = rootJSON.getInt("lose");
                    title = rootJSON.getString("title");
                }
                catch (JSONException ex){
                    Log.e(DEBUG_TAG,"JSON解析失敗",ex);
                }}
                //tvをgetして書き換え
                if(_order =="GET_R"){
                    TextView response = findViewById(R.id.tv_r);
                    response.setText(title);
                    receiveHttp(url_img+title+".jpg","GETIMG_R");
//                    response.setText("id:"+Integer.valueOf(id).toString()+","+"win:"+Integer.valueOf(win).toString()+"lose:"+Integer.valueOf(lose).toString()+"title:"+title);
                }
                else if(_order =="GET_L"){
                    TextView response = findViewById(R.id.tv_l);
                    response.setText(title);
                    receiveHttp(url_img+title+".jpg","GETIMG_L");
//                    response.setText("id:"+Integer.valueOf(id).toString()+","+"win:"+Integer.valueOf(win).toString()+"lose:"+Integer.valueOf(lose).toString()+"title:"+title);
                }
                else{
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
                if(_order == "GETIMG_R"){
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
//            case R.id.menuListOptionCamera:
//                Intent intent_camera = new Intent(MainActivity.this,CameraActivity.class);
//                startActivity(intent_camera);
//                break;
//            case R.id.menuListOptionHTTPtest:
//                Intent intent_HTTPtest = new Intent(MainActivity.this,HTTPtestActivity.class);
//                startActivity(intent_HTTPtest);
//                break;
            default:
                resultVal = super.onOptionsItemSelected(item);
                break;
        }
        return resultVal;
    }

}
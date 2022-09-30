package com.example.puppywar;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HTTPtestActivity extends AppCompatActivity {
//    private String url= "https://quiet-shelf-97515.herokuapp.com/";
    private static final String DEBUG_TAG = "HTTP test";
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
        String urlFull = "http://flask-env-eb-3.eba-kndu7tft.ap-northeast-1.elasticbeanstalk.com/getjson/17";
        String urlImage = "http://flask-env-eb-3.eba-kndu7tft.ap-northeast-1.elasticbeanstalk.com/static/images/あああ.jpg";
        @Override
        public void onClick(View view) {
            TextView response = findViewById(R.id.tvResponse);
            int id = view.getId();
            switch (id) {
                case R.id.btGet:
                    receiveHttp(urlFull,"GET");
                    receiveHttp(urlImage,"GETIMG");
                    break;
                case R.id.btPost:
                    receiveHttp(urlFull,"POST");
                    break;
                case R.id.btClear:
                    response.setText("");
                    break;
            }

        }
    }
    @UiThread
    private void receiveHttp(final String urlFull,String order){
        Looper mainlooper = Looper.getMainLooper();
        Handler handler = HandlerCompat.createAsync(mainlooper);
        HttpBackgroundReceiver backgroundReceiver = new HttpBackgroundReceiver(handler,urlFull,order);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(backgroundReceiver);
    }

    private class HttpBackgroundReceiver implements Runnable{

        private  final Handler _handler;
        private final String _urlFull;
        private final String _order;

        public HttpBackgroundReceiver(Handler handler, String urlFull,String order){
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
            try{
                URL url = new URL(_urlFull);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(2000);
                con.setReadTimeout(2000);
                if(_order =="GET") {
                    con.setRequestMethod("GET");
                    con.connect();
                    is = con.getInputStream();
                    result = is2String(is);
                }
                else if (_order == "GETIMG"){
                    con.setRequestMethod("GET");
                    con.connect();
                    is = con.getInputStream();
                    bmp = BitmapFactory.decodeStream(is);
                }
                else{
//                    String postData="name=testfromandroid" ;
                    String json="{\n" +
                            "    \"name\": \"test from android2\",\n" +
                            "    \"items\":[\n" +
                            "        {\n" +
                            "            \"name\":\"item name\",\n" +
                            "            \"price\": 110\n" +
                            "        }\n" +
                            "    ]\n" +
                            "}";
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    con.setRequestProperty("Content-Type","application/json; charset=utf-8");
                    con.connect();
                    PrintStream ps = new PrintStream(con.getOutputStream());
                    ps.print(json);
                    ps.close();
                    if (con.getResponseCode() != 200) {
                        Log.e(DEBUG_TAG,"con.getResponseCode() != 200");

                    }
//                    OutputStream os = con.getOutputStream();
//                    os.write(postData.getBytes());
//                    os.flush();
//                    os.close();
                    con.disconnect();
                }
            }
            catch (MalformedURLException ex){
                Log.e(DEBUG_TAG, "URL変換失敗",ex);
            }
            catch (SocketTimeoutException ex){
                Log.w(DEBUG_TAG, "通信タイム",ex);
            }
            catch (IOException ex){
                Log.e(DEBUG_TAG, "通信失敗",ex);
            }
            finally {
                if(con != null){
                    con.disconnect();
                }
                if(is != null){
                    try{
                        is.close();
                    }
                    catch (IOException ex){
                        Log.e(DEBUG_TAG,"InputStream解放失敗");
                    }
                }
            }
            if(_order=="GET" || _order=="POST"){
                HttpPostExecutor postExecutor = new HttpPostExecutor(result);
                _handler.post(postExecutor);
            }
            else{
                HTTPPOSTImgExecutor postImgExecutor = new HTTPPOSTImgExecutor(bmp);
                _handler.post(postImgExecutor);
            }
        }
    }

    private class HttpPostExecutor implements Runnable{
        private final String _result;

        public HttpPostExecutor(String result){
            _result = result;
        }

        @UiThread
        @Override
        public void run (){
            //UIスレッドの記述
//            String root_name ="";
            String category_name = "";
            String store_name = "";
            String item_name = "";
            int id=-1,win=-1,lose=-1;
            String title="default_title";
            Log.e(DEBUG_TAG,"テスト＝＝＝＝＝＝＝＝");
            try{
                JSONObject rootJSON = new JSONObject(_result);
                id = rootJSON.getInt("id");
                win = rootJSON.getInt("win");
                lose = rootJSON.getInt("lose");
                title = rootJSON.getString("title");
            }
            catch (JSONException ex){
                Log.e(DEBUG_TAG,"JSON解析失敗",ex);
            }
            //tvをgetして書き換え
            TextView response = findViewById(R.id.tvResponse);
            response.setText("id:"+Integer.valueOf(id).toString()+","+"win:"+Integer.valueOf(win).toString()+"lose:"+Integer.valueOf(lose).toString()+"title:"+title);


        }
    }

    private  class  HTTPPOSTImgExecutor implements  Runnable {
        private Bitmap _bmp = null;

        public HTTPPOSTImgExecutor(Bitmap bmp) {
            _bmp = bmp;
        }

        @UiThread
        @Override
        public void run() {
            //UIスレッドの記述
//
            Log.e(DEBUG_TAG, "テストIMG＝＝＝＝＝＝＝＝");
            //tvをgetして書き換え
            ImageView imageView = findViewById(R.id.ivGET);
            imageView.setImageBitmap(_bmp);
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
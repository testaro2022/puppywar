package com.example.puppywar;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
        String urlFull = "https://quiet-shelf-97515.herokuapp.com/stores";
        @Override
        public void onClick(View view) {
            TextView response = findViewById(R.id.tvResponse);
            int id = view.getId();
            switch (id) {
                case R.id.btGet:
                    receiveHttp(urlFull,"GET");
//                    response.setText("Get");
                    break;
                case R.id.btPost:
//                    response.setText("Post");
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
                else{
                    String postData=
                            "{"+
                                    "name"+":"+ "test from android,"+
                            "items:["+
                    "{"+
                        "name:item name,"+
                            "price: 110"+
                    "}" +"]"+ "};";
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    OutputStream os = con.getOutputStream();
                    os.write(postData.getBytes());
                    os.flush();
                    os.close();
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

            HttpPostExecutor postExecutor = new HttpPostExecutor(result);
            _handler.post(postExecutor);
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
            int price = 0;
            Log.e(DEBUG_TAG,"テスト＝＝＝＝＝＝＝＝");
            try{
                JSONObject rootJSON = new JSONObject(_result);
                JSONArray storesJSON = rootJSON.getJSONArray("stores");
                store_name = storesJSON.getJSONObject(0).getString("name");
                JSONArray itemJSON = storesJSON.getJSONObject(0).getJSONArray("items");
                item_name = itemJSON.getJSONObject(0).getString("name");
                price = itemJSON.getJSONObject(0).getInt("price");
//                store_name = rootJSON.getString("name");
//                JSONObject itemJSON = storesJSON.getJSONObject("items");
//                item_name = itemJSON.getString("name");
//                price = itemJSON.getInt("price");
            }
            catch (JSONException ex){
                Log.e(DEBUG_TAG,"JSON解析失敗",ex);
            }
            //tvをgetして書き換え
            TextView response = findViewById(R.id.tvResponse);
            Integer i = Integer.valueOf(price);
            response.setText("stores:"+store_name+","+"item_name:"+item_name+","+"price:"+i.toString());
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
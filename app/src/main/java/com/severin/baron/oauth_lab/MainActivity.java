package com.severin.baron.oauth_lab;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public final static String CONSUMER_KEY = URLEncoder.encode("42uy1FERx76x6khYEBUJfNI7h");
    public final static String CONSUMER_SECRET = URLEncoder
            .encode("4p7aTHnVINgRjcfon4BDUrzKXClcbOHtgczi4wYLldcimQ35ta");

    String plainString = CONSUMER_KEY + ":" + CONSUMER_SECRET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        construct();
    }

    private void construct() {

        byte[] data = new byte[0];
        try {
            data = plainString.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String base64 = Base64.encodeToString(data, Base64.NO_WRAP);
        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build();

        Request request = new Request.Builder()
                .url("https://api.twitter.com/oauth2/token")
                .header("Authorization", "Basic " + base64)
                .addHeader("Content-Type", "application/x-www-urlencoed;charset=UTF-8")
                .post(formBody)
                .build();

        final OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected coe " + response);
                Log.d("Response: ", response.body().toString());
            }
        });


    }


}

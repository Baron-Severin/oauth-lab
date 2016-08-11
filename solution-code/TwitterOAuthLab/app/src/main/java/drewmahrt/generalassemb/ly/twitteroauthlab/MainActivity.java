package drewmahrt.generalassemb.ly.twitteroauthlab;

import android.database.DataSetObserver;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

//import drewmahrt.generalassemb.ly.twitteroauthlab.models.BearerToken;
import drewmahrt.generalassemb.ly.twitteroauthlab.models.Tweet;
import drewmahrt.generalassemb.ly.twitteroauthlab.models.Tweets;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getName();

    private String mAccessToken;

    private Button searchButton;
    private EditText editText;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchButton = (Button)findViewById(R.id.search_button);
        editText = (EditText)findViewById(R.id.search_edit_text);
        listView = (ListView)findViewById(R.id.tweets_list);

        // remember that the rest of onCreate will fire before this onClickListener is called
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTweets();
            }
        });

        // get current time(?), format it into GSON
//        Gson gson = new GsonBuilder()
//                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
//                .create();

        // interceptor catches and formats our header before our request is made.  Not 100% clear
        // on how this works
//        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
//        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
//        OkHttpClient client = new OkHttpClient.Builder().build();

        // Start with Retrofit class, add on our base URL, add the client (interceptor), add our
        // GSON formatted time string
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TwitterAppData.BASE_URL)
//                .client(client)
//                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // I believe this is a Retrofit object that extends our interface
        TwitterApiEndpointInterface twitterApi = retrofit.create(TwitterApiEndpointInterface.class);

        // Add together our public and private key in the specified manner
        String plainString = TwitterAppData.CONSUMER_KEY+":"+TwitterAppData.CONSUMER_SECRET;
        byte[] data = new byte[0];
        // Format public+:+private into bytes
        try {
            data = plainString.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // Encode public+:+private bytes using Base64
        String base64 = Base64.encodeToString(data, Base64.NO_WRAP);

        Log.d(TAG,"Ready to make bearer req: "+base64);

        // This Retrofit object extends from our interface and includes base URL, header
        // interceptor, GSON time stamp.  We feed it our completed auth string and requested content
        // type, and request client creds
        Call<ResponseBody> call = twitterApi.authorizeApplication("Basic "+base64,"application/x-www-form-urlencoded;charset=UTF-8","client_credentials");
//        Log.d("SEVTEST HEAD", "" + call.request().headers());

        for (String head : call.request().headers().names()) {
                       Log.d("Header","Names: " + head + ":" + call.request().headers().values(head));
                   }


        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG,"Call completed!");
                try {
                    // Grab response information as a string
                    String responseString = response.body().string();
                    Log.d(TAG,"Token: "+responseString);
                    // Turn response into JSONObject
                    JSONObject object = new JSONObject(responseString);
                    // Access token variable of the object, store it in memory as a string
                    mAccessToken = object.getString("access_token");
                    Log.d(TAG,"Access Token: "+mAccessToken);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG,"Bearer token call failed");
                Log.d(TAG,t.getMessage().toString());
            }
        });
    }

    private void getTweets(){
//         BELOW THIS POINT : same as earlier
//        Gson gson = new GsonBuilder()
//                .setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz")
//                .create();

//        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
//        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
//        OkHttpClient client = new OkHttpClient.Builder().build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TwitterAppData.BASE_URL)
//                .client(client)
//                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        TwitterApiEndpointInterface twitterApi = retrofit.create(TwitterApiEndpointInterface.class);
        // ABOVE THIS POINT : same as earlier

        // Collect query from EditText
        String searchName = editText.getText().toString();
        // Make request with access token, query, and limit
        Call<ResponseBody> call = twitterApi.userTimeline("Bearer "+mAccessToken,searchName,20);
        Log.d("SEVTEST ", "" + call.request().toString());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG,"Call completed!");
                Log.d("SEVTEST ", call.request().toString());
                try {
                    // Ingest response as a string
                    String responseBody = response.body().string();
                    Log.d(TAG,"Response: "+responseBody);
                    // Turn response into JSONArray
                    JSONArray tweets = new JSONArray(responseBody);

                    // Instantiate AL of our model (Tweet)
                    ArrayList<Tweet> tweetList = new ArrayList<Tweet>();

                    // For each JSONObject in our JSONArray
                    for (int i = 0; i < tweets.length(); i++) {
                        // Grab individual object to work with
                        JSONObject object = tweets.getJSONObject(i);
                        // Temp strings taken from 'text' and 'created_at' fields
                        String text = object.getString("text");
                        String date = object.getString("created_at");
                        // Create a new tweet object, feed it our temp strings
                        Tweet tweet  = new Tweet();
                        tweet.setText(text);
                        tweet.setCreatedAt(date);
                        // Add tweet to our AL
                        tweetList.add(tweet);
                    }

                    // After we've ingested all tweet responses, feed the AL into our adapter
                    TweetAdapter adapter = new TweetAdapter(MainActivity.this,tweetList);
                    // Give the adapter to our LV
                    listView.setAdapter(adapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG,"Get tweet call failed");
                Log.d(TAG,t.getMessage().toString());
            }
        });

    }
}

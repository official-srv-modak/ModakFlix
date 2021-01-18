package com.example.modakflix;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.khizar1556.mkvideoplayer.MKPlayerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class MKPlayer extends MKPlayerActivity{

    @Override
    public void onBackPressed() {
        Toast.makeText(MKPlayer.this, "HIhihIHIHIHi" , Toast.LENGTH_LONG).show();
        finish();
    }
}
public class Description extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            //JSONObject card = reload.execute(Movies.get_shows_watched_path);
            JSONObject card = new JSONObject(getIntent().getStringExtra("description"));
            String resumeFlag = getIntent().getStringExtra("resumeFlag");

            ImageView imageView = (ImageView) findViewById(R.id.image);

            String album_art_path = card.getString("album_art_path");
            if(!album_art_path.isEmpty())
                Glide.with(this).load(album_art_path).into(imageView);
            TextView showName = findViewById(R.id.showName);
            String name = card.getString("name");
            showName.setText(name);
            String desc = card.getString("des");
            if(!desc.isEmpty())
            {
                TextView description = findViewById(R.id.description);
                description.setText(desc);
            }


            ImageButton backBtn = findViewById(R.id.backBtn);

            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            });



            if(resumeFlag.equals("0")) // start over
            {
                String videoUrl = handleUrl(card.getString("playable_file_path"));
                Button playBtn = findViewById(R.id.playBtn);
                playBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        MKPlayer m = new MKPlayer();
                        m.configPlayer(Description.this).play(videoUrl);

                    }
                });

                Button openWith = findViewById(R.id.playWithBtn);
                openWith.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String appPackageName = "com.mxtech.videoplayer.ad";
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setPackage("com.mxtech.videoplayer.ad");
                            intent.setClassName("com.mxtech.videoplayer.ad", "com.mxtech.videoplayer.ad.ActivityScreen");
                            Uri videoUri = Uri.parse(videoUrl);
                            intent.setDataAndType(videoUri, "application/x-mpegURL");
                            int pos = 1000;
                            //intent.putExtra("position", 0000);
                            intent.setPackage("com.mxtech.videoplayer.ad"); // com.mxtech.videoplayer.pro
                            intent.putExtra("position", pos);
                            intent.putExtra("return_result", true);
                            startActivityForResult(intent, 1);

                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(Description.this, "MX Player not installed. Install MX Player" , Toast.LENGTH_LONG).show();
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                });
            }
            else    // Resume
            {
                String videoUrl = handleUrl(card.getString("url"));
                Button openWith = findViewById(R.id.playWithBtn);
                int dur = Integer.parseInt(card.getString("duration")), pos = Integer.parseInt(card.getString("position"));
                int rem = dur - pos;
                rem /= 1000;
                int mins = rem/60;
                int hrs = mins/60;
                if(hrs > 0)
                {
                    mins = mins%60;
                    openWith.setText("Resume "+hrs+" hour "+mins+" min(s) left");
                }
                else
                {
                    openWith.setText("Resume "+mins+" min(s) left");
                }

                openWith.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String appPackageName = "com.mxtech.videoplayer.ad";
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setPackage("com.mxtech.videoplayer.ad");
                            intent.setClassName("com.mxtech.videoplayer.ad", "com.mxtech.videoplayer.ad.ActivityScreen");
                            Uri videoUri = Uri.parse(videoUrl);
                            intent.setDataAndType(videoUri, "application/x-mpegURL");
                            intent.setPackage("com.mxtech.videoplayer.ad"); // com.mxtech.videoplayer.pro
                            intent.putExtra("position", pos);
                            intent.putExtra("return_result", true);
                            startActivityForResult(intent, 1);

                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(Description.this, "MX Player not installed. Install MX Player" , Toast.LENGTH_LONG).show();
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                });
                Button playBtn = findViewById(R.id.playBtn);
                playBtn.setVisibility(View.INVISIBLE);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);


    }

    public static String pingDataServer(String URL)
    {
        String output = "";
        try{
            java.net.URL url = new URL(URL);
            Map params = new LinkedHashMap<>();
            StringBuilder postData = new StringBuilder();
            Set<Map.Entry> s = params.entrySet();
            for (Map.Entry param : s) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode((String) param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                {
                    output += inputLine;
                }
            }
            in.close();
            //JSONObject jsonObj = new JSONObject(output);
            return output;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)  // -1 RESULT_OK : Playback was completed or stopped by user request.
            //Activity.RESULT_CANCELED: User canceled before starting any playback.
            //RESULT_ERROR (=Activity.RESULT_FIRST_USER): Last playback was ended with an error.

            if (data.getAction().equals("com.mxtech.intent.result.VIEW")) {
                //data.getData()
                PostProcess p = new PostProcess();
                p.execute(data);
            }
    }

    public static String handleUrl(String URL)
    {
        String output = "http:";
        String temp = URL.split(output)[1];
        temp = temp.replace("/", "forwardslash");
        temp = temp.replace(" ", "spacebarspace");
        try {
            temp= URLEncoder.encode(temp, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        temp = temp.replace("forwardslash", "/");
        temp = temp.replace("spacebarspace", "%20");
        output += temp;
        return output;
    }
    private void doPostProcess(Intent data)
    {
        int pos = data.getIntExtra("position", -1); // Last playback position in milliseconds. This extra will not exist if playback is completed.
        int dur = data.getIntExtra("duration", -1); // Duration of last played video in milliseconds. This extra will not exist if playback is completed.
        String cause = data.getStringExtra("end_by"); //  Indicates reason of activity closure.
        Uri uri = data.getData();
        String name = "";
        try {
            name = URLDecoder.decode(uri.toString().split("/")[uri.toString().split("/").length - 2], "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            double rem = 0;
            rem = dur - pos;
            rem = (rem/dur)*100;
            if (rem >= 5)
                pingDataServer(Movies.record_position_path+"?username=admin&show="+ URLDecoder.decode(uri.toString(), "UTF-8")+"&pos="+pos+"&duration="+dur+"&cause="+cause+"&name="+name);
            else
                pingDataServer(Movies.delete_position_path+"?username=admin&show="+name);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private class Reload extends AsyncTask<String, Void, JSONObject> {
        protected JSONObject doInBackground(String... url) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                JSONObject jsonObject = Movies.getDataFromServer(url[0]);
                return jsonObject;
            }
            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });*/
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

    }

    private class PostProcess extends AsyncTask<Intent, Void, Integer> {
        protected Integer doInBackground(Intent... data) {
            doPostProcess(data[0]);
            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });*/
            return 0;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

    }

}
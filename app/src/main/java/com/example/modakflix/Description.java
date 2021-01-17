package com.example.modakflix;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.khizar1556.mkvideoplayer.MKPlayerActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        try {
            JSONObject card = new JSONObject(getIntent().getStringExtra("description"));
            ImageView imageView = (ImageView) findViewById(R.id.image);

            String album_art_path = card.getString("album_art_path");
            if(!album_art_path.isEmpty())
                Glide.with(this).load(album_art_path).into(imageView);
            TextView showName = findViewById(R.id.showName);
            showName.setText(card.getString("name"));
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
                    finish();
                }
            });

            Button playBtn = findViewById(R.id.playBtn);
            String videoUrl = handleUrl(card.getString("playable_file_path"));
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
                        intent.setPackage("com.mxtech.videoplayer.ad"); // com.mxtech.videoplayer.pro
                        intent.putExtra("position", 3521729);
                        intent.putExtra("return_result", true);
                        startActivityForResult(intent, 1);

                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(Description.this, "MX Player not installed. Install MX Player" , Toast.LENGTH_LONG).show();
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
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
                int pos = data.getIntExtra("position", -1); // Last playback position in milliseconds. This extra will not exist if playback is completed.
                int dur = data.getIntExtra("duration", -1); // Duration of last played video in milliseconds. This extra will not exist if playback is completed.
                String cause = data.getStringExtra("end_by"); //  Indicates reason of activity closure.

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
}
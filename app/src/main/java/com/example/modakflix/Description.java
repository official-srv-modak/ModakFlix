package com.example.modakflix;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
            playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        MKPlayerActivity.configPlayer(Description.this).play(handleUrl(card.getString("playable_file_path")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
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
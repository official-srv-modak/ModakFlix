package com.example.modakflix;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Profiles extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        overridePendingTransition(0, 0);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}


        LoadCard ld = new LoadCard();
        ld.execute(Movies.get_profiles);
    }

    private class LoadCard extends AsyncTask<String, Void, Integer> {
        protected Integer doInBackground(String... urls) {

            JSONObject jsonData = null, resumeData = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                jsonData = Movies.getDataFromServer(urls[0]);
            }
            JSONObject finalJsonData = jsonData;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    JSONArray show = null;
                    try {
                        show = finalJsonData.getJSONArray("cards");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    LinearLayout c = findViewById(R.id.linearLayout2);


                    for (int i = 0; i < show.length(); ) {
                        LinearLayout linearLayout2 = new LinearLayout(Profiles.this);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                                (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        linearLayout2.setLayoutParams(params);
                        linearLayout2.setWeightSum(2f);
                        linearLayout2.setOrientation(LinearLayout.HORIZONTAL);
                        linearLayout2.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

                        for (int j = 0; j < 2 && i<show.length(); j++) {
                            JSONObject card = null;
                            try {
                                card = show.getJSONObject(i++);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            View view = LayoutInflater.from(Profiles.this).inflate(R.layout.profiles, null);
                            @SuppressLint({"NewApi", "LocalSuppress"}) int uniqueId = View.generateViewId();
                            view.setId(uniqueId);

                            TextView tv = view.findViewById(R.id.accountName);
                            try {
                                tv.setText(card.getString("first_name") + " " + card.getString("last_name"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            linearLayout2.addView(view);

                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    overridePendingTransition(0, R.anim.fade_out);
                                    Intent intent = new Intent(Profiles.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }

                        c.addView(linearLayout2);
                    }
                }
            });

            return null;
        }

    }
}
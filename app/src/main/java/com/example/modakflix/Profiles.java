package com.example.modakflix;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;

public class Profiles extends AppCompatActivity {

    public static String ip = "", ipInfoFilePath = "";

    public static String domain_name = "http://"+ip+"/OTTServer/ModakFlix/";
    public static String record_position_path = domain_name+"record_position.php";
    public static String delete_position_path = domain_name+"delete_from_shows_watched.php";
    public static String get_shows_watched_path = domain_name+"get_shows_watched.php?username=admin";
    public static String reset_profile = domain_name+"reset_profile.php?username=admin";
    public static String get_movies_list = domain_name+"get_movies_list_json.php";
    public static String reload_shows_watched = domain_name+"reload_shows_watched.php";
    public static String search_shows = domain_name+"search_show.php";
    public static String get_profiles = domain_name+"get_profiles.php";
    public static String reload_description = domain_name+"reload_description.php";
    public static String get_description = domain_name+"get_description.php";
    public static String add_profile = domain_name+"add_profile.php";
    private static int actResume = 0;

    public static String fetchIpDataFromFile(String ipInfoFilePath)
    {
        File file = null;
        String ipFromFile = "00";
        if(!ipInfoFilePath.isEmpty())
        {
            file = new File(ipInfoFilePath);
        }
        if(file != null)
        {
            try {
                ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(file));
                ipFromFile = (String) objIn.readObject();
                objIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        Log.e("IP", ipFromFile);
        return ipFromFile;
    }

    public static void writeIpData(String ipInfoFilePath, String ipData)
    {
        File file = new File(ipInfoFilePath);
        try {
            ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(file));
            objOut.writeObject(ipData);
            objOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);

        overridePendingTransition(0,0);
        ipInfoFilePath = getApplicationContext().getFilesDir().getAbsolutePath() + "/ipInfo.dat";
        ip = fetchIpDataFromFile(ipInfoFilePath);

        domain_name = "http://"+ip+"/OTTServer/ModakFlix/";
        record_position_path = domain_name+"record_position.php";
        delete_position_path = domain_name+"delete_from_shows_watched.php";
        get_shows_watched_path = domain_name+"get_shows_watched.php?username=admin";
        reset_profile = domain_name+"reset_profile.php?username=admin";
        get_movies_list = domain_name+"get_movies_list_json.php";
        reload_shows_watched = domain_name+"reload_shows_watched.php";
        search_shows = domain_name+"search_show.php";
        get_profiles = domain_name+"get_profiles.php";
        reload_description = domain_name+"reload_description.php";
        get_description = domain_name+"get_description.php";
        add_profile = domain_name+"add_profile.php";
        actResume = 0;

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        String startFlag = "0";
        if(getIntent().hasExtra("startFlag"))
        {
            startFlag = getIntent().getStringExtra("startFlag");
        }
        if(startFlag.equals("0"))
            overridePendingTransition(0, 0);
        else
            overridePendingTransition(R.anim.fade_in, 0);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        /*LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 27, 0, 0);
        AppBarLayout ap = findViewById(R.id.appBarLayout);
        ap.setLayoutParams(lp);

        LoadCard ld = new LoadCard();
        ld.execute(get_profiles);*/

    }

    /*boolean hasAnimationStarted;
    AppBarLayout logo;
    public void onWindowFocusChanged(boolean hasFocus) {
        logo = findViewById(R.id.appBarLayout);
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !hasAnimationStarted) {
            hasAnimationStarted=true;
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            ObjectAnimator translationY = ObjectAnimator.ofFloat(logo, "y", metrics.heightPixels / 2 - (metrics.heightPixels / 2)); // metrics.heightPixels or root.getHeight()
            translationY.setDuration(1);
            translationY.start();
        }
    }*/

    public void showServerDialog(String Message)
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(Message);

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Movies.writeIpData("192.168.0.4");

                AlertDialog.Builder builder = new AlertDialog.Builder(Profiles.this);
                builder.setMessage("Enter Server's Local IP Address");
                final EditText input = new EditText(Profiles.this);
                input.setHint("IP Address");
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        writeIpData(ipInfoFilePath, input.getText().toString().trim());
                        ip = input.getText().toString().trim();
                        Intent intent = new Intent(Profiles.this, SplashScreen.class);
                        finish();
                        startActivity(intent);
                    }
                });
                builder.show();
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alertDialogBuilder.show();

    }

    private class LoadCard extends AsyncTask<String, Void, Integer> {
        protected Integer doInBackground(String... urls) {

            JSONObject jsonData = null, resumeData = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                jsonData = Movies.getDataFromServer(urls[0]);
            }
            final JSONObject finalJsonData = jsonData;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONArray show = null;
                    try {
                        show = finalJsonData.getJSONArray("cards");
                    } catch (Exception e) {
                        e.printStackTrace();
                        showServerDialog("Server not found! Want to input local IP?");
                    }
                    LinearLayout c = findViewById(R.id.linearLayout2);
                    if(show!=null)
                    {
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
                                        intent.putExtra("username", tv.getText());
                                        intent.putExtra("ip", ip);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                            // c.removeView(loading);
                            c.addView(linearLayout2);
                        }
                    }
                    View view = LayoutInflater.from(Profiles.this).inflate(R.layout.profiles, null);
                    @SuppressLint({"NewApi", "LocalSuppress"}) int uniqueId = View.generateViewId();
                    view.setId(uniqueId);
                    TextView tv = view.findViewById(R.id.accountName);
                    tv.setText("Edit Profiles");

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            overridePendingTransition(0, R.anim.fade_out);
                            Intent intent = new Intent(Profiles.this, EditProfile.class);
                            intent.putExtra("profileData", finalJsonData.toString());
                            startActivity(intent);
                        }
                    });
                    c.addView(view);
                    TextView loading = findViewById(R.id.loading);
                    loading.setVisibility(View.INVISIBLE);
                }
            });

            return null;
        }
        ProgressDialog progressDialog = new ProgressDialog(Profiles.this);;

        /*@Override
        protected void onPreExecute() {
            super.onPreExecute();


            progressDialog.setMessage("Loading...");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.show();


            int temp = 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
                progressDialog.dismiss();
        }*/

    }
}
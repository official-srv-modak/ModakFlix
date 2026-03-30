package com.souravmodak.modakflix;

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
import com.souravmodak.modakflix.R;

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

    //public static String ip = "3.108.236.185", ipInfoFilePath = "";
    public static String ip = "modakflix.com", ipInfoFilePath = "";

    public static String domain_name = "http://"+ip+"/";
    public static String record_position_path = domain_name+"record_position.php";
    public static String delete_position_path = domain_name+"delete_from_shows_watched.php";
    public static String get_shows_watched_path = domain_name+"get_shows_watched.php?username=";
    public static String reset_profile = domain_name+"reset_profile.php?username=";
    public static String get_movies_list = domain_name+"get_movies_list_json.php";
    public static String reload_shows_watched = domain_name+"reload_shows_watched.php";
    public static String search_shows = domain_name+"search_show.php";
    public static String get_profiles = domain_name+"get_profiles.php";
    public static String reload_description = domain_name+"reload_description.php";
    public static String get_description = domain_name+"get_description.php";
    public static String add_profile = domain_name+"add_profile.php";
    public static String reset_show = domain_name+"reset_show.php";
    public static String upload = domain_name+"upload.php";
    private static int actResume = 0;

    public static String fetchIpDataFromFile(String ipInfoFilePath)
    {
        File file = null;
        String ipFromFile = ip;
        if(!ipInfoFilePath.isEmpty())
        {
            file = new File(ipInfoFilePath);
        }
        if(file != null && file.exists())
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
        if(ipData.contains("http://"))
            ipData = ipData.split("http://")[1];
        else if(ipData.contains("https://"))
            ipData = ipData.split("https://")[1];
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

        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
        ipInfoFilePath = getApplicationContext().getFilesDir().getAbsolutePath() + "/ipInfo.dat";
        ip = fetchIpDataFromFile(ipInfoFilePath);

        updatePaths(ip);
        
        actResume = 0;
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

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
            if (this.getSupportActionBar() != null) {
                this.getSupportActionBar().hide();
            }
        }
        catch (NullPointerException e){}

        LoadCard ld = new LoadCard();
        ld.execute(get_profiles);

    }

    public static void updatePaths(String newIp) {
        ip = newIp;
        domain_name = "http://"+ip+"/";
        record_position_path = domain_name+"record_position.php";
        delete_position_path = domain_name+"delete_from_shows_watched.php";
        get_shows_watched_path = domain_name+"get_shows_watched.php?username=";
        reset_profile = domain_name+"reset_profile.php?username=";
        get_movies_list = domain_name+"get_movies_list_json.php";
        reload_shows_watched = domain_name+"reload_shows_watched.php";
        search_shows = domain_name+"search_show.php";
        get_profiles = domain_name+"get_profiles.php";
        reload_description = domain_name+"reload_description.php";
        get_description = domain_name+"get_description.php";
        add_profile = domain_name+"add_profile.php";
        reset_show = domain_name+"reset_show.php";
        upload = domain_name+"upload.php";
    }

    public void showServerDialog(String Message)
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(Message);

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Profiles.this);
                builder.setMessage("Enter Server's Local IP Address");
                final EditText input = new EditText(Profiles.this);
                input.setHint("IP Address");
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newIp = input.getText().toString().trim();
                        writeIpData(ipInfoFilePath, newIp);
                        updatePaths(newIp);
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

    private class LoadCard extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... urls) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                return Movies.getDataFromServer(urls[0]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final JSONObject finalJsonData) {
            super.onPostExecute(finalJsonData);
            
            JSONArray show = null;
            if (finalJsonData != null) {
                try {
                    show = finalJsonData.getJSONArray("cards");
                } catch (Exception e) {
                    e.printStackTrace();
                    showServerDialog("Server not found! Want to input local IP?");
                }
            } else {
                showServerDialog("Server not found! Want to input local IP?");
            }

            LinearLayout c = findViewById(R.id.linearLayout2);
            if (c == null) return;
            
            c.removeAllViews(); // Clear existing views before adding new ones
            
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
                        if (card == null) continue;

                        View view = LayoutInflater.from(Profiles.this).inflate(R.layout.profiles, null);
                        @SuppressLint({"NewApi", "LocalSuppress"}) int uniqueId = View.generateViewId();
                        view.setId(uniqueId);

                        final TextView tv = view.findViewById(R.id.accountName);
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
                                intent.putExtra("username", tv.getText().toString());
                                intent.putExtra("ip", ip);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                    c.addView(linearLayout2);
                }
            }
            
            View editView = LayoutInflater.from(Profiles.this).inflate(R.layout.profiles, null);
            @SuppressLint({"NewApi", "LocalSuppress"}) int editUniqueId = View.generateViewId();
            editView.setId(editUniqueId);
            TextView editTv = editView.findViewById(R.id.accountName);
            editTv.setText("Edit Profiles");

            editView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    overridePendingTransition(0, R.anim.fade_out);
                    Intent intent = new Intent(Profiles.this, EditProfile.class);
                    if (finalJsonData != null) {
                        intent.putExtra("profileData", finalJsonData.toString());
                    }
                    startActivity(intent);
                }
            });
            c.addView(editView);
            
            TextView loading = findViewById(R.id.loading);
            if (loading != null) loading.setVisibility(View.INVISIBLE);
            View progress = findViewById(R.id.progress);
            if (progress != null) progress.setVisibility(View.GONE);
        }
    }
}
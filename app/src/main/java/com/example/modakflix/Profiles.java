package com.example.modakflix;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        ipInfoFilePath = getApplicationContext().getFilesDir().getAbsolutePath() + "/ipInfo.dat";
        ip = fetchIpDataFromFile(ipInfoFilePath);

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


        LoadCard ld = new LoadCard();
        ld.execute(Movies.get_profiles);
    }
    public void showServerDialog()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Server not found! Want to input local IP?");

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
            JSONObject finalJsonData = jsonData;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    JSONArray show = null;
                    try {
                        show = finalJsonData.getJSONArray("cards");
                    } catch (Exception e) {
                        e.printStackTrace();
                        showServerDialog();
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
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                            TextView loading = findViewById(R.id.loading);
                            loading.setVisibility(View.INVISIBLE);
                            // c.removeView(loading);
                            c.addView(linearLayout2);
                        }
                    }
                    else
                    {
                        showServerDialog();
                    }
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
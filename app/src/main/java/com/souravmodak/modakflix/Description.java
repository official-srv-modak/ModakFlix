package com.souravmodak.modakflix;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.khizar1556.mkvideoplayer.MKPlayerActivity;
import com.souravmodak.modakflix.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE;

public class Description extends AppCompatActivity {

    static  long durFromMx=0, posFromMx=0;
    static JSONObject card;
    static String resumeFlag = null, name = null;
    static String activityResume = "0";
    private static String username = "";
    private static String descriptionStr = "";
    private static String imageUrl = "";
    public static String modakflixPlayerAction = "modakflix_player_current_pos";

    class MKPlayer extends MKPlayerActivity{
        @Override
        public void onBackPressed() {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
        this.overridePendingTransition(R.anim.nothing,R.anim.activity_slide_down);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getAction() != null)
            if (data.getAction().equals("com.mxtech.intent.result.VIEW")) {
                PostProcess p = new PostProcess();
                p.execute(data);
            }
            else if (data.getAction().equals(modakflixPlayerAction)) {
                PostProcess p = new PostProcess();
                p.execute(data);
            }
    }

    private void doPostProcess(Intent data)
    {
        if(data.getAction().equals(modakflixPlayerAction))
        {
            long pos = data.getLongExtra("position", -1);
            long dur = data.getLongExtra("duration", -1);
            String cause = data.getStringExtra("end_by");
            Uri uri = data.getData();
            String name = "";
            durFromMx = dur;
            posFromMx = pos;
            if(pos != -1 && dur != -1)
            {
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
                    {
                        Movies.pingDataServer(Profiles.record_position_path+"?username="+username+"&show="+ URLDecoder.decode(uri.toString(), "UTF-8")+"&pos="+pos+"&duration="+dur+"&cause="+cause+"&name="+name);
                    }
                    else
                        Movies.pingDataServer(Profiles.delete_position_path+"?username="+username+"&show="+name);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Button openWith = findViewById(R.id.playWithBtn);
                long rem = dur - pos;
                rem /= 1000;
                final long[] mins = {rem / 60};
                long hrs = mins[0] /60;
                if(hrs > 0)
                {
                    mins[0] = mins[0] %60;
                    openWith.setText("Resume "+hrs+" hour "+ mins[0] +" min(s) left");
                    runOnUiThread(() -> {
                        Button playBtn = findViewById(R.id.playBtn);
                        playBtn.setText("Resume with internal player : "+hrs+" hour "+ mins[0] +" min(s) left");
                    });
                }
                else
                {
                    openWith.setText("Resume "+ mins[0] +" min(s) left");
                    runOnUiThread(() -> {
                        Button playBtn = findViewById(R.id.playBtn);
                        playBtn.setText("Resume with internal player : "+ mins[0] +" min(s) left");
                    });
                }
            }
        }
        else
        {
            int pos = data.getIntExtra("position", -1);
            int dur = data.getIntExtra("duration", -1);
            String cause = data.getStringExtra("end_by");
            Uri uri = data.getData();
            String name = "";
            durFromMx = dur;
            posFromMx = pos;
            if(pos != -1 && dur != -1)
            {
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
                    {
                        Movies.pingDataServer(Profiles.record_position_path+"?username="+username+"&show="+ URLDecoder.decode(uri.toString(), "UTF-8")+"&pos="+pos+"&duration="+dur+"&cause="+cause+"&name="+name);
                    }
                    else
                        Movies.pingDataServer(Profiles.delete_position_path+"?username="+username+"&show="+name);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Button openWith = findViewById(R.id.playWithBtn);
                int rem = dur - pos;
                rem /= 1000;
                final int[] mins = {rem / 60};
                int hrs = mins[0] /60;
                if(hrs > 0)
                {
                    mins[0] = mins[0] %60;
                    openWith.setText("Resume "+hrs+" hour "+ mins[0] +" min(s) left");
                    runOnUiThread(() -> {
                        Button playBtn = findViewById(R.id.playBtn);
                        playBtn.setText("Resume with internal player : "+hrs+" hour "+ mins[0] +" min(s) left");
                    });
                }
                else
                {
                    openWith.setText("Resume "+ mins[0] +" min(s) left");
                    runOnUiThread(() -> {
                        Button playBtn = findViewById(R.id.playBtn);
                        playBtn.setText("Resume with internal player : "+ mins[0] +" min(s) left");
                    });
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.swipeRefreshDesc);
        if(getIntent().hasExtra("username"))
            username = getIntent().getStringExtra("username");

        pullToRefresh.setOnRefreshListener(() -> pullToRefresh.setRefreshing(false));
        activityResume = "0";
        try {
            card = new JSONObject(getIntent().getStringExtra("description"));
            resumeFlag = getIntent().getStringExtra("resumeFlag");
            name = card.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if(activityResume.equals("0")) {
                BackgroundProcess bp = new BackgroundProcess();
                bp.execute(name, Profiles.get_description, resumeFlag);
            } else {
                BackgroundProcessResume bp = new BackgroundProcessResume();
                bp.execute(name, Profiles.get_description, resumeFlag);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String handleUrl(String URL)
    {
        if(!URL.isEmpty())
        {
            String output = "http:";
            String [] splitList = URL.split(output);
            if(splitList.length>1)
            {
                String temp = splitList[1];
                temp = temp.replace("/", "forwardslash");
                temp = temp.replace(" ", "spacebarspace");
                temp = temp.replace("?", "questionmarkquestion");
                temp = temp.replace("&", "emparsandemparsand");
                temp = temp.replace("=", "equaltoequal");
                try {
                    temp= URLEncoder.encode(temp, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                temp = temp.replace("forwardslash", "/");
                temp = temp.replace("spacebarspace", "%20");
                temp = temp.replace("questionmarkquestion", "?");
                temp = temp.replace("emparsandemparsand", "&");
                temp = temp.replace("equaltoequal", "=");
                output += temp;
                return output;
            }
            else return URL;
        }
        else return URL;
    }

    @SuppressLint("WrongConstant")
    private void processCards(JSONObject card, String username) throws JSONException {
        ImageView imageView = findViewById(R.id.image);
        String album_art_path = card.getString("album_art_path");
        if(!album_art_path.isEmpty()) {
            Glide.with(getApplicationContext()).load(album_art_path).into(imageView);
            imageUrl = album_art_path;
        }
        TextView showName = findViewById(R.id.showName);
        String name = card.getString("name");
        showName.setText(name);
        String desc = card.getString("des");
        if(!desc.isEmpty()) {
            TextView description = findViewById(R.id.summary);
            String summary = desc.split("IMDB")[0].trim();
            String rest = "IMDB "+desc.split("IMDB")[1].trim();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                description.setJustificationMode(android.text.Layout.JUSTIFICATION_MODE_INTER_WORD);
            }
            description.setText(summary);
            TextView restOfThings = findViewById(R.id.rest);
            restOfThings.setText(rest);
            descriptionStr = description.getText().toString()+"\n\n"+restOfThings.getText().toString();
        }

        ImageButton backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> {
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
            overridePendingTransition(R.anim.nothing,R.anim.activity_slide_down);
        });

        if(card.has("position") && card.has("duration")) {
            String videoUrl = handleUrl(card.getString("url"));
            Button openWith = findViewById(R.id.playWithBtn);
            ImageView resetShowBtn = findViewById(R.id.resetShowBtn);
            resetShowBtn.setVisibility(View.VISIBLE);
            int dur = Integer.parseInt(card.getString("duration"));
            int pos = Integer.parseInt(card.getString("position"));
            int rem = dur - pos;
            rem /= 1000;
            int mins = rem/60;
            int hrs = mins/60;
            if(hrs > 0) {
                mins = mins%60;
                openWith.setText("Resume "+hrs+" hour "+mins+" min(s) left");
            } else {
                openWith.setText("Resume "+mins+" min(s) left");
            }
            openWith.setOnClickListener(v -> {
                String appPackageName = "com.mxtech.videoplayer.ad";
                try {
                    resumeFlag = "1";
                    activityResume = "1";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("com.mxtech.videoplayer.ad");
                    intent.setClassName("com.mxtech.videoplayer.ad", "com.mxtech.videoplayer.ad.ActivityScreen");
                    intent.setDataAndType(Uri.parse(videoUrl), "application/x-mpegURL");
                    intent.putExtra("position", pos);
                    intent.putExtra("decode_mode", (byte)2);
                    intent.putExtra("return_result", true);
                    startActivityForResult(intent, 1);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(Description.this, "MX Player not installed.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            });
            Button playBtn = findViewById(R.id.playBtn);
            if(hrs > 0) {
                mins = mins%60;
                playBtn.setText("Resume with internal player : "+hrs+" hour "+mins+" min(s) left");
            } else {
                playBtn.setText("Resume with internal player : "+mins+" min(s) left");
            }
            playBtn.setOnClickListener(v -> startModakFlixPlayer("0", name, videoUrl, descriptionStr, imageUrl, (long) pos, (long) dur));
            resetShowBtn.setOnClickListener(v -> {
                String url = Profiles.reset_show+"?username="+username+"&showname="+name;
                new PingUrl().execute(Description.handleUrl(url));
                Toast.makeText(Description.this, "Show : "+name+" marked as completed", Toast.LENGTH_LONG).show();
                finish();
                overridePendingTransition(R.anim.nothing,R.anim.activity_slide_down);
            });
        } else {
            String videoUrl = handleUrl(card.getString("url"));
            Button playBtn = findViewById(R.id.playBtn);
            playBtn.setOnClickListener(v -> startModakFlixPlayer("0", name, videoUrl, descriptionStr, imageUrl, 0L, 0L));
            Button openWith = findViewById(R.id.playWithBtn);
            openWith.setOnClickListener(v -> {
                String appPackageName = "com.mxtech.videoplayer.ad";
                try {
                    resumeFlag = "1";
                    activityResume = "1";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("com.mxtech.videoplayer.ad");
                    intent.setClassName("com.mxtech.videoplayer.ad", "com.mxtech.videoplayer.ad.ActivityScreen");
                    intent.setDataAndType(Uri.parse(videoUrl), "application/x-mpegURL");
                    intent.putExtra("position", 1000);
                    intent.putExtra("decode_mode", (byte)2);
                    intent.putExtra("return_result", true);
                    startActivityForResult(intent, 1);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(Description.this, "MX Player not installed.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            });
        }
    }

    private void startModakFlixPlayer(String videoId, String videoName, String videoUrl, String description, String imageUrl, long position, long duration) {
        Intent intent = new Intent(Description.this, OnlinePlayerActivity.class);
        intent.putExtra("video_id", videoId);
        intent.putExtra("video_name", videoName);
        intent.putExtra("video_url", videoUrl);
        intent.putExtra("description", description);
        intent.putExtra("image_url", imageUrl);
        intent.putExtra("position", String.valueOf(position));
        intent.putExtra("video_duration", duration);
        startActivityForResult(intent, 1);
    }

    private class BackgroundProcess extends AsyncTask<String, Void, Integer> {
        ProgressDialog progressDialog = new ProgressDialog(Description.this);
        protected Integer doInBackground(String... params) {
            String showname = params[0];
            String url = params[1];
            String resumeFlag = params[2];
            JSONObject cards = Movies.getDataFromServer(handleUrl(url+"?username="+username+"&show="+showname+"&resumeflag="+resumeFlag));
            if(cards != null) {
                try {
                    JSONArray cardArr = cards.getJSONArray("cards");
                    JSONObject card = cardArr.getJSONObject(0);
                    runOnUiThread(() -> {
                        try {
                            processCards(card, username);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            progressDialog.dismiss();
        }
    }

    private class BackgroundProcessResume extends AsyncTask<String, Void, Integer> {
        protected Integer doInBackground(String... params) {
            String showname = params[0];
            String url = params[1];
            String resumeFlag = params[2];
            JSONObject cards = Movies.getDataFromServer(handleUrl(url+"?username="+username+"&show="+showname+"&resumeflag="+resumeFlag));
            if(cards != null) {
                try {
                    JSONArray cardArr = cards.getJSONArray("cards");
                    JSONObject card = cardArr.getJSONObject(0);
                    runOnUiThread(() -> {
                        try {
                            processCards(card, username);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityResume = "1";
    }

    private class PostProcess extends AsyncTask<Intent, Void, Integer> {
        protected Integer doInBackground(Intent... data) {
            android.os.Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND + THREAD_PRIORITY_MORE_FAVORABLE);
            doPostProcess(data[0]);
            return 0;
        }
    }

    private class PingUrl extends AsyncTask<String, Void, Integer> {
        protected Integer doInBackground(String... data) {
            Movies.pingDataServer(data[0]);
            return 0;
        }
    }
}

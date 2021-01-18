package com.example.modakflix;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Movies extends Fragment {

    public static String record_position_path = "http://192.168.0.4/OTTServer/ModakFlix/record_position.php", delete_position_path = "http://192.168.0.4/OTTServer/ModakFlix/delete_from_shows_watched.php";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        /*LoadCard ld = new LoadCard();
        ld.execute("http://192.168.0.4/OTTServer/ModakFlix/get_movies_list_json.php", "http://192.168.0.4/OTTServer/ModakFlix/get_shows_watched.php?username=admin");*/



        return inflater.inflate(R.layout.fragment_movies, container, false);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("WrongConstant")

    public static JSONObject getDataFromServer(String URL)
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
            JSONObject jsonObj = new JSONObject(output);
            return jsonObj;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


    private class LoadCard extends AsyncTask<String, Void, Integer> {
        protected Integer doInBackground(String... urls) {

            JSONObject jsonData = null, resumeData = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                jsonData = getDataFromServer(urls[0]);
                resumeData = getDataFromServer(urls[1]); //**************
            }
            JSONObject finalJsonData = jsonData;
            JSONObject finalresumeData = resumeData;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LinearLayout linearLayout1 = getView().findViewById(R.id.linearLayout1);
                    linearLayout1.removeAllViews();
                    List<Integer> idList = new ArrayList<Integer>();
                    try {
                        if (finalresumeData!=null) // Resume
                        {
                            JSONArray show = finalresumeData.getJSONArray("cards");

                            TextView heading1 = new TextView(getContext());
                            heading1.setText("\nResume watching the shows\n");
                            heading1.setGravity(Gravity.CENTER);
                            heading1.setTextSize(18f);
                            heading1.setTextColor(Color.WHITE);
                            linearLayout1.addView(heading1);
                            LinearLayout linearLayoutH = new LinearLayout(getActivity());

                            LinearLayout.LayoutParams paramsH = new LinearLayout.LayoutParams
                                    (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            linearLayoutH.setLayoutParams(paramsH);
                            linearLayoutH.setWeightSum(2f);
                            linearLayoutH.setOrientation(LinearLayout.HORIZONTAL);
                            linearLayoutH.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

                            HorizontalScrollView scrollViewH = new HorizontalScrollView(getActivity());
                            scrollViewH.addView(linearLayoutH);
                            for(int i = 0; i < show.length(); i++)
                            {

                                JSONObject card = show.getJSONObject(i);


                                View view = LayoutInflater.from(getContext()).inflate(R.layout.resume_elements, null);
                                @SuppressLint({"NewApi", "LocalSuppress"}) int uniqueId = View.generateViewId();
                                view.setId(uniqueId);
                                idList.add(uniqueId);
                                ProgressBar progressBarH = (ProgressBar) view.findViewById(R.id.resumeBar);
                                double pos = Double.parseDouble(card.getString("position")), dur = Double.parseDouble(card.getString("duration"));
                                double rem = ((dur - pos)/dur)*100;
                                int remaining = (int) (100 - rem);
                                progressBarH.setProgress(remaining);
                                ImageView imageView = (ImageView) view.findViewById(R.id.image);
                                String album_art_path = card.getString("album_art_path");
                                if(!album_art_path.isEmpty())
                                    Glide.with(getContext()).load(album_art_path).into(imageView);

                                view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getContext(), Description.class);
                                        intent.putExtra("description", card.toString());
                                        intent.putExtra("resumeFlag", "1");
                                        getActivity().startActivity(intent);
                                    }
                                });
                                linearLayoutH.addView(view);
                            }

                            linearLayout1.addView(scrollViewH);
                        }

                        // Normal

                        JSONArray cards = finalJsonData.getJSONArray("cards");
                        TextView heading2 = new TextView(getContext());
                        heading2.setText("\nMovies from your collection\n");
                        heading2.setTextColor(Color.WHITE);
                        heading2.setGravity(Gravity.CENTER);
                        heading2.setTextSize(18f);
                        linearLayout1.addView(heading2);
                        for(int i = 0; i < cards.length(); i++)
                        {
                            LinearLayout linearLayout2 = new LinearLayout(getActivity());

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                                    (LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            linearLayout2.setLayoutParams(params);
                            linearLayout2.setWeightSum(2f);
                            linearLayout2.setOrientation(LinearLayout.HORIZONTAL);
                            linearLayout2.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

                            for(int j = 0; j<2; j++)
                            {
                                JSONObject card = cards.getJSONObject(i+j);
                                View view = LayoutInflater.from(getContext()).inflate(R.layout.front_page_elemets, null);
                                @SuppressLint({"NewApi", "LocalSuppress"}) int uniqueId = View.generateViewId();
                                view.setId(uniqueId);
                                idList.add(uniqueId);

                                /*ProgressBar progressBarH = (ProgressBar) getActivity().findViewById(R.id.progressBar);
                                progressBarH.setVisibility(View.INVISIBLE);*/
                                ImageView imageView = (ImageView) view.findViewById(R.id.image);
                                String album_art_path = card.getString("album_art_path");
                                if(!album_art_path.isEmpty())
                                    Glide.with(getContext()).load(album_art_path).into(imageView);

                                view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getContext(), Description.class);
                                        intent.putExtra("description", card.toString());
                                        intent.putExtra("resumeFlag", "0");
                                        Movies.this.startActivityForResult(intent, 1);
                                    }
                                });
                                linearLayout2.addView(view);

                            }
                            i++;

                            linearLayout1.addView(linearLayout2);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            return 0;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

    }
    @Override
    public void onResume() {
        super.onResume();
        LoadCard ld = new LoadCard();
        ld.execute("http://192.168.0.4/OTTServer/ModakFlix/get_movies_list_json.php", "http://192.168.0.4/OTTServer/ModakFlix/get_shows_watched.php?username=admin");
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


}
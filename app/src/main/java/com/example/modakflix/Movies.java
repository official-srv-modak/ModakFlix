package com.example.modakflix;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Movies extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        LoadCard ld = new LoadCard();
        ld.execute("http://192.168.0.7/ModakFlix/get_movies_list_json.php");



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

            JSONObject jsonData = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                jsonData = getDataFromServer(urls[0]);
            }
            JSONObject finalJsonData = jsonData;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LinearLayout linearLayout1 = getView().findViewById(R.id.linearLayout1);
                    List<Integer> idList = new ArrayList<Integer>();
                    try {
                        JSONArray cards = finalJsonData.getJSONArray("cards");
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

                                ImageView imageView = (ImageView) view.findViewById(R.id.image);
                                String album_art_path = card.getString("album_art_path");
                                if(!album_art_path.isEmpty())
                                    Glide.with(getContext()).load(album_art_path).into(imageView);

                                view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getContext(), Description.class);
                                        intent.putExtra("description", card.toString());
                                        getActivity().startActivity(intent);
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
}
package com.example.modakflix;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.example.modakflix.Movies.getDataFromServer;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ImageButton backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        EditText searchTextBox = findViewById(R.id.searchTextBox);
        searchTextBox.requestFocus();
        searchTextBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    if(!searchTextBox.getText().toString().isEmpty())
                    {
                        LoadCard ld = new LoadCard();
                        ld.execute(Movies.search_shows+"?query="+searchTextBox.getText());
                    }
                    else
                    {
                        LinearLayout linearLayout1 = SearchActivity.this.findViewById(R.id.linearLayout1);
                        linearLayout1.removeAllViews();
                    }

                }

            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });
    }

    private class LoadCard extends AsyncTask<String, Void, Integer> {
        protected Integer doInBackground(String... urls) {

            JSONObject result = null, resumeData = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                result = getDataFromServer(urls[0]);
            }
            JSONObject finalResult = result;
            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    // Stuff that updates the UI
                    TextView resultV = findViewById(R.id.result);

                    if(finalResult != null) {   //json object of search is not null
                        /*resultV.setText("");
                        try {
                            resultV.setText(finalResult.getString("cards"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }*/

                        LinearLayout linearLayout1 = SearchActivity.this.findViewById(R.id.linearLayout1);
                        linearLayout1.removeAllViews();
                        try {
                            JSONArray show = finalResult.getJSONArray("cards");
                            List<Integer> idList = new ArrayList<Integer>();
                            for(int i = 0; i < show.length(); i++)
                            {
                                JSONObject card = show.getJSONObject(i);
                                View view = LayoutInflater.from(SearchActivity.this).inflate(R.layout.search_result_elements, null);
                                @SuppressLint({"NewApi", "LocalSuppress"}) int uniqueId = View.generateViewId();
                                view.setId(uniqueId);
                                idList.add(uniqueId);

                                ImageView imageView = (ImageView) view.findViewById(R.id.image);
                                String album_art_path = card.getString("album_art_path");
                                if(!album_art_path.isEmpty())
                                    Glide.with(SearchActivity.this).load(album_art_path).into(imageView);

                                TextView tv = (TextView) view.findViewById(R.id.showNameSearch);
                                CharSequence name = card.getString("name");
                                if(!name.toString().isEmpty())
                                    tv.setText(name);

                                view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(SearchActivity.this, Description.class);
                                        intent.putExtra("description", card.toString());
                                        //intent.putExtra("url", get_shows_watched_path);
                                        int pos = 0;
                                        String resumeFlag = "0";
                                        if(card.has("position"))
                                                resumeFlag = "1";
                                        else
                                                resumeFlag = "0";
                                        intent.putExtra("resumeFlag", resumeFlag);
                                        SearchActivity.this.startActivityForResult(intent, 1);
                                    }
                                });


                                linearLayout1.addView(view);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    else
                    {
                        // show nothing found
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
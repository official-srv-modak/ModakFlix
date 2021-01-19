package com.example.modakflix;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.inputmethodservice.Keyboard;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
                        TextView resultV = findViewById(R.id.result);
                        resultV.setText("");
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

                    if(finalResult != null) {
                        resultV.setText("");
                        try {
                            resultV.setText(finalResult.getString("cards"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
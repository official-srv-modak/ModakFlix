package com.souravmodak.modakflix;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.souravmodak.modakflix.ui.main.SectionsPagerAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.souravmodak.modakflix.R;

import static com.souravmodak.modakflix.Profiles.ipInfoFilePath;

public class MainActivity extends AppCompatActivity {

    public static String username = "admin";
    public static String ip = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        ImageView menu = findViewById(R.id.menu);
        username = getIntent().getStringExtra("username");
        ip = getIntent().getStringExtra("ip");

        DrawerLayout drawerLayout = findViewById(R.id.drawerlayout);
        menu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        ImageButton searchBtn = findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(v -> {
            Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
            searchIntent.putExtra("username", username);
            startActivity(searchIntent);
        });

        NavigationView navigationView = findViewById(R.id.nav_bar);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.profiles) {
                Intent intent = new Intent(MainActivity.this, Profiles.class);
                intent.putExtra("startFlag", "1");
                startActivity(intent);
                finish();
            } else if (id == R.id.resetProfile) {
                resetProfile("Do you really want to reset all you watching history?");
            } else if (id == R.id.contactUs) {
                showContactUs("Developer - Sourav Modak\nContact Number - +91 9500166574\nE-Mail - official.srv.modak@gmail.com.");
            } else if (id == R.id.resetIp) {
                showServerDialogNoExit(getString(R.string.reset_ip_message));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
        
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.profileName);
        navUsername.setText(username);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawerlayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void showContactUs(String Message) {
        new AlertDialog.Builder(this)
                .setMessage(Message)
                .setPositiveButton("OK", null)
                .show();
    }

    public void resetProfile(String Message) {
        new AlertDialog.Builder(this)
                .setMessage(Message)
                .setPositiveButton("Yes", (dialog, which) -> new ResetProfile().execute(Profiles.reset_profile))
                .setNegativeButton("No", null)
                .show();
    }

    private class ResetProfile extends AsyncTask<String, Void, Integer> {
        private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... url) {
            Movies.pingDataServer(Description.handleUrl(url[0]));
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            progressDialog.dismiss();
            recreate();
        }
    }

    public void showServerDialogNoExit(String Message) {
        new AlertDialog.Builder(this)
                .setMessage(Message)
                .setPositiveButton("Yes", (dialog, which) -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Enter Server's Local IP Address");
                    final EditText input = new EditText(MainActivity.this);
                    input.setHint("IP Address");
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);
                    builder.setPositiveButton("OK", (dialog1, which1) -> {
                        Profiles.writeIpData(ipInfoFilePath, input.getText().toString().trim());
                        MainActivity.ip = input.getText().toString().trim();
                        startActivity(new Intent(MainActivity.this, SplashScreen.class));
                        finish();
                    });
                    builder.show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
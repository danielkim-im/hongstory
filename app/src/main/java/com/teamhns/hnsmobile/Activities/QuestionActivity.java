package com.teamhns.hnsmobile.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.teamhns.hnsmobile.R;

public class QuestionActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        getSupportActionBar().hide();

        bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.nav_forum);

        //BottomNavigationView Controller [KEEP AT BOTTOM OF THE INIT]
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        startActivity(new Intent(QuestionActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
//                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        overridePendingTransition(0, 0);
                        return false;
                    case R.id.nav_explore:
                        startActivity(new Intent(QuestionActivity.this, ExploreActivity.class));
//                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        overridePendingTransition(0, 0);
                        return false;
                    case R.id.nav_forum:
                        //
                        return true;
                    case R.id.nav_profile:
                        startActivity(new Intent(QuestionActivity.this, ProfileActivity.class));
//                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        overridePendingTransition(0, 0);
                        return false;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        overridePendingTransition(0,0);
    }
}
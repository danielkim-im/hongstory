package com.teamhns.hnsmobile.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.teamhns.hnsmobile.R;

public class ProfileImageActivity extends AppCompatActivity {

    ImageView img;
    Button closeBtn;
    String imgUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int nightModeFlags =
                ProfileImageActivity.this.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                setTheme(R.style.darkTheme);
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                setTheme(R.style.AppTheme);
                break;

            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                setTheme(R.style.AppTheme);
                break;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_image);
        getSupportActionBar().hide();

        img = findViewById(R.id.profile_image_img);
        closeBtn = findViewById(R.id.profileimage_closeBtn);

        Intent intent = getIntent();
        imgUrl = intent.getStringExtra(Intent.EXTRA_TEXT);

        Glide.with(ProfileImageActivity.this)
                .load(imgUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(img);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
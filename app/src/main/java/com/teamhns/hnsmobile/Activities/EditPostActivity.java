package com.teamhns.hnsmobile.Activities;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.teamhns.hnsmobile.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class EditPostActivity extends AppCompatActivity {

    ImageView uploadBtn, addTagImg;
    Button closeBtn;
    EditText hashtagEdt, descriptionEdt;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int nightModeFlags =
                EditPostActivity.this.getResources().getConfiguration().uiMode &
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
        setContentView(R.layout.activity_edit_post);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getInstance().getCurrentUser();
        closeBtn = findViewById(R.id.editpost_close_btn);
        addTagImg = findViewById(R.id.add_tag_img);
        hashtagEdt = findViewById(R.id.tag_hashtag);
        descriptionEdt = findViewById(R.id.tag_description);
        uploadBtn = findViewById(R.id.upload_tag_btn);
        progressBar = findViewById(R.id.add_tag_progressbar);

        //Get Tag Data
        final String postKey = getIntent().getExtras().getString("postKey");
        final String description = getIntent().getExtras().getString("description");
        final String picture = getIntent().getExtras().getString("picture");
        final String hashtag = getIntent().getExtras().getString("hashtag");
        final String contentType = getIntent().getExtras().getString("contentType");
        String timeStamp = timestampToString(getIntent().getExtras().getLong("timeStamp"));
        final String userId = getIntent().getExtras().getString("userId");
        final String userPhoto = getIntent().getExtras().getString("userPhoto");
        final String userName = getIntent().getExtras().getString("userName");

        addTagImg.setEnabled(false);

        if (contentType.equals("image") && !picture.isEmpty()) { //url.isEmpty()
            Picasso.get()
                    .load(picture)
                    .placeholder(R.drawable.loading_placeholder)
                    .error(R.drawable.loading_placeholder)
                    .config(Bitmap.Config.RGB_565)
                    .into(addTagImg);
        }else{

        }

        hashtagEdt.setText(hashtag);
        descriptionEdt.setText(description);

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadBtn.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                hashtagEdt.setEnabled(false);
                descriptionEdt.setEnabled(false);
                addTagImg.setEnabled(false);
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("description", descriptionEdt.getText().toString());
                hashMap.put("hashtag", hashtagEdt.getText().toString());

                FirebaseDatabase.getInstance().getReference("Posts")
                        .child(postKey).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast.makeText(EditTagActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                        uploadBtn.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        hashtagEdt.setEnabled(true);
                        descriptionEdt.setEnabled(true);
                        addTagImg.setEnabled(true);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }
                });
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private String timestampToString(long time) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("MM-dd-yyyy", calendar).toString();
        return date;
    }
}
package com.teamhns.hnsmobile.Activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.teamhns.hnsmobile.Adapter.ProfilePostAdapter;
import com.teamhns.hnsmobile.Model.Post;
import com.teamhns.hnsmobile.Model.User;
import com.teamhns.hnsmobile.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kr.co.prnd.readmore.ReadMoreTextView;

public class ProfileActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    RecyclerView recyclerView;
    CircleImageView profileImg, dialog_userprofile_profile;
    TextView userName, toolbarName, numofTags, dialog_userprofile_username, dialog_userprofile_useremail;
    ReadMoreTextView userBio;
    ImageView menuBtn, verifiedIcon;
    Button dialog_copyUID_btn, editProfileBtn, addTagBtn;
    LinearLayout numofTagsLine1;
    AppBarLayout appBarLayout;
    ProgressBar progressBar;
    ProfilePostAdapter profilePostAdapter;
    List tagList;
    public static Boolean isConnected;
    FirebaseAuth mAuth;
    FirebaseRemoteConfig firebaseRemoteConfig;
    String pImgURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int nightModeFlags =
                ProfileActivity.this.getResources().getConfiguration().uiMode &
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
        setContentView(R.layout.activity_profile);
        getSupportActionBar().hide();

        bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        mAuth = FirebaseAuth.getInstance();
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        profileImg = findViewById(R.id.profile_userimg);
        userName = findViewById(R.id.profile_username);
        verifiedIcon = findViewById(R.id.verifiedIcon);
        userBio = findViewById(R.id.profile_userbio);
        menuBtn = findViewById(R.id.profile_menu_btn);
        toolbarName = findViewById(R.id.toolbar_username_txt);
        numofTags = findViewById(R.id.numOfTagsTxt);
        numofTagsLine1 = findViewById(R.id.numoftagsLin);
        progressBar = findViewById(R.id.profile_progressbar);
        addTagBtn = findViewById(R.id.profile_add_tag);
        appBarLayout = findViewById(R.id.profile_appbarlayout);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        recyclerView = findViewById(R.id.profile_tag_recyclerview);
        recyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        tagList = new ArrayList<>();
        profilePostAdapter = new ProfilePostAdapter(ProfileActivity.this, tagList);
        recyclerView.setAdapter(profilePostAdapter);

        //BottomNavigationView Controller [KEEP AT BOTTOM OF THE INIT]
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        startActivity(new Intent(ProfileActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
//                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        overridePendingTransition(0, 0);
                        return false;
                    case R.id.nav_explore:
                        startActivity(new Intent(ProfileActivity.this, ExploreActivity.class));
//                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        overridePendingTransition(0, 0);
                        return false;
                    case R.id.nav_forum:
                        startActivity(new Intent(ProfileActivity.this, QuestionActivity.class));
//                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        overridePendingTransition(0, 0);
                        return false;
                    case R.id.nav_profile:
                        recyclerView.smoothScrollToPosition(0);
                        return true;
                }
                return false;
            }
        });

        publisherInfo(profileImg, userName, mAuth.getCurrentUser().getUid());

/*        if(mAuth.getCurrentUser().getUid().equals(getString(R.string.yolastudioUID))){
            verifiedIcon.setVisibility(View.VISIBLE);
        }else{
            verifiedIcon.setVisibility(View.GONE);
        }*/

/*        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });*/

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            }
        });

        toolbarName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ProfileActivity.this);
                bottomSheetDialog.setContentView(R.layout.user_bottom_sheet_dialog1);
                bottomSheetDialog.setCanceledOnTouchOutside(true);

                dialog_userprofile_profile = bottomSheetDialog.findViewById(R.id.uprofile_dialog_profile);
                dialog_userprofile_username = bottomSheetDialog.findViewById(R.id.uprofile_dialog_username);
                dialog_userprofile_useremail = bottomSheetDialog.findViewById(R.id.uprofile_dialog_useremail);
                dialog_copyUID_btn = bottomSheetDialog.findViewById(R.id.uprofile_dialog_copyUIDBtn);

                publisherInfo(dialog_userprofile_profile, dialog_userprofile_username, mAuth.getCurrentUser().getUid());

                dialog_userprofile_useremail.setText(mAuth.getCurrentUser().getEmail());

                dialog_copyUID_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("UID", mAuth.getCurrentUser().getUid());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(ProfileActivity.this, "사용자 ID 복사 완료", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                    }
                });

                bottomSheetDialog.show();
            }
        });

        addTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, CreatePostActivity.class));
            }
        });

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ProfileImageActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, pImgURL);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) ProfileActivity.this, profileImg, ViewCompat.getTransitionName(profileImg));
                startActivity(intent, optionsCompat.toBundle());
            }
        });

        loadProfileTags();
    }

    @Override
    public void onStart() {
        super.onStart();

        GetServerStatus();
        checkUserStatus();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        overridePendingTransition(0, 0);
    }

    private void publisherInfo(final ImageView image_profile, final TextView username, final String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Activity activity = ProfileActivity.this;
                if (activity.isFinishing())
                    return;

                User user = snapshot.getValue(User.class);
                if (snapshot.exists()){
                    Glide.with(ProfileActivity.this)
                            .load(user.getPimg())
                            .apply(new RequestOptions().override(720, 720))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(image_profile);
                    username.setText(user.getName());
                    if (user.getBio().isEmpty()) {
                        userBio.setVisibility(View.GONE);
                    } else {
                        userBio.setVisibility(View.VISIBLE);
                        userBio.setText(user.getBio());
                    }
                    toolbarName.setText("@" + user.getUid());

                    pImgURL = user.getPimg();
                }else {
                    Glide.with(ProfileActivity.this)
                            .load(R.drawable.commentbg)
                            .apply(new RequestOptions().override(720, 720))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(profileImg);
                    userName.setText("존재하지 않는 사용자");
                    userBio.setVisibility(View.GONE);
                    toolbarName.setText("@error");
                    pImgURL = "";
                    //Toast.makeText(mContext, "snapshot. userinfo does not exist", Toast.LENGTH_SHORT).show();
                }

                progressBar.setVisibility(View.GONE);
                appBarLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void GetServerStatus() {
        firebaseRemoteConfig.fetch(0)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            firebaseRemoteConfig.activate();
                            if (firebaseRemoteConfig.getBoolean("server_under_maintenance") == true && mAuth.getCurrentUser().getEmail().equals("11202@hongchun.hs.kr")) {
                                // Allow access
                            } else if (firebaseRemoteConfig.getBoolean("server_under_maintenance") == true && !mAuth.getCurrentUser().getEmail().equals("11202@hongchun.hs.kr")) {
                                startActivity(new Intent(ProfileActivity.this, ServerDownActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            } else if (firebaseRemoteConfig.getBoolean("server_under_maintenance") == false) {
                                // Server is not down
                            }
                        } else {
                            //Firebase Remote Config is not responding
                        }
                    }
                });
    }

    private void loadProfileTags() {
        tagList.clear();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Activity activity = ProfileActivity.this;
                if (activity.isFinishing())
                    return;

                if (snapshot.hasChildren()) {
                    for (DataSnapshot tagsnap : snapshot.getChildren()) {
                        Post post = tagsnap.getValue(Post.class);
                        if (post.getUserId().equals(mAuth.getCurrentUser().getUid())) {
                            tagList.add(post);
                        }
                    }

                    profilePostAdapter = new ProfilePostAdapter(ProfileActivity.this, tagList);
                    profilePostAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(profilePostAdapter);
                    numofTags.setText("" + tagList.size());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showMessage(error.getMessage());
            }
        });
    }

    private void checkUserStatus() {

        checkConnection();

        if (isConnected) {
            mAuth.getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (mAuth.getCurrentUser() == null) {
                        Toast.makeText(ProfileActivity.this, "사용자 인증에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ProfileActivity.this, AuthActivity.class));
                        finish();
                    } else {
                        // User is not null
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ProfileActivity.this, AuthActivity.class));
                    finish();
                }
            });
        } else {
            // Do not check user status if not connected
        }
    }

    public void checkConnection() {
        ConnectivityManager manager = (ConnectivityManager)
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();

        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                isConnected = true;
            }

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                isConnected = true;
            }
        } else {
            isConnected = false;
        }
    }


    private void showMessage(String message) {
        Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void showLongMessage(String message) {
        Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
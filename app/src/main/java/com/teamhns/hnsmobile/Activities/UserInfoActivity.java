package com.teamhns.hnsmobile.Activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.DialogFragment;
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
import com.teamhns.hnsmobile.Fragments.ReportSingleChoiceDialogFragment;
import com.teamhns.hnsmobile.Model.Post;
import com.teamhns.hnsmobile.Model.ReportPost;
import com.teamhns.hnsmobile.Model.User;
import com.teamhns.hnsmobile.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kr.co.prnd.readmore.ReadMoreTextView;

public class UserInfoActivity extends AppCompatActivity implements ReportSingleChoiceDialogFragment.SingleChoiceListener{

    RecyclerView recyclerView;
    CircleImageView profileImg;
    TextView userName, toolbarName, numofTags;
    ReadMoreTextView userBio;
    ImageView menuBtn, verifiedIcon, closeDialogBtn;
    Button backBtn;
    ConstraintLayout reportCons;
    ProgressBar progressBar;
    AppBarLayout appBarLayout;
    BottomSheetDialog bottomSheetDialog;
    ProfilePostAdapter profilePostAdapter;
    List tagList;
    public static Boolean isConnected;
    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseRemoteConfig firebaseRemoteConfig;
    String userId, pImgURL;
    Boolean nightmode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int nightModeFlags =
                UserInfoActivity.this.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                setTheme(R.style.darkTheme);
                nightmode = true;
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                setTheme(R.style.AppTheme);
                nightmode = false;
                break;

            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                setTheme(R.style.AppTheme);
                nightmode = false;
                break;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        profileImg = findViewById(R.id.profile_userimg);
        userName = findViewById(R.id.profile_username);
        verifiedIcon = findViewById(R.id.verifiedIcon);
        userBio = findViewById(R.id.profile_userbio);
        menuBtn = findViewById(R.id.profile_menu_btn);
        toolbarName = findViewById(R.id.toolbar_username_txt);
        backBtn = findViewById(R.id.backBtn8);
        numofTags = findViewById(R.id.numOfTagsTxt);
        appBarLayout = findViewById(R.id.userinfo_appbar);
        progressBar = findViewById(R.id.userinfo_progressbar);
        recyclerView = findViewById(R.id.profile_tag_recyclerview);
        recyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        tagList = new ArrayList<>();
        profilePostAdapter = new ProfilePostAdapter(UserInfoActivity.this, tagList);
        recyclerView.setAdapter(profilePostAdapter);

        Intent intent = getIntent();
        userId = intent.getStringExtra(Intent.EXTRA_TEXT);

        publisherInfo(userId);

/*        if(userId.equals(getString(R.string.yolastudioUID))){
            verifiedIcon.setVisibility(View.VISIBLE);
        }else{
            verifiedIcon.setVisibility(View.GONE);
        }*/

        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog = new BottomSheetDialog(UserInfoActivity.this);
                bottomSheetDialog.setContentView(R.layout.user_report_bottom_sheet_dialog1);
                bottomSheetDialog.setCanceledOnTouchOutside(true);
                bottomSheetDialog.show();

                closeDialogBtn = bottomSheetDialog.findViewById(R.id.closeDialogBtn);
                reportCons = bottomSheetDialog.findViewById(R.id.reportCons);

                reportCons.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogFragment singleChoiceDiailog = new ReportSingleChoiceDialogFragment();
                        singleChoiceDiailog.setCancelable(true);
                        singleChoiceDiailog.show(getSupportFragmentManager(), "Single Choice Dialog");
                    }
                });

                closeDialogBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });
            }
        });

        toolbarName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("UID", mAuth.getCurrentUser().getUid());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(UserInfoActivity.this, "사용자 ID 복사 완료", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserInfoActivity.this, ProfileImageActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, pImgURL);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) UserInfoActivity.this, profileImg, ViewCompat.getTransitionName(profileImg));
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
    public void onPositiveButtonClicked(String[] list, int position) {
        ReportPost reportPost = new ReportPost(
                userId,
                mAuth.getCurrentUser().getUid(),
                list[position]
        );
        reportTag(reportPost);
    }

    @Override
    public void onNegativeButtonClicked() {
        bottomSheetDialog.dismiss();
    }

    private void reportTag(ReportPost reportPost) {
        DatabaseReference myRef = firebaseDatabase.getReference("ReportedUser").child(userId).push();

        //get post unique ID and update post key
        String key = myRef.getKey();
        reportPost.setReportKey(key);

        // add post data to firebase database
        myRef.setValue(reportPost).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                bottomSheetDialog.dismiss();
                Toast.makeText(UserInfoActivity.this, "사용자 신고 완료.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void publisherInfo(final String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Activity activity = UserInfoActivity.this;
                if (activity.isFinishing())
                    return;

                User user = snapshot.getValue(User.class);

                if (snapshot.exists()){
                    Glide.with(UserInfoActivity.this)
                            .load(user.getPimg())
                            .apply(new RequestOptions().override(720, 720))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(profileImg);
                    userName.setText(user.getName());
                    if(user.getBio().isEmpty()){
                        userBio.setVisibility(View.GONE);
                    }else{
                        userBio.setVisibility(View.VISIBLE);
                        userBio.setText(user.getBio());
                    }
                    toolbarName.setText("@" + user.getUid());

                    pImgURL = user.getPimg();
                }else {
                    Glide.with(UserInfoActivity.this)
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
                                startActivity(new Intent(UserInfoActivity.this, ServerDownActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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

        Query query;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Activity activity = UserInfoActivity.this;
                if (activity.isFinishing())
                    return;

                if (snapshot.hasChildren()) {
                    for (DataSnapshot tagsnap : snapshot.getChildren()) {
                        Post post = tagsnap.getValue(Post.class);
                        if (post.getUserId().equals(userId)&&post.getPrivacy().equals("public")) {
                            tagList.add(post);
                        }
                    }

                    profilePostAdapter = new ProfilePostAdapter(UserInfoActivity.this, tagList);
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
                        Toast.makeText(UserInfoActivity.this, "사용자 인증에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(UserInfoActivity.this, AuthActivity.class));
                        finish();
                    } else {
                        // User is not null
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UserInfoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UserInfoActivity.this, AuthActivity.class));
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
        Toast.makeText(UserInfoActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void showLongMessage(String message) {
        Toast.makeText(UserInfoActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
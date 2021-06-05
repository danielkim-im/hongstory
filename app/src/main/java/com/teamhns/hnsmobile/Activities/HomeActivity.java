package com.teamhns.hnsmobile.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.icu.text.CaseMap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.squareup.picasso.Picasso;
import com.teamhns.hnsmobile.Adapter.HomeHorizPostAdapter;
import com.teamhns.hnsmobile.Adapter.MainPostAdapter;
import com.teamhns.hnsmobile.Model.Post;
import com.teamhns.hnsmobile.Model.User;
import com.teamhns.hnsmobile.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    NestedScrollView nestedScrollView;
    RecyclerView postRv, horizPostRv;
    CircleImageView toolbarImg, homeProfileImg;
    Button addTagBtn, gotoMenuBtn;
    TextView username, searchTxt, seeMorePost;
    SwipeRefreshLayout swipeRefreshLayout;
    ProgressBar progressBar;
    ImageView topBannerImg;
    String topBannerClickUrl;
    ConstraintLayout bannerCons;

    HomeHorizPostAdapter homeHorizPostAdapter;
    MainPostAdapter mainPostAdapter;
    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference postRef;
    FirebaseRemoteConfig firebaseRemoteConfig;
    List mainPostList, horizRecPostList;
    Boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int nightModeFlags =
                HomeActivity.this.getResources().getConfiguration().uiMode &
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
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();

        bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        nestedScrollView = findViewById(R.id.homeNested);
        toolbarImg = findViewById(R.id.home_toolbar_icon);
        topBannerImg = findViewById(R.id.topBannerImg);
        username = findViewById(R.id.nameTitle);
        homeProfileImg = findViewById(R.id.home_profile);
        searchTxt = findViewById(R.id.home_search_post_edtx);
        bannerCons = findViewById(R.id.topRecCons);
        addTagBtn = findViewById(R.id.home_add_tag);
        seeMorePost = findViewById(R.id.seePrevPostsBtn);
        gotoMenuBtn = findViewById(R.id.home_goto_menu);
        progressBar = findViewById(R.id.home_progressbar);
        swipeRefreshLayout = findViewById(R.id.home_swipe);
        horizPostRv = findViewById(R.id.recPostRV);
        postRv = findViewById(R.id.home_community_rv);
        horizPostRv.setHasFixedSize(true);
        postRv.setHasFixedSize(true);
        horizPostRv.setLayoutManager(new LinearLayoutManager(HomeActivity.this, RecyclerView.HORIZONTAL, false));
        postRv.setLayoutManager(new LinearLayoutManager(HomeActivity.this, RecyclerView.VERTICAL, false));

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        postRef = firebaseDatabase.getInstance().getReference("Posts");
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        //BottomNavigationView Controller [KEEP AT BOTTOM OF THE INIT]
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        nestedScrollView.smoothScrollTo(0, 0);
                        horizPostRv.smoothScrollToPosition(0);
                        return true;
                    case R.id.nav_explore:
                        startActivity(new Intent(HomeActivity.this, ExploreActivity.class));
//                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        overridePendingTransition(0, 0);
                        return false;
                    case R.id.nav_forum:
                        startActivity(new Intent(HomeActivity.this, QuestionActivity.class));
//                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        overridePendingTransition(0, 0);
                        return false;
                    case R.id.nav_profile:
                        startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                        //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        overridePendingTransition(0, 0);
                        return false;
                }
                return false;
            }
        });

        //swipeRefreshLayout.setRefreshing(true);

        toolbarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nestedScrollView.smoothScrollTo(0, 0);
            }
        });

        addTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, CreatePostActivity.class));
            }
        });

        gotoMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, MenuActivity.class));
            }
        });

        topBannerImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!topBannerClickUrl.isEmpty()){
                    Intent webviewActivity = new Intent(HomeActivity.this, WebViewActivity.class);
                    webviewActivity.putExtra("weburl", topBannerClickUrl);
                    webviewActivity.putExtra("webtitle", "바로가기");
                    startActivity(webviewActivity);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        homeProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                overridePendingTransition(0, 0);
            }
        });

        searchTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, SearchPostActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        seeMorePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ExploreActivity.class));
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                GetServerStatus();
                GetTopBanner();
                GetHorizRecPost();
                GetPosts();
                CheckUserExistanceInDatabase();
            }
        });

        publisherInfo(mAuth.getCurrentUser().getUid());
        GetTopBanner();
        GetHorizRecPost();
        GetPosts();
        CheckUserExistanceInDatabase();
    }

    private void publisherInfo(final String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Activity activity = HomeActivity.this;
                if (activity.isFinishing())
                    return;

                User user = snapshot.getValue(User.class);
                if (snapshot.exists()){
                    Glide.with(HomeActivity.this)
                            .load(user.getPimg())
                            .apply(new RequestOptions().override(720, 720))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(homeProfileImg);
                    username.setText(" "+user.getName()+"님");
                }else {
                    Glide.with(HomeActivity.this)
                            .load(R.drawable.commentbg)
                            .apply(new RequestOptions().override(720, 720))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(homeProfileImg);
                    username.setText(" 사용자님");
                    //Toast.makeText(mContext, "snapshot. userinfo does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void CheckUserExistanceInDatabase() {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //No Problem
                }else{
                    DatabaseReference databaseReference;
                    databaseReference = firebaseDatabase.getReference().child("Users").child(mAuth.getCurrentUser().getUid());

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("uid", mAuth.getCurrentUser().getUid());
                    hashMap.put("name", mAuth.getCurrentUser().getDisplayName());
                    hashMap.put("bio", "");
                    hashMap.put("pimg", mAuth.getCurrentUser().getPhotoUrl().toString());

                    databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(HomeActivity.this, "사용자 정보가 데이터베이스에 저장되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(HomeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(HomeActivity.this, AuthActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        GetServerStatus();
        checkUserStatus();
    }

    private void GetTopBanner() {
        firebaseRemoteConfig.fetch(0)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            firebaseRemoteConfig.activate();
                            if (!firebaseRemoteConfig.getString("home_banner_img").isEmpty()){
                                bannerCons.setVisibility(View.VISIBLE);
                                Glide.with(HomeActivity.this)
                                        .load(firebaseRemoteConfig.getString("home_banner_img"))
                                        .placeholder(R.drawable.loading_placeholder)
                                        .error(R.drawable.loading_placeholder)
                                        .into(topBannerImg);
                                topBannerClickUrl = firebaseRemoteConfig.getString("home_banner_onclick");
                            }else {
                                bannerCons.setVisibility(View.GONE);
                            }
                        }else {
                            //Firebase Remote Config is not responding
                            bannerCons.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void GetHorizRecPost() {
        horizRecPostList = new ArrayList<>();

        Query query;
        query = postRef.limitToLast(10);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Activity activity = HomeActivity.this;
                if (activity.isFinishing())
                    return;
                for (DataSnapshot postsnap : snapshot.getChildren()) {
                    Post post = postsnap.getValue(Post.class);
                    horizRecPostList.add(post);
                }
                homeHorizPostAdapter = new HomeHorizPostAdapter(HomeActivity.this, horizRecPostList);
                homeHorizPostAdapter.notifyDataSetChanged();
                horizPostRv.setAdapter(homeHorizPostAdapter);
                horizPostRv.setNestedScrollingEnabled(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void GetPosts() {
        mainPostList = new ArrayList<>();

        Query query;
        query = postRef.limitToLast(500);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Activity activity = HomeActivity.this;
                if (activity.isFinishing())
                    return;
                for (DataSnapshot postsnap : snapshot.getChildren()) {
                    Post post = postsnap.getValue(Post.class);
                    mainPostList.add(post);
                }

                Collections.shuffle(mainPostList);
                mainPostAdapter = new MainPostAdapter(HomeActivity.this, mainPostList);
                mainPostAdapter.notifyDataSetChanged();
                postRv.setAdapter(mainPostAdapter);
                postRv.setNestedScrollingEnabled(false);
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                nestedScrollView.setVisibility(View.VISIBLE);
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
                                Toast.makeText(HomeActivity.this, "server status: down, admin access", Toast.LENGTH_SHORT).show();
                            } else if (firebaseRemoteConfig.getBoolean("server_under_maintenance") == true && !mAuth.getCurrentUser().getEmail().equals("11202@hongchun.hs.kr")) {
                                startActivity(new Intent(HomeActivity.this, ServerDownActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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

    private void checkUserStatus() {

        checkConnection();

        if (isConnected) {
            mAuth.getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (mAuth.getCurrentUser() == null) {
                        Toast.makeText(HomeActivity.this, "사용자 인증에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(HomeActivity.this, AuthActivity.class));
                        finish();
                    } else {
                        // User is not null
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(HomeActivity.this, AuthActivity.class));
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

    public void logout() {

        GoogleSignInOptions gso = new GoogleSignInOptions.
                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);
        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(HomeActivity.this, AuthActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                }
            }
        });
    }
}
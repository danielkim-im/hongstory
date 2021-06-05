package com.teamhns.hnsmobile.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.FirebaseStorage;
import com.teamhns.hnsmobile.Adapter.ExplorePostAdapter;
import com.teamhns.hnsmobile.Adapter.MainPostAdapter;
import com.teamhns.hnsmobile.Model.Post;
import com.teamhns.hnsmobile.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExploreActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    RecyclerView recyclerView;
    TextView recTag, privateTag, publicTag, funTag;
    Button gotoSearchBtn;
    SwipeRefreshLayout swipeRefreshLayout;
    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseRemoteConfig firebaseRemoteConfig;
    List postList;
    Boolean isConnected;
    ExplorePostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int nightModeFlags =
                ExploreActivity.this.getResources().getConfiguration().uiMode &
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
        setContentView(R.layout.activity_explore);
        getSupportActionBar().hide();

        bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setSelectedItemId(R.id.nav_explore);
        recTag = findViewById(R.id.recommendTag);
        privateTag = findViewById(R.id.privateTag);
        publicTag = findViewById(R.id.publicTag);
        funTag = findViewById(R.id.funTag);
        swipeRefreshLayout = findViewById(R.id.explore_swipe);
        gotoSearchBtn = findViewById(R.id.gotoSearchBtn);
        recyclerView = findViewById(R.id.discover_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ExploreActivity.this, RecyclerView.VERTICAL, false));

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getInstance().getReference("Posts");
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        //BottomNavigationView Controller [KEEP AT BOTTOM OF THE INIT]
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        startActivity(new Intent(ExploreActivity.this, HomeActivity.class));
//                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        overridePendingTransition(0, 0);
                        return false;
                    case R.id.nav_explore:
                        recyclerView.smoothScrollToPosition(0);
                        return true;
                    case R.id.nav_forum:
                        startActivity(new Intent(ExploreActivity.this, QuestionActivity.class));
//                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                        overridePendingTransition(0, 0);
                        return false;
                    case R.id.nav_profile:
                        startActivity(new Intent(ExploreActivity.this, ProfileActivity.class));
                        //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        overridePendingTransition(0, 0);
                        return false;
                }
                return false;
            }
        });


        recTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadRecPosts();
            }
        });

        privateTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadPrivateTags();
            }
        });

        publicTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadPublicTags();
            }
        });

        funTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadFunTags();
            }
        });

        gotoSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ExploreActivity.this, SearchPostActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                GetServerStatus();
                LoadRecPosts();
            }
        });

        LoadRecPosts();
    }

    @Override
    protected void onStart() {
        super.onStart();

        GetServerStatus();
        checkUserStatus();
    }

    private void LoadRecPosts() {
        postList = new ArrayList<>();

        Query query;
        query = databaseReference.limitToLast(500);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Activity activity = ExploreActivity.this;
                if (activity.isFinishing())
                    return;
                for (DataSnapshot postsnap : snapshot.getChildren()) {
                    Post post = postsnap.getValue(Post.class);
                    postList.add(post);
                }

                Collections.shuffle(postList);
                postAdapter = new ExplorePostAdapter(ExploreActivity.this, postList);
                postAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(postAdapter);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void LoadPrivateTags() {
        postList = new ArrayList<>();

        Query query;
        query = databaseReference.limitToLast(500);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList = new ArrayList<>();
                for (DataSnapshot postsnap: dataSnapshot.getChildren()){
                    Post post = postsnap.getValue(Post.class);

                    if(post.getPrivacy().equals("private")){
                        postList.add(post);
                    }
                }

                Collections.shuffle(postList);
                postAdapter = new ExplorePostAdapter(ExploreActivity.this, postList);
                postAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(postAdapter);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void LoadPublicTags() {
        postList = new ArrayList<>();

        Query query;
        query = databaseReference.limitToLast(500);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList = new ArrayList<>();
                for (DataSnapshot postsnap: dataSnapshot.getChildren()){
                    Post post = postsnap.getValue(Post.class);

                    if(post.getPrivacy().equals("public")){
                        postList.add(post);
                    }
                }

                Collections.shuffle(postList);
                postAdapter = new ExplorePostAdapter(ExploreActivity.this, postList);
                postAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(postAdapter);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void LoadFunTags() {
        postList = new ArrayList<>();

        Query query;
        query = databaseReference.limitToLast(500);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList = new ArrayList<>();
                for (DataSnapshot postsnap: dataSnapshot.getChildren()){
                    Post post = postsnap.getValue(Post.class);

                    if(post.getHashtag().toLowerCase().contains("fun") || post.getDescription().toLowerCase().contains("fun")){
                        postList.add(post);
                    }
                }

                Collections.shuffle(postList);
                postAdapter = new ExplorePostAdapter(ExploreActivity.this, postList);
                postAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(postAdapter);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
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
                                Toast.makeText(ExploreActivity.this, "server status: down, admin access", Toast.LENGTH_SHORT).show();
                            } else if (firebaseRemoteConfig.getBoolean("server_under_maintenance") == true && !mAuth.getCurrentUser().getEmail().equals("11202@hongchun.hs.kr")) {
                                startActivity(new Intent(ExploreActivity.this, ServerDownActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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
                        Toast.makeText(ExploreActivity.this, "사용자 인증에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ExploreActivity.this, AuthActivity.class));
                        finish();
                    } else {
                        // User is not null
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ExploreActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ExploreActivity.this, AuthActivity.class));
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
}
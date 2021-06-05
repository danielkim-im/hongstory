package com.teamhns.hnsmobile.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.teamhns.hnsmobile.Adapter.CommentAdapter;
import com.teamhns.hnsmobile.Model.Comment;
import com.teamhns.hnsmobile.Model.User;
import com.teamhns.hnsmobile.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewAllCommentActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Button backBtn;
    CheckBox privacyBox;
    SwipeRefreshLayout swipeRefreshLayout;
    EditText commentContentEdtx;
    ImageButton addCommentBtn;
    String postKey;
    public static String uploaderUid;
    CommentAdapter commentAdapter;
    List listComment;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    Boolean isConnected;
    String privacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int nightModeFlags =
                ViewAllCommentActivity.this.getResources().getConfiguration().uiMode &
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
        setContentView(R.layout.activity_view_all_comment);
        getSupportActionBar().hide();

        backBtn = findViewById(R.id.backBtn5);
        commentContentEdtx = findViewById(R.id.vAllCEdtx);
        addCommentBtn = findViewById(R.id.vAllCSendBtn);
        privacyBox = findViewById(R.id.privacyBox);
        swipeRefreshLayout = findViewById(R.id.swipeLayoutVComment);
        swipeRefreshLayout.setRefreshing(true);
        recyclerView = findViewById(R.id.seeAllComment_rv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(ViewAllCommentActivity.this, 1, LinearLayoutManager.VERTICAL, false));

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        postKey = getIntent().getExtras().getString("postKey");
        uploaderUid = getIntent().getExtras().getString("uploaderId");

        commentContentEdtx.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(commentContentEdtx,0);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                iniRvComment();
            }
        });

        // add comment button click listener
        addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkConnection();

                if(isConnected){
                    if(TextUtils.isEmpty(commentContentEdtx.getText().toString())){
                        //do nothing
                    }else{
                        addCommentBtn.setVisibility(View.INVISIBLE);

                        if (privacyBox.isChecked()){
                            privacy = "private";
                        }else{
                            privacy = "public";
                        }

                        Comment comment = new Comment(
                                commentContentEdtx.getText().toString(),
                                privacy,
                                firebaseAuth.getCurrentUser().getUid());

                        addComment(comment);
                    }
                }else if(isConnected = false){
                    Toast.makeText(ViewAllCommentActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        iniRvComment();
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkUserStatus();
    }

    private void checkUserStatus() {

        checkConnection();

        if (isConnected) {
            firebaseAuth.getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (firebaseAuth.getCurrentUser() == null) {
                        Toast.makeText(ViewAllCommentActivity.this, "사용자 인증에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ViewAllCommentActivity.this, AuthActivity.class));
                        finish();
                    } else {
                        // User is not null
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ViewAllCommentActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ViewAllCommentActivity.this, AuthActivity.class));
                    finish();
                }
            });
        } else {
            // Do not check user status if not connected
        }
    }

    private void iniRvComment() {
        listComment = new ArrayList<>();

        Query query =
                firebaseDatabase.getReference("Comment")
                        .child(postKey);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listComment.clear();
                Activity activity = ViewAllCommentActivity.this;
                if(activity.isFinishing())
                    return;

                if(snapshot.hasChildren()){
                    for (DataSnapshot commentsnap: snapshot.getChildren()){
                        Comment comment = commentsnap.getValue(Comment.class);
                        listComment.add(comment);
                    }

                    commentAdapter = new CommentAdapter(ViewAllCommentActivity.this, listComment, postKey);
                    recyclerView.setAdapter(commentAdapter);
                    swipeRefreshLayout.setRefreshing(false);
                }else{
                    commentAdapter = new CommentAdapter(ViewAllCommentActivity.this, listComment, postKey);
                    recyclerView.setAdapter(commentAdapter);
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewAllCommentActivity.this, "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addComment(Comment comment) {
        DatabaseReference myRef = firebaseDatabase.getReference("Comment").child(postKey).push();

        //get post unique ID and update post key
        String key = myRef.getKey();
        comment.setCommentKey(key);

        // add post data to firebase database
        myRef.setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ViewAllCommentActivity.this, "댓글이 게시되었습니다.", Toast.LENGTH_SHORT).show();
                commentContentEdtx.setText("");
                addCommentBtn.setVisibility(View.VISIBLE);
                //iniRvComment();
            }
        });
    }

    public void checkConnection(){
        ConnectivityManager manager = (ConnectivityManager)
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();

        if(null != activeNetwork){
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI){
                isConnected = true;
            }

            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE){
                isConnected = true;
            }
        }
        else{
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            isConnected = false;
        }
    }

    @Override
    public void finish(){
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
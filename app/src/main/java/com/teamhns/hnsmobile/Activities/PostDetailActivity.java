package com.teamhns.hnsmobile.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.teamhns.hnsmobile.Fragments.ReportSingleChoiceDialogFragment;
import com.teamhns.hnsmobile.Model.ReportPost;
import com.teamhns.hnsmobile.Model.User;
import com.teamhns.hnsmobile.R;

import de.hdodenhof.circleimageview.CircleImageView;
import kr.co.prnd.readmore.ReadMoreTextView;

public class PostDetailActivity extends AppCompatActivity implements ReportSingleChoiceDialogFragment.SingleChoiceListener{

    PhotoView img;
    ImageView commentBtn, reportBtn;
    Button backBtn, likeBtn;
    CircleImageView profileImg;
    TextView username, likeCount;
    ReadMoreTextView desc;
    String postKeyStr, uploaderStr, privacy;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.darkTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        getSupportActionBar().hide();
        Window window = getWindow();
        window.setStatusBarColor(getColor(R.color.Black));
        window.setNavigationBarColor(getColor(R.color.Black));

        img = findViewById(R.id.postdetail_img);
        desc = findViewById(R.id.postdetail_description);
        username = findViewById(R.id.postdetail_username);
        likeCount = findViewById(R.id.postdetail_likecount);
        profileImg = findViewById(R.id.postdetail_profile);
        backBtn = findViewById(R.id.backBtn2);
        commentBtn = findViewById(R.id.postdetail_comment);
        likeBtn = findViewById(R.id.postdetail_like);
        reportBtn = findViewById(R.id.postdetail_reportbtn);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        postKeyStr = getIntent().getExtras().getString("postKey");
        final String picture = getIntent().getExtras().getString("picture");
        final String hashtagStr = getIntent().getExtras().getString("hashtag");
        privacy = getIntent().getExtras().getString("privacy");
        final String descStr = getIntent().getExtras().getString("description");
        final String timeStr = getIntent().getExtras().getString("timeStamp");
        uploaderStr = getIntent().getExtras().getString("userId");
        final Boolean downloadImg = getIntent().getExtras().getBoolean("downloadImg");
        final Boolean reportTagBool = getIntent().getExtras().getBoolean("reportTag");

        if (picture.isEmpty()) { //url.isEmpty()
            Picasso.get()
                    .load(R.drawable.loading_placeholder)
                    .error(R.drawable.loading_placeholder)
                    .into(img);
        }else{
            Picasso.get()
                    .load(picture)
                    .error(R.drawable.loading_placeholder)
                    .config(Bitmap.Config.RGB_565)
                    .into(img);
        }

        desc.setText(descStr);

        if (privacy.equals("private")){
            username.setText("익명");
        }else{
            publisherInfo(profileImg, username, uploaderStr);
        }

        if (reportTagBool == true){
            DialogFragment singleChoiceDiailog = new ReportSingleChoiceDialogFragment();
            singleChoiceDiailog.setCancelable(true);
            singleChoiceDiailog.show(getSupportFragmentManager(), "Single Choice Dialog");
        }

        isLikes(postKeyStr, likeBtn);
        nrLikes(likeCount, postKeyStr);

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (likeBtn.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postKeyStr)
                            .child(firebaseAuth.getCurrentUser().getUid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postKeyStr)
                            .child(firebaseAuth.getCurrentUser().getUid()).removeValue();
                }
            }
        });

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (privacy.equals("public")){
                    Intent intent = new Intent(PostDetailActivity.this, UserInfoActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, uploaderStr);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment singleChoiceDiailog = new ReportSingleChoiceDialogFragment();
                singleChoiceDiailog.setCancelable(true);
                singleChoiceDiailog.show(getSupportFragmentManager(), "Single Choice Dialog");
            }
        });

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (privacy.equals("public")){
                    Intent intent = new Intent(PostDetailActivity.this, UserInfoActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, uploaderStr);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }
        });

        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent allCommentActivity = new Intent(PostDetailActivity.this, ViewAllCommentActivity.class);
                allCommentActivity.putExtra("postKey", postKeyStr);
                allCommentActivity.putExtra("uploaderId", uploaderStr);
                startActivity(allCommentActivity);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
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

    private void isLikes(String postid, Button imageView) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(mAuth.getCurrentUser().getUid()).exists()) {
                    imageView.setBackgroundResource(R.drawable.ic_liked);
                    imageView.setBackgroundTintList(ContextCompat.getColorStateList(PostDetailActivity.this, R.color.MyOpenChatColor));
                    imageView.setTag("liked");
                } else {
                    imageView.setBackgroundResource(R.drawable.ic_like);
                    imageView.setBackgroundTintList(ContextCompat.getColorStateList(PostDetailActivity.this, R.color.White));
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void nrLikes(TextView likes, String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likes.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void publisherInfo(final ImageView image_profile, final TextView username, final String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Activity activity = PostDetailActivity.this;
                if(activity.isFinishing())
                    return;

                User user = snapshot.getValue(User.class);

                if (snapshot.exists()){
                    Glide.with(PostDetailActivity.this)
                            .load(user.getPimg())
                            .apply(new RequestOptions().override(240, 240))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(image_profile);
                    username.setText(user.getName());
                }else {
                    Glide.with(PostDetailActivity.this)
                            .load(R.drawable.commentbg)
                            .apply(new RequestOptions().override(240, 240))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(image_profile);
                    username.setText("존재하지 않는 사용자");
                    //Toast.makeText(mContext, "snapshot. userinfo does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onPositiveButtonClicked(String[] list, int position) {
        ReportPost reportPost = new ReportPost(
                postKeyStr,
                firebaseAuth.getCurrentUser().getUid(),
                list[position]
        );
        reportTag(reportPost);
    }

    @Override
    public void onNegativeButtonClicked() {
        //
    }

    private void reportTag(ReportPost reportPost) {
        DatabaseReference myRef = firebaseDatabase.getReference("ReportedPost").child(postKeyStr).push();

        //get post unique ID and update post key
        String key = myRef.getKey();
        reportPost.setReportKey(key);

        // add post data to firebase database
        myRef.setValue(reportPost).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(PostDetailActivity.this, "신고 완료.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
package com.teamhns.hnsmobile.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pedromassango.doubleclick.DoubleClick;
import com.pedromassango.doubleclick.DoubleClickListener;
import com.squareup.picasso.Picasso;
import com.teamhns.hnsmobile.Activities.PostDetailActivity;
import com.teamhns.hnsmobile.Activities.UserInfoActivity;
import com.teamhns.hnsmobile.Activities.ViewAllCommentActivity;
import com.teamhns.hnsmobile.Model.Post;
import com.teamhns.hnsmobile.Model.User;
import com.teamhns.hnsmobile.R;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import kr.co.prnd.readmore.ReadMoreTextView;

public class ExplorePostAdapter extends RecyclerView.Adapter<ExplorePostAdapter.ViewHolder> {

    Context mContext;
    List<Post> mData;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth mAuth;
    BottomSheetDialog bottomSheetDialog;

    public static final int POST_CONTENT = 0;
    public static final int TEXT_CONTENT = 1;

    public ExplorePostAdapter(Context mContext, List<Post> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == POST_CONTENT) {
            View row = LayoutInflater.from(mContext).inflate(R.layout.row_main_post_item, parent, false);
            return new ViewHolder(row);
        } else {
            View row = LayoutInflater.from(mContext).inflate(R.layout.row_main_text_item, parent, false);
            return new ViewHolder(row);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = mData.get(position);
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        //Verified Badge
/*        if(mData.get(position).getUserId().equals(mContext.getString(R.string.yolastudioUID))){
            holder.verifiedIcon.setVisibility(View.VISIBLE);
        }else{
            holder.verifiedIcon.setVisibility(View.GONE);
        }*/

        if (mData.get(position).getPrivacy().equals("private")) {
            Glide.with(mContext)
                    .load(R.drawable.app_icon_512)
                    .apply(new RequestOptions().override(480, 480))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.userProfileImg);
            holder.userName.setText("익명");
        } else {
            publisherInfo(holder.userProfileImg, holder.userName, post.getUserId());
        }

        if (mData.get(position).getContentType().equals("image") && !mData.get(position).getPicture().isEmpty()) { //url.isEmpty()
            Picasso.get()
                    .load(mData.get(position).getPicture())
                    .placeholder(R.drawable.loading_placeholder)
                    .resize(1080, 1080)
                    .onlyScaleDown()
                    .centerInside()
                    .error(R.drawable.loading_placeholder)
                    .config(Bitmap.Config.RGB_565)
                    .into(holder.imgTag);
        } else {
            holder.imgTag.setVisibility(View.GONE);
        }

        if (mData.get(position).getHashtag().isEmpty()) {
            holder.tagHashtag.setVisibility(View.GONE);
        } else {
            holder.tagHashtag.setVisibility(View.VISIBLE);
            holder.tagHashtag.setText(mData.get(position).getHashtag());
        }

        if (mData.get(position).getDescription().isEmpty()) {
            holder.txtDescription.setVisibility(View.GONE);
        } else {
            holder.txtDescription.setVisibility(View.VISIBLE);
            holder.txtDescription.setText(mData.get(position).getDescription());
        }

        final long timestamp = (long) mData.get(position).getTimeStamp();
        String date = timestampToString(timestamp);
        holder.dateTxt.setText(date);

        isLikes(post.getPostKey(), holder.likeBtn, holder.likeTxt);
        nrLikes(holder.likeCount, post.getPostKey());

        holder.likeLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.likeBtn.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostKey())
                            .child(mAuth.getCurrentUser().getUid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostKey())
                            .child(mAuth.getCurrentUser().getUid()).removeValue();
                }
            }
        });

        holder.imgTag.setOnClickListener(new DoubleClick(new DoubleClickListener() {
            @Override
            public void onSingleClick(View view) {
                Intent postDetailActivity = new Intent(mContext, PostDetailActivity.class);

                postDetailActivity.putExtra("postKey", post.getPostKey());
                postDetailActivity.putExtra("description", post.getDescription());
                postDetailActivity.putExtra("picture", post.getPicture());
                postDetailActivity.putExtra("hashtag", post.getHashtag());
                postDetailActivity.putExtra("privacy", post.getPrivacy());
                postDetailActivity.putExtra("contentType", post.getContentType());
                long timestamp = (long) post.getTimeStamp();
                postDetailActivity.putExtra("timeStamp", timestamp);
                postDetailActivity.putExtra("userId", post.getUserId());
                postDetailActivity.putExtra("downloadImg", false);
                postDetailActivity.putExtra("reportTag", false);
      /*          mContext.startActivity(postDetailActivity);
                if (mContext instanceof Activity) {
                    ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }*/
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, holder.imgTag, ViewCompat.getTransitionName(holder.imgTag));
                mContext.startActivity(postDetailActivity, optionsCompat.toBundle());
            }

            @Override
            public void onDoubleClick(View view) {
                if (holder.likeBtn.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostKey())
                            .child(mAuth.getCurrentUser().getUid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostKey())
                            .child(mAuth.getCurrentUser().getUid()).removeValue();
                }
            }
        }));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgTag, closeDialogBtn, verifiedIcon;
        Button moreBtn, likeBtn;
        LinearLayout likeLine, commentLine;
        TextView tagHashtag, userName, likeCount, dateTxt, likeTxt;
        ReadMoreTextView txtDescription;
        CircleImageView userProfileImg;
        ConstraintLayout reportCons;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTag = itemView.findViewById(R.id.row_post_img);
            tagHashtag = itemView.findViewById(R.id.txtHashtags);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            likeLine = itemView.findViewById(R.id.likeLine);
            commentLine = itemView.findViewById(R.id.commentLine);
            userName = itemView.findViewById(R.id.txtUserName);
            userProfileImg = itemView.findViewById(R.id.profileimg);
            moreBtn = itemView.findViewById(R.id.moreBtn);
            likeCount = itemView.findViewById(R.id.likeCount);
            likeTxt = itemView.findViewById(R.id.likeTxt);
            dateTxt = itemView.findViewById(R.id.txtDate);
            verifiedIcon = itemView.findViewById(R.id.verifiedIcon);

            userName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (mData.get(position).getPrivacy().equals("public")) {
                        Intent intent = new Intent(mContext, UserInfoActivity.class);
                        intent.putExtra(Intent.EXTRA_TEXT, mData.get(position).getUserId());
                        mContext.startActivity(intent);
                        if (mContext instanceof Activity) {
                            ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    }
                }
            });

            userProfileImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (mData.get(position).getPrivacy().equals("public")) {
                        Intent intent = new Intent(mContext, UserInfoActivity.class);
                        intent.putExtra(Intent.EXTRA_TEXT, mData.get(position).getUserId());
                        mContext.startActivity(intent);
                        if (mContext instanceof Activity) {
                            ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    }
                }
            });

            commentLine.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Intent allCommentActivity = new Intent(mContext, ViewAllCommentActivity.class);

                    allCommentActivity.putExtra("postKey", mData.get(position).getPostKey());
                    mContext.startActivity(allCommentActivity);
                    if (mContext instanceof Activity) {
                        ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                }
            });

            moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog = new BottomSheetDialog(mContext);
                    bottomSheetDialog.setContentView(R.layout.tag_item_report_bottom_sheet_dialog1);
                    bottomSheetDialog.setCanceledOnTouchOutside(true);
                    bottomSheetDialog.show();

                    closeDialogBtn = bottomSheetDialog.findViewById(R.id.closeDialogBtn);
                    reportCons = bottomSheetDialog.findViewById(R.id.reportCons);

                    reportCons.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent postDetailActivity = new Intent(mContext, PostDetailActivity.class);
                            int position = getAdapterPosition();
                            if (mData.get(position).getContentType().equals("image")) { //url.isEmpty()
                                postDetailActivity.putExtra("postKey", mData.get(position).getPostKey());
                                postDetailActivity.putExtra("description", mData.get(position).getDescription());
                                postDetailActivity.putExtra("picture", mData.get(position).getPicture());
                                postDetailActivity.putExtra("hashtag", mData.get(position).getHashtag());
                                postDetailActivity.putExtra("privacy", mData.get(position).getPrivacy());
                                postDetailActivity.putExtra("contentType", mData.get(position).getContentType());
                                long timestamp = (long) mData.get(position).getTimeStamp();
                                postDetailActivity.putExtra("timeStamp", timestamp);
                                postDetailActivity.putExtra("userId", mData.get(position).getUserId());
                                postDetailActivity.putExtra("downloadImg", false);
                                postDetailActivity.putExtra("reportTag", true);
                                bottomSheetDialog.dismiss();
                                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, imgTag, ViewCompat.getTransitionName(imgTag));
                                mContext.startActivity(postDetailActivity, optionsCompat.toBundle());
                            } else {
                                Toast.makeText(mContext, "report text content", Toast.LENGTH_SHORT).show();
                            }
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

            likeCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //do not show people who liked the post
                  /*  Intent reportActivity = new Intent(mContext, ViewLikersActivity.class);
                    int position = getAdapterPosition();

                    reportActivity.putExtra("postKey", mData.get(position).getPostKey());
                    mContext.startActivity(reportActivity);
                    if (mContext instanceof Activity) {
                        ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }*/
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.get(position).getContentType().equals("image")) {
            return POST_CONTENT;
        } else {
            return TEXT_CONTENT;
        }
    }

    private void publisherInfo(final ImageView image_profile, final TextView username, final String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (snapshot.exists()){
                    Glide.with(mContext)
                            .load(user.getPimg())
                            .apply(new RequestOptions().override(480, 480))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(image_profile);
                    username.setText(user.getName());
                }else {
                    Glide.with(mContext)
                            .load(R.drawable.commentbg)
                            .apply(new RequestOptions().override(480, 480))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(image_profile);
                    username.setText("존재하지 않는 사용자");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isLikes(String postid, Button imageView, TextView likeTxt) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(mAuth.getCurrentUser().getUid()).exists()) {
                    imageView.setBackgroundResource(R.drawable.ic_liked);
                    imageView.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.MyOpenChatColor));
                    likeTxt.setTextColor(mContext.getColor(R.color.MyOpenChatColor));
                    imageView.setTag("liked");
                } else {
                    imageView.setBackgroundResource(R.drawable.ic_like);
                    imageView.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.Grey));
                    likeTxt.setTextColor(mContext.getColor(R.color.Grey));
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
                likes.setText(snapshot.getChildrenCount() + " 좋아요");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String timestampToString(long time) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date;
        //date = DateFormat.format("MMM d일 yyyy a h:mm", calendar).toString();
        date = DateFormat.format("M월 d일 a h:mm", calendar).toString();
        return date;
    }
}
package com.teamhns.hnsmobile.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.teamhns.hnsmobile.Activities.PostDetailActivity;
import com.teamhns.hnsmobile.Model.Post;
import com.teamhns.hnsmobile.Model.User;
import com.teamhns.hnsmobile.R;

import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeHorizPostAdapter extends RecyclerView.Adapter<HomeHorizPostAdapter.ViewHolder> {

    Context mContext;
    List<Post> mData;

    public HomeHorizPostAdapter(Context mContext, List<Post> mData) {
        this.mContext = mContext;
        this.mData = mData;
        Collections.reverse(mData);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.row_home_horiz_post_item, parent, false);
        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Post post = mData.get(position);


        if (mData.get(position).getPicture().isEmpty()) { //url.isEmpty()
            Picasso.get()
                    .load(R.drawable.loading_placeholder)
                    .placeholder(R.drawable.loading_placeholder)
                    .error(R.drawable.loading_placeholder)
                    .into(holder.img);
        }else{
            Picasso.get()
                    .load(mData.get(position).getPicture())
                    .placeholder(R.drawable.loading_placeholder)
                    .resize(720, 720)
                    .onlyScaleDown()
                    .centerInside()
                    .error(R.drawable.loading_placeholder)
                    .config(Bitmap.Config.RGB_565)
                    .into(holder.img);
        }

        if (mData.get(position).getPrivacy().equals("private")) {
            Glide.with(mContext)
                    .load(R.drawable.app_icon_512)
                    .apply(new RequestOptions().override(480, 480))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.profileImg);
            holder.username.setText("익명");
        } else {
            publisherInfo(holder.username, holder.profileImg, post.getUserId());
        }

        holder.desc.setText(mData.get(position).getDescription());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        CircleImageView profileImg;
        TextView username, desc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.row_rec_img);
            profileImg = itemView.findViewById(R.id.row_rec_profileimg);
            username = itemView.findViewById(R.id.row_rec_username);
            desc = itemView.findViewById(R.id.row_rec_content);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postDetailActivity = new Intent(mContext, PostDetailActivity.class);
                    int position = getAdapterPosition();

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
                    postDetailActivity.putExtra("reportTag", false);
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, img, ViewCompat.getTransitionName(img));
                    mContext.startActivity(postDetailActivity, optionsCompat.toBundle());
                }
            });
        }
    }

    private void publisherInfo(final TextView username, final CircleImageView profileImg, final String userid){
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
                            .into(profileImg);
                    username.setText(user.getName());
                }else {
                    Glide.with(mContext)
                            .load(R.drawable.commentbg)
                            .apply(new RequestOptions().override(480, 480))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(profileImg);
                    username.setText("존재하지 않는 사용자");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

package com.teamhns.hnsmobile.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.teamhns.hnsmobile.Activities.EditPostActivity;
import com.teamhns.hnsmobile.Activities.PostDetailActivity;
import com.teamhns.hnsmobile.Activities.ProfileActivity;
import com.teamhns.hnsmobile.Model.Post;
import com.teamhns.hnsmobile.R;

import java.util.Collections;
import java.util.List;

public class ProfilePostAdapter extends RecyclerView.Adapter<ProfilePostAdapter.ViewHolder> {

    Context mContext;
    List<Post> mData;
    FirebaseAuth mAuth;
    FirebaseStorage mStorage;

    public static final int POST_CONTENT = 0;
    public static final int TEXT_CONTENT = 1;

    public ProfilePostAdapter(Context mContext, List<Post> mData) {
        this.mContext = mContext;
        this.mData = mData;
        Collections.reverse(mData);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == POST_CONTENT){
            View row = LayoutInflater.from(mContext).inflate(R.layout.row_profile_img_post_item, parent, false);
            return new ViewHolder(row);
        }else{
            View row = LayoutInflater.from(mContext).inflate(R.layout.row_profile_txt_post_item, parent, false);
            return new ViewHolder(row);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(mData.get(position).getContentType().equals("image")){
            return POST_CONTENT;
        }else{
            return TEXT_CONTENT;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();

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
            if (mData.get(position).getPrivacy().equals("private")){
                holder.tagDesc.setText("#익명게시물");
            }else {
                holder.tagDesc.setText(mData.get(position).getHashtag());
            }
        }else{
            holder.tagContent.setText(mData.get(position).getDescription());
            if (mData.get(position).getPrivacy().equals("private")){
                holder.tagDesc.setText("#익명게시물");
            }else {
                holder.tagDesc.setText(mData.get(position).getHashtag());
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgTag, moreBtn, closeDialogBtn;
        TextView tagDesc, tagContent;
        ConstraintLayout editCons, reportCons, deleteCons;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTag = itemView.findViewById(R.id.row_img_tag_photoview);
            moreBtn = itemView.findViewById(R.id.row_img_tag_more);
            tagDesc = itemView.findViewById(R.id.row_img_tag_tags);
            tagContent = itemView.findViewById(R.id.row_txt_desc);

            moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mContext);
                    bottomSheetDialog.setContentView(R.layout.tag_item_bottom_sheet_dialog1);
                    bottomSheetDialog.setCanceledOnTouchOutside(true);

                    closeDialogBtn = bottomSheetDialog.findViewById(R.id.closeDialogBtn);
                    editCons = bottomSheetDialog.findViewById(R.id.editCons);
                    reportCons = bottomSheetDialog.findViewById(R.id.reportCons);
                    deleteCons = bottomSheetDialog.findViewById(R.id.deleteCons);

                    editCons.setVisibility(View.GONE);
                    deleteCons.setVisibility(View.GONE);
                    if (mData.get(position).getUserId().equals(mAuth.getCurrentUser().getUid())) {
                        editCons.setVisibility(View.VISIBLE);
                        deleteCons.setVisibility(View.VISIBLE);
                        reportCons.setVisibility(View.GONE);
                    } else {
                        editCons.setVisibility(View.GONE);
                        deleteCons.setVisibility(View.GONE);
                        reportCons.setVisibility(View.VISIBLE);
                    }

                    closeDialogBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.dismiss();
                        }
                    });

                    editCons.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mData.get(position).getUserId().equals(mAuth.getCurrentUser().getUid())) {
                                Intent editTagActivity = new Intent(mContext, EditPostActivity.class);
                                int position = getAdapterPosition();

                                editTagActivity.putExtra("postKey", mData.get(position).getPostKey());
                                editTagActivity.putExtra("description", mData.get(position).getDescription());
                                editTagActivity.putExtra("picture", mData.get(position).getPicture());
                                editTagActivity.putExtra("hashtag", mData.get(position).getHashtag());
                                editTagActivity.putExtra("contentType", mData.get(position).getContentType());
                                long timestamp = (long) mData.get(position).getTimeStamp();
                                editTagActivity.putExtra("timeStamp", timestamp);
                                editTagActivity.putExtra("userId", mData.get(position).getUserId());
                                editTagActivity.putExtra("editTag", true);
                                mContext.startActivity(editTagActivity);
                                if (mContext instanceof Activity) {
                                    ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                }
                            } else {
                                // Do Nothing
                            }
                            bottomSheetDialog.dismiss();
                        }
                    });

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
                            }else{
                                Toast.makeText(mContext, "report text content", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    deleteCons.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mData.get(position).getUserId().equals(mAuth.getCurrentUser().getUid())) {

                                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                                builder.setTitle("게시물을 삭제하시겠습니까?")
                                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String imageurl = mData.get(position).getPicture();
                                                FirebaseDatabase.getInstance().getReference("Posts").child(mData.get(position).getPostKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            if (!imageurl.isEmpty()){
                                                                StorageReference imageRef = mStorage.getReferenceFromUrl(mData.get(position).getPicture());
                                                                imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Toast.makeText(mContext, "게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                                                        bottomSheetDialog.dismiss();
                                                                        mContext.startActivity(new Intent(mContext, ProfileActivity.class));
                                                                        if (mContext instanceof Activity) {
                                                                            ((Activity) mContext).finish();
                                                                        }
                                                                    }
                                                                });
                                                            }else{
                                                                Toast.makeText(mContext, "게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                                                bottomSheetDialog.dismiss();
                                                                mContext.startActivity(new Intent(mContext, ProfileActivity.class));
                                                                if (mContext instanceof Activity) {
                                                                    ((Activity) mContext).finish();
                                                                }
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        }).setNegativeButton("취소", null);

                                AlertDialog alert = builder.create();
                                alert.show();
                            } else {
                                // Do Nothing
                            }
                        }
                    });

                    bottomSheetDialog.show();
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
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
                        postDetailActivity.putExtra("reportTag", false);
                        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, imgTag, ViewCompat.getTransitionName(imgTag));
                        mContext.startActivity(postDetailActivity, optionsCompat.toBundle());
                    }else{
                        Toast.makeText(mContext, "report text content", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
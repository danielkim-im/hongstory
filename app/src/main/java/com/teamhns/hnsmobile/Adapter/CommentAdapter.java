package com.teamhns.hnsmobile.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamhns.hnsmobile.Activities.PostDetailActivity;
import com.teamhns.hnsmobile.Activities.ReportCommentActivity;
import com.teamhns.hnsmobile.Activities.UserInfoActivity;
import com.teamhns.hnsmobile.Activities.ViewAllCommentActivity;
import com.teamhns.hnsmobile.Model.Comment;
import com.teamhns.hnsmobile.Model.User;
import com.teamhns.hnsmobile.R;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context mContext;
    private List<Comment> mData;
    FirebaseAuth mAuth;
    private String postId;
    BottomSheetDialog bottomSheetDialog;

    public CommentAdapter(Context mContext, List<Comment> mData, String postId) {
        this.mContext = mContext;
        this.mData = mData;
        this.postId = postId;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.row_comment_item, parent, false);
        return new CommentViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        Comment comment = mData.get(position);

        holder.tv_content.setText(mData.get(position).getContent());
        holder.tv_date.setText(timestampToString((Long) mData.get(position).getTimestamp()));

        if (mData.get(position).getPrivacy().equals("private")) {
            if (mData.get(position).getUid().equals(mAuth.getCurrentUser().getUid())) {
                holder.tv_name.setText("익명(나)");
            } else if (mData.get(position).getUid().equals(ViewAllCommentActivity.uploaderUid)) {
                holder.tv_name.setText("익명(글쓴이)");
            } else {
                holder.tv_name.setText("익명");
                Glide.with(mContext)
                        .load(R.drawable.commentbg)
                        .apply(new RequestOptions().override(720, 720))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(holder.img_user);
            }
        } else if (mData.get(position).getPrivacy().equals("public")) {
            if (mData.get(position).getUid().equals(mAuth.getCurrentUser().getUid())) {
                publicMyComment(holder.img_user, holder.tv_name, comment.getUid());
            } else if (mData.get(position).getUid().equals(ViewAllCommentActivity.uploaderUid)) {
                publicUploaderComment(holder.img_user, holder.tv_name, comment.getUid());
            } else {
                publisherInfo(holder.img_user, holder.tv_name, comment.getUid());
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        CircleImageView img_user;
        Button delBtn;
        ProgressBar progressBar;
        ImageView closeDialogBtn;
        ConstraintLayout reportCons, deleteCons;
        LinearLayout line1;
        TextView tv_name, tv_content, tv_date, delCommentcontent;

        public CommentViewHolder(View itemView) {
            super(itemView);
            img_user = itemView.findViewById(R.id.comment_user_img);
            tv_name = itemView.findViewById(R.id.comment_username);
            tv_content = itemView.findViewById(R.id.comment_content);
            tv_date = itemView.findViewById(R.id.comment_date);
            line1 = itemView.findViewById(R.id.contentLine1);

            img_user.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Intent intent = new Intent(mContext, UserInfoActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, mData.get(position).getUid());
                    mContext.startActivity(intent);
                    if (mContext instanceof Activity) {
                        ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                }
            });

            tv_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Intent intent = new Intent(mContext, UserInfoActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, mData.get(position).getUid());
                    mContext.startActivity(intent);
                    if (mContext instanceof Activity) {
                        ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                }
            });

            line1.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    bottomSheetDialog = new BottomSheetDialog(mContext);
                    bottomSheetDialog.setContentView(R.layout.comment_item_report_bottom_sheet_dialog1);
                    bottomSheetDialog.setCanceledOnTouchOutside(true);
                    bottomSheetDialog.show();

                    closeDialogBtn = bottomSheetDialog.findViewById(R.id.closeDialogBtn);
                    reportCons = bottomSheetDialog.findViewById(R.id.reportCons);
                    deleteCons = bottomSheetDialog.findViewById(R.id.deleteCons);

                    if (mData.get(position).getUid().equals(mAuth.getCurrentUser().getUid())) {
                        deleteCons.setVisibility(View.VISIBLE);
                        reportCons.setVisibility(View.VISIBLE);
                    } else {
                        deleteCons.setVisibility(View.GONE);
                        reportCons.setVisibility(View.VISIBLE);
                    }

                    reportCons.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent reportCommentActivity = new Intent(mContext, ReportCommentActivity.class);
                            reportCommentActivity.putExtra("commentKey", mData.get(position).getCommentKey());
                            bottomSheetDialog.dismiss();
                            mContext.startActivity(reportCommentActivity);
                            if (mContext instanceof Activity) {
                                ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                        }
                    });

                    deleteCons.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.dismiss();
                            BottomSheetDialog deleteBottomDialog = new BottomSheetDialog(mContext);
                            deleteBottomDialog.setContentView(R.layout.delete_comment_bottom_sheet_dialog1);
                            deleteBottomDialog.setCanceledOnTouchOutside(true);

                            delCommentcontent = deleteBottomDialog.findViewById(R.id.delete_comment_content);
                            delBtn = deleteBottomDialog.findViewById(R.id.delete_comment_btn);
                            progressBar = deleteBottomDialog.findViewById(R.id.progress_bar_delete_comment);
                            progressBar.setVisibility(View.GONE);

                            delCommentcontent.setText(mData.get(position).getContent());

                            delBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    delBtn.setText("");
                                    progressBar.setVisibility(View.VISIBLE);
                                    FirebaseDatabase.getInstance().getReference("Comment")
                                            .child(postId).child(mData.get(position).getCommentKey())
                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                delBtn.setText("댓글 삭제하기");
                                                progressBar.setVisibility(View.GONE);
                                                deleteBottomDialog.dismiss();
                                                Toast.makeText(mContext, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            delBtn.setText("댓글 삭제하기");
                                            progressBar.setVisibility(View.GONE);
                                            deleteBottomDialog.dismiss();
                                            Toast.makeText(mContext, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                            deleteBottomDialog.show();
                        }
                    });
                    return true;
                }
            });

            tv_name.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    bottomSheetDialog = new BottomSheetDialog(mContext);
                    bottomSheetDialog.setContentView(R.layout.comment_item_report_bottom_sheet_dialog1);
                    bottomSheetDialog.setCanceledOnTouchOutside(true);
                    bottomSheetDialog.show();

                    closeDialogBtn = bottomSheetDialog.findViewById(R.id.closeDialogBtn);
                    reportCons = bottomSheetDialog.findViewById(R.id.reportCons);
                    deleteCons = bottomSheetDialog.findViewById(R.id.deleteCons);

                    if (mData.get(position).getUid().equals(mAuth.getCurrentUser().getUid())) {
                        deleteCons.setVisibility(View.VISIBLE);
                        reportCons.setVisibility(View.VISIBLE);
                    } else {
                        deleteCons.setVisibility(View.GONE);
                        reportCons.setVisibility(View.VISIBLE);
                    }

                    reportCons.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent reportCommentActivity = new Intent(mContext, ReportCommentActivity.class);
                            reportCommentActivity.putExtra("commentKey", mData.get(position).getCommentKey());
                            bottomSheetDialog.dismiss();
                            mContext.startActivity(reportCommentActivity);
                            if (mContext instanceof Activity) {
                                ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                        }
                    });

                    deleteCons.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.dismiss();
                            BottomSheetDialog deleteBottomDialog = new BottomSheetDialog(mContext);
                            deleteBottomDialog.setContentView(R.layout.delete_comment_bottom_sheet_dialog1);
                            deleteBottomDialog.setCanceledOnTouchOutside(true);

                            delCommentcontent = deleteBottomDialog.findViewById(R.id.delete_comment_content);
                            delBtn = deleteBottomDialog.findViewById(R.id.delete_comment_btn);
                            progressBar = deleteBottomDialog.findViewById(R.id.progress_bar_delete_comment);
                            progressBar.setVisibility(View.GONE);

                            delCommentcontent.setText(mData.get(position).getContent());

                            delBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    delBtn.setText("");
                                    progressBar.setVisibility(View.VISIBLE);
                                    FirebaseDatabase.getInstance().getReference("Comment")
                                            .child(postId).child(mData.get(position).getCommentKey())
                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                delBtn.setText("댓글 삭제하기");
                                                progressBar.setVisibility(View.GONE);
                                                deleteBottomDialog.dismiss();
                                                Toast.makeText(mContext, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            delBtn.setText("댓글 삭제하기");
                                            progressBar.setVisibility(View.GONE);
                                            deleteBottomDialog.dismiss();
                                            Toast.makeText(mContext, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                            deleteBottomDialog.show();
                        }
                    });
                    return true;
                }
            });

            tv_content.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    bottomSheetDialog = new BottomSheetDialog(mContext);
                    bottomSheetDialog.setContentView(R.layout.comment_item_report_bottom_sheet_dialog1);
                    bottomSheetDialog.setCanceledOnTouchOutside(true);
                    bottomSheetDialog.show();

                    closeDialogBtn = bottomSheetDialog.findViewById(R.id.closeDialogBtn);
                    reportCons = bottomSheetDialog.findViewById(R.id.reportCons);
                    deleteCons = bottomSheetDialog.findViewById(R.id.deleteCons);

                    if (mData.get(position).getUid().equals(mAuth.getCurrentUser().getUid())) {
                        deleteCons.setVisibility(View.VISIBLE);
                        reportCons.setVisibility(View.VISIBLE);
                    } else {
                        deleteCons.setVisibility(View.GONE);
                        reportCons.setVisibility(View.VISIBLE);
                    }

                    reportCons.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent reportCommentActivity = new Intent(mContext, ReportCommentActivity.class);
                            reportCommentActivity.putExtra("commentKey", mData.get(position).getCommentKey());
                            bottomSheetDialog.dismiss();
                            mContext.startActivity(reportCommentActivity);
                            if (mContext instanceof Activity) {
                                ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                        }
                    });

                    deleteCons.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.dismiss();
                            BottomSheetDialog deleteBottomDialog = new BottomSheetDialog(mContext);
                            deleteBottomDialog.setContentView(R.layout.delete_comment_bottom_sheet_dialog1);
                            deleteBottomDialog.setCanceledOnTouchOutside(true);

                            delCommentcontent = deleteBottomDialog.findViewById(R.id.delete_comment_content);
                            delBtn = deleteBottomDialog.findViewById(R.id.delete_comment_btn);
                            progressBar = deleteBottomDialog.findViewById(R.id.progress_bar_delete_comment);
                            progressBar.setVisibility(View.GONE);

                            delCommentcontent.setText(mData.get(position).getContent());

                            delBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    delBtn.setText("");
                                    progressBar.setVisibility(View.VISIBLE);
                                    FirebaseDatabase.getInstance().getReference("Comment")
                                            .child(postId).child(mData.get(position).getCommentKey())
                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                delBtn.setText("댓글 삭제하기");
                                                progressBar.setVisibility(View.GONE);
                                                deleteBottomDialog.dismiss();
                                                Toast.makeText(mContext, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            delBtn.setText("댓글 삭제하기");
                                            progressBar.setVisibility(View.GONE);
                                            deleteBottomDialog.dismiss();
                                            Toast.makeText(mContext, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                            deleteBottomDialog.show();
                        }
                    });
                    return true;
                }
            });
        }
    }

    private void publicMyComment(final ImageView image_profile, final TextView username, final String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (snapshot.exists()) {
                    Glide.with(mContext)
                            .load(user.getPimg())
                            .apply(new RequestOptions().override(720, 720))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(image_profile);
                    username.setText(user.getName() + "(나)");
                } else {
                    Glide.with(mContext)
                            .load(R.drawable.commentbg)
                            .apply(new RequestOptions().override(720, 720))
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

    private void publicUploaderComment(final ImageView image_profile, final TextView username, final String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (snapshot.exists()) {
                    Glide.with(mContext)
                            .load(user.getPimg())
                            .apply(new RequestOptions().override(720, 720))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(image_profile);
                    username.setText(user.getName() + "(글쓴이)");
                } else {
                    Glide.with(mContext)
                            .load(R.drawable.commentbg)
                            .apply(new RequestOptions().override(720, 720))
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

    private void publisherInfo(final ImageView image_profile, final TextView username, final String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (snapshot.exists()) {
                    Glide.with(mContext)
                            .load(user.getPimg())
                            .apply(new RequestOptions().override(720, 720))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(image_profile);
                    username.setText(user.getName());
                } else {
                    Glide.with(mContext)
                            .load(R.drawable.commentbg)
                            .apply(new RequestOptions().override(720, 720))
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

    private String timestampToString(long time) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("hh:mm a", calendar).toString();
        return date;
    }
}

package com.teamhns.hnsmobile.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teamhns.hnsmobile.Activities.AuthActivity;
import com.teamhns.hnsmobile.Activities.InAppUpdateActivity;
import com.teamhns.hnsmobile.Activities.MenuActivity;
import com.teamhns.hnsmobile.Activities.PostDetailActivity;
import com.teamhns.hnsmobile.Activities.ViewAllCommentActivity;
import com.teamhns.hnsmobile.Activities.WebViewActivity;
import com.teamhns.hnsmobile.Model.Barogo;
import com.teamhns.hnsmobile.Model.User;
import com.teamhns.hnsmobile.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BarogoAdapter extends RecyclerView.Adapter<BarogoAdapter.ViewHolder> {

    private Context mContext;
    private List<Barogo> mData;
    CircleImageView dialog_signout_profile;
    FirebaseAuth mAuth;

    public BarogoAdapter(Context mContext, List<Barogo> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.row_barogo_item, parent, false);
        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull BarogoAdapter.ViewHolder holder, int position) {
        mAuth = FirebaseAuth.getInstance();
        Barogo barogo = mData.get(position);

        Glide.with(mContext).load(barogo.getImage()).into(holder.img);
        holder.title.setText(barogo.getName());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        TextView title;
        Button dialog_signout_btn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.barogo_img);
            title = itemView.findViewById(R.id.barogo_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    if (mData.get(position).getUrl().equals("signout_key")) {
                        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mContext);
                        bottomSheetDialog.setContentView(R.layout.logout_item_bottom_sheet_dialog1);
                        bottomSheetDialog.setCanceledOnTouchOutside(true);

                        dialog_signout_profile = bottomSheetDialog.findViewById(R.id.logout_dialog_profile);
                        dialog_signout_btn = bottomSheetDialog.findViewById(R.id.logout_dialog_btn);

                        bottomDialogInfo(mAuth.getCurrentUser().getUid());

                        dialog_signout_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                logout();
                                bottomSheetDialog.dismiss();
                            }
                        });

                        bottomSheetDialog.show();
                    }else if (mData.get(position).getUrl().equals("checkupdate_key")){
                        mContext.startActivity(new Intent(mContext, InAppUpdateActivity.class));
                        if (mContext instanceof Activity) {
                            ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    }else {
                        Intent webviewActivity = new Intent(mContext, WebViewActivity.class);
                        webviewActivity.putExtra("weburl", mData.get(position).getUrl());
                        webviewActivity.putExtra("webtitle", mData.get(position).getName());
                        mContext.startActivity(webviewActivity);
                        if (mContext instanceof Activity) {
                            ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    }
                }
            });
        }
    }

    public void logout(){

        GoogleSignInOptions gso = new GoogleSignInOptions.
                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                build();

        GoogleSignInClient googleSignInClient= GoogleSignIn.getClient(mContext,gso);
        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    FirebaseAuth.getInstance().signOut();
                    mContext.startActivity(new Intent(mContext, AuthActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    if (mContext instanceof Activity) {
                        ((Activity) mContext).finish();
                        ((Activity) mContext).overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }
                }
            }
        });
    }

    private void bottomDialogInfo(final String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (snapshot.exists()){
                    Glide.with(mContext)
                            .load(user.getPimg())
                            .apply(new RequestOptions().override(720, 720))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(dialog_signout_profile);

                }else {
                    Glide.with(mContext)
                            .load(R.drawable.commentbg)
                            .apply(new RequestOptions().override(720, 720))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(dialog_signout_profile);

                    //Toast.makeText(mContext, "snapshot. userinfo does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
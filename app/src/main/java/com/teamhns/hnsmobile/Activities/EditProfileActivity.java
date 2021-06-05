package com.teamhns.hnsmobile.Activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.teamhns.hnsmobile.Model.User;
import com.teamhns.hnsmobile.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    ImageView saveBtn;
    Button closeBtn;
    ProgressBar savingPb;
    CircleImageView profileImg;
    TextView useremail, userid;
    EditText editUsername, editBio;

    FirebaseAuth mAuth;
    FirebaseStorage firebaseStorage;
    DatabaseReference reference;
    String profileimgtosave;
    private Uri pickedImgUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int nightModeFlags =
                EditProfileActivity.this.getResources().getConfiguration().uiMode &
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
        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());
        firebaseStorage = FirebaseStorage.getInstance();
        saveBtn = findViewById(R.id.save_changes_btn);
        closeBtn = findViewById(R.id.editprofile_close_btn);
        savingPb = findViewById(R.id.saving_changes_progress);
        profileImg = findViewById(R.id.userImgEditProfile);
        editUsername = findViewById(R.id.edit_username);
        editBio = findViewById(R.id.edit_bio);
        useremail = findViewById(R.id.useremail);
        userid = findViewById(R.id.userid);

        getUserInfo(profileImg, editUsername, editBio, mAuth.getCurrentUser().getUid());

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*  CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setAspectRatio(1, 1)
                        .setMaxCropResultSize(1080, 1080)
                        .start(EditProfileActivity.this);*/
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setAspectRatio(1, 1)
                        .start(EditProfileActivity.this);
            }
        });

        userid.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("UID", mAuth.getCurrentUser().getUid());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(EditProfileActivity.this, "사용자 ID 복사 완료", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInfo();
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void saveUserInfo(){
        saveBtn.setVisibility(View.INVISIBLE);
        savingPb.setVisibility(View.VISIBLE);
        editUsername.setEnabled(false);
        editBio.setEnabled(false);
        profileImg.setEnabled(false);

        if(pickedImgUri == null){
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("bio", editBio.getText().toString());
            hashMap.put("name", editUsername.getText().toString());
            hashMap.put("pimg", profileimgtosave);
            hashMap.put("uid", mAuth.getCurrentUser().getUid());

            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profile_images");
            final StorageReference imageFilePath = storageReference.child(pickedImgUri.getLastPathSegment());
            imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String pictureUrl = uri.toString();

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("bio", editBio.getText().toString());
                            hashMap.put("name", editUsername.getText().toString());
                            hashMap.put("pimg", pictureUrl);
                            hashMap.put("uid", mAuth.getCurrentUser().getUid());

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        //delete previous profile image from the server
                                        if(profileimgtosave.contains("googleusercontent")){
                                            startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
                                            finish();
                                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                        }else{
                                            StorageReference imageRef = firebaseStorage.getReferenceFromUrl(profileimgtosave);
                                            imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
                                                    finish();
                                                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(EditProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
                                                    finish();
                                                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                                }
                                            });
                                        }
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(EditProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
                                    finish();
                                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    private void getUserInfo(final CircleImageView image_profile, final EditText username, final EditText bio, final String uid){
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Activity activity = EditProfileActivity.this;
                if(activity.isFinishing())
                    return;

                User user = snapshot.getValue(User.class);
                if (snapshot.exists()){
                    Glide.with(EditProfileActivity.this)
                            .load(user.getPimg())
                            .apply(new RequestOptions().override(1080, 1080))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(image_profile);
                    username.setText(user.getName());
                    bio.setText(user.getBio());
                    useremail.setText(mAuth.getCurrentUser().getEmail());
                    userid.setText(mAuth.getCurrentUser().getUid());

                    profileimgtosave = user.getPimg();
                }else {
                    Glide.with(EditProfileActivity.this)
                            .load(R.drawable.commentbg)
                            .apply(new RequestOptions().override(1080, 1080))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(image_profile);
                    username.setText("존재하지 않는 사용자");
                    bio.setText("데이터가 존재하지 않습니다.");
                    useremail.setText(mAuth.getCurrentUser().getEmail());
                    userid.setText(mAuth.getCurrentUser().getUid());

                    profileimgtosave = mAuth.getCurrentUser().getPhotoUrl().toString();
                    //Toast.makeText(mContext, "snapshot. userinfo does not exist", Toast.LENGTH_SHORT).show();
                }
                saveBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                pickedImgUri = result.getUri();
                profileImg.setImageURI(pickedImgUri);
            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
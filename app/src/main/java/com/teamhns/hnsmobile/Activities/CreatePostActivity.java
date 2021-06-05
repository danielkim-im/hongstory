package com.teamhns.hnsmobile.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.teamhns.hnsmobile.Model.Post;
import com.teamhns.hnsmobile.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;
import java.util.Locale;

public class CreatePostActivity extends AppCompatActivity {

    private static final int REQUESCODE = 1000;

    ImageView uploadBtn, addTagImg;
    Button closeBtn;
    EditText hashtagEdt, descriptionEdt;
    CheckBox privacyBox;
    ProgressBar progressBar;
    private Uri pickedImgUri = null;
    public static Boolean isConnected;
    String privacy;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int nightModeFlags =
                CreatePostActivity.this.getResources().getConfiguration().uiMode &
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
        setContentView(R.layout.activity_create_post);
        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getInstance().getCurrentUser();
        addTagImg = findViewById(R.id.add_tag_img);
        hashtagEdt = findViewById(R.id.tag_hashtag);
        descriptionEdt = findViewById(R.id.tag_description);
        privacyBox = findViewById(R.id.privacyBox);
        uploadBtn = findViewById(R.id.upload_tag_btn);
        progressBar = findViewById(R.id.add_tag_progressbar);
        closeBtn = findViewById(R.id.createpost_close_btn);

        addTagImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checkAndRequestForPermission();
     /*           CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setMaxCropResultSize(2160, 2160)
                        .start(CreatePostActivity.this);*/

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setMaxCropResultSize(4032, 4032)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .start(CreatePostActivity.this);
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnection();

                if(isConnected){
                    uploadBtn.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    hashtagEdt.setEnabled(false);
                    descriptionEdt.setEnabled(false);
                    addTagImg.setEnabled(false);
                    privacyBox.setEnabled(false);

                    if(pickedImgUri != null){
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("post_images");
                        final StorageReference imageFilePath = storageReference.child(pickedImgUri.getLastPathSegment());
                        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String pictureUrl = uri.toString();
                                        String contentType = "image";

                                        if (privacyBox.isChecked()){
                                            privacy = "private";
                                        }else{
                                            privacy = "public";
                                        }

                                        // Create post object
                                        Post post = new Post(
                                                descriptionEdt.getText().toString(),
                                                pictureUrl,
                                                hashtagEdt.getText().toString(),
                                                contentType,
                                                privacy,
                                                currentUser.getUid());

                                        // Add post to firebase database
                                        addPost(post);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // something goes wrong uploading picture

                                        showMessage(e.getMessage());
                                        uploadBtn.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.INVISIBLE);
                                        hashtagEdt.setEnabled(true);
                                        descriptionEdt.setEnabled(true);
                                        addTagImg.setEnabled(true);
                                        privacyBox.setEnabled(true);
                                    }
                                });
                            }
                        });
                    }else{
                        uploadBtn.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        hashtagEdt.setEnabled(true);
                        descriptionEdt.setEnabled(true);
                        addTagImg.setEnabled(true);
                        privacyBox.setEnabled(true);
                    }
                }else{
                    showMessage("No Internet Connection");
                }
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //openGallery();
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512, 512)
                .setMaxCropResultSize(4032, 4032)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .start(CreatePostActivity.this);
    }

    private void addPost(Post post) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Posts").push();

        //get post unique ID and update post key
        String key = myRef.getKey();
        post.setPostKey(key);

        // add post data to firebase database
        myRef.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(CreatePostActivity.this, "게시물이 업로드 되었습니다.", Toast.LENGTH_SHORT).show();
                uploadBtn.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                hashtagEdt.setEnabled(true);
                descriptionEdt.setEnabled(true);
                addTagImg.setEnabled(true);
                privacyBox.setEnabled(true);
                startActivity(new Intent(CreatePostActivity.this, ProfileActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void openGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if(resultCode == RESULT_OK && requestCode == REQUESCODE && data!=null){
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            pickedImgUri = data.getData();
            addTagImg.setImageURI(pickedImgUri);
        }*/

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                pickedImgUri = result.getUri();
                addTagImg.setImageURI(pickedImgUri);
            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
                showMessage(error.getMessage());
            }
        }
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
            isConnected = false;
        }
    }

    private String timestampToString(long time){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("MM-dd-yyyy", calendar).toString();
        return date;
    }

    private void showMessage(String message) {
        Toast.makeText(CreatePostActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
package com.teamhns.hnsmobile.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.teamhns.hnsmobile.Model.QAPost;
import com.teamhns.hnsmobile.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;
import java.util.Locale;

public class CreateQAActivity extends AppCompatActivity {

    // 선언

    private static final int REQUESCODE = 1000;

    EditText titleedt, descriptionedt;
    ImageView addtag, done;
    Button close;
    CheckBox checkbox;
    ProgressBar progressBar;
    String privacy;
    private Uri pickedImgUri = null;
    public static Boolean isConnected;

    FirebaseAuth mAuth;

    // 다크모드 인식하는 코드

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int nightModeFlags =
                CreateQAActivity.this.getResources().getConfiguration().uiMode &
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
        setContentView(R.layout.activity_create_qaactivity);
        getSupportActionBar().hide();

        // 아이디 선언

        mAuth = FirebaseAuth.getInstance();
        titleedt = findViewById(R.id.title);
        descriptionedt = findViewById(R.id.description);
        addtag = findViewById(R.id.add_tag_img);
        done = findViewById(R.id.upload_tag_btn);
        close = findViewById(R.id.createpost_close_btn);
        checkbox = findViewById(R.id.privacyBox);
        progressBar = findViewById(R.id.add_tag_progressbar);

        addtag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setMaxCropResultSize(4032, 4032)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .start(CreateQAActivity.this);
            }
        });

        /* uplodBtn = done

         */

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnection();

                if(isConnected){
                    done.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    titleedt.setEnabled(false);
                    descriptionedt.setEnabled(false);
                    addtag.setEnabled(false);
                    checkbox.setEnabled(false);

                    if(pickedImgUri != null){
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("qa_images");
                        final StorageReference imageFilePath = storageReference.child(pickedImgUri.getLastPathSegment());
                        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String pictureUrl = uri.toString();
                                        String contentType = "image";

                                        if (checkbox.isChecked()){
                                            privacy = "private";
                                        }else{
                                            privacy = "public";
                                        }

                                        // Create post object
                                        QAPost QAPost = new QAPost(
                                                descriptionedt.getText().toString(),
                                                pictureUrl,
                                                titleedt.getText().toString(),
                                                contentType,
                                                privacy,
                                                mAuth.getCurrentUser().getUid());

                                        // Add post to firebase database
                                        addPost(QAPost);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // something goes wrong uploading picture

                                        showMessage(e.getMessage());
                                        done.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.INVISIBLE);
                                        titleedt.setEnabled(true);
                                        descriptionedt.setEnabled(true);
                                        addtag.setEnabled(true);
                                        checkbox.setEnabled(true);
                                    }
                                });
                            }
                        });
                    }else if (!descriptionedt.getText().toString().isEmpty()&&pickedImgUri == null){
                        String contentType = "text";

                        if (checkbox.isChecked()){
                            privacy = "private";
                        }else{
                            privacy = "public";
                        }

                        // Create post object
                        QAPost QAPost = new QAPost(
                                descriptionedt.getText().toString(),
                                "",
                                titleedt.getText().toString(),
                                contentType,
                                privacy,
                                mAuth.getCurrentUser().getUid());

                        // Add post to firebase database
                        addPost(QAPost);
                    }else{
                        done.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        titleedt.setEnabled(true);
                        descriptionedt.setEnabled(true);
                        addtag.setEnabled(true);
                        checkbox.setEnabled(true);
                    }
                }else{
                    showMessage("No Internet Connection");
                }
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void addPost(QAPost QAPost) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("QAPosts").push();

        //get post unique ID and update post key
        String key = myRef.getKey();
        QAPost.setPostKey(key);

        // add post data to firebase database
        myRef.setValue(QAPost).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(CreateQAActivity.this, "질문이 업로드 되었습니다.", Toast.LENGTH_SHORT).show();
                done.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                titleedt.setEnabled(true);
                descriptionedt.setEnabled(true);
                addtag.setEnabled(true);
                checkbox.setEnabled(true);
                startActivity(new Intent(CreateQAActivity.this, QuestionActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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
                addtag.setImageURI(pickedImgUri);
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
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) isConnected = true;
            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) isConnected = true;
        }
        else isConnected = false;
    }

    private String timestampToString(long time){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("MM-dd-yyyy", calendar).toString();
        return date;
    }

    private void showMessage(String message) {
        Toast.makeText(CreateQAActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
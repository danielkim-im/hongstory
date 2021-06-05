package com.teamhns.hnsmobile.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.teamhns.hnsmobile.R;

import java.util.HashMap;

public class AuthActivity extends AppCompatActivity {

    ConstraintLayout signinGoogleBtn;
    TextView noticeTxt;
    ImageView authTopImg;
    boolean isDarkmode = false;

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private GoogleSignInClient mGoogleSignInClient;
    private final static  int RC_SIGN_IN = 123;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int nightModeFlags =
                AuthActivity.this.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                setTheme(R.style.darkTheme);
                isDarkmode = true;
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                setTheme(R.style.AppTheme);
                isDarkmode = false;
                break;

            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                setTheme(R.style.AppTheme);
                isDarkmode = false;
                break;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        getSupportActionBar().hide();

        noticeTxt = findViewById(R.id.auth_bottom_notice);
        authTopImg = findViewById(R.id.auth_topimg);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        signinGoogleBtn = findViewById(R.id.signinGoogle);
        firebaseDatabase = FirebaseDatabase.getInstance();

        //set top img
        if (isDarkmode == true){
            Picasso.get()
                    .load(R.drawable.auth_img_dark_blur)
                    .placeholder(R.drawable.auth_img_default_blur)
                    .error(R.drawable.loading_placeholder)
                    .into(authTopImg);
        }else{
            Picasso.get()
                    .load(R.drawable.auth_img_default_blur)
                    .placeholder(R.drawable.auth_img_default_blur)
                    .error(R.drawable.loading_placeholder)
                    .into(authTopImg);
        }

        signinGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        SpannableString ss = new SpannableString("계속 진행하면 홍스토리의 이용약관 및 개인정보\n처리방침에 동의한 것으로 간주됩니다.");

        ClickableSpan tou = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Uri uri = Uri.parse("https://sites.google.com/view/hnstermsofuse/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        };

        ClickableSpan pp = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Uri uri = Uri.parse("https://sites.google.com/view/hnsprivacypolicy/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        };

        ss.setSpan(tou, 14, 18, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(pp, 21, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        noticeTxt.setText(ss);
        noticeTxt.setMovementMethod(LinkMovementMethod.getInstance());

        createRequest();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null && mAuth.getCurrentUser().isEmailVerified()){
            //user is already connected so we need to redirect the user to home page
            startActivity(new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        }
    }

    private void createRequest() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void firebaseAuthWithGoogle(String idToken){
        ProgressDialog pd = new ProgressDialog(AuthActivity.this);
        pd.setMessage("로그인하는 중...");
        pd.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    if(mAuth.getCurrentUser().getEmail().contains("hongchun.hs.kr")||mAuth.getCurrentUser().getEmail().equals("yolastudio05@gmail.com")){
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
                        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    pd.dismiss();
                                    startActivity(new Intent(AuthActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                    finish();
                                }else{
                                    databaseReference = firebaseDatabase.getReference().child("Users").child(mAuth.getCurrentUser().getUid());

                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("uid", mAuth.getCurrentUser().getUid());
                                    hashMap.put("name", mAuth.getCurrentUser().getDisplayName());
                                    hashMap.put("bio", "");
                                    hashMap.put("pimg", mAuth.getCurrentUser().getPhotoUrl().toString());

                                    databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                pd.dismiss();
                                                startActivity(new Intent(AuthActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                                finish();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(AuthActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }else {
                        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    pd.dismiss();
                                    Toast.makeText(AuthActivity.this, "용인홍천고등학교 구글 이메일로 로그인해주세요. (예:학번@hongchun.hs.kr)", Toast.LENGTH_LONG).show();
                                    mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                FirebaseAuth.getInstance().signOut(); // very important if you are using firebase.
                                            }
                                        }
                                    });

                                }else{
                                    pd.dismiss();
                                    Toast.makeText(AuthActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }else{
                    pd.dismiss();
                    Toast.makeText(AuthActivity.this, "Error: Please try again later", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AuthActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
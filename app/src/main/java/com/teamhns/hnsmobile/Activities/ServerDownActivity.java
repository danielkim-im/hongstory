package com.teamhns.hnsmobile.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.teamhns.hnsmobile.R;

public class ServerDownActivity extends AppCompatActivity {

    Button refreshBtn;
    ProgressBar progressBar;
    FirebaseRemoteConfig firebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int nightModeFlags =
                ServerDownActivity.this.getResources().getConfiguration().uiMode &
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
        setContentView(R.layout.activity_server_down);
        getSupportActionBar().hide();

        refreshBtn = findViewById(R.id.serverdown_refreshBtn);
        progressBar = findViewById(R.id.progressbar_serverdown);

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshBtn.setText("");
                refreshBtn.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                firebaseRemoteConfig.fetch(0)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    firebaseRemoteConfig.activate();
                                    if(firebaseRemoteConfig.getBoolean("server_under_maintenance") == true){
                                        refreshBtn.setText("새로고침");
                                        refreshBtn.setEnabled(true);
                                        progressBar.setVisibility(View.GONE);
                                    }else if(firebaseRemoteConfig.getBoolean("server_under_maintenance") == false){
                                        refreshBtn.setText("새로고침");
                                        refreshBtn.setEnabled(true);
                                        progressBar.setVisibility(View.GONE);
                                        startActivity(new Intent(ServerDownActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                        finish();
                                    }
                                }else {
                                    refreshBtn.setText("새로고침");
                                    refreshBtn.setEnabled(true);
                                    progressBar.setVisibility(View.GONE);
                                    //Firebase Remote Config is not responding
                                }
                            }
                        });
            }
        });

        fetchRemoteConfigData();
    }

    private void fetchRemoteConfigData() {
        refreshBtn.setText("");
        refreshBtn.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        firebaseRemoteConfig.fetch(0)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            firebaseRemoteConfig.activate();
                            if(firebaseRemoteConfig.getBoolean("server_under_maintenance") == true){
                                refreshBtn.setText("새로고침");
                                refreshBtn.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                            }else if(firebaseRemoteConfig.getBoolean("server_under_maintenance") == false){
                                refreshBtn.setText("새로고침");
                                refreshBtn.setEnabled(true);
                                progressBar.setVisibility(View.GONE);
                                startActivity(new Intent(ServerDownActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            }
                        }else {
                            refreshBtn.setText("새로고침");
                            refreshBtn.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            //Firebase Remote Config is not responding
                        }
                    }
                });
    }
}
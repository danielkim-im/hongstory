package com.teamhns.hnsmobile.Activities;

import android.content.IntentSender;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.teamhns.hnsmobile.BuildConfig;
import com.teamhns.hnsmobile.R;

public class InAppUpdateActivity extends AppCompatActivity {

    TextView appVersionTxt, usingLatestVersionTxt;
    Button backBtn;
    AppUpdateManager appUpdateManager;
    ProgressBar progressBar;
    int RequestUpdate = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int nightModeFlags =
                InAppUpdateActivity.this.getResources().getConfiguration().uiMode &
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
        setContentView(R.layout.activity_in_app_update);
        getSupportActionBar().hide();

        appVersionTxt = findViewById(R.id.appVersionTxt);
        usingLatestVersionTxt = findViewById(R.id.usingLatestVersionTxt);
        backBtn = findViewById(R.id.backBtn3);
        progressBar = findViewById(R.id.iauProgressbar);

        appVersionTxt.setText(BuildConfig.VERSION_NAME);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        checkforUpdate();
    }

    private void checkforUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(InAppUpdateActivity.this);
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(new com.google.android.play.core.tasks.OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(final AppUpdateInfo result) {
                progressBar.setVisibility(View.GONE);
                if ((result.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE)
                        && result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(result,
                                AppUpdateType.IMMEDIATE,
                                InAppUpdateActivity.this,
                                RequestUpdate);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
package com.teamhns.hnsmobile.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.teamhns.hnsmobile.Fragments.ReportSingleChoiceDialogFragment;
import com.teamhns.hnsmobile.Model.ReportComment;
import com.teamhns.hnsmobile.Model.ReportPost;
import com.teamhns.hnsmobile.R;

public class ReportCommentActivity extends AppCompatActivity implements ReportSingleChoiceDialogFragment.SingleChoiceListener{

    String commentKey;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int nightModeFlags =
                ReportCommentActivity.this.getResources().getConfiguration().uiMode &
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
        setContentView(R.layout.activity_report_comment);
        getSupportActionBar().hide();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        commentKey = getIntent().getExtras().getString("commentKey");

        DialogFragment singleChoiceDiailog = new ReportSingleChoiceDialogFragment();
        singleChoiceDiailog.setCancelable(true);
        singleChoiceDiailog.show(getSupportFragmentManager(), "Single Choice Dialog");
    }

    @Override
    public void onPositiveButtonClicked(String[] list, int position) {
        ReportComment reportComment = new ReportComment(
                commentKey,
                firebaseAuth.getCurrentUser().getUid(),
                list[position]
        );
        reportTag(reportComment);
    }

    @Override
    public void onNegativeButtonClicked() {
        finish();
    }

    private void reportTag(ReportComment reportComment) {
        DatabaseReference myRef = firebaseDatabase.getReference("ReportedComment").child(commentKey).push();

        //get post unique ID and update post key
        String key = myRef.getKey();
        reportComment.setReportKey(key);

        // add post data to firebase database
        myRef.setValue(reportComment).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ReportCommentActivity.this, "신고 완료.", Toast.LENGTH_SHORT).show();
                finish();

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void finish(){
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
package com.teamhns.hnsmobile.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.teamhns.hnsmobile.Adapter.SearchPostAdapter;
import com.teamhns.hnsmobile.Model.Post;
import com.teamhns.hnsmobile.R;

import java.util.ArrayList;
import java.util.List;

public class SearchPostActivity extends AppCompatActivity {

    EditText searchEdtx;
    Button backBtn;
    RecyclerView recyclerView;
    SearchPostAdapter searchPostAdapter;
    List postList;

    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int nightModeFlags =
                SearchPostActivity.this.getResources().getConfiguration().uiMode &
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
        setContentView(R.layout.activity_search_post);
        getSupportActionBar().hide();

        searchEdtx = findViewById(R.id.search_image_edittext);
        backBtn = findViewById(R.id.backBtn12);
        recyclerView = findViewById(R.id.searchRv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        postList = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getInstance().getReference("Posts");

        searchEdtx.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEdtx,0);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        if (searchEdtx.getText().toString().isEmpty()) {
            getPost();
        }

        searchEdtx.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchEdtx.getText().toString().isEmpty()) {
                    getPost();
                } else {
                    searchPost(searchEdtx.getText().toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        getPost();
    }

    private void getPost(){
        Query query;
        query = databaseReference.limitToLast(50);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot postsnap : dataSnapshot.getChildren()) {
                    Post tag = postsnap.getValue(Post.class);
                    if (!tag.getUserId().equals(mAuth.getCurrentUser().getUid())){
                        postList.add(tag);
                    }
                }

                searchPostAdapter = new SearchPostAdapter(SearchPostActivity.this, postList);
                searchPostAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(searchPostAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void searchPost(final String query) {
        Query query1;
        query1 = databaseReference.limitToLast(500);

        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot postsnap : dataSnapshot.getChildren()) {
                    Post tag = postsnap.getValue(Post.class);

                    if (tag.getHashtag().toLowerCase().contains(query.toLowerCase())
                            || tag.getDescription().toLowerCase().contains(query.toLowerCase())) {
                        postList.add(tag);
                    }
                }

                searchPostAdapter = new SearchPostAdapter(SearchPostActivity.this, postList);
                searchPostAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(searchPostAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
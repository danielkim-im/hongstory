package com.teamhns.hnsmobile.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.squareup.picasso.Picasso;
import com.teamhns.hnsmobile.Adapter.BarogoAdapter;
import com.teamhns.hnsmobile.Model.Barogo;
import com.teamhns.hnsmobile.Model.User;
import com.teamhns.hnsmobile.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MenuActivity extends AppCompatActivity {

    Button closeBtn;
    CardView cardView;
    TextView username, useremail, tempTxt, locationTxt;
    ImageView weatherIcon;
    RecyclerView barogo_rv;
    CircleImageView profileImg;
    List barogoItemList;
    FirebaseRemoteConfig firebaseRemoteConfig;
    BarogoAdapter barogoAdapter;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int nightModeFlags =
                MenuActivity.this.getResources().getConfiguration().uiMode &
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
        setContentView(R.layout.activity_menu);
        getSupportActionBar().hide();

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        closeBtn = findViewById(R.id.menu_close_btn);
        username = findViewById(R.id.menu_username);
        useremail = findViewById(R.id.menu_useremail);
        profileImg = findViewById(R.id.menu_profileimg);
        tempTxt = findViewById(R.id.tempTxt);
        weatherIcon = findViewById(R.id.weatherIcon);
        locationTxt = findViewById(R.id.locationTxt);
        barogo_rv = findViewById(R.id.barogo_rv);
        cardView = findViewById(R.id.video_cardview);
        barogo_rv.setHasFixedSize(true);
        barogo_rv.setLayoutManager(new GridLayoutManager(MenuActivity.this, 4, RecyclerView.VERTICAL, false));

        mAuth = FirebaseAuth.getInstance();

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        weatherIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent webviewActivity = new Intent(MenuActivity.this, WebViewActivity.class);
                webviewActivity.putExtra("weburl", "https://openweathermap.org/city/1832427");
                webviewActivity.putExtra("webtitle", "용인시 날씨");
                startActivity(webviewActivity);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        tempTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent webviewActivity = new Intent(MenuActivity.this, WebViewActivity.class);
                webviewActivity.putExtra("weburl", "https://openweathermap.org/city/1832427");
                webviewActivity.putExtra("webtitle", "용인시 날씨");
                startActivity(webviewActivity);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        useremail.setText(mAuth.getCurrentUser().getEmail());

        getUserInfo(profileImg, username, mAuth.getCurrentUser().getUid());

        getWeatherInfo();
        getBarogoItems();
    }

    private void getBarogoItems() {
        barogoItemList = new ArrayList<>();
        barogoItemList.add(new Barogo("용인홍천고등학교", "http://www.hongchun.hs.kr/", getDrawable(R.drawable.ic_hongchun)));
        barogoItemList.add(new Barogo("경기도교육청", "https://www.goe.go.kr/", getDrawable(R.drawable.ic_gyeonggido)));
        barogoItemList.add(new Barogo("보건복지부", "http://www.mohw.go.kr/", getDrawable(R.drawable.ic_bogun)));
        barogoItemList.add(new Barogo("질병관리청", "https://kdca.go.kr/index.es?sid=a2", getDrawable(R.drawable.ic_jilbyung)));
        barogoItemList.add(new Barogo("용인서부경찰서", "https://www.ggpolice.go.kr/yisb", getDrawable(R.drawable.ic_subupolice)));
        barogoItemList.add(new Barogo("업데이트 확인", "checkupdate_key", getDrawable(R.drawable.ic_googleplay)));
        barogoItemList.add(new Barogo("개인정보 처리방침", "https://sites.google.com/view/hnsprivacypolicy/", getDrawable(R.drawable.ic_privacypolicy)));
        barogoItemList.add(new Barogo("이용약관", "https://sites.google.com/view/hnstermsofuse/", getDrawable(R.drawable.ic_termsofuse)));
        barogoItemList.add(new Barogo("로그아웃", "signout_key", getDrawable(R.drawable.ic_logout)));
        barogoAdapter = new BarogoAdapter(MenuActivity.this, barogoItemList);
        barogo_rv.setAdapter(barogoAdapter);
    }

    private void getWeatherInfo() {
        String url = "http://api.openweathermap.org/data/2.5/weather?q=Yongin&appid=b4440c7bb0c833cc70b622f3c454569f&units=metric";

        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject main_object = response.getJSONObject("main");
                    JSONArray array = response.getJSONArray("weather");
                    JSONObject object = array.getJSONObject(0);
                    String temp = String.valueOf(main_object.getDouble("temp"));
                    String iconurl = object.getString("icon");

                    tempTxt.setText(temp+"°C");
                    Picasso.get().load("http://openweathermap.org/img/wn/"+iconurl+"@2x.png").into(weatherIcon);

                    locationTxt.setVisibility(View.VISIBLE);
                    tempTxt.setVisibility(View.VISIBLE);
                    weatherIcon.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(MenuActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                locationTxt.setVisibility(View.GONE);
                tempTxt.setVisibility(View.GONE);
                weatherIcon.setVisibility(View.GONE);
            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jor);
    }

    private void getUserInfo(final CircleImageView image_profile, final TextView username, final String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Activity activity = MenuActivity.this;
                if(activity.isFinishing())
                    return;

                User user = snapshot.getValue(User.class);

                if (snapshot.exists()){
                    Glide.with(MenuActivity.this)
                            .load(user.getPimg())
                            .apply(new RequestOptions().override(720, 720))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(image_profile);
                    username.setText(user.getName());
                }else {
                    Glide.with(MenuActivity.this)
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
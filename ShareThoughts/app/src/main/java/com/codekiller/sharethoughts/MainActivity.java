package com.codekiller.sharethoughts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.codekiller.sharethoughts.UI.UIUtils;
import com.codekiller.sharethoughts.firebase.FBDatabase;
import com.codekiller.sharethoughts.firebase.FBStorage;
import com.codekiller.sharethoughts.firebase.FBUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MAIN ACTIVITY";

    Toolbar toolbar;
    RecyclerView recyclerView;
    BottomNavigationView bottomNavigationView;
    CircleImageView userImage;
    TextView userName;
    ImageView addPostBtn;

    FBUtils fbUtils;
    FBStorage fbStorage;
    FBDatabase fbDatabase;
    UIUtils uiUtils;
    HashMap<String,String> map;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recycler_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        userImage = findViewById(R.id.user_image);
        userName = findViewById(R.id.user_name);
        addPostBtn = findViewById(R.id.add_post_btn);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fbUtils = new FBUtils();
        fbDatabase = new FBDatabase();
        fbStorage = new FBStorage();
        uiUtils = new UIUtils(this);

        initDatas();

        bottomNavigationView.setSelectedItemId(R.id.home_btn);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch ( item.getItemId() ){
                    case R.id.notification_btn:
                        break;
                    case R.id.chats_btn:
                        break;
                    case R.id.settings_btn:
                        break;
                    case R.id.friends_btn:
                        break;
                }
                return true;
            }
        });
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,CreatepostActivity.class));
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initDatas() {
        map = new HashMap<>();
        fbDatabase.getDB("Users")
                .child(fbUtils.getUID())
                .child("profile")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        map = (HashMap<String, String>) snapshot.getValue();
                        if( map.get("imageurl").equals("default") ){
                            Glide.with(MainActivity.this)
                                    .load(R.drawable.rought_usericon)
                                    .into(userImage);
                        }else{
                            Glide.with(MainActivity.this)
                                    .load(map.get("imageurl"))
                                    .into(userImage);
                        }
                        userName.setText(map.get("username"));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        uiUtils.toast("db error");
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if( item.getItemId() == R.id.logout ){
            fbUtils.signOut();
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        }
        return true;
    }
}
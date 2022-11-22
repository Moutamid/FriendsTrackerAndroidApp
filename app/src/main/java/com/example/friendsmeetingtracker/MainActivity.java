package com.example.friendsmeetingtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.example.friendsmeetingtracker.Fragments.HomeScreen;
import com.example.friendsmeetingtracker.Fragments.MeetingRooms;
import com.example.friendsmeetingtracker.Fragments.ProfileScreen;
import com.example.friendsmeetingtracker.databinding.ActivityLoginScreenBinding;
import com.example.friendsmeetingtracker.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding b;
    BottomNavigationView navigationView;
    FrameLayout fragmentLayouts;
    private String reredirect = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        navigationView = findViewById(R.id.bottomNavigation);
        fragmentLayouts = findViewById(R.id.fragment_container);
        navigationView.setItemIconTintList(null);
      //  reredirect = getIntent().getStringExtra("redirect");
        if (getIntent().getStringExtra("redirect") != null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new MeetingRooms()).commit();
            navigationView.setSelectedItemId(R.id.meeting_room);
        }else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeScreen()).commit();
            navigationView.setSelectedItemId(R.id.home_menu);
        }
        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_menu:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new HomeScreen()).commit();
                        break;
                    case R.id.meeting_room:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                new MeetingRooms()).commit();
                        break;
                    case R.id.profile_menu:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                                , new ProfileScreen()).commit();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

    }
}
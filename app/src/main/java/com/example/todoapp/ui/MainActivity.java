package com.example.todoapp.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.todoapp.R;
import com.example.todoapp.ui.fragment.CompletedFragment;
import com.example.todoapp.ui.fragment.ProfileFragment;
import com.example.todoapp.ui.fragment.TaskFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        //Kiểm tra đăng nhập
        //Nếu chưa đăng nhập, đá về Login ngay
        if (mAuth.getCurrentUser() == null) {
            goToLogin();
            return; //Dừng onCreate nếu chưa đăng nhập
        }

        bottomNav = findViewById(R.id.bottom_navigation);

        //Xử lý sự kiện nhấn nút
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_tasks) {
                selectedFragment = new TaskFragment();
            } else if (itemId == R.id.nav_completed) {
                selectedFragment = new CompletedFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });

        //Tải Fragment mặc định (TasksFragment)
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_tasks); // Đặt nút Tasks là mặc định
            loadFragment(new TaskFragment());
        }
    }

    //Hàm helper để thay đổi Fragment
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    //Hàm goToLogin
    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
package com.example.todoapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todoapp.R;
import com.example.todoapp.dao.UserDao; // Cần UserDao
import com.example.todoapp.model.User; // Cần User model
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView ivProfileImage;
    private TextView tvUsername, tvEmail;
    private Button btnLogoutProfile;

    private FirebaseAuth mAuth;
    private UserDao userDao;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        userDao = new UserDao();
        currentUser = mAuth.getCurrentUser();

        toolbar = findViewById(R.id.toolbarProfile);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        btnLogoutProfile = findViewById(R.id.btnLogoutProfile);

        toolbar.setNavigationOnClickListener(v -> finish());

        btnLogoutProfile.setOnClickListener(v -> {
            mAuth.signOut();
            goToLogin();
        });

        if (currentUser != null) {
            loadUserProfile();
        }
    }

    private void loadUserProfile() {
        String uid = currentUser.getUid();

        //Tải ảnh (nếu dùng Google/FB login)
        //Glide.with(this).load(currentUser.getPhotoUrl()).into(ivProfileImage);

        //Lấy thông tin từ Realtime Database (username)
        userDao.getUserInfo(uid, new UserDao.FirebaseUserCallback() {
            @Override
            public void onSuccess(User user) {
                tvUsername.setText(user.getUsername());
                tvEmail.setText(user.getEmail()); // Email từ DB
            }

            @Override
            public void onFailure(String error) {
                //Nếu thất bại, ít nhất hiển thị email từ Auth
                tvEmail.setText(currentUser.getEmail());
                tvUsername.setText(currentUser.getDisplayName()); // Tên từ Google/FB
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
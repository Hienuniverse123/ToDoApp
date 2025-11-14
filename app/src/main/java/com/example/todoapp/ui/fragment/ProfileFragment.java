package com.example.todoapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.todoapp.R;
import com.example.todoapp.dao.UserDao;
import com.example.todoapp.model.User;
import com.example.todoapp.ui.LoginActivity;
import com.example.todoapp.ui.NotificationSettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.bumptech.glide.Glide;

public class ProfileFragment extends Fragment {

    private ImageView ivProfileImage;
    private TextView tvUsername, tvEmail;
    private Button btnLogoutProfile, btnNotificationSettings;

    private FirebaseAuth mAuth;
    private UserDao userDao;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        userDao = new UserDao();
        currentUser = mAuth.getCurrentUser();

        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnLogoutProfile = view.findViewById(R.id.btnLogoutProfile);
        btnNotificationSettings = view.findViewById(R.id.btnNotificationSettings);

        btnLogoutProfile.setOnClickListener(v -> {
            mAuth.signOut();
            goToLogin();
        });

        // Mở màn hình cài đặt
        btnNotificationSettings.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), NotificationSettingsActivity.class));
        });

        if (currentUser != null) {
            loadUserProfile();
        }

        return view;
    }

    private void loadUserProfile() {
        String uid = currentUser.getUid();

        // Tải ảnh (từ Google/FB)
        if (currentUser.getPhotoUrl() != null) {
            Glide.with(this).load(currentUser.getPhotoUrl()).circleCrop().into(ivProfileImage);
        }

        // Lấy thông tin từ Realtime Database (username)
        userDao.getUserInfo(uid, new UserDao.FirebaseUserCallback() {
            @Override
            public void onSuccess(User user) {
                tvUsername.setText(user.getUsername());
                tvEmail.setText(user.getEmail());
            }
            @Override
            public void onFailure(String error) {
                // Nếu thất bại, ít nhất hiển thị từ Auth
                tvEmail.setText(currentUser.getEmail());
                if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                    tvUsername.setText(currentUser.getDisplayName());
                }
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}
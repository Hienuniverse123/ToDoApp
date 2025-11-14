package com.example.todoapp.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.todoapp.R;
import com.example.todoapp.dao.UserDao;
import com.example.todoapp.model.User;
import com.example.todoapp.ui.LoginActivity;
import com.example.todoapp.ui.NotificationSettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private ImageView ivProfileImage;
    private TextView tvUsername, tvEmail;
    private LinearLayout llTheme, llLanguage, llNotifications;
    private Button btnLogoutProfile;

    private FirebaseAuth mAuth;
    private UserDao userDao;
    private FirebaseUser currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        userDao = new UserDao();
        currentUser = mAuth.getCurrentUser();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Ánh xạ các views
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        llTheme = view.findViewById(R.id.llTheme);
        llLanguage = view.findViewById(R.id.llLanguage);
        llNotifications = view.findViewById(R.id.llNotifications);
        btnLogoutProfile = view.findViewById(R.id.btnLogoutProfile);

        // Đặt sự kiện click
        llTheme.setOnClickListener(v -> showThemeDialog());
        llLanguage.setOnClickListener(v -> showLanguageSettings());
        llNotifications.setOnClickListener(v ->
                startActivity(new Intent(getContext(), NotificationSettingsActivity.class))
        );
        btnLogoutProfile.setOnClickListener(v -> showLogoutConfirmation());

        // Tải thông tin
        if (currentUser != null) {
            loadUserProfile();
        }

        return view;
    }

    private void loadUserProfile() {
        String uid = currentUser.getUid();

        // Tải ảnh đại diện (nếu có)
        userDao.getUserInfo(uid, new UserDao.FirebaseUserCallback() {
            @Override
            public void onSuccess(User user) {
                tvUsername.setText(user.getUsername());
                tvEmail.setText(user.getEmail());

                // Chỉ hiển thị ảnh từ Google/Facebook (nếu có)
                if (currentUser.getPhotoUrl() != null) {
                    Glide.with(getContext()).load(currentUser.getPhotoUrl()).circleCrop().into(ivProfileImage);
                }
            }
            @Override
            public void onFailure(String error) {
                // Hiển thị thông tin cơ bản từ Auth
                tvEmail.setText(currentUser.getEmail());
                if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                    tvUsername.setText(currentUser.getDisplayName());
                }
                if (currentUser.getPhotoUrl() != null) {
                    Glide.with(getContext()).load(currentUser.getPhotoUrl()).circleCrop().into(ivProfileImage);
                }
            }
        });
    }

    //Giao diện Sáng/Tối
    private void showThemeDialog() {
        final String[] themes = {"Sáng", "Tối", "Mặc định hệ thống"};

        int currentNightMode = AppCompatDelegate.getDefaultNightMode();
        int checkedItem;
        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            checkedItem = 1; // Tối
        } else if (currentNightMode == AppCompatDelegate.MODE_NIGHT_NO) {
            checkedItem = 0; // Sáng
        } else {
            checkedItem = 2; // Hệ thống
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Chọn giao diện")
                .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                    if (which == 0) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    } else if (which == 1) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    }
                    dialog.dismiss();
                })
                .show();
    }

    //Ngôn ngữ
    private void showLanguageSettings() {
        Toast.makeText(getContext(), "Mở Cài đặt hệ thống cho ứng dụng", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    //Đăng xuất
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(getContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    mAuth.signOut();
                    goToLogin();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void goToLogin() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
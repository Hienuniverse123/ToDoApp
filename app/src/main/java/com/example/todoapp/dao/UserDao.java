package com.example.todoapp.dao;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.todoapp.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserDao {
    private final DatabaseReference usersRef;

    public UserDao() {
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }
    public void saveUserInfo(User user, FirebaseCallback callback) {
        if (user.getId() == null || user.getId().isEmpty()) {
            callback.onFailure("User ID không hợp lệ (UID từ Auth)");
            return;
        }
        //Dùng UID từ Authentication làm key
        usersRef.child(user.getId()).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("UserDao", "Lưu thông tin thành công cho " + user.getEmail());
                    callback.onSuccess("Lưu thông tin thành công!");
                })
                .addOnFailureListener(e -> {
                    Log.e("UserDao", "Lỗi khi lưu thông tin", e);
                    callback.onFailure("Lỗi khi lưu thông tin: " + e.getMessage());
                });
    }

    public void getUserInfo(String uid, FirebaseUserCallback callback) {
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        //Gán lại ID (vì key chính là ID)
                        user.setId(snapshot.getKey());
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("Không thể phân tích dữ liệu user");
                    }
                } else {
                    callback.onFailure("Không tìm thấy thông tin user cho UID này");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    //Callback chung
    public interface FirebaseCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    //Callback lấy thông tin (trả về User)
    public interface FirebaseUserCallback {
        void onSuccess(User user);
        void onFailure(String error);
    }
}
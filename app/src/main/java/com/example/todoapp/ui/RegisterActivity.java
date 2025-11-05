package com.example.todoapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.todoapp.R;
import com.example.todoapp.dao.UserDao;
import com.example.todoapp.model.User;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText edtUsername, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private UserDao userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        userDAO = new UserDao();

        btnRegister.setOnClickListener(view -> registerUser());
    }

    private void registerUser() {
        String username = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Đăng ký thành công trên Authentication
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser == null) {
                            Toast.makeText(this, "Không thể lấy thông tin user", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String uid = firebaseUser.getUid();

                        User user = new User();
                        user.setId(uid); // DÙNG UID TỪ AUTHENTICATION LÀM ID
                        user.setUsername(username);
                        user.setEmail(email);

                        userDAO.saveUserInfo(user, new UserDao.FirebaseCallback() {
                            @Override
                            public void onSuccess(String message) {
                                Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                finish(); // Quay lại trang Login
                            }

                            @Override
                            public void onFailure(String error) {
                                // Vẫn thành công, nhưng chỉ là không lưu được info
                                Toast.makeText(RegisterActivity.this, "Đăng ký thành công (lỗi lưu info: " + error + ")", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });

                    } else {
                        // Đăng ký thất bại (ví dụ: email đã tồn tại, mật khẩu yếu)
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}

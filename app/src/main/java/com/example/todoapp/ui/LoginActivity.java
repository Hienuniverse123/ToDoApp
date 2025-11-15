package com.example.todoapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todoapp.R;
import com.example.todoapp.dao.UserDao;
import com.example.todoapp.model.User;
import com.facebook.*;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1000;
    private EditText etUsername, etPassword;
    private Button btnLogin, btnGoogle, btnFacebook;
    private TextView tvRegister;
    private UserDao userDAO;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;
    private FirebaseAuth mAuth; //Khai báo FirebaseAuth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Khởi tạo
        mAuth = FirebaseAuth.getInstance();
        userDAO = new UserDao();

        //Ánh xạ view
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        tvRegister = findViewById(R.id.tvRegister);

        //Đăng nhập thường
        btnLogin.setOnClickListener(v -> {
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            //Dùng FirebaseAuth để đăng nhập
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            //Đăng nhập thành công
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            goToMainActivity();
                        } else {
                            //Đăng nhập thất bại
                            Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        //Mở trang đăng ký
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        //Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        //Facebook Login
        callbackManager = CallbackManager.Factory.create();

        btnFacebook.setOnClickListener(v ->
                LoginManager.getInstance().logInWithReadPermissions(
                        this, Arrays.asList("email", "public_profile")));

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //Đăng nhập Facebook thành công, giờ dùng token đó đăng nhập vào Firebase
                Log.d("FacebookLogin", "Đăng nhập Facebook thành công. Token: " + loginResult.getAccessToken().getToken());
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Đã hủy đăng nhập", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Lỗi Facebook: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("FacebookLogin", "Lỗi Facebook: ", error);
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Google
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else {
            // Facebook
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    //Hàm xử lý cho Google
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account == null) {
                Toast.makeText(this, "Không nhận được tài khoản Google", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("GoogleLogin", "Đã lấy được tài khoản Google: " + account.getEmail());
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            firebaseAuthWithCredential(credential, account.getDisplayName(), account.getEmail());

        } catch (ApiException e) {
            Toast.makeText(this, "Đăng nhập Google thất bại (API): " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            Log.e("GoogleLogin", "Lỗi API Google: ", e);
        }
    }

    //Hàm xử lý cho Facebook
    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuthWithCredential(credential, null, null); // Tên và email sẽ lấy từ FirebaseUser
    }

    //Hàm chung để xác thực Firebase
    private void firebaseAuthWithCredential(AuthCredential credential, @Nullable String displayName, @Nullable String email) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, authTask -> {
                    if (authTask.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        //Kiểm tra xem đây có phải là người dùng mới không
                        boolean isNewUser = authTask.getResult().getAdditionalUserInfo().isNewUser();

                        if (isNewUser && user != null) {
                            //Nếu là user mới, lưu thông tin vào Realtime Database
                            String uid = user.getUid();
                            //Lấy tên/email. Ưu tiên lấy từ provider (Google) hoặc từ Firebase Auth profile
                            String nameToSave = (displayName != null) ? displayName : user.getDisplayName();
                            String emailToSave = (email != null) ? email : user.getEmail();

                            Log.d("FirebaseAuth", "User mới, đang lưu vào Database...");
                            User newUser = new User(uid, nameToSave, emailToSave);
                            userDAO.saveUserInfo(newUser, new UserDao.FirebaseCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    Log.d("FirebaseAuth", "Lưu thông tin user mới thành công.");
                                }
                                @Override
                                public void onFailure(String error) {
                                    Log.e("FirebaseAuth", "Lỗi lưu thông tin user mới: " + error);
                                }
                            });
                        }

                        Toast.makeText(this, "Chào " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        goToMainActivity();

                    } else {
                        // Xác thực Firebase thất bại
                        Toast.makeText(this, "Xác thực Firebase thất bại: " + authTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    //Hàm chuyển màn hình
    private void goToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish(); // Đóng LoginActivity để người dùng không quay lại được
    }
}
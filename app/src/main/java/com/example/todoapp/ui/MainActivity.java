package com.example.todoapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import đúng loại Toolbar
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.R;
import com.example.todoapp.adapter.TaskAdapter;
import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.model.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TaskDao taskDao;

    private RecyclerView recyclerViewTasks;
    private FloatingActionButton fabAddTask;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private Toolbar toolbar;

    private TaskAdapter taskAdapter;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        taskDao = new TaskDao();

        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        fabAddTask = findViewById(R.id.fabAddTask);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState); // Ánh xạ TextView
        toolbar = findViewById(R.id.toolbar); // Ánh xạ Toolbar

        // Thiết lập Toolbar làm ActionBar
        setSupportActionBar(toolbar);
        // getSupportActionBar().setTitle("Công việc của bạn"); // Tiêu đề đã set trong XML

        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(this, taskList);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTasks.setAdapter(taskAdapter);

        fabAddTask.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddTaskActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserAndLoadTasks();
    }

    private void checkUserAndLoadTasks() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            goToLogin();
        } else {
            loadTasks(currentUser.getUid());
        }
    }

    private void loadTasks(String userId) {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE); // Ẩn EmptyState khi đang tải

        taskDao.getTasksByUser(userId, new TaskDao.FirebaseTaskListCallback() {
            @Override
            public void onSuccess(List<Task> tasks) {
                progressBar.setVisibility(View.GONE);

                taskList.clear();
                taskList.addAll(tasks);
                taskAdapter.notifyDataSetChanged();

                if (tasks.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE); // Hiển thị EmptyState nếu không có task
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.GONE); // Đảm bảo ẩn EmptyState khi có lỗi
                Toast.makeText(MainActivity.this, "Lỗi tải dữ liệu: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // --- Xử lý Menu cho Toolbar (Nút đăng xuất) ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu); // Tải menu từ file XML
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            mAuth.signOut();
            goToLogin();
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_completed_task) {
            startActivity(new Intent(MainActivity.this, CompletedTaskActivity.class));
            return true;
        } else if (id == R.id.action_notification_settings) { // *** THÊM KHỐI LỆNH NÀY ***
            startActivity(new Intent(MainActivity.this, NotificationSettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
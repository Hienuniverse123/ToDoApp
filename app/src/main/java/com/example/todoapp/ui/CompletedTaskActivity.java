package com.example.todoapp.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.R;
import com.example.todoapp.adapter.TaskAdapter; // Dùng lại Adapter cũ
import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.model.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // Cần import này để filter

public class CompletedTaskActivity extends AppCompatActivity {

    private TaskDao taskDao;
    private FirebaseUser currentUser;

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> completedTaskList;

    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_task);

        //Khởi tạo
        taskDao = new TaskDao();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Ánh xạ views
        toolbar = findViewById(R.id.toolbarCompleted);
        progressBar = findViewById(R.id.progressBarCompleted);
        tvEmptyState = findViewById(R.id.tvEmptyStateCompleted);
        recyclerView = findViewById(R.id.recyclerViewCompletedTasks);

        //Cài đặt Toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        //Cài đặt RecyclerView
        completedTaskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(this, completedTaskList); // Dùng adapter cũ
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(taskAdapter);

        //Tải dữ liệu
        if (currentUser != null) {
            loadCompletedTasks(currentUser.getUid());
        } else {
            finish(); //Quay lại nếu không có user
        }
    }

    private void loadCompletedTasks(String userId) {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        taskDao.getTasksByUser(userId, new TaskDao.FirebaseTaskListCallback() {
            @Override
            public void onSuccess(List<Task> allTasks) {
                progressBar.setVisibility(View.GONE);

                //LỌC DANH SÁCH
                List<Task> filteredList;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    // Cách hiện đại (dùng Stream)
                    filteredList = allTasks.stream()
                            .filter(task -> task.getStatus() == 1) // Chỉ lấy task có status == 1
                            .collect(Collectors.toList());
                } else {
                    //Cách truyền thống (nếu máy cũ)
                    filteredList = new ArrayList<>();
                    for (Task task : allTasks) {
                        if (task.getStatus() == 1) {
                            filteredList.add(task);
                        }
                    }
                }

                //Cập nhật RecyclerView
                completedTaskList.clear();
                completedTaskList.addAll(filteredList);
                taskAdapter.notifyDataSetChanged();

                //Kiểm tra trạng thái trống
                if (completedTaskList.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CompletedTaskActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
package com.example.todoapp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.R;
import com.example.todoapp.adapter.TaskAdapter;
import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.model.Task;
import com.example.todoapp.ui.AddTaskActivity; // Nút Add vẫn mở Activity này
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TaskFragment extends Fragment {

    private TaskDao taskDao;
    private RecyclerView recyclerViewTasks;
    private FloatingActionButton fabAddTask;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // "Thổi" layout fragment_tasks.xml lên
        View view = inflater.inflate(R.layout.fragment_task, container, false);

        taskDao = new TaskDao();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Ánh xạ views từ 'view'
        recyclerViewTasks = view.findViewById(R.id.recyclerViewTasks);
        fabAddTask = view.findViewById(R.id.fabAddTask);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        // Setup RecyclerView
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(getContext(), taskList); // Dùng getContext()
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewTasks.setAdapter(taskAdapter);

        fabAddTask.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AddTaskActivity.class));
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại task mỗi khi quay lại fragment này
        if (currentUser != null) {
            loadTasks(currentUser.getUid());
        }
    }

    // Hàm loadTasks (giống hệt MainActivity cũ, nhưng có thêm LỌC)
    private void loadTasks(String userId) {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        taskDao.getTasksByUser(userId, new TaskDao.FirebaseTaskListCallback() {
            @Override
            public void onSuccess(List<Task> allTasks) {
                progressBar.setVisibility(View.GONE);
                taskList.clear();

                // *** LỌC QUAN TRỌNG ***
                // Màn hình này chỉ hiện task CHƯA hoàn thành (status == 0)
                List<Task> filteredList;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    filteredList = allTasks.stream()
                            .filter(task -> task.getStatus() == 0) // Chỉ lấy task chưa xong
                            .collect(Collectors.toList());
                } else {
                    filteredList = new ArrayList<>();
                    for (Task task : allTasks) {
                        if (task.getStatus() == 0) {
                            filteredList.add(task);
                        }
                    }
                }

                taskList.addAll(filteredList);
                taskAdapter.notifyDataSetChanged();

                if (taskList.isEmpty()) {
                    tvEmptyState.setText("Không có công việc nào.");
                    tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
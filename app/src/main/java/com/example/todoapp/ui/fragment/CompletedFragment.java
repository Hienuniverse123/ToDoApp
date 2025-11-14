package com.example.todoapp.ui.fragment;

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
import com.example.todoapp.adapter.TaskAdapter; // Dùng lại Adapter
import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.model.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CompletedFragment extends Fragment {

    private TaskDao taskDao;
    private FirebaseUser currentUser;

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> completedTaskList;

    private ProgressBar progressBar;
    private TextView tvEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // "Thổi" layout fragment_completed.xml lên
        View view = inflater.inflate(R.layout.fragment_completed, container, false);

        // Khởi tạo
        taskDao = new TaskDao();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Ánh xạ views
        progressBar = view.findViewById(R.id.progressBarCompleted);
        tvEmptyState = view.findViewById(R.id.tvEmptyStateCompleted);
        recyclerView = view.findViewById(R.id.recyclerViewCompletedTasks);

        // Cài đặt RecyclerView
        completedTaskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(getContext(), completedTaskList); // Dùng adapter cũ
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(taskAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải dữ liệu mỗi khi tab này được mở
        if (currentUser != null) {
            loadCompletedTasks(currentUser.getUid());
        }
    }

    private void loadCompletedTasks(String userId) {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        taskDao.getTasksByUser(userId, new TaskDao.FirebaseTaskListCallback() {
            @Override
            public void onSuccess(List<Task> allTasks) {
                progressBar.setVisibility(View.GONE);

                // *** PHẦN QUAN TRỌNG: LỌC DANH SÁCH ***
                List<Task> filteredList;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    // Cách hiện đại (dùng Stream)
                    filteredList = allTasks.stream()
                            .filter(task -> task.getStatus() == 1) // Chỉ lấy task có status == 1
                            .collect(Collectors.toList());
                } else {
                    // Cách truyền thống (nếu máy cũ)
                    filteredList = new ArrayList<>();
                    for (Task task : allTasks) {
                        if (task.getStatus() == 1) {
                            filteredList.add(task);
                        }
                    }
                }

                // Cập nhật RecyclerView
                completedTaskList.clear();
                completedTaskList.addAll(filteredList);
                taskAdapter.notifyDataSetChanged();

                // Kiểm tra trạng thái trống
                if (completedTaskList.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
package com.example.todoapp.dao;

import androidx.annotation.NonNull;

import com.example.todoapp.model.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TaskDao {
    private DatabaseReference tasksRef;

    public TaskDao() {
        tasksRef = FirebaseDatabase.getInstance().getReference("tasks");
    }

    // 1. Thêm interface callback mới
    public interface FirebaseTaskCallback {
        void onSuccess(Task task);
        void onFailure(String error);
    }

    // 2. Thêm hàm mới để lấy 1 task
    public void getTaskById(String taskId, FirebaseTaskCallback callback) {
        tasksRef.child(taskId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Task task = snapshot.getValue(Task.class);
                    callback.onSuccess(task);
                } else {
                    callback.onFailure("Task không tồn tại");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    // Thêm task
    public void addTask(Task task, FirebaseCallback callback) {
        String taskId = tasksRef.push().getKey();
        task.setId(taskId);
        tasksRef.child(taskId).setValue(task)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Thêm thành công"))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Lấy danh sách task theo userId
    public void getTasksByUser(String userId, FirebaseTaskListCallback callback) {
        tasksRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Task> taskList = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Task task = data.getValue(Task.class);
                            taskList.add(task);
                        }
                        callback.onSuccess(taskList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    // Cập nhật task
    public void updateTask(Task task, FirebaseCallback callback) {
        tasksRef.child(task.getId()).setValue(task)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Cập nhật thành công"))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Xóa task
    public void deleteTask(String taskId, FirebaseCallback callback) {
        tasksRef.child(taskId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess("Xóa thành công"))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Callback interface
    public interface FirebaseCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public interface FirebaseTaskListCallback {
        void onSuccess(List<Task> tasks);
        void onFailure(String error);
    }
}

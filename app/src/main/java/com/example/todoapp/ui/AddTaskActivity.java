package com.example.todoapp.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todoapp.R;
import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.model.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etTaskTitle, etTaskDescription, etDueDate;
    private Button btnSaveTask, btnDeleteTask;
    private MaterialToolbar toolbar;

    private TaskDao taskDao;
    private FirebaseUser currentUser;
    private Calendar selectedDueDate = Calendar.getInstance();

    private boolean isEditMode = false;
    private String taskId = null;
    private Task currentTask; // Lưu đối tượng task đang sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task); // Layout này đã có nút Xóa (chỉ bị ẩn)

        // Khởi tạo
        taskDao = new TaskDao();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        // Ánh xạ
        toolbar = findViewById(R.id.toolbarAddTask);
        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        etDueDate = findViewById(R.id.etDueDate);
        btnSaveTask = findViewById(R.id.btnSaveTask);
        btnDeleteTask = findViewById(R.id.btnDeleteTask);

        // --- KIỂM TRA: Đang Thêm hay đang Sửa? ---
        if (getIntent().hasExtra("TASK_ID")) {
            // Chế độ SỬA / CHI TIẾT
            isEditMode = true;
            taskId = getIntent().getStringExtra("TASK_ID");
            setupEditMode();
        } else {
            // Chế độ THÊM
            isEditMode = false;
            toolbar.setTitle("Thêm công việc mới");
            btnSaveTask.setText("Lưu công việc");
            btnDeleteTask.setVisibility(View.GONE); // Ẩn nút xóa
        }

        // --- Sự kiện ---
        toolbar.setNavigationOnClickListener(v -> finish());

        etDueDate.setOnClickListener(v -> showDatePickerDialog());

        btnSaveTask.setOnClickListener(v -> saveOrUpdateTask());

        btnDeleteTask.setOnClickListener(v -> showDeleteConfirmation());
    }

    // Hàm này được gọi khi ở chế độ Sửa/Chi tiết
    private void setupEditMode() {
        toolbar.setTitle("Chi tiết công việc");
        btnSaveTask.setText("Cập nhật");
        btnDeleteTask.setVisibility(View.VISIBLE); // Hiện nút xóa

        // Lấy dữ liệu task từ Firebase để hiển thị
        DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("tasks").child(taskId);
        taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentTask = snapshot.getValue(Task.class);
                    if (currentTask != null) {
                        // Đổ dữ liệu vào các ô
                        etTaskTitle.setText(currentTask.getTitle());
                        etTaskDescription.setText(currentTask.getDescription());
                        etDueDate.setText(currentTask.getDueDate());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddTaskActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDueDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etDueDate.setText(sdf.format(selectedDueDate.getTime()));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveOrUpdateTask() {
        String title = etTaskTitle.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim();

        if (title.isEmpty()) {
            etTaskTitle.setError("Tiêu đề là bắt buộc");
            return;
        }
        if (dueDate.isEmpty()) {
            etDueDate.setError("Ngày hết hạn là bắt buộc");
            return;
        }

        if (isEditMode) {
            // --- CẬP NHẬT ---
            if (currentTask != null) {
                currentTask.setTitle(title);
                currentTask.setDescription(description);
                currentTask.setDueDate(dueDate);

                taskDao.updateTask(currentTask, new TaskDao.FirebaseCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(AddTaskActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(AddTaskActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            // --- THÊM MỚI ---
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String createdAt = sdf.format(Calendar.getInstance().getTime());

            Task newTask = new Task();
            newTask.setUserId(currentUser.getUid());
            newTask.setTitle(title);
            newTask.setDescription(description);
            newTask.setDueDate(dueDate);
            newTask.setCreatedAt(createdAt);
            newTask.setStatus(0); // Mặc định chưa hoàn thành

            taskDao.addTask(newTask, new TaskDao.FirebaseCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(AddTaskActivity.this, "Thêm mới thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                @Override
                public void onFailure(String error) {
                    Toast.makeText(AddTaskActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Hàm này dùng cho nút Xóa
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa công việc")
                .setMessage("Bạn có chắc chắn muốn xóa công việc này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    taskDao.deleteTask(taskId, new TaskDao.FirebaseCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(AddTaskActivity.this, "Đã xóa!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(AddTaskActivity.this, "Lỗi xóa: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
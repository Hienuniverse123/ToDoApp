package com.example.todoapp.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todoapp.R;
import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.model.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etTaskTitle, etTaskDescription, etDueDate;
    private Button btnSaveTask;
    private MaterialToolbar toolbar;

    private TaskDao taskDao;
    private FirebaseUser currentUser;
    private Calendar selectedDueDate = Calendar.getInstance(); // Lưu ngày được chọn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Khởi tạo
        taskDao = new TaskDao();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Kiểm tra xem user có bị null không (dù không nên xảy ra)
        if (currentUser == null) {
            Toast.makeText(this, "Lỗi xác thực người dùng!", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity nếu không có user
            return;
        }

        // Ánh xạ views
        toolbar = findViewById(R.id.toolbarAddTask);
        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        etDueDate = findViewById(R.id.etDueDate);
        btnSaveTask = findViewById(R.id.btnSaveTask);

        // --- Xử lý sự kiện ---

        // 1. Nút Quay lại (icon 'X' trên toolbar)
        toolbar.setNavigationOnClickListener(v -> finish()); // Đóng activity

        // 2. Click vào ô Ngày
        etDueDate.setOnClickListener(v -> showDatePickerDialog());

        // 3. Click vào nút Lưu
        btnSaveTask.setOnClickListener(v -> saveTask());
    }

    private void showDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    // Lưu lại ngày đã chọn
                    selectedDueDate.set(year, month, dayOfMonth);
                    // Cập nhật text cho EditText
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etDueDate.setText(sdf.format(selectedDueDate.getTime()));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveTask() {
        String title = etTaskTitle.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim();

        // Kiểm tra thông tin bắt buộc
        if (title.isEmpty()) {
            etTaskTitle.setError("Tiêu đề là bắt buộc");
            etTaskTitle.requestFocus();
            return;
        }

        if (dueDate.isEmpty()) {
            etDueDate.setError("Ngày hết hạn là bắt buộc");
            etDueDate.requestFocus();
            // Không return, nhưng có thể bạn muốn bắt buộc
            Toast.makeText(this, "Vui lòng chọn ngày", Toast.LENGTH_SHORT).show();
            return;
        }
        ///111112322
        // Lấy ngày tạo (hiện tại)
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String createdAt = sdf.format(Calendar.getInstance().getTime());

        // Tạo đối tượng Task
        Task task = new Task();
        // ID sẽ được tạo tự động bởi Firebase (hàm addTask của TaskDao)
        task.setUserId(currentUser.getUid()); // Quan trọng: Gán task cho user hiện tại
        task.setTitle(title);
        task.setDescription(description);
        task.setCreatedAt(createdAt);
        task.setDueDate(dueDate);
        task.setStatus(0); // 0 = Chưa hoàn thành

        // Gọi DAO để lưu
        taskDao.addTask(task, new TaskDao.FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(AddTaskActivity.this, "Thêm công việc thành công!", Toast.LENGTH_SHORT).show();
                finish(); // Đóng màn hình Thêm và quay lại MainActivity
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AddTaskActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
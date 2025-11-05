package com.example.todoapp.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.R;
import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.model.Task;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context context;
    private List<Task> taskList;
    private TaskDao taskDao;

    public TaskAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList;
        this.taskDao = new TaskDao(); // Khởi tạo TaskDao để cập nhật/xóa
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tạo view từ file layout item_task.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        // Lấy task tại vị trí hiện tại
        Task task = taskList.get(position);

        // Đổ dữ liệu lên các view
        holder.tvTaskTitle.setText(task.getTitle());
        holder.tvTaskDescription.setText(task.getDescription());
        holder.tvDueDate.setText(task.getDueDate()); // Hiển thị ngày

        // Xử lý trạng thái (status)
        // 0 = Chưa hoàn thành, 1 = Đã hoàn thành
        if (task.getStatus() == 1) {
            holder.cbTaskCompleted.setChecked(true);
            // Thêm hiệu ứng gạch ngang cho tiêu đề
            holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.cbTaskCompleted.setChecked(false);
            // Bỏ hiệu ứng gạch ngang
            holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // --- Xử lý sự kiện (Clicks) ---

        // 1. Khi nhấn vào CheckBox
        holder.cbTaskCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Cập nhật trạng thái của task
            task.setStatus(isChecked ? 1 : 0);

            // Cập nhật giao diện ngay lập tức
            if (isChecked) {
                holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            // Gọi Dao để cập nhật lên Firebase
            taskDao.updateTask(task, new TaskDao.FirebaseCallback() {
                @Override
                public void onSuccess(String message) {
                    Log.d("TaskAdapter", "Cập nhật trạng thái thành công");
                }
                @Override
                public void onFailure(String error) {
                    Log.e("TaskAdapter", "Lỗi cập nhật trạng thái: " + error);
                    // Có thể revert lại checkbox nếu cập nhật thất bại
                    holder.cbTaskCompleted.setChecked(!isChecked);
                }
            });
        });

        // 2. Khi nhấn vào toàn bộ Card (để Sửa/Chi tiết)
        holder.cardTask.setOnClickListener(v -> {
            // TODO: Mở màn hình AddTaskActivity và gửi task qua Intent để sửa
            // Ví dụ:
            // Intent intent = new Intent(context, AddTaskActivity.class);
            // intent.putExtra("TASK_ID", task.getId()); // Gửi ID để Activity mới tải chi tiết
            // context.startActivity(intent);
            Toast.makeText(context, "Mở màn hình sửa cho: " + task.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    /**
     * Lớp ViewHolder chứa các View của file item_task.xml
     */
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardTask;
        CheckBox cbTaskCompleted;
        TextView tvTaskTitle;
        TextView tvTaskDescription;
        TextView tvDueDate;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các view từ layout item_task.xml
            cardTask = itemView.findViewById(R.id.cardTask);
            cbTaskCompleted = itemView.findViewById(R.id.cbTaskCompleted);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
        }
    }
}
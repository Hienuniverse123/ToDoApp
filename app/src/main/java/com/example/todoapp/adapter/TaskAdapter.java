package com.example.todoapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.R;
import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.model.Task;
import com.example.todoapp.ui.AddTaskActivity;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final Context context;
    private final List<Task> taskList;
    private final TaskDao taskDao;

    public TaskAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList;
        this.taskDao = new TaskDao();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        final Task task = taskList.get(position);

        //Đổ dữ liệu
        holder.tvTaskTitle.setText(task.getTitle());
        holder.tvTaskDescription.setText(task.getDescription());
        holder.tvDueDate.setText(task.getDueDate());

        //Xử lý CheckBox
        if (task.getStatus() == 1) {
            holder.cbTaskCompleted.setChecked(true);
            updatePaintFlags(holder.tvTaskTitle, true);
        } else {
            holder.cbTaskCompleted.setChecked(false);
            updatePaintFlags(holder.tvTaskTitle, false);
        }

        //Sự kiện CheckBox
        holder.cbTaskCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setStatus(isChecked ? 1 : 0);
            updatePaintFlags(holder.tvTaskTitle, isChecked);

            taskDao.updateTask(task, new TaskDao.FirebaseCallback() {
                @Override
                public void onSuccess(String message) {
                    Log.d("TaskAdapter", "Cập nhật trạng thái OK");
                }
                @Override
                public void onFailure(String error) {
                    Log.e("TaskAdapter", "Lỗi cập nhật trạng thái: " + error);
                    holder.cbTaskCompleted.setChecked(!isChecked);
                    updatePaintFlags(holder.tvTaskTitle, !isChecked);
                }
            });
        });

        //Click (Nhấn 1 lần)
        holder.cardTask.setOnClickListener(v -> {
            // Mở màn hình AddTaskActivity (chế độ Sửa/Chi tiết)
            openEditTaskActivity(task.getId());
        });

        //Long Click (Nhấn giữ)
        holder.cardTask.setOnLongClickListener(v -> {
            // Hiển thị Dialog Chi tiết tùy chỉnh
            showTaskDetailsDialog(task, holder.getAdapterPosition());
            return true; // Báo đã xử lý
        });
    }


    //Hàm hiển thị Dialog Chi tiết
    private void showTaskDetailsDialog(Task task, int position) {
        //"Thổi phồng" (inflate) layout dialog mới
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_task_details, null);

        //Ánh xạ các View bên trong Dialog
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvDialogDescription = dialogView.findViewById(R.id.tvDialogDescription);
        TextView tvDialogDueDate = dialogView.findViewById(R.id.tvDialogDueDate);
        Button btnDialogDelete = dialogView.findViewById(R.id.btnDialogDelete);
        Button btnDialogEdit = dialogView.findViewById(R.id.btnDialogEdit);

        //Đổ dữ liệu của task vào Dialog
        tvDialogTitle.setText(task.getTitle());
        tvDialogDescription.setText(task.getDescription());
        tvDialogDueDate.setText("Ngày hết hạn: " + task.getDueDate());

        //Tạo và hiển thị Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        //Tạo đối tượng dialog để có thể đóng (dismiss) nó
        final AlertDialog dialog = builder.create();

        //Gán sự kiện cho các nút
        btnDialogEdit.setOnClickListener(v -> {
            // Mở màn hình AddTaskActivity (chế độ Sửa)
            openEditTaskActivity(task.getId());
            dialog.dismiss(); // Đóng dialog
        });

        btnDialogDelete.setOnClickListener(v -> {
            // Mở dialog xác nhận xóa
            showDeleteConfirmation(task, position);
            dialog.dismiss(); // Đóng dialog
        });

        dialog.show();
    }

    //Hàm mở Dialog Xác nhận Xóa (Giữ nguyên)
    private void showDeleteConfirmation(Task task, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xóa công việc")
                .setMessage("Bạn có chắc chắn muốn xóa '" + task.getTitle() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    taskDao.deleteTask(task.getId(), new TaskDao.FirebaseCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(context, "Đã xóa!", Toast.LENGTH_SHORT).show();
                            if (position >= 0 && position < taskList.size()) {
                                taskList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, taskList.size());
                            }
                        }
                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(context, "Lỗi xóa: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void openEditTaskActivity(String taskId) {
        Intent intent = new Intent(context, AddTaskActivity.class);
        intent.putExtra("TASK_ID", taskId);
        context.startActivity(intent);
    }

    private void updatePaintFlags(TextView textView, boolean isChecked) {
        if (isChecked) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardTask;
        CheckBox cbTaskCompleted;
        TextView tvTaskTitle;
        TextView tvTaskDescription;
        TextView tvDueDate;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTask = itemView.findViewById(R.id.cardTask);
            cbTaskCompleted = itemView.findViewById(R.id.cbTaskCompleted);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
        }
    }
}
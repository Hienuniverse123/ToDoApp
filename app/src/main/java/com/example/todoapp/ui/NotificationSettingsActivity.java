package com.example.todoapp.ui;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todoapp.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.Calendar;
import java.util.Locale;

public class NotificationSettingsActivity extends AppCompatActivity {

    // Khóa (key) để lưu cài đặt
    public static final String PREFS_NAME = "NotificationPrefs";
    public static final String KEY_NOTIFICATIONS_ENABLED = "NotificationsEnabled";
    public static final String KEY_REMINDER_HOUR = "ReminderHour";
    public static final String KEY_REMINDER_MINUTE = "ReminderMinute";

    private MaterialToolbar toolbar;
    private MaterialSwitch switchNotifications;
    private TextView tvReminderTime;

    private SharedPreferences sharedPreferences;
    private int reminderHour = 9; // Mặc định 9 giờ sáng
    private int reminderMinute = 0; // Mặc định 0 phút

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        //Ánh xạ views
        toolbar = findViewById(R.id.toolbarNotification);
        switchNotifications = findViewById(R.id.switchNotifications);
        tvReminderTime = findViewById(R.id.tvReminderTime);

        //Cài đặt Toolbar
        toolbar.setNavigationOnClickListener(v -> finish()); // Nút 'X' để đóng

        //Tải cài đặt đã lưu
        loadSettings();

        //Xử lý sự kiện
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Lưu cài đặt Bật/Tắt
            saveBooleanSetting(KEY_NOTIFICATIONS_ENABLED, isChecked);
            // Bật/tắt ô chọn giờ
            tvReminderTime.setEnabled(isChecked);
        });

        tvReminderTime.setOnClickListener(v -> {
            //Hiển thị đồng hồ chọn giờ
            showTimePickerDialog();
        });
    }

    //Tải cài đặt từ SharedPreferences
    private void loadSettings() {
        boolean notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        reminderHour = sharedPreferences.getInt(KEY_REMINDER_HOUR, 9);
        reminderMinute = sharedPreferences.getInt(KEY_REMINDER_MINUTE, 0);

        switchNotifications.setChecked(notificationsEnabled);
        tvReminderTime.setEnabled(notificationsEnabled);
        updateReminderTimeText();
    }

    //Hiển thị đồng hồ
    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    reminderHour = hourOfDay;
                    reminderMinute = minute;
                    //Lưu cài đặt mới
                    saveIntSetting(KEY_REMINDER_HOUR, reminderHour);
                    saveIntSetting(KEY_REMINDER_MINUTE, reminderMinute);
                    //Cập nhật text
                    updateReminderTimeText();
                },
                reminderHour,
                reminderMinute,
                false //Dùng 12-hour (false) hay 24-hour (true)
        );
        timePickerDialog.show();
    }

    //Cập nhật text hiển thị giờ
    private void updateReminderTimeText() {
        // Định dạng lại giờ cho đẹp
        String period = reminderHour < 12 ? "Sáng" : "Chiều";
        int displayHour = reminderHour % 12;
        if (displayHour == 0) displayHour = 12; // 12 AM/PM

        tvReminderTime.setText(String.format(Locale.getDefault(), "%d:%02d %s", displayHour, reminderMinute, period));
    }

    //Hàm lưu (boolean)
    private void saveBooleanSetting(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    //Hàm lưu (int)
    private void saveIntSetting(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }
}
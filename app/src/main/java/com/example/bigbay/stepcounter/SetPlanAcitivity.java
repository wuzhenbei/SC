package com.example.bigbay.stepcounter;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.example.bigbay.stepcounter.util.SpTools;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SetPlanAcitivity extends AppCompatActivity implements View.OnClickListener {
    private SpTools sp;

    private LinearLayout layout_titlebar;
    private ImageView iv_left;
    private ImageView iv_right;
    private EditText tv_step_number;
    private CheckBox cb_remind;
    private TextView tv_remind_time;
    private Button btn_save;

    private void assignViews() {
        layout_titlebar = (LinearLayout) findViewById(R.id.layout_titlebar);
        iv_left = (ImageView) findViewById(R.id.iv_left);
        iv_right = (ImageView) findViewById(R.id.iv_right);
        tv_step_number = (EditText) findViewById(R.id.tv_step_number);
        cb_remind = (CheckBox) findViewById(R.id.cb_remind);
        tv_remind_time = (TextView) findViewById(R.id.tv_remind_time);
        btn_save = (Button) findViewById(R.id.btn_save);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_exercise_plan);
        assignViews();
        initData();
        addListener();
    }

    public void initData() {//获取锻炼计划
        sp = new SpTools(this);
        String planWalk_QTY = (String) sp.getParam("stepGoal", "10000");
        String remind = (String) sp.getParam("remind", "1");
        String achieveTime = (String) sp.getParam("achieveTime", "20:00");
        if (!planWalk_QTY.isEmpty()) {
            if ("0".equals(planWalk_QTY)) {
                tv_step_number.setText("10000");
            } else {
                tv_step_number.setText(planWalk_QTY);
            }
        }
        if (!remind.isEmpty()) {
            if ("0".equals(remind)) {
                cb_remind.setChecked(false);
            } else if ("1".equals(remind)) {
                cb_remind.setChecked(true);
            }
        }

        if (!achieveTime.isEmpty()) {
            tv_remind_time.setText(achieveTime);
        }

    }


    public void addListener() {
        iv_left.setOnClickListener(this);
        iv_right.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        tv_remind_time.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_left:
                finish();
                break;
            case R.id.btn_save:
                save();
                break;

            case R.id.tv_remind_time:
                showTimeDialog1();
                break;
        }
    }

    private void save() {
        String stepGoal = tv_step_number.getText().toString().trim();
//        remind = "";
        String remind;
        if (cb_remind.isChecked()) {
            remind = "1";
        } else {
            remind = "0";
        }
        String achieveTime = tv_remind_time.getText().toString().trim();
        if (stepGoal.isEmpty() || "0".equals(stepGoal)) {
            sp.setParam("stepGoal", "10000");
        } else {
            sp.setParam("stepGoal", stepGoal);
        }
        sp.setParam("remind", remind);

        if (achieveTime.isEmpty()) {
            sp.setParam("achieveTime", "21:00");
            achieveTime = "21:00";
        } else {
            sp.setParam("achieveTime", achieveTime);
        }
        finish();
    }

    private void showTimeDialog1() {
        final Calendar calendar = Calendar.getInstance(Locale.CHINA);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
//        String time = tv_remind_time.getText().toString().trim();
        final DateFormat df = new SimpleDateFormat("HH:mm");
//        Date date = null;
//        try {
//            date = df.parse(time);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        if (null != date) {
//            calendar.setTime(date);
//        }
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                String remaintime = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
                Date date = null;
                try {
                    date = df.parse(remaintime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (null != date) {
                    calendar.setTime(date);
                }
                tv_remind_time.setText(df.format(date));
            }
        }, hour, minute, true).show();
    }
}

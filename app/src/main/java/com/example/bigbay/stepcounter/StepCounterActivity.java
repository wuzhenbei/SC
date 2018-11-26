package com.example.bigbay.stepcounter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.bigbay.stepcounter.database.DatabaseManager;
import com.example.bigbay.stepcounter.database.StepDatabaseHelper;
import com.example.bigbay.stepcounter.service.StepService;
import com.example.bigbay.stepcounter.util.SpTools;
import com.example.bigbay.stepcounter.util.UploadTools;
import com.example.bigbay.stepcounter.database.DatabaseManager;

import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.bigbay.stepcounter.util.StepTools.getTodayUserStep;
import static com.example.bigbay.stepcounter.util.UploadTools.*;


public class StepCounterActivity<mDatabaseManager> extends AppCompatActivity implements View.OnClickListener {

    static final String TAG = "StepCounterActivity";
    private StepArcView cc;
    private TextView tv_set;
    private SpTools sp;
    String versionCode = BuildConfig.VERSION_NAME;
    StepDatabaseHelper dbHelper = new StepDatabaseHelper(this, "StepDatabaseHelper.db", null, 1);

    DatabaseManager mDatabaseManager = DatabaseManager.getInstance(dbHelper);

    //StepDatabaseHelper dbHelper = new StepDatabaseHelper(this, "StepDatabaseHelper.db", null, 1);


    /**====== UI刷新相关 ======*/
    private Handler handler=new Handler();
    private Runnable runnable=new Runnable() {
        @Override
        public void run() {
            //需要做的事情：显示步数

            showStepNumber();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    uploadData(versionCode,sp,mDatabaseManager);
                }
            }).start();
            handler.postDelayed(this, 3000);

        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.step_counter_layout);
        assignViews();
        initData();
        tv_set.setOnClickListener(this);


        Log.d(TAG, "活动被创建!");

    }


    /**===== 当app处于前台时定时刷新UI =====*/

    @Override
    protected void onResume() {

        super.onResume();

        // 启动service
        Intent intent = new Intent(this, StepService.class);
        startService(intent);

        // 刷新一次UI
        showStepNumber();


        // 启动定时器刷新
        handler.postDelayed(runnable, 1000);//每两秒执行一次runnable

        Log.d(TAG, "活动启动!");
    }


    /**===== 当app不活动时停止刷新 =====*/

    @Override
    protected void onPause() {

        super.onPause();

        // 销毁service
        Intent intent = new Intent(this, StepService.class);
        stopService(intent);

        // 停止定时器刷新
        handler.removeCallbacks(runnable);

        Log.d(TAG, "活动暂停!");
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();

        Log.d(TAG,"活动被销毁！");
    }


    /**======= 创建menu ========*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.step_counter, menu);
        return true;
    }


    // 创建menu响应事件

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_webView:
                // 跳转到web_view界面
                Intent webViewIntent = new Intent(this, WebViewActivity.class);
                webViewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(webViewIntent);
                StepCounterActivity.this.finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**========== 显示步数 ===========**/

    public void showStepNumber() {

        // 声明变量
        int user_step;  // 用户步数
        String stepGoal = (String) sp.getParam("stepGoal", "10000");
        //StepDatabaseHelper dbHelper;

        // 读取数据库
        //dbHelper = new StepDatabaseHelper(this, "StepDatabaseHelper.db", null, 1);

        // 获取今天的用户步数
        user_step = getTodayUserStep(mDatabaseManager);

        // 更新步数显示
        cc.setCurrentCount(Integer.parseInt(stepGoal),user_step);
        //text_step = (TextView) findViewById(R.id.text_step);
        //text_step.setText(String.valueOf(user_step));

    }
    private void assignViews(){
        cc = findViewById(R.id.cc);
        tv_set = findViewById(R.id.tv_set);
    }


    private void initData() {
        sp = new SpTools(this);
        //获取用户设置的计划锻炼步数，没有设置过的话默认10000
        String stepGoal = (String) sp.getParam("stepGoal", "10000");
        //设置当前步数为0
        cc.setCurrentCount(Integer.parseInt(stepGoal), 0);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_set:
                startActivity(new Intent(this,SetPlanAcitivity.class));
                break;
        }
    }
}
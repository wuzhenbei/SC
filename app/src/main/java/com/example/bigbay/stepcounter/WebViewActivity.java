package com.example.bigbay.stepcounter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.util.Log;
import android.provider.Settings.Secure;

import com.example.bigbay.stepcounter.database.DatabaseManager;
import com.example.bigbay.stepcounter.database.StepDatabaseHelper;
import com.example.bigbay.stepcounter.util.UploadTools;

import org.json.JSONException;
import org.json.JSONObject;


public class WebViewActivity extends AppCompatActivity {

    static final String TAG = "WebViewActivity";
    public SharedPreferences preferences ;
    SharedPreferences.Editor editor ;



    @SuppressLint("JavascriptInterface")
   // @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view_layout);
        android.webkit.WebView webView= findViewById(R.id.web_view);
        WebSettings webSettings = webView.getSettings();
        String ua = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(ua + ";sjtu-android");

        webSettings.setJavaScriptEnabled(true);

        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);  //关键点
        webSettings.setBuiltInZoomControls(true); // 设置显示缩放按钮
        webSettings.setDisplayZoomControls(false);
        webSettings.setDefaultFontSize(18);
        android.webkit.WebView.setWebContentsDebuggingEnabled(true);

        webView.setWebViewClient(new WebViewClient());//跳转网页时目标网页仍在当前webview中显示，而不是打开系统浏览器
        webView.loadUrl("http://health.sjtu.edu.cn/myhome.php");

        webView.addJavascriptInterface(this,"wv" );

    }




    //************ 创建menu ***********//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web_view, menu);
        return true;
    }






    //************* 创建menu响应事件 ***********//

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_stepCounter:
                // 跳转到StepCounter界面
                Intent stepCounterIntent = new Intent(this, StepCounterActivity.class);
                stepCounterIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(stepCounterIntent);
                WebViewActivity.this.finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    @android.webkit.JavascriptInterface
    public void PostDataFromWebToWebView(String msg)throws JSONException{

        // 读取数据库
        StepDatabaseHelper dbHelper;
        dbHelper = new StepDatabaseHelper(this, "StepDatabaseHelper.db", null, 1);
        DatabaseManager mDatabaseManager = DatabaseManager.getInstance(dbHelper);
        //SQLiteDatabase db = dbHelper.getWritableDatabase();

        UploadTools.parseJSONWithJSONObject(msg, mDatabaseManager);
        // UploadTools.uploadData(dbHelper);

        Log.d("dataToWebView",msg);

    }


}



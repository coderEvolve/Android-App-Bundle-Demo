package com.halejcwu.dynamic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.halejcwu.dynamic_feature.R;


public class MainActivity1 extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "dynamic_main1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        findViewById(R.id.button2).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this,text1.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"动态模块处于运行期");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"动态模块处于暂停期");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"动态模块处于销毁期");
    }
}

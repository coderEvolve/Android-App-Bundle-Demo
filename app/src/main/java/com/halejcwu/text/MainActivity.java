package com.halejcwu.text;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Handler;
import android.support.v4.os.BuildCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.play.core.splitinstall.SplitInstallHelper;
import com.google.android.play.core.splitinstall.SplitInstallManager;
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory;
import com.google.android.play.core.splitinstall.SplitInstallRequest;
import com.google.android.play.core.splitinstall.SplitInstallSessionState;
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener;
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MainActivity_TAG";
    private static final String DYNAMIC_FEATURE = "dynamic_feature";
    private SplitInstallManager splitInstallManager;             //安装动态模块管理类
    private progressDialog loading;
    private SplitInstallRequest request;                        //安装那些动态模块的请求类

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener(this);

        splitInstallManager = SplitInstallManagerFactory.create(this);
        loading = new progressDialog(this,R.style.CustomDialog);
        request = SplitInstallRequest.newBuilder()
                .addModule(DYNAMIC_FEATURE)
                .build();
    }

    private void onSuccessfulLoad(String dynamicFeature) {
        try {
            Intent intent1 = new Intent();
            intent1.setClassName(this,"com.halejcwu.dynamic.MainActivity1");    //使用显示跳转，证明其activity在清单文件已经声明
            Intent intent = new Intent(this, Class.forName("com.halejcwu.dynamic.MainActivity1"));  //使用反射机制进行界面跳转
            startActivity(intent1);
        } catch (ClassNotFoundException e) {
            Toast.makeText(MainActivity.this,"跳转失败"+e.getMessage(),Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

        splitInstallManager.registerListener(new SplitInstallStateUpdatedListener() {
            @Override
            public void onStateUpdate(SplitInstallSessionState splitInstallSessionState) {
                if (splitInstallSessionState.status() == SplitInstallSessionStatus.INSTALLED) { //新模块下载完成
                    if (BuildCompat.isAtLeastO()) { //当在Android8.0及以上时
                        SplitInstallHelper.updateAppInfo(MainActivity.this);    //更新与新模块相关联的上下文
                        new Handler().post(new Runnable() {
                            @Override public void run() {
                                AssetManager am = MainActivity.this.getAssets();    //访问模块assets的资源
                                InputStream in = null;
                                try {
                                    in = am.open("text4.png");
                                    Log.i(TAG,"图片输入流："+in.toString());  //输出证明能够获取到图片
                                    in.close();
                                } catch (IOException e) {
                                    Log.i(TAG,"获取错误"+e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    else{
                        Context newContext = null;
                        try {
                            newContext = MainActivity.this.createPackageContext(MainActivity.this.getPackageName(), 0); //更新上下文，以立即访问模块的资源
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        AssetManager am = newContext.getAssets();
                    }
                    Toast.makeText(MainActivity.this,"下载完成",Toast.LENGTH_SHORT).show();
                    loading.dismiss();
                    onSuccessfulLoad(DYNAMIC_FEATURE);
                } else if (splitInstallSessionState.status() == SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION) { //当下载大于10M的资源时，需要用户确认
                    try {
                        startIntentSender(splitInstallSessionState.resolutionIntent().getIntentSender(),
                                null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
                else if (splitInstallSessionState.status() == SplitInstallSessionStatus.DOWNLOADING){   //资源正在下载中
                    long totalBytes = splitInstallSessionState.totalBytesToDownload();
                    long progress = splitInstallSessionState.bytesDownloaded();
                    loading.show();
                }
            }
        });

        splitInstallManager.startInstall(request)
                .addOnSuccessListener(new OnSuccessListener<Integer>() {
                    @Override
                    public void onSuccess(Integer integer) {    //当平台接受你的按需下载请求
                        Toast.makeText(MainActivity.this,"平台接受请求，开始下载...",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() { //当平台拒接你的下载请求
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Integer>() {
                    @Override
                    public void onComplete(Task<Integer> task) {

                    }
                });
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
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG,"动态模块处于重新运行期");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"动态模块处于销毁期");
    }
}

package cn.an0nymous.chatroom.Activity;

import android.Manifest;
import android.app.Activity;;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;

import cn.an0nymous.chatroom.Bean.IP;
import cn.an0nymous.chatroom.Bean.VersionBean;
import cn.an0nymous.chatroom.R;
import cn.an0nymous.chatroom.Util.SPUtil;

public class SplashActivity extends Activity {


    private static final int LOADMAIN = 1;// 加载主界面
    private static final int SHOWUPDATEDIALOG = 2;// 显示是否更新的对话框
    protected static final int ERROR = 3;// 错误统一代号

    private static final int MY_PERMISSIONS = 4;


    private long startTimeMillis;// 记录开始访问网络的时间
    private String nowVersion;   //本地版本号

    private VersionBean bean;//json信息实体类

    //下载进度
    private ProgressDialog progressDialog;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        if (SPUtil.getBoolean("update",true)){
            getPermission();
            initData();
            checkUpdate();
        }else {
            loadMain();
        }

    }

    /**
     * 获取SD卡权限
     */
    private void getPermission(){
        if (ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SplashActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS);
        }
    }

    private void initData() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            nowVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            // 处理消息
            switch (msg.what) {
                case LOADMAIN:// 加载主界面
                    loadMain();
                    break;
                case ERROR:// 有异常
                    Toast.makeText(SplashActivity.this, "服务器异常", Toast.LENGTH_SHORT).show();
                    loadMain();// 进入主界面
                    break;
                case SHOWUPDATEDIALOG:// 显示更新版本的对话框
                    showUpDialog();
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 下载更新,
     */
    protected void checkUpdate() {
       new Thread(new Runnable() {
           @Override
           public void run() {
               String url = IP.ServerIP+"updateinfo.html";
               startTimeMillis = System.currentTimeMillis();//开始连接时间
               RequestParams params = new RequestParams(url);
               x.http().get(params, new Callback.CommonCallback<String>() {

                   @Override
                   public void onCancelled(CancelledException arg0) {

                   }

                   @Override
                   public void onError(Throwable arg0, boolean arg1) {
                       handler.sendEmptyMessage(ERROR);
                   }

                   @Override
                   public void onFinished() {

                   }

                   @Override
                   public void onSuccess(String arg0) {
                       try {
                           JSONObject object = new JSONObject(arg0);
                           bean = new VersionBean();
                           String desc = object.getString("des");
                           String downloadurl = object.getString("apkurl");
                           String versionname = object.getString("code");
                           bean.setDesc(desc);
                           bean.setUrl(downloadurl);
                           bean.setVersion(versionname);
                           //System.out.println("------------------------------------------------->");
                           //bean.toString();
                           System.out.println("-------------------------------------------------->");
                           long endTime = System.currentTimeMillis();
                           if (endTime - startTimeMillis < 2000) {
                               SystemClock.sleep(2000 - (endTime - startTimeMillis));// 时间不超过3秒，补足3秒
                           }
                           if (!nowVersion.equals(versionname)) {   //如果版本号不一样则弹出dialog
                               handler.sendEmptyMessage(SHOWUPDATEDIALOG);
                           }else {
                               handler.sendEmptyMessage(LOADMAIN);
                           }

                       } catch (JSONException e) {
                           handler.sendEmptyMessage(ERROR);
                           System.out.println("---------------------------------->JSON异常");
                           e.printStackTrace();
                       }
                   }
               });
           }
       }).start();
    }


    protected void showUpDialog() {

        AlertDialog dialog = new AlertDialog.Builder(this).setCancelable(false)
                .setTitle("下载" + bean.getVersion() + "版本").setMessage(bean.getDesc())
                .setNegativeButton("下次再说", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadMain();
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("立即更新", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        downLoadAPK();
                    }
                }).create();
        dialog.show();
    }


    protected void downLoadAPK() {
        final String path = Environment.getExternalStorageDirectory().getPath()+"/ChatRoom.apk";
        RequestParams params = new RequestParams(bean.getUrl());
        params.setSaveFilePath(path);
        x.http().get(params, new Callback.ProgressCallback<File>() {

            @Override
            public void onCancelled(CancelledException arg0) {

            }

            @Override
            public void onError(Throwable arg0, boolean arg1) {
                if(progressDialog!=null && progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                Toast.makeText(SplashActivity.this, "更新失败", Toast.LENGTH_SHORT).show();
                loadMain();
            }

            @Override
            public void onFinished() {

            }

            @Override
            public void onSuccess(File arg0) {
                if(progressDialog!=null && progressDialog.isShowing()){   //下载完成
                    progressDialog.dismiss();
                }
                //安装apk
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.fromFile(new File(path)),
                        "application/vnd.android.package-archive");
                startActivity(intent);
            }

            @Override
            public void onLoading(long arg0, long arg1, boolean arg2) {   //正在下载
                progressDialog.setMax((int)arg0);
                progressDialog.setProgress((int)arg1);
            }

            @Override
            public void onStarted() {
                System.out.println("开始下载");
                progressDialog = new ProgressDialog(SplashActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//设置为水平进行条
                progressDialog.setMessage("正在下载更新...");
                progressDialog.setProgress(0);
                progressDialog.show();
            }

            @Override
            public void onWaiting() {

            }
        });
    }


    /**
     * 跳转到登录界面
     */
    private void loadMain() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);// 进入主界面
        finish();// 关闭自己
    }

}

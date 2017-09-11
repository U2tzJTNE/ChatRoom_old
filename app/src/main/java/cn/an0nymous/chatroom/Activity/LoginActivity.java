package cn.an0nymous.chatroom.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import cn.an0nymous.chatroom.R;
import cn.an0nymous.chatroom.Util.LoginUtil;
import cn.an0nymous.chatroom.Util.SPUtil;


public class LoginActivity extends AppCompatActivity {

    private String username;
    private String password;
    private int icon;

    //服务器返回的信息
    private String info=null;
    // 返回主线程更新数据
    private static Handler handler = new Handler();
    // 创建等待框
    private ProgressDialog dialog;


    @Bind(R.id.input_name)
    EditText inputName;
    @Bind(R.id.input_password)
    EditText inputPassword;
    @Bind(R.id.btn_login)
    AppCompatButton btnLogin;
    @Bind(R.id.link_signup)
    TextView linkSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        butterknife.ButterKnife.bind(this);

        inputName.setText(SPUtil.getString("username",""));
        inputPassword.setText(SPUtil.getString("password",""));

        //登录
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        //注册账号
        linkSignup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 跳转到注册界面
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
                finish();
                //切换动画
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });


    }

    /**
     * 登录
     */
    public void login() {

        // 检测网络是否可用，无法检测wifi
        if (!checkNetwork()) {
            Toast toast = Toast.makeText(LoginActivity.this,"网络不可用", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        if (!validate()) {
            //验证失败
            onLoginFailed();
            return;
        }

        dialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        //设置为一直转
        dialog.setIndeterminate(true);
        dialog.setMessage("正在登录...");
        dialog.show();

        username = inputName.getText().toString();
        password = inputPassword.getText().toString();

        //开始登录
        new Thread(new MyThread()).start();

    }


    /**
     * 屏蔽返回键
     */
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    /**
     * 登录成功
     */
    public void onLoginSuccess() {
        SPUtil.putString("username",username);
        SPUtil.putString("password",password);
        SPUtil.putInt("icon",icon);
        //跳转到聊天界面
        startActivity(new Intent(LoginActivity.this,ChatActivity.class));
        finish();
    }

    /**
     * 登录失败
     */
    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "登录失败", Toast.LENGTH_LONG).show();
        inputName.setText("");
        inputPassword.setText("");
    }

    /**
     * @return 验证结果
     */
    public boolean validate() {
        boolean valid = true;

        String username = inputName.getText().toString();
        String password = inputPassword.getText().toString();

        if (username.isEmpty() || username.length()>8) {
            inputName.setError("用户名长度在2~8之间");
            valid = false;
        } else {
            inputName.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 20) {
            inputPassword.setError("密码的长度在4~20之间");
            valid = false;
        } else {
            inputPassword.setError(null);
        }

        return valid;
    }

    // 检测网络
    private boolean checkNetwork() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager.getActiveNetworkInfo() != null) {
            System.out.println("-------------->网络状态"+connManager.getActiveNetworkInfo().isAvailable());
            return connManager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    // 子线程接收数据，主线程修改数据
    public class MyThread implements Runnable {
        @Override
        public void run() {
            System.out.println("开始执行-------->"+username+"-----"+password);
            info = LoginUtil.executeHttpGet(username,password);
            System.out.println("-------------->执行结束，服务器返回的数据为：  "+info);

            String[] array = info.split("♣");
            final String serverName = array[0];
            final String serverPass = array[1];
            icon = Integer.parseInt(array[2]);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (info != null){
                        if (username.equals(serverName)&&password.equals(serverPass)){
                            onLoginSuccess();
                        }else {
                            onLoginFailed();
                        }
                    }
                    dialog.dismiss();
                }
            },1000);
        }
    }

}

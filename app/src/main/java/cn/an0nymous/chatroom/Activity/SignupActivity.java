package cn.an0nymous.chatroom.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterViewFlipper;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.an0nymous.chatroom.R;
import cn.an0nymous.chatroom.Util.QueryUserUtil;
import cn.an0nymous.chatroom.Util.RegisterUtil;
import cn.an0nymous.chatroom.Util.SPUtil;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    //服务器返回的信息
    private String info=null;
    // 返回主线程更新数据
    private static Handler handler = new Handler();
    // 创建等待框
    private ProgressDialog dialog;

    //用户注册信息
    private String icon = null;
    private String username = null;
    private String password = null;


    @Bind(R.id.previous_icon)
    Button previousIcon;
    @Bind(R.id.next_icon)
    Button nextIcon;
    @Bind(R.id.input_name)
    EditText inputName;
    @Bind(R.id.input_password)
    EditText inputPassword;
    @Bind(R.id.input_reEnterPassword)
    EditText inputReEnterPassword;
    @Bind(R.id.btn_signup)
    AppCompatButton btnSignup;
    @Bind(R.id.link_login)
    TextView linkLogin;
    @Bind(R.id.view_flipper)
    AdapterViewFlipper viewFlipper;

    private int[] icons = new int[]{
            R.drawable.icon_1, R.drawable.icon_2, R.drawable.icon_3,
            R.drawable.icon_4, R.drawable.icon_5, R.drawable.icon_6,
            R.drawable.icon_7, R.drawable.icon_8, R.drawable.icon_9,
            R.drawable.icon_10, R.drawable.icon_11, R.drawable.icon_12,
            R.drawable.icon_13, R.drawable.icon_14, R.drawable.icon_15,
            R.drawable.icon_16, R.drawable.icon_17, R.drawable.icon_18,
            R.drawable.icon_19, R.drawable.icon_20, R.drawable.icon_21,
            R.drawable.icon_22, R.drawable.icon_23, R.drawable.icon_24,
            R.drawable.icon_25, R.drawable.icon_26, R.drawable.icon_27,
            R.drawable.icon_28, R.drawable.icon_29, R.drawable.icon_30,
            R.drawable.icon_31, R.drawable.icon_32, R.drawable.icon_33,
            R.drawable.icon_34, R.drawable.icon_35, R.drawable.icon_36,
            R.drawable.icon_37, R.drawable.icon_38, R.drawable.icon_39,
            R.drawable.icon_40, R.drawable.icon_41, R.drawable.icon_42,
            R.drawable.icon_43, R.drawable.icon_44, R.drawable.icon_45,
            R.drawable.icon_46, R.drawable.icon_47, R.drawable.icon_48

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        //适配器
        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return icons.length;
            }

            @Override
            public Object getItem(int i) {
                return i;
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {

                //创建一个ImageView
                ImageView imageView = new ImageView(SignupActivity.this);
                imageView.setImageResource(icons[i]);

                //设置ImageView的缩放类型
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                //布局参数
                imageView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

                return imageView;
            }
        };
        viewFlipper.setAdapter(adapter);

        //下一张
        nextIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewFlipper.showNext();
            }
        });

        //上一张
        previousIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewFlipper.showPrevious();
                //System.out.println("icon------------->"+icons[viewFlipper.getDisplayedChild()]);
            }
        });
        //注册
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        //跳转到登录界面
        linkLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    /**
     * 注册
     */
    public void signup() {
        Log.d(TAG, "Signup");

        // 检测网络，无法检测wifi
        if (!checkNetwork()) {
            Toast toast = Toast.makeText(SignupActivity.this,"网络不可用", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if (!validate()) {
            onSignupFailed();
            return;
        }

        dialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        dialog.setIndeterminate(true);
        dialog.setMessage("正在注册...");
        dialog.show();

        username = inputName.getText().toString();
        password = inputPassword.getText().toString();
        icon = Integer.toString(icons[viewFlipper.getDisplayedChild()]);
        System.out.println("注册页面选择的icon:----->"+icon);

        // 查询用户名是否已被注册
        new Thread(new MyThread()).start();


    }


    /**
     * 注册成功
     */
    public void onSignupSuccess() {
        //跳转到设置密保界面
        SPUtil.putInt("icon",Integer.parseInt(icon));
        SPUtil.putString("username",username);
        SPUtil.putString("password",password);
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 注册失败
     */
    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "该用户名已被注册", Toast.LENGTH_LONG).show();
        inputName.setText("");
    }


    /**
     * @return 输入格式校验
     */
    public boolean validate() {
        boolean valid = true;
        String name = inputName.getText().toString();
        String password = inputPassword.getText().toString();
        String reEnterPassword = inputReEnterPassword.getText().toString();

        if (name.isEmpty() || name.length() < 2 || name.length() > 8) {
            inputName.setError("用户名长度为2~8之间");
            valid = false;
        } else {
            inputName.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 20) {
            inputPassword.setError("密码长度为4~20之间");
            valid = false;
        } else {
            inputPassword.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 20 || !(reEnterPassword.equals(password))) {
            inputReEnterPassword.setError("密码不一致");
            valid = false;
        } else {
            inputReEnterPassword.setError(null);
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
            //System.out.println("-------------->开始执行");
            info = QueryUserUtil.executeHttpGet(username);
            //System.out.println("-------------->执行结束，服务器返回的数据为：  "+info);
            if (info != null){
                if (info.equals(username)){
                    info = RegisterUtil.executeHttpGet(username,password,icon);
                    String[] array = info.split("♣");
                    final String serverName = array[0];
                    final String serverPass = array[1];
                    final String serverIcon = array[2];
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (info != null){
                                if (serverName.equals(username)&&serverPass.equals(password)&&serverIcon.equals(icon)){
                                    onSignupSuccess();
                                }else if (info.equals("x")){
                                    Toast.makeText(SignupActivity.this, "写入数据库失败", Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(SignupActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                            dialog.dismiss();
                        }
                    },1000);
                }else if (info.equals("x")){
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onSignupFailed();
                            dialog.dismiss();
                        }
                    },1000);
                }
            }

        }
    }

}

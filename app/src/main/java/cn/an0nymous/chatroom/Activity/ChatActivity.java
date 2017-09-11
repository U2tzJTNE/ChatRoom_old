package cn.an0nymous.chatroom.Activity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.an0nymous.chatroom.R;
import cn.an0nymous.chatroom.Util.ClientServerUtil;
import cn.an0nymous.chatroom.Util.SPUtil;
import io.github.rockerhieu.emojicon.EmojiconEditText;
import io.github.rockerhieu.emojicon.EmojiconGridFragment;
import io.github.rockerhieu.emojicon.EmojiconTextView;
import io.github.rockerhieu.emojicon.EmojiconsFragment;
import io.github.rockerhieu.emojicon.emoji.Emojicon;

public class ChatActivity extends AppCompatActivity
        implements
        EmojiconGridFragment.OnEmojiconClickedListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener {

    @Bind(R.id.emoji_box)
    CheckBox emojiBox;
    @Bind(R.id.emoji_edit)
    EmojiconEditText emojiEdit;
    @Bind(R.id.btn_send)
    AppCompatButton btnSend;
    @Bind(R.id.emoji_keyboard)
    FrameLayout emojiKeyboard;
    @Bind(R.id.chat_list)
    ListView chatList;

    private String name;
    private int icon;


    //发送时间
    private String date;
    private long exitTime = 0;

    //震动
    private Vibrator vibrator;
    //提示音
    private SoundPool soundPool;
    private int soundId;
    private boolean flag = false;

    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

    private Handler handler;
    // 定义与服务器通信的子线程
    private ClientServerUtil clientThread;

    private static String  DIV= "♥";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        initData();


        handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                // 如果消息来自于子线程
                if (msg.what == 0x123)
                {
                    String packM = msg.obj.toString();

                    getData(packM);

                    MyAdapter myAdapter = new MyAdapter();

                    chatList.setAdapter(myAdapter);

                    String MYname = (String) list.get(list.size()-1).get("name");

                    //震动
                    if (SPUtil.getBoolean("shake",true) && !MYname.equals(name)){

                        long [] pattern = {100,400,100,400};
                        vibrator.vibrate(pattern,-1);
                    }

                    //提示音
                    if (SPUtil.getBoolean("sound",true) && !MYname.equals(name)){
                        if (flag){
                            //  播放声音池中的文件, 可以指定播放音量，优先级 声音播放的速率
                            soundPool.play(soundId, 1.0f, 0.5f, 1, 0, 1.0f);
                        }else {
                            Toast.makeText(ChatActivity.this, "提示音加载失败", Toast.LENGTH_SHORT).show();
                        }
                    }


                    chatList.setSelection(myAdapter.getCount()-1);

                }
            }
        };

        clientThread = new ClientServerUtil(handler);
        // 客户端启动ClientThread线程创建网络连接、读取来自服务器的数据
        new Thread(clientThread).start();
        btnSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                if(emojiEdit.getText().toString().equals("")){
                    Toast.makeText(ChatActivity.this, "消息不能为空", Toast.LENGTH_SHORT).show();
                }else {
                    try
                    {
                        // 当用户按下发送按钮后，将用户输入的数据封装成Message
                        // 然后发送给子线程的Handler
                        Message msg = new Message();

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        date = dateFormat.format(new Date());

                        msg.what = 0x345;

                        String packMsg = name+DIV+icon+DIV+emojiEdit.getText().toString()+DIV+date;

                        msg.obj = packMsg;

                        clientThread.revHandler.sendMessage(msg);
                        // 清空input文本框
                        emojiEdit.setText("");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

            }
        });


        emojiBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showEmojiKeyboard(isChecked);
            }
        });

        //当点击输入框的时候先将emoji键盘隐藏
        emojiEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                emojiKeyboard.setVisibility(View.GONE);
                emojiBox.setChecked(false);
                return false;
            }
        });

        //初始化emoji键盘
        setEmojiconFragment(false);
    }

    /**
     * 初始化数据
     */
    public void initData(){
        name = SPUtil.getString("username","test");
        System.out.println("------------------>登录界面：icon "+icon);
        icon = SPUtil.getInt("icon",R.drawable.icon_1);
        //震动
        vibrator = (Vibrator) ChatActivity.this.getSystemService(Service.VIBRATOR_SERVICE);
        //提示音
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0); //分别对应声音池数量，AudioManager.STREAM_MUSIC 和 0
        //使用soundPool加载声音，该操作位异步操作，如果资源很大，需要一定的时间
        soundId = soundPool.load(this, R.raw.msg, 1);
        // 为声音池设定加载完成监听事件
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                flag = true;    //  表示加载完成
            }
        });
    }

    //显示或隐藏emoji键盘
    private void showEmojiKeyboard(boolean isShow) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (isShow) {
            imm.hideSoftInputFromWindow(emojiEdit.getWindowToken(), 0); //强制隐藏键盘
            emojiKeyboard.setVisibility(View.VISIBLE);
        } else {
            emojiKeyboard.setVisibility(View.GONE);
        }
    }

    //设置Fragment
    private void setEmojiconFragment(boolean useSystemDefault) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.emoji_keyboard, EmojiconsFragment.newInstance(useSystemDefault))
                .commit();
    }

    //emoji点击监听
    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(emojiEdit, emojicon);
    }

    //清除按钮
    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(emojiEdit);
    }

    /**
     * @param mss  服务器发来的数据
     * @return
     */
    private List<Map<String, Object>> getData(String mss){
        Map<String, Object> map = new HashMap<String, Object>();

        String[] arr = mss.split(DIV);

        String name = arr[0];
        int icon = Integer.parseInt(arr[1]);
        String text = arr[2];
        map.put("icon",icon);
        map.put("name",name);
        map.put("text",text);
        list.add(map);

        return list;
    }


    private class MyAdapter extends BaseAdapter {

        private int Type_1=0;//注意这个不同布局的类型起始值必须从0开始
        private int Type_2=1;

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {

            String name_1 = (String) list.get(position).get("name");
            if (name_1.equals(name)){
                return Type_2;
            }else{
                return Type_1;
            }

        }

        @Override
        public int getViewTypeCount() {

            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder_left viewHolder_1 = null;
            ViewHolder_right viewHolder_2 = null;
            int Type=getItemViewType(position);
            if(convertView==null){
                //左边显示
                if(Type==Type_1){
                    convertView=getLayoutInflater().inflate(R.layout.chat_left,parent,false);
                    viewHolder_1 = new ViewHolder_left();

                    viewHolder_1.icon_left = (ImageView) convertView.findViewById(R.id.chat_icon_left);
                    viewHolder_1.text_left = (EmojiconTextView) convertView.findViewById(R.id.chat_text_left);
                    viewHolder_1.name_left = (TextView) convertView.findViewById(R.id.chat_name_left);

                    convertView.setTag(viewHolder_1);
                    //右边显示
                }else if(Type==Type_2){
                    convertView=getLayoutInflater().inflate(R.layout.chat_right,parent,false);
                    viewHolder_2 = new ViewHolder_right();

                    viewHolder_2.icon_right = (ImageView) convertView.findViewById(R.id.chat_icon_right);
                    viewHolder_2.text_right = (EmojiconTextView) convertView.findViewById(R.id.chat_text_right);
                    viewHolder_2.name_right = (TextView) convertView.findViewById(R.id.chat_name_right);

                    convertView.setTag(viewHolder_2);
                }

            }else {

                if(Type==Type_1){
                    viewHolder_1 = (ViewHolder_left) convertView.getTag();
                }else if(Type==Type_2){
                    viewHolder_2 = (ViewHolder_right) convertView.getTag();
                }

            }
            //设置数据
            if(Type==Type_1){
                viewHolder_1.name_left.setText((String) list.get(position).get("name"));
                viewHolder_1.text_left.setText((String) list.get(position).get("text"));
                viewHolder_1.icon_left.setImageResource((Integer) list.get(position).get("icon"));
            }else if(Type==Type_2){

                viewHolder_2.text_right.setText((String) list.get(position).get("text"));
                viewHolder_2.icon_right.setImageResource((Integer) list.get(position).get("icon"));
            }

            return convertView;
        }

        private class ViewHolder_left{
            private EmojiconTextView text_left;
            private ImageView icon_left;
            private TextView name_left;

        }

        private class ViewHolder_right{
            private EmojiconTextView text_right;
            private ImageView icon_right;
            private TextView name_right;
        }
    }

    /**
     * 再按一次退出程序
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.chat_menu,menu);

        menu.findItem(R.id.sound).setChecked(SPUtil.getBoolean("sound",true));
        menu.findItem(R.id.shake).setChecked(SPUtil.getBoolean("shake",true));
        menu.findItem(R.id.auto_update).setChecked(SPUtil.getBoolean("update",true));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.isCheckable()){
            if (item.isChecked()){
                item.setChecked(false);
            }else {
                item.setChecked(true);
            }
        }

        switch (item.getItemId()){
            case R.id.sound://声音
                SPUtil.putBoolean("sound",item.isChecked());
                Toast.makeText(ChatActivity.this,item.isChecked()?"提示音已开启":"提示音已关闭", Toast.LENGTH_SHORT).show();
                break;
            case R.id.shake://震动
                SPUtil.putBoolean("shake",item.isChecked());
                Toast.makeText(ChatActivity.this,item.isChecked()?"震动已开启":"震动已关闭", Toast.LENGTH_SHORT).show();
                break;
            case R.id.auto_update://自动更新
                SPUtil.putBoolean("update",item.isChecked());
                Toast.makeText(ChatActivity.this, item.isChecked()?"自动更新已开启":"自动更新已关闭", Toast.LENGTH_SHORT).show();
                break;
            case R.id.logout://注销
                SPUtil.putString("username","");
                SPUtil.putString("password","");
                SPUtil.putInt("icon",R.drawable.icon_1);
                startActivity(new Intent(ChatActivity.this,LoginActivity.class));
                break;
            case R.id.exit: //退出
                finish();
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 当Activity关闭时   释放资源
     */
    @Override
    protected void onDestroy() {
        soundPool.release();
        soundPool = null;
        super.onDestroy();
    }
}

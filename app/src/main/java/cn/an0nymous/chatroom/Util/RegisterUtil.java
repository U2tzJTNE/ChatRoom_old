package cn.an0nymous.chatroom.Util;

/**
 * Created by 77009 on 2017-01-23.
 */


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.an0nymous.chatroom.Bean.IP;

public class RegisterUtil {

    // 通过Get方式获取HTTP服务器数据
    public static String executeHttpGet(String username, String password,String icon) {

        HttpURLConnection conn = null;
        InputStream is = null;

        try {
            // 用户名 密码
            // URL 地址
            String path = IP.ServerIP+"RegLet";
            path = path + "?username=" + username + "&password=" + password+ "&icon=" + icon;

            conn = (HttpURLConnection) new URL(path).openConnection();
            conn.setConnectTimeout(3000); // 设置超时时间
            conn.setReadTimeout(3000);
            conn.setDoInput(true);
            conn.setRequestMethod("GET"); // 设置获取信息方式
            conn.setRequestProperty("Charset", "UTF-8"); // 设置接收数据编码格式

            conn.connect();

            if (conn.getResponseCode() == 200) {
                is = conn.getInputStream();
                return ParseInfoUtil.parseInfo(is);
            }

        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 意外退出时进行连接关闭保护
            if (conn != null) {
                conn.disconnect();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return null;
    }
}
package cn.an0nymous.chatroom.Util;

/**
 * Created by jk on 2017-01-23.
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.an0nymous.chatroom.Bean.IP;

public class QueryUserUtil {

    // 通过Get方式获取HTTP服务器数据
    public static String executeHttpGet(String username) {

        HttpURLConnection conn = null;
        InputStream is = null;

        try {
            // 用户名
            // URL 地址
            String path = IP.ServerIP+"QueryUserLet";
            path = path + "?username=" + username;

            System.out.println("-------------->开始创建连接");
            conn = (HttpURLConnection) new URL(path).openConnection();
            System.out.println("-------------->创建连接成功");
            //System.out.println("-------------->请求返回码为： "+conn.getResponseCode());
            conn.setConnectTimeout(3000); // 设置超时时间
            conn.setReadTimeout(3000);
            conn.setDoInput(true);
            conn.setRequestMethod("GET"); // 设置获取信息方式
            conn.setRequestProperty("Charset", "UTF-8"); // 设置接收数据编码格式

            conn.connect();

            if (conn.getResponseCode() == 200) {
                System.out.println("------------------>接收数据成功");
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
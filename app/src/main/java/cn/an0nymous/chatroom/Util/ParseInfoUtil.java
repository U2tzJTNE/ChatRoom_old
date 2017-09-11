package cn.an0nymous.chatroom.Util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by 77009 on 2017-01-23.
 */

public class ParseInfoUtil {
    // 将输入流转化为 String 型
    public static String parseInfo(InputStream inStream) throws Exception {
        byte[] data = read(inStream);
        // 转化为字符串
        return new String(data, "UTF-8");
    }

    // 将输入流转化为byte型
    public static byte[] read(InputStream inStream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        inStream.close();
        return outputStream.toByteArray();
    }
}

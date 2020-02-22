package com.xuecheng.framework.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author: LJJ
 * @Program: eduBackend
 * @Description: 转换工具类
 * @Create: 2020-02-21 21:31:31
 * @Modified By:
 */
public class ConvertUtils {
    /*
    *
    * 将接受到的微信支付流转换为xml字符串
    */
    public static String convertToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        outputStream.close();
        inputStream.close();
        String result = new String(outputStream.toByteArray(), "utf-8");
        return result;
    }

    public static void main(String[] args) {

    }
}

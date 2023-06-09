package com.xiliulou.electricity.utils;

import lombok.extern.slf4j.Slf4j;
import shaded.org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.util.Base64;
import java.util.Objects;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-05-23-9:08
 */
@Slf4j
public class ImageUtil {
    private ImageUtil() {
    }

    /**
     * base64字符串转换成字节数组
     *
     * @param imgStr base64字符串
     */
    public static byte[] base64ToImage(String imgStr) {

        try {
            // Base64解码
            byte[] b = Base64.getDecoder().decode(imgStr);
            for (int i = 0; i < b.length; ++i) {
                // 调整异常数据
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            return b;
        } catch (Exception e) {
            log.error("base64ToImage数据转换异常", e);
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
    }

    /**
     * 将图片转换成Base64编码
     *
     * @param imgFile 待处理图片
     * @return
     */
    public static String imageToBase64(String imgFile) {
        // 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        byte[] data = null;
        try (InputStream in = new FileInputStream(imgFile)) {
            data = new byte[in.available()];
            in.read(data);
        } catch (IOException e) {
            log.error("base64ToImage数据转换异常", e);
        }
        return new String(Objects.nonNull(data) ? Base64.getEncoder().encode(data) : ArrayUtils.EMPTY_BYTE_ARRAY);
    }





    public static void main(String[] args) {
        String s = imageToBase64("C:\\Users\\BY\\Pictures\\test.jpg");

        byte[] bytes = base64ToImage(s);

    //写文件流
        try (OutputStream out = new FileOutputStream("D:\\aaa.gif");) {
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

    }


}

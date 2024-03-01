package com.xiliulou.electricity.utils;

import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import org.apache.commons.lang3.StringUtils;

import java.util.Base64;

/**
 * @author HeYafeng
 * @description 二维码相关工具
 * @date 2024/2/29 22:12:44
 */
public class QrCodeUtils {
    
    public static void main(String[] args) {
        Long merchantId = 38L;
        Long uid = 152083L;
        Integer type =1;
        String code = merchantId + ":" + uid + ":" + type;
        System.out.println(codeEnCoder(code));
        System.out.println(codeDeCoder("NlFoVldzOGppU1k0bXV5MU5SbjNrdz09"));
    }
    
    /**
     * 二维码加密
     */
    public static String codeEnCoder(String code) {
        String encrypt = AESUtils.encrypt(code);
        
        if (StringUtils.isNotBlank(encrypt)) {
            Base64.Encoder encoder = Base64.getUrlEncoder();
            byte[] base64Result = encoder.encode(encrypt.getBytes());
            return new String(base64Result);
        }
        return null;
    }
    
    /**
     * 二维码解密
     */
    public static String codeDeCoder(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        
        Base64.Decoder decoder = Base64.getUrlDecoder();
        byte[] decode = decoder.decode(code.getBytes());
        String base64Result = new String(decode);
        
        if (StringUtils.isNotBlank(base64Result)) {
            return AESUtils.decrypt(base64Result);
        }
        return null;
    }
}

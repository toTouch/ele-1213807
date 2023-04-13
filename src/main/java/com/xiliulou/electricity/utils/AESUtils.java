package com.xiliulou.electricity.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * @author: eclair
 * @Date: 2020/12/1 09:45
 * @Description:
 */
@Slf4j
public class AESUtils {
	private static final String ALGORITHM = "AES";
	private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

	private static final String SECERT_KEY = "123abc*@123yxab$";

	public static String encrypt(String content) {
		try {
			Cipher instance = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
			SecretKeySpec secretKeySpec = new SecretKeySpec(SECERT_KEY.getBytes(), ALGORITHM);
			instance.init(Cipher.ENCRYPT_MODE, secretKeySpec);
			byte[] bytes = instance.doFinal(content.getBytes(StandardCharsets.UTF_8));
			return Base64Utils.encodeToString(bytes);
		} catch (Exception ex) {
			log.error("AES ERROR! encrypt error! content={}", content, ex);
		}
		return null;
	}

	public static String decrypt(String content) {
		try {
			byte[] bytes = Base64Utils.decodeFromString(content);
			Cipher instance = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
			SecretKeySpec secretKeySpec = new SecretKeySpec(SECERT_KEY.getBytes(), ALGORITHM);
			instance.init(Cipher.DECRYPT_MODE, secretKeySpec);
			byte[] bytes1 = instance.doFinal(bytes);
			return new String(bytes1);
		} catch (Exception e) {
			log.error("AES ERROR! decrypt error! content={}", content, e);
		}
		return null;
	}


	public static String encrypt(String content, String secretKey) {
		try {
			Cipher instance = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
			SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
			instance.init(Cipher.ENCRYPT_MODE, secretKeySpec);
			byte[] bytes = instance.doFinal(content.getBytes(StandardCharsets.UTF_8));
			return Base64Utils.encodeToString(bytes);
		} catch (Exception ex) {
			log.error("AES ERROR! encrypt error! content={}", content, ex);
		}
		return null;
	}

	public static String decrypt(String content, String secretKey) {
		try {
			byte[] bytes = Base64Utils.decodeFromString(content);
			Cipher instance = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
			SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
			instance.init(Cipher.DECRYPT_MODE, secretKeySpec);
			byte[] bytes1 = instance.doFinal(bytes);
			return new String(bytes1);
		} catch (Exception e) {
			log.error("AES ERROR! decrypt error! content={}", content, e);
		}
		return null;
	}

//	public static void main(String[] args) {
//		String niaho = encrypt("123456");
//		System.out.println(niaho);
//		String decrypt = decrypt(niaho);
//		System.out.println(decrypt);
//	}
}

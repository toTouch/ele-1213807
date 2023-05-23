package com.xiliulou.electricity.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author: Miss.Li
 * @Date: 2021/8/30 09:33
 * @Description:
 */
public class DesensitizationUtil {

	/**
	 * 只显示第一个汉字，其他隐藏为2个星号<例子：李**>
	 *
	 * @param fullName
	 * @return
	 */
	public static String left(String fullName) {
		if (StringUtils.isBlank(fullName)) {
			return "";
		}
		String name = StringUtils.left(fullName, 1);
		return StringUtils.rightPad(name, StringUtils.length(fullName), "*");
	}

	/**
	 * 110****58，前面保留3位明文，后面保留2位明文
	 *
	 * @param name
	 * @param index 3
	 * @param end   2
	 * @return
	 */
	public static String around(String name, int index, int end) {
		if (StringUtils.isBlank(name)) {
			return "";
		}
		return StringUtils.left(name, index).concat(StringUtils.removeStart(StringUtils.leftPad(StringUtils.right(name, end), StringUtils.length(name), "*"), "***"));
	}

	/**
	 * 后四位，其他隐藏<例子：****1234>
	 *
	 * @param num
	 * @return
	 */
	public static String right(String num, int end) {
		if (StringUtils.isBlank(num)) {
			return "";
		}
		return StringUtils.leftPad(StringUtils.right(num, end), StringUtils.length(num), "*");
	}

	// 手机号码前三后四脱敏
	public static String mobileEncrypt(String mobile) {
		if (StringUtils.isEmpty(mobile) || (mobile.length() != 11)) {
			return mobile;
		}
		return mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
	}

	//身份证前三后四脱敏
	public static String idEncrypt(String id) {
		if (StringUtils.isEmpty(id) || (id.length() < 8)) {
			return id;
		}
		return id.replaceAll("(?<=\\w{3})\\w(?=\\w{4})", "*");
	}

	//护照前2后3位脱敏，护照一般为8或9位
	public static String idPassport(String id) {
		if (StringUtils.isEmpty(id) || (id.length() < 8)) {
			return id;
		}
		return id.substring(0, 2) + new String(new char[id.length() - 5]).replace("\0", "*") + id.substring(id.length() - 3);
	}

	/**
	 * 证件后几位脱敏
	 *
	 * @param id
	 * @param sensitiveSize
	 * @return
	 */
	public static String idPassport(String id, int sensitiveSize) {
		if (StringUtils.isBlank(id)) {
			return "";
		}
		int length = StringUtils.length(id);
		return StringUtils.rightPad(StringUtils.left(id, length - sensitiveSize), length, "*");
	}
	/**
	 * [身份证号] 前六位，后四位，其他用星号隐藏每位1个星号<例子:451002********1647>
	 *
	 * @param carId
	 * @return
	 */
	public static String idCard(String carId){
		if (StringUtils.isBlank(carId)) {
			return "";
		}
		return StringUtils.left(carId, 1).concat(StringUtils.removeStart(StringUtils.leftPad(StringUtils.right(carId, 1), StringUtils.length(carId), "*"), "******"));
	}

	/**
	 * [银行卡号] 前六位，后四位，其他用星号隐藏每位1个星号<例子:6222600**********1234>
	 *
	 * @param cardNum
	 * @return
	 */
	public static String bankCard(String cardNum) {
		if (StringUtils.isBlank(cardNum)) {
			return "";
		}
		return StringUtils.left(cardNum, 6).concat(StringUtils.removeStart(StringUtils.leftPad(StringUtils.right(cardNum, 4), StringUtils.length(cardNum), "*"), "******"));
	}
    
    /**
     * 手机号脱敏 138****8888
     *
     * @param
     * @return
     */
    public static String phoneDesensitization(String phone) {
        if (StringUtils.isBlank(phone)) {
            return "";
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }
}

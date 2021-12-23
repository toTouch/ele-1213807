package com.xiliulou.electricity.utils;

import cn.hutool.core.util.ObjectUtil;

import java.math.BigDecimal;

/**
 * @ClassName : BigDecimalUtil
 * @Description : 关于价格的计算工具类
 * @Author : YG
 * @Date: 2020-03-19 13:55
 */
public class BigDecimalUtil {

	/**
	 * 判断是否小于0
	 *
	 * @param b
	 * @return
	 */
	public static boolean smallerThanZero(BigDecimal b) {
		if (ObjectUtil.isEmpty(b)) {
			return true;
		}
		BigDecimal zero1 = BigDecimal.ZERO;
		if (zero1.compareTo(b) == 1) {
			return true;
		}
		return false;
	}

	/**
	 * 转换金钱 元为分
	 *
	 * @return
	 */
	public static Long transformBigDecimalToLong(BigDecimal a) {
		BigDecimal b = new BigDecimal(100);
		return Long.valueOf(String.valueOf(a.multiply(b).intValue()));
	}

	/**
	 * 转换金钱 元为分
	 *
	 * @return
	 */
	public static Double transformLongToDouble(long a) {
		return Double.valueOf(BigDecimal.valueOf
				(Long.valueOf(a)).divide(new BigDecimal(100)).toString());
	}



}

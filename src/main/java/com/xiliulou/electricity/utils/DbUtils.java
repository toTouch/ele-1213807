package com.xiliulou.electricity.utils;

import java.util.function.Supplier;

/**
 * @author: eclair
 * @Date: 2020/12/1 10:16
 * @Description:
 */
public class DbUtils {
	public static <T> T dbOperateSuccessThen(Integer id, Supplier<T> supplier) {
		if (id > 0) {
			return supplier.get();
		}
		return null;
	}
}

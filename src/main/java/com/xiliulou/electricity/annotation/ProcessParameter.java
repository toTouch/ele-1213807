package com.xiliulou.electricity.annotation;

import com.xiliulou.electricity.constant.installment.InstallmentConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ！！！！！仅有使用对象接收请求参数的方法可以使用，并且参数对象需要设置在方法入参的第一个，这是写死了的！！！！！
 * 默认仅做登录校验，其他功能需要设置type开启，type的值建议设置常量
 *
 * @see InstallmentConstants PROCESS_PARAMETER_*  此类常量名为用于校验的常量
 * @author SJP
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProcessParameter {
    int type() default 0;
}

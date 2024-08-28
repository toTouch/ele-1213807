package com.xiliulou.electricity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解用于登录校验，设置数据访问权限，以及校验分页查询接口分页参数
 * ！！！！！仅有使用对象接收请求参数的方法可以使用，并且参数对象需要设置在controller方法入参的第一个，这是写死了的！！！！！
 * 需要校验分页参数时，type的值需设置为：InstallmentConstants.PROCESS_PARAMETER_TYPE_PAGE
 * @author SJP
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProcessParameter {
    String type() default "";
}

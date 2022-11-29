package com.xiliulou.electricity.annotation;

import java.lang.annotation.*;

/**
 * 自定义操作日志注解
 *
 * @author zzlong
 * @since 2022-10-11 14:20:37
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * 操作内容
     */
    String title() default "";

    /**
     * 功能
     */
    String businessType() default "other";

    /**
     * 是否保存请求的参数
     */
    boolean isSaveRequestData() default false;

    /**
     * 是否保存响应的参数
     */
    boolean isSaveResponseData() default false;
}

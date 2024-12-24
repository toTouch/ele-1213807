package com.xiliulou.electricity.annotation;


import java.lang.annotation.*;

/**
 * 幂等性校验注解
 *
 * @author caobotao.cbt
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface IdempotentCheck {
    
    /**
     * 请求间隔的毫秒
     */
    long requestIntervalMilliseconds() default 3000L;

    /**
     * 请求间隔的毫秒
     */
    String prefix() default "";
}

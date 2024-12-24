/**
 *  Create date: 2024/5/30
 */

package com.xiliulou.electricity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/5/30 18:21
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface ParamIdempotent {
    
    /**
     * 字段名
     */
    String[] value() default {};
    
}

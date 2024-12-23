package com.xiliulou.electricity.utils;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.electricity.exception.BizException;

import java.util.Objects;

/**
 * @ClassName: AssertUtil
 * @description:
 * @author: renhang
 * @create: 2024-11-21 11:02
 */
public class AssertUtil {
    
    public static void assertStrIsNull(String input, String code, String msg) {
        if (StrUtil.isBlank(input)) {
            throw new BizException(code, msg);
        }
    }
    
    
    public static void assertObjectIsNull(Object obj, String code, String msg) {
        if (Objects.isNull(obj)) {
            throw new BizException(code, msg);
        }
    }
}

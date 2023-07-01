package com.xiliulou.electricity.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.xiliulou.electricity.enums.BusinessType;

/**
 * 订单id工具类
 *
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-06-12:59
 */
public class OrderIdUtil {
    
    private static final String PURE_DATE_PATTERN = "yyMMddHHmm";
    
    private OrderIdUtil() {
    }
    
    /**
     * 生成业务ID 规则：业务类型 + 年月日时分秒(221011111111) + uid + 随机2位 ）
     *
     * @return
     */
    public static String generateBusinessOrderId(BusinessType businessType, Long uid) {
        return businessType.getBusiness().toString() + DateUtil.format(DateUtil.date(), PURE_DATE_PATTERN) + uid
                + RandomUtil.randomInt(10, 99);
    }

    public static void main(String[] args) {
        String yyMMdd = DateUtil.format(DateUtil.date(), "yyMMddHHmm");

        System.out.println(yyMMdd);
    }
    
}

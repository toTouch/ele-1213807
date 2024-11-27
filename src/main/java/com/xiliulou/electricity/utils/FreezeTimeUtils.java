package com.xiliulou.electricity.utils;

import com.xiliulou.electricity.constant.TimeConstant;
import org.apache.commons.lang3.tuple.Pair;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/11/27 15:09
 */
public class FreezeTimeUtils {
    
    
    /**
     * 计算剩余冻结天数
     *
     * @param applyTerm
     * @param startFreezeTime
     * @param endFreezeTime
     * @author caobotao.cbt
     * @date 2024/11/27 15:11
     */
    public static Pair<Boolean, Long> calculateRemainingFrozenDays(Integer applyTerm, Long startFreezeTime, Long endFreezeTime) {
        // 总冻结时间 - 已使用时间 = 剩余时间
        long remainingTime = (applyTerm * TimeConstant.DAY_MILLISECOND) - (endFreezeTime - startFreezeTime);
        if (remainingTime <= 0) {
            return Pair.of(false, null);
        }
        
        return Pair.of(true,remainingTime);
    }
    
}

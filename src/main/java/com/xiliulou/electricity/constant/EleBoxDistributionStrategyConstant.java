package com.xiliulou.electricity.constant;

/**
 * @author zgw
 * @date 2023/2/6 19:31
 * @mood
 */
public interface EleBoxDistributionStrategyConstant {
    
    /**
     * 分配上一个满电的格挡策略
     */
    String PRE_FULL_CHARGE_CELL_DISTRIBUTION_STRATEGY = "PRE_FULL_CHARGE_CELL_DISTRIBUTION_STRATEGY";
    /**
     * 分配空闲时间最大的格挡策略
     */
    String FREE_TIME_MAX_CELL_DISTRIBUTION_STRATEGY = "FREE_TIME_MAX_CELL_DISTRIBUTION_STRATEGY";
    /**
     * 随机分配格挡策略
     */
    String RANDOM_CELL_DISTRIBUTION_STRATEGY = "RANDOM_CELL_DISTRIBUTION_STRATEGY";
}

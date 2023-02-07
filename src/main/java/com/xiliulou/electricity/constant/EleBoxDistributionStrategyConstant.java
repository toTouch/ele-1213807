package com.xiliulou.electricity.constant;

/**
 * @author zgw
 * @date 2023/2/6 19:31
 * @mood
 */
public interface EleBoxDistributionStrategyConstant {
    
    /**
     * 分配上一个取电池格挡策略
     */
    String PRE_TAKE_CELL_DISTRIBUTION_STRATEGY = "PRE_TAKE_CELL_DISTRIBUTION_STRATEGY";
    /**
     * 分配空闲时间最大的格挡策略
     */
    String FREE_TIME_MAX_CELL_DISTRIBUTION_STRATEGY = "FREE_TIME_MAX_CELL_DISTRIBUTION_STRATEGY";
    /**
     * 分配随机格挡策略
     */
    String RANDOM_CELL_DISTRIBUTION_STRATEGY = "RANDOM_CELL_DISTRIBUTION_STRATEGY";
}

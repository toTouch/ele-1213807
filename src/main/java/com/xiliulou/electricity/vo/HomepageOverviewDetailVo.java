package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author hrp
 * @date 2022/08/01 14:22
 * @mood 首页押金
 */
@Data
public class HomepageOverviewDetailVo {

    /**
     * 实名认证用户数量
     */
    private Integer authenticationUserCount;

    /**
     * 门店数量
     */
    private Integer storeCount;

    /**
     * 柜机数量
     */
    private Integer electricityCabinetCount;

    /**
     * 车辆数量
     */
    private Integer carCount;
}

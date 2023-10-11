package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-04-9:24
 */
@Data
public class CabinetBatteryVO {

    /**
     * 空仓数量
     */
    private long emptyCellNumber;

    /**
     * 有电池数量
     */
    private long haveBatteryNumber;

    /**
     * 可换电数量
     */
    private long exchangeableNumber;
}

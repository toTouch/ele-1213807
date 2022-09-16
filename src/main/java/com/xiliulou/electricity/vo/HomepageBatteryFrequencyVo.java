package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author hrp
 * @date 2022/08/03 14:22
 * @mood 首页电池分析
 */
@Data
public class HomepageBatteryFrequencyVo {

    private String sn;

    private Integer useCount;
    @Deprecated
    private Integer status;
    private Integer physicsStatus;
}

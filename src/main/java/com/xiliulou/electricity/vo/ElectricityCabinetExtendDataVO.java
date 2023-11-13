package com.xiliulou.electricity.vo;

import lombok.Data;

@Data
public class ElectricityCabinetExtendDataVO {
    
    /**
     * 信号强度
     */
    private Integer dbm;
    
    /**
     * 信号类型(当前字冗余段未使用):wifi 移动网络 有线
     */
    private Integer netType;
}

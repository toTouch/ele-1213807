package com.xiliulou.electricity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @ClassName: FreeDepositDelayDTO
 * @description:
 * @author: renhang
 * @create: 2024-08-26 11:00
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatteryRecycleDelayDTO implements Serializable {
    /**
     * 电池sn
     */
    private String sn;
    
    /**
     * 柜机id
     */
    private Integer cabinetId;
    
    /**
     * 格口
     */
    private String cellNo;
    
    /**
     * 回收记录id
     */
    private Long recycleId;
}

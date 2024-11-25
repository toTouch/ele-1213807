package com.xiliulou.electricity.dto;

import lombok.Data;

/**
 * @author maxiaodong
 * @date 2024/11/12 15:20
 * @desc
 */
@Data
public class EleBatteryMarkRecordDTO {
    /**
     * 电池出仓门对应的换电订单
     */
    private String rentOrderId;
    
    /**
     * 电池进仓门对应的换电订单
     */
    private String returnOrderId;
    
    private Integer cabinetId;
    
    private Integer cellNo;
    
    private String batterySn;
}

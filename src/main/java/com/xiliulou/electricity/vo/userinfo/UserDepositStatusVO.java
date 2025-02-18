package com.xiliulou.electricity.vo.userinfo;

import lombok.Builder;
import lombok.Data;

/**
 * @author HeYafeng
 * @date 2024/8/16 14:35:10
 */
@Builder
@Data
public class UserDepositStatusVO {
    
    private Long uid;
    
    /**
     * 电池押金状态 0--未缴纳押金，1--已缴纳押金,2--押金退款中
     */
    private Integer batteryDepositStatus;
    
    /**
     * 车辆押金状态:0--未缴纳押金，1--已缴纳押金
     */
    private Integer carDepositStatus;
    
    /**
     * 车电一体押金状态:0--已缴纳 1--未缴纳
     *
     * @see com.xiliulou.electricity.enums.YesNoEnum
     */
    private Integer carBatteryDepositStatus;
}

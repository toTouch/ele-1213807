package com.xiliulou.electricity.dto;

import com.xiliulou.electricity.entity.ElectricityCabinet;
import lombok.Builder;
import lombok.Data;

/**
 * @ClassName: BackSelfOpenCellDTO
 * @description:
 * @author: renhang
 * @create: 2024-11-06 10:56
 */
@Data
@Builder
public class BackSelfOpenCellDTO {
    
    /**
     * 订单id
     */
    private Long id;
    
    /**
     * 格挡号
     */
    private Integer cell;
    
    /**
     * 用户绑定电池
     */
    private String userBindingBatterySn;
    
    /**
     * 订单id
     */
    private String orderId;
    
    /**
     * 柜机
     */
    private ElectricityCabinet cabinet;
    
    /**
     * msg
     */
    private String msg;
    
    /**
     * 租户id
     */
    private Integer tenantId;
    
    /**
     * 上次订单类型
     *
     * @see com.xiliulou.electricity.enums.LastOrderTypeEnum
     */
    private Integer lastOrderType;
}

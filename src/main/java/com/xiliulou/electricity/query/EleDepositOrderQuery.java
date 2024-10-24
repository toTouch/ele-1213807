package com.xiliulou.electricity.query;

import com.xiliulou.electricity.enums.enterprise.PackageOrderTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author: eclair
 * @Date: 2020/6/15 10:02
 * @Description:
 */
@Data
@Builder
public class EleDepositOrderQuery {
    private Long size;
    private Long offset;
    /**
     * 用户名字
     */
    private String name;
    private String phone;

    private Long beginTime;
    private Long endTime;
    private Integer status;
    private String orderId;

    private Integer tenantId;

    private List<Long> franchiseeIds;

    private Long franchiseeId;

    private String franchiseeName;

    private Long uid;

    private Integer depositType;
    private Integer payType;

    private Integer refundOrderType;

    private List<Long> storeIds;

    private Long storeId;

    private String carModel;

    private String storeName;
    
    /**
     * 订单类型： 0-普通换电订单，1-企业渠道换电订单
     * @see PackageOrderTypeEnum
     */
    private Integer orderType;
    
    /**
     * @see com.xiliulou.core.base.enums.ChannelEnum
     */
    private String paymentChannel;
}

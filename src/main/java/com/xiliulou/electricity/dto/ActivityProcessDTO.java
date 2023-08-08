package com.xiliulou.electricity.dto;

import com.xiliulou.electricity.enums.ActivityEnum;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import lombok.Data;

/**
 * @author: Kenneth
 * @Date: 2023/8/5 11:33
 * @Description:
 */

@Data
public class ActivityProcessDTO {

    /**
     * 用户ID 用于登录注册或实名认证情况下的活动处理
     */
    private Long uid;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 套餐类型 (1 - 换电， 2 - 租车, 3 - 车电一体)
     * @see PackageTypeEnum
     */
    private Integer Type;

    /**
     * 活动途径类型
     * @see ActivityEnum
     */
    private Integer activityType;

    /**
     * 链路ID
     */
    private String traceId;

}

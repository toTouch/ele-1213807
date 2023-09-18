package com.xiliulou.electricity.dto;

import com.xiliulou.electricity.enums.DivisionAccountEnum;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import lombok.Data;

/**
 * @author: Kenneth
 * @Date: 2023/7/26 9:55
 * @Description:
 */

@Data
public class DivisionAccountOrderDTO {

    /**
     * 订单号码
     */
    private String orderNo;

    /**
     * 套餐类型 (1 - 换电， 2 - 租车, 3 - 车电一体)
     * @see PackageTypeEnum
     */
    private Integer Type;

    /**
     * 分账类型 (0 - 购买， 1 - 退租)
     * @see DivisionAccountEnum
     */
    private Integer divisionAccountType;

    /**
     * 链路ID
     */
    private String traceId;

}

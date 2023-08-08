package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.enums.PackageTypeEnum;
import lombok.Data;

/**
 * @author: Kenneth
 * @Date: 2023/8/7 19:24
 * @Description:
 */

@Data
public class DivisionAccountBatteryMemberCardVO {

    private Long id;

    private Long divisionAccountId;

    private Long refId;

    /**
     * 套餐类型
     * @see PackageTypeEnum
     */
    private Integer type;

    private Integer tenantId;

    private Integer delFlag;

    private Long createTime;

    private Long updateTime;

    private Integer hierarchy;

    /**
     * 状态（0-启用，1-禁用）
     */
    private Integer status;

}

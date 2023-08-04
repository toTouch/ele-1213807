package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.enums.PackageTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-27-16:13
 */
@Data
public class DivisionAccountConfigRefVO {

    private Long id;
    /**
     * 分帐配置名称
     */
    private String name;
    /**
     * 分帐层级
     */
    private Integer hierarchy;
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    /**
     * 门店id
     */
    private Long storeId;
    /**
     *
     */
    private BigDecimal operatorRate;

    private BigDecimal operatorRateOther;
    /**
     * 加盟商收益率
     */
    private BigDecimal franchiseeRate;

    private BigDecimal franchiseeRateOther;
    /**
     * 门店收益率
     */
    private BigDecimal storeRate;
    /**
     * 状态（0-启用，1-禁用）
     */
    private Integer status;
    /**
     * 业务类型
     */
    private Integer type;

    private Long refId;

    /**
     * 套餐类型
     * @see PackageTypeEnum
     */
    private Integer packageType;

}

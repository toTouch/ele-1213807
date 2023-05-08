package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DivisionAccountOperationRecordVO {
    /**
     * 用户绑定的用户名
     */
    private String userName;

    /**
     * 分账配置id
     */
    private Integer divisionAccountId;

    /**
     * 分账层级
     */
    private Integer hierarchy;

    /**
     * 分账状态
     */
    private Integer status;

    /**
     * 分账类型
     */
    private Integer type;

    private Long size;

    private Long offset;
    /**
     * id
     */
    private Long id;

    /**
     * 修改人id
     */
    private Long uid;
    /**
     * 业务名称
     */
    private String name;
    /**
     * 柜机运营商收益率
     */
    private BigDecimal cabinetOperatorRate;
    /**
     * 柜机加盟商收益率
     */
    private BigDecimal cabinetFranchiseeRate;
    /**
     * 柜机门店收益率
     */
    private BigDecimal cabinetStoreRate;
    /**
     * 非柜机运营商收益率
     */
    private BigDecimal nonCabOperatorRate;
    /**
     * 非柜机加盟商收益率
     */
    private BigDecimal nonCabFranchiseeRate;
    /**
     * 租户id
     */
    private Integer tenantId;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 修改时间
     */
    private Long updateTime;
    /**
     * json格式的分账套餐
     */
    private String accountMemberCard;
}

package com.xiliulou.electricity.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * (DivisionAccountOperationRecord)ʵ����
 *
 * @author zyb
 * @since 2023-05-08 13:38:49
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_division_account_operation_record")
@Accessors(chain = true)
public class DivisionAccountOperationRecord {

    /**
     * id
     */
    private Long id;

    /**
     * 分账配置id
     */
    private Integer divisionAccountId;

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

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}

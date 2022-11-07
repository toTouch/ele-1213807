package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 换电柜保险(InsuranceUserInfo)实体类
 *
 * @author makejava
 * @since 2022-11-02 14:44:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_insurance_user_info")
public class InsuranceUserInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Long uid;

    /**
     * 加盟商id
     */
    private Long franchiseeId;

    /**
     * 保费
     */
    private BigDecimal premium;

    /**
     * 保额
     */
    private BigDecimal forehead;

    /**
     * 保险Id
     */
    private Integer insuranceId;

    /**
     * 保险订单编号
     */
    private String insuranceOrderId;

    /**
     * 保险过期时间
     */
    private Long insuranceExpireTime;

    /**
     * 是否出险 0--未出险 1--已出险
     */
    private Integer isUse;

    /**
     * 删除标志 0--正常 1--删除
     */
    private Integer delFlag;

    //租户id
    private Integer tenantId;

    private Long createTime;

    private Long updateTime;


    //禁用状态
    public static final Integer STATUS_UN_USABLE = 1;
    //可用状态
    public static final Integer STATUS_USABLE = 0;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}

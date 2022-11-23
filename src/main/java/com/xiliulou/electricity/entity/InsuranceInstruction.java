package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 换电柜保险说明(InsuranceInstruction)实体类
 *
 * @author makejava
 * @since 2022-11-03 14:44:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_insurance_instruction")
public class InsuranceInstruction {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 保险说明
     */
    private String instruction;

    /**
     * 保险Id
     */
    private Integer insuranceId;

    /**
     * 加盟商id
     */
    private Long franchiseeId;

    /**
     * 删除状态 0--正常 1--删除
     */
    private Integer delFlag;


    //租户id
    private Integer tenantId;

    private Long createTime;

    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}

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
 * (StoreSplitAccountHistory)实体类
 *
 * @author lxc
 * @since 2021-09-13 20:09:25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_store_split_account_history")
public class StoreSplitAccountHistory {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
     * 商家id
     */
    private Long storeId;

    private Long createTime;
    /**
     * 分账金额
     */
    private BigDecimal splitAmount;
    /**
     * 这次分账的当前收入
     */
    private BigDecimal currentTotalIncome;
    /**
     * 分账的订单id
     */
    private String orderId;

    private Integer tenantId;
    /**
     * 收益来源 1--月卡
     */
    private Integer type;

    /**
     * 这次订单收益
     */
    private BigDecimal payAmount;
    /**
     * 应该分润比例
     */
    private Integer percent;

    public static final Integer TYPE_MEMBER = 1;

    public static final Integer TYPE_OPERATOR = 2;

}

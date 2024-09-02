package com.xiliulou.electricity.entity.meituan;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.thirdmall.enums.meituan.virtualtrade.VirtualTradeStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author HeYafeng
 * @description 美团骑手商城订单表实体类
 * @date 2024/8/28 10:26:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_meituan_rider_mall_order")
public class MeiTuanRiderMallOrder {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 订单ID
     */
    private String meiTuanOrderId;
    
    /**
     * 下单时间
     */
    private Long meiTuanOrderTime;
    
    /**
     * 订单状态: 10-已付款, 20-订单取消, 40-已发货
     *
     * @see VirtualTradeStatusEnum
     */
    private Integer meiTuanOrderStatus;
    
    /**
     * 实付价
     */
    private BigDecimal meiTuanActuallyPayPrice;
    
    /**
     * 充值模式: 1-兑换码模式, 2-直充模式
     */
    private Integer meiTuanVirtualRechargeType;
    
    /**
     * 手机号
     */
    private String meiTuanAccount;
    
    /**
     * 券码
     */
    private String coupon;
    
    /**
     * 套餐订单ID
     */
    private String orderId;
    
    /**
     * 同步对账状态: 1-已处理, 2-未处理, 3-已对账
     *
     * @see VirtualTradeStatusEnum
     */
    private Integer orderSyncStatus;
    
    /**
     * 订单使用状态: 0-未使用, 1-已使用
     *
     * @see VirtualTradeStatusEnum
     */
    private Integer orderUseStatus;
    
    /**
     * 套餐ID(也就是美团skuId)
     */
    private Long packageId;
    
    /**
     * 套餐类型: 1-电, 2-车, 3-车电
     */
    private Integer packageType;
    
    /**
     * 用户uid
     */
    private Long uid;
    
    /**
     * 租户ID
     */
    private Integer tenantId;
    
    /**
     * 删除标志: 0--正常, 1--删除
     */
    private Integer delFlag;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
}

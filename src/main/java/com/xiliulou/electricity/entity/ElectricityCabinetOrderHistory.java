package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 历史订单表
 *
 * @author renhang
 * @since 2024-09-12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet_order_history")
public class ElectricityCabinetOrderHistory {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单编号--时间戳+柜子id+仓门号+用户id+5位随机数,20190203 21 155 1232)
     */
    private String orderId;
    
    /**
     * 换电人手机号
     */
    private String phone;
    
    /**
     * 换电人id
     */
    private Long uid;
    
    /**
     * 支付金额
     */
    private Double payAmount;
    
    /**
     * 换电柜id
     */
    private Integer electricityCabinetId;
    
    /**
     * 老电池编号
     */
    private String oldElectricityBatterySn;
    
    /**
     * 新电池编号
     */
    private String newElectricityBatterySn;
    
    /**
     * 换电柜的旧仓门号
     */
    private Integer oldCellNo;
    
    /**
     * 换电柜的新仓门号
     */
    private Integer newCellNo;
    
    //订单状态序号
    private Double orderSeq;
    
    /**
     * 订单的状态
     */
    private String status;
    
    /**
     * 类型(0:月卡,1:季卡,2:年卡)
     */
    private Integer paymentMethod;
    
    /**
     * 下单的来源 1--微信公众号 2--小程序
     */
    private Integer source;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 换电开始时间
     */
    private Long switchBeginTime;
    
    /**
     * 换电结束时间
     */
    private Long switchEndTime;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    //租户id
    private Integer tenantId;
    
    private Long franchiseeId;
    
    //门店id
    private Long storeId;
    
    /**
     * 渠道
     *
     * @see com.xiliulou.core.base.enums.ChannelEnum
     */
    private String channel;
    
    
    /**
     * 订单阶段状态
     */
    private String orderStatus;
    
}

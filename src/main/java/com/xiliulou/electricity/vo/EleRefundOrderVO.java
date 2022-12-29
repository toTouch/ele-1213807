package com.xiliulou.electricity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 退款订单表(TEleRefundOrder)实体类
 *
 * @author makejava
 * @since 2021-02-22 10:17:06
 */
@Data
@Builder
public class EleRefundOrderVO {
    /**
    * 退款Id
    */
    private Long id;
    /**
    * 退款单号
    */
    private String refundOrderNo;
    /**
    * 支付单号
    */
    private String orderId;
    /**
    * 支付金额,单位元
    */
    private BigDecimal payAmount;
    /**
    * 退款金额,单位元
    */
    private BigDecimal refundAmount;
    /**
    * 退款状态:0--订单生成,1-退款中,2-退款成功,-1-退款失败
    */
    private Integer status;
    /**
    * 错误原因
    */
    private String errMsg;
    /**
    * 是否删除（0-正常，1-删除）
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
    /**
     * 用户Id
     */
    private Long uid;
    /**
     * 用户名
     */
    private String name;
    /**
     * 手机号
     */
    private String phone;

    private String payType;

}

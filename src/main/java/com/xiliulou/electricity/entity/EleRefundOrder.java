package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * 退款订单表(TEleRefundOrder)实体类
 *
 * @author makejava
 * @since 2021-02-22 10:17:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_refund_order")
public class EleRefundOrder {
    /**
    * 退款Id
    */
    @TableId(value = "id",type = IdType.AUTO)
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
    private Double payAmount;
    /**
    * 退款金额,单位元
    */
    private Double refundAmount;
    /**
    * 退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败,4-业务处理完成
    */
    private Object status;
    /**
    * 错误原因
    */
    private String errMsg;
    /**
    * 是否删除（0-正常，1-删除）
    */
    private Object delFlag;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 更新时间
    */
    private Long updateTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
package com.xiliulou.electricity.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (ApiOrderOperHistory)实体类
 *
 * @author Eclair
 * @since 2021-11-09 16:57:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_api_order_oper_history")
public class ApiOrderOperHistory {
    
    private Long id;
    /**
    * 订单id
    */
    private String orderId;
    /**
    * 创建时间
    */
    private Long createTime;
    /**
    * 订单类型
    */
    private Integer type;
    /**
    * 报错信息
    */
    private String msg;
    
    private Integer tenantId;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

    /**
     * 租电池和还电池订单类型
     */
    public static final Integer ORDER_TYPE_RENT_BACK = 1;
    /**
     * 换电池订单类型
     */
    public static final Integer ORDER_TYPE_EXCHANGE = 2;


}

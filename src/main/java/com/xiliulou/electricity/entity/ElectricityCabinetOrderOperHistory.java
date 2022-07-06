package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * 订单的操作历史记录(TElectricityCabinetOrderOperHistory)实体类
 *
 * @author makejava
 * @since 2020-11-26 10:57:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_electricity_cabinet_order_oper_history")
public class ElectricityCabinetOrderOperHistory {

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    /**
     * 订单编号--时间戳+柜子id+仓门号+用户id+5位随机数,20190203 21 155 1232)
     */
    private String orderId;
    /**
     *订单的类型 1--租、还电,2--换电
     */
    private Integer type;

    //错误信息
    private String msg;

    /**
     * 操作步骤序号
     */
    private Integer seq;
    /**
     * 操作结果 0：成功，1：失败
     */
    private Integer result;

    /**
     * 创建时间
     */
    private Long createTime;

    //租户id
    private Integer tenantId;


    //租\还电
    public static final Integer ORDER_TYPE_RENT_BACK = 1;
    //换电
    public static final Integer  ORDER_TYPE_EXCHANGE = 2;

    //操作结果 0：成功，1：失败
    public static final Integer  OPERATE_RESULT_SUCCESS = 0;
    public static final Integer  OPERATE_RESULT_FAIL = 1;

}

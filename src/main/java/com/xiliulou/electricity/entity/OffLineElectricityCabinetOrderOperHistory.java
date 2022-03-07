package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.vo.OperateMsgVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 离线换电订单的操作历史记录(TElectricityCabinetOrderOperHistory)实体类
 *
 * @author HRP
 * @since 2022-03-07 16:04:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OffLineElectricityCabinetOrderOperHistory {

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
    private List<OperateMsgVo> operateMsgVos;

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



}

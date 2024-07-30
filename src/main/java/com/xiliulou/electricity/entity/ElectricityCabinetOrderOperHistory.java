package com.xiliulou.electricity.entity;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 订单编号--时间戳+柜子id+仓门号+用户id+5位随机数,20190203 21 155 1232)
     */
    private String orderId;
    
    /**
     * 订单状态阶段
     */
    private String title;
    
    /**
     * 订单的类型 1--租、还电,2--换电
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
    public static final Integer ORDER_TYPE_EXCHANGE = 2;
    
    //操作结果 0：成功，1：失败
    public static final Integer OPERATE_RESULT_SUCCESS = 0;
    
    public static final Integer OPERATE_RESULT_FAIL = 1;
    
    
    public static final Integer SELF_OPEN_CELL_SEQ = 5;
    
    public static final Integer SELF_OPEN_CELL_SEQ_COMPLETE = 6;
    
    public static final Integer SELF_OPEN_CELL_BY_RETURN_BATTERY = 7;
    
    public static final Integer SELF_OPEN_CELL_BY_RETURN_BATTERY_COMPLETE = 8;
    
    
    /**
     * 取满电仓操作记录seq
     */
    public static final Integer OPEN_FULL_CELL_BATTERY = 14;
    
    /**
     * 自助开仓
     */
    public static final Integer ORDER_TYPE_SELF_OPEN = 3;
    
    /**
     * 开门失败又要根据msg截取判断是新仓还是旧仓
     */
    public static final String OPEN_CELL_FAIL = "开门失败";
    
  
    
}

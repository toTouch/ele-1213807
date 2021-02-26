package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.IdType;
                                            import com.baomidou.mybatisplus.annotation.TableId;
                                            import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * 租电池记录(TRentBatteryOrder)实体类
 *
 * @author makejava
 * @since 2020-12-08 15:08:47
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_rent_battery_order")
public class RentBatteryOrder {
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    
    private Long uid;
    /**
    * 用户名
    */
    private String userName;
    /**
    * 手机号
    */
    private String phone;
    /**
    * 电池编号
    */
    private String electricityBatterySn;
    /**
    * 租电池押金
    */
    private BigDecimal batteryDeposit;
    /**
     * 换电柜id
     */
    private Integer electricityCabinetId;
    /**
     * 仓门号
     */
    private Integer cellNo;
    /**
     * 订单类型(1--租电池,2--还电池,3--后台绑电池,4--后台解绑电池)
     */
    private Integer type;
    /**
    * 订单状态(1--初始化,2--已开门,3--..)
    */
    private Integer status;
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

    //租电池
    public static final Integer TYPE_USER_RENT = 1;
    //还电池
    public static final Integer TYPE_USER_RETURN = 2;
    //后台绑电池
    public static final Integer TYPE_WEB_BIND = 3;
    //后台解绑电池
    public static final Integer TYPE_WEB_UNBIND = 4;

    //初始化
    public static final Integer STATUS_INIT = 1;
    //租电池/还电池 开门
    public static final Integer STATUS_RENT_BATTERY_OPEN_DOOR = 2;
    //电池检测成功(订单完成)
    public static final Integer STATUS_RENT_BATTERY_DEPOSITED = 3;
    //订单异常结束
    public static final Integer STATUS_ORDER_EXCEPTION_CANCEL = 4;
    //订单取消
    public static final Integer STATUS_ORDER_CANCEL = 5;

}
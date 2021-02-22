package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.IdType;
                                            import com.baomidou.mybatisplus.annotation.TableId;
                                            import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private Double batteryDeposit;
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

}
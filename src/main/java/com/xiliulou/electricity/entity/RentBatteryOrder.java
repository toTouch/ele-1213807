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
    * 用户姓名
    */
    private String name;
    /**
    * 手机号
    */
    private String phone;
    /**
    * 身份证号
    */
    private String idNumber;
    /**
    * 电池门店Id
    */
    private Integer batteryStoreId;
    /**
    * 电池门店名称
    */
    private String batteryStoreName;
    /**
    * 电池编号
    */
    private String electricityBatterySn;
    /**
    * 租电池押金
    */
    private Double batteryDeposit;
    /**
    * 可用状态(0--租用中，1--已退租)
    */
    private Object status;
    /**
    * 创建时间
    */
    private Long createTime;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;

}
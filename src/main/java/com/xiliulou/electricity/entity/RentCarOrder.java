package com.xiliulou.electricity.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * 租车记录(TRentCarOrder)实体类
 *
 * @author makejava
 * @since 2020-12-08 15:09:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_rent_car_order")
public class RentCarOrder {
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
    * 车门店Id
    */
    private Integer carStoreId;
    /**
    * 车门店名称
    */
    private String carStoreName;
    /**
    * 车编号
    */
    private String carSn;
    /**
    * 车牌号
    */
    private String numberPlate;
    /**
    * 租车押金
    */
    private Double carDeposit;
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

    //租用中
    public static final Integer IS_USE_STATUS = 0;
    //已退租
    public static final Integer NO_USE_STATUS = 1;

}
package com.xiliulou.electricity.entity.car;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.entity.car.basic.BasicCarPO;

import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import lombok.Data;

/**
 * 租车套餐会员期限表
 *
 * @author xiaohui.song
 **/
@Data
@TableName("t_car_rental_package_member_term")
public class CarRentalPackageMemberTermPO extends BasicCarPO {

    private static final long serialVersionUID = -342704315388619926L;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 购买订单编号
     */
    private String rentalPackageOrderNo;

    /**
     * 套餐ID
     */
    private Long rentalPackageId;

    /**
     * 套餐类型
     * <pre>
     *     1-单车
     *     2-车电一体
     * </pre>
     * @see CarRentalPackageTypeEnum
     */
    private Integer rentalPackageType;

    /**
     * 到期时间
     */
    private Integer dueTime;

    /**
     * 当前套餐订单余量
     */
    private Integer residue;

    /**
     * 状态
     * <pre>
     *     1-正常
     *     2-异常
     * </pre>
     * @see MemberTermStatusEnum
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}

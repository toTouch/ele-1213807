package com.xiliulou.electricity.entity.car;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.entity.basic.BasicCarPO;

import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.RenalPackageConfineEnum;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

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
     * 套餐限制
     * <pre>
     *     0-不限制
     *     1-次数
     * </pre>
     * @see RenalPackageConfineEnum
     */
    private Integer rentalPackageConfine;

    /**
     * 到期时间
     */
    private Long dueTime;

    /**
     * 总计到期时间
     */
    private Long dueTimeTotal;

    /**
     * 当前余量
     */
    private Long residue;

    /**
     * 总计余量
     */
    private Long residueTotal;

    /**
     * 状态
     * <pre>
     *     0-待生效
     *     1-正常
     *     2-申请冻结
     *     3-冻结
     *     4-申请退押
     *     5-申请退租
     * </pre>
     * @see MemberTermStatusEnum
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 押金金额(元)
     */
    private BigDecimal deposit;

    /**
     * 当前使用的保险订单号
     */
    private String insuranceOrderId;


}

package com.xiliulou.electricity.entity.car;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiliulou.electricity.entity.basic.BasicCarPo;

import com.xiliulou.electricity.enums.RentalPackageOrderFreezeStatusEnum;
import com.xiliulou.electricity.enums.RentalUnitEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 租车套餐订单冻结表
 *
 * @author xiaohui.song
 **/
@Data
@TableName("t_car_rental_package_order_freeze")
public class CarRentalPackageOrderFreezePo extends BasicCarPo {

    private static final long serialVersionUID = 5478346441293230084L;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 订单编码
     */
    private String orderNo;

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
     * @see RentalPackageTypeEnum
     */
    private Integer rentalPackageType;

    /**
     * 余量
     */
    private Long residue;

    /**
     * 余量单位
     * <pre>
     *     -1-次数
     *     1-天
     *     0-分钟
     * </pre>
     * @see RentalUnitEnum
     */
    private Integer residueUnit;

    /**
     * 滞纳金(元/天)
     */
    private BigDecimal lateFee;

    /**
     * 申请期限(天)
     */
    private Integer applyTerm;

    /**
     * 实际期限(天)
     */
    private Integer realTerm;

    /**
     * 申请时间
     */
    private Long applyTime;

    /**
     * 审核时间
     */
    private Long auditTime;

    /**
     * 启用时间
     */
    private Long enableTime;

    /**
     * 状态
     * <pre>
     *     1-待审核
     *     2-审核通过
     *     3-审核拒绝
     *     4-提前启用
     *     5-自动启用
     *     6-撤回申请
     *     7-已失效
     * </pre>
     * @see RentalPackageOrderFreezeStatusEnum
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 申请原因
     */
    private String applyReason;

}

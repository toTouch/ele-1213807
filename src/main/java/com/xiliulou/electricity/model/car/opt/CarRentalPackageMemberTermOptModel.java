package com.xiliulou.electricity.model.car.opt;

import com.xiliulou.electricity.enums.MemberTermStatusEnum;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 租车套餐会员期限表<br />
 * 转换DB
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageMemberTermOptModel implements Serializable {

    private static final long serialVersionUID = -5555937537233746323L;

    /**
     * 主键ID
     */
    private Long id;

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
    private Long dueTime;

    /**
     * 总计到期时间
     */
    private Long dueTimeTotal;

    /**
     * 当前余量
     */
    private Integer residue;

    /**
     * 总计余量
     */
    private Integer residueTotal;

    /**
     * 状态
     * <pre>
     *     0-待生效
     *     1-正常
     *     2-申请冻结
     *     3-冻结
     *     4-申请退押
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
     * 操作人ID
     */
    private Long optUid;

}

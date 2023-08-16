package com.xiliulou.electricity.vo.car;

import com.xiliulou.electricity.enums.RentalPackageOrderFreezeStatusEnum;
import com.xiliulou.electricity.enums.RentalUnitEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 租车套餐购买订单冻结展现层数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageOrderFreezeVo implements Serializable {

    private static final long serialVersionUID = -4712482555477714776L;

    /**
     * 订单编码
     */
    private String orderNo;

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
     * 滞纳金(元/天)
     */
    private BigDecimal lateFee;

    /**
     * 创建时间，时间戳
     */
    private Long createTime;

    /**
     * 修改时间，时间戳
     */
    private Long updateTime;

    /**
     * 申请期限(天)
     */
    private Integer applyTerm;

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
     * 申请原因
     */
    private String applyReason;


    // ++++++++++ 辅助业务数据 ++++++++++

    /**
     * 用户真实姓名
     */
    private String userRelName;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 租车套餐名称
     */
    private String carRentalPackageName;

}

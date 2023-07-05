package com.xiliulou.electricity.vo.car;

import com.xiliulou.electricity.enums.DepositTypeEnum;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.PayTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 租车套餐押金缴纳展现层数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageDepositPayVO implements Serializable {

    private static final long serialVersionUID = 1718767898763254057L;

    /**
     * 订单编码
     */
    private String orderNo;

    /**
     * 类型
     * <pre>
     *     1-正常缴纳
     *     2-转押
     * </pre>
     *
     * @see DepositTypeEnum
     */
    private Integer type;

    /**
     * 押金
     */
    private BigDecimal deposit;

    /**
     * 交易方式
     * <pre>
     *     1-线上
     *     2-线下
     *     3-免押
     * </pre>
     * @see PayTypeEnum
     */
    private Integer payType;

    /**
     * 支付状态
     * <pre>
     *     1-未支付
     *     2-支付成功
     *     3-支付失败
     * </pre>
     * @see PayStateEnum
     */
    private Integer payState;

    /**
     * 创建时间，时间戳
     */
    private Long createTime;

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

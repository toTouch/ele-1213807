package com.xiliulou.electricity.vo.car;

import com.xiliulou.core.base.enums.ChannelEnum;
import com.xiliulou.electricity.enums.DepositTypeEnum;
import com.xiliulou.electricity.enums.PayStateEnum;
import com.xiliulou.electricity.enums.PayTypeEnum;
import com.xiliulou.electricity.enums.RentalPackageTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 租车套餐押金缴纳展现层数据模型
 *
 * @author xiaohui.song
 **/
@Data
public class CarRentalPackageDepositPayVo implements Serializable {
    
    private static final long serialVersionUID = 1718767898763254057L;
    
    /**
     * 用户ID
     */
    private Long uid;
    
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
     *
     * @see RentalPackageTypeEnum
     */
    private Integer rentalPackageType;
    
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
     * 实缴押金
     */
    private BigDecimal deposit;
    
    /**
     * 交易方式
     * <pre>
     *     1-线上
     *     2-线下
     *     3-免押
     * </pre>
     *
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
     *
     * @see PayStateEnum
     */
    private Integer payState;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间，时间戳
     */
    private Long createTime;
    
    /**
     * 套餐押金
     */
    private BigDecimal rentalPackageDeposit;
    
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
     * 业务状态，没有枚举值
     * <pre>
     *     0-正常
     *     1-申请退押
     *     2-退款中
     * </pre>
     */
    private Integer status;
    
    /**
     * 门店ID
     */
    private Integer storeId;
    
    /**
     * 车辆型号ID
     */
    private Integer carModelId;
    
    /**
     * 可退标识
     */
    private boolean refundFlag = true;
    
    /**
     * 退押拒绝原因
     */
    private String rejectReason;
    
    /**
     * 特殊可退标识（2.0迁移数据过度）
     */
    private boolean refundSpecialFlag = false;
    
    /**
     * <p>
     *    Description: 加盟商名称
     * </p>
     */
    private String franchiseeName;
    
    /**
     * 支付渠道
     * @see ChannelEnum
     */
    private String paymentChannel;
    
    /**
     * <p>
     *    Description: 剩余可代扣金额
     * </p>
    */
    private BigDecimal payTransAmt;
    
    
    /**
     * 用户状态:0-正常,1-已删除, 2-已注销
     */
    private Integer userStatus;

    private Integer payCount;
}

package com.xiliulou.electricity.vo;

import com.xiliulou.electricity.enums.SignStatusEnum;
import com.xiliulou.electricity.vo.enterprise.EnterpriseChannelUserVO;
import com.xiliulou.electricity.vo.userinfo.emergencyContact.EmergencyContactVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zgw
 * @date 2023/2/13 15:27
 * @mood
 */
@Data
public class DetailsUserInfoVo {
    
    /**
     * 用户名称
     */
    private String name;
    
    /**
     * 联系方式
     */
    private String phone;

    /**
     * 电池押金状态 0--未缴纳押金，1--已缴纳押金,2--押金退款中
     */
    private Integer batteryDepositStatus;

    /**
     * 车辆押金状态
     */
    private Integer carDepositStatus;

    /**
     * 车电一体押金状态
     */
    private Integer carBatteryDepositStatus;
    
    /**
     * 电池租借状态
     * <pre>
     *     0-未租
     *     1-已租
     * </pre>
     */
    private Integer batteryRentStatus;
    
    /**
     * 车辆租借状态
     * <pre>
     *     0-未租
     *     1-已租
     * </pre>
     */
    private Integer carRentStatus;
    
    /**
     * 车辆sn码
     */
    private String carSn;
    
    /**
     * 电池月卡营业额
     */
    private BigDecimal memberCardTurnover;
    
    /**
     * 租车月卡营业额
     */
    private BigDecimal carMemberCardTurnover;
    
    /**
     * 服务费营业额
     */
    private BigDecimal batteryServiceFee;
    
    /**
     * 认证时间
     */
    private Long userCertificationTime;

    private Integer modelType;

    private String franchiseeName;

    private String storeName;

    private Long franchiseeId;

    private Long storeId;
    
    private EnterpriseChannelUserVO enterpriseChannelUserInfo;

    /**
     * 租车套餐金额（总支付成功-总退租成功）
     */
    private BigDecimal carRentalPackageOrderAmountTotal;

    /**
     * 租车滞纳金金额（总支付成功）
     */
    private BigDecimal carRentalPackageOrderSlippageAmountTotal;
    
    /**
     * 是否可解绑微信 0：不可解绑 1：可解绑
     */
    private Integer bindWX;
    
    /**
     * 是否可解绑微信 0：不可解绑 1：可解绑
     */
    private Integer bindAlipay;
    
    /**
     * 邀请人是否可被修改 0：可修改 1：不可修改
     */
    private Integer canModifyInviter;
    
    /**
     * 邀请记录按钮是否显示 0：是 1：否
     */
    private Integer modifyInviterRecordIsView;
    
    
    /**
     * 邀请人名称
     */
    private String inviterName;
    
    /**
     * 邀请人来源：1-用户邀请，2-商户邀请
     */
    private Integer inviterSource;
    
    /**
     * 签署流程ID
     */
    private String signFlowId;
    
    /**
     * 签署完成状态；0签了未部署，1签了部署了，2未做电子签名
     * @see SignStatusEnum
     */
    private Integer signFinishStatus;
    
    /**
     * 是否对换电套餐购买次数限制:0-不限制 1-限制
     */
    private Integer eleLimit;
    
    /**
     * 紧急联系人
     */
    private List<EmergencyContactVO> emergencyContactList;
}

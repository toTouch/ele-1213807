package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * 用户绑定列表(FranchiseeUserInfo)实体类
 *
 * @author Eclair
 * @since 2021-06-17 10:10:13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_franchisee_user_info")
public class FranchiseeUserInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userInfoId;
    /**
     * 服务状态 (2--已缴纳押金，3--已租电池)
     */
    private Integer serviceStatus;
    /**
     * 加盟商id
     */
    private Long franchiseeId;
    /**
     * 套餐id
     */
    private Integer cardId;
    /**
     * 套餐名称
     */
    private String cardName;
    /**
     * 类型(0:月卡,1:季卡,2:年卡,3:次卡)
     */
    private Integer cardType;
    /**
     * 月卡过期时间
     */
    private Long memberCardExpireTime;
    /**
     * 月卡剩余次数
     */
    private Long remainingNumber;
    /**
     * 初始电池编号
     */
    private String initElectricityBatterySn;
    /**
     * 当前电池编号
     */
    private String nowElectricityBatterySn;
    /**
     * 租电池押金
     */
    private BigDecimal batteryDeposit;
    /**
     * 押金订单编号
     */
    private String orderId;
    /**
     * 0--正常 1--删除
     */
    private Integer delFlag;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;

    //租户
    private Integer tenantId;

    /**
     * 加盟商类型 1--老（不分型号） 2--新（分型号）
     */
    private Integer modelType;

    /**
     * 电池类型
     */
    private String batteryType;

    /**
     * 电池服务费状态 0-初始状态 1-已支付
     */
    private Integer batteryServiceFeeStatus;

    //初始化
    public static final Integer STATUS_IS_INIT = 1;
    //已缴纳押金
    public static final Integer STATUS_IS_DEPOSIT = 2;
    //已租电池
    public static final Integer STATUS_IS_BATTERY = 3;

    //电池服务费状态
    public static final Integer STATUS_NOT_IS_SERVICE_FEE = 0;
    public static final Integer STATUS_IS_SERVICE_FEE = 1;

    public static final Integer DEL_NORMAL = 0;
    public static final Integer DEL_DEL = 1;


    public static final Integer OLD_MODEL_TYPE = 1;
    public static final Integer MEW_MODEL_TYPE = 2;


    //送次数卡
    public static final Integer TYPE_COUNT = 3;

    public static final Integer MEMBER_CARD_OWE = 1;

    public static final Long UN_LIMIT_COUNT_REMAINING_NUMBER = 9999L;

    /**
     *用户电池服务费为0元
     */
    public static final BigDecimal BATTERY_SERVICE_FEE_ZERO=new BigDecimal(0);

}

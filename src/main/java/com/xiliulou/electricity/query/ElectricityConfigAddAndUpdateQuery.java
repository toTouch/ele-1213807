package com.xiliulou.electricity.query;

import com.xiliulou.electricity.entity.FranchiseeMoveInfo;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 活动表(ElectricityConfig)实体类
 *
 * @author makejava
 * @since 2022-07-05 09:27:12
 */
@Data
public class ElectricityConfigAddAndUpdateQuery {
    
    /**
     * 平台名称
     */
    @NotEmpty(message = "平台名称不能为空!", groups = {CreateGroup.class})
    private String name;
    
    /**
     * 订单间隔时间
     */
    private Integer orderTime;
    
    /**
     * 实名审核方式审核（0:人工审核 ,1:自动审核,2:人脸核身）
     */
    private Integer isManualReview;
    
    /**
     * 是否线上提现（0--是，1--否）
     */
    private Integer isWithdraw;
    
    /**
     * 是否开启异常仓锁仓功能 (0--开启，1--关闭)
     */
    private Integer isOpenDoorLock;
    
    /**
     * 是否电池检测 (0--是，1--否)
     */
    private Integer isBatteryReview;
    
    /**
     * 是否开启暂停月卡功能 (0--关闭，1--开启)
     */
    private Integer disableMemberCard;
    
    /**
     * 是否开启低电量换电 (0--关闭，1--开启)
     */
    private Integer isLowBatteryExchange;
    
    /**
     * 是否开启选仓换电 (0--关闭，1--开启)
     */
    private Integer isSelectionExchange;
    
    /**
     * 低电量换电配置模式
     */
    private List<LowBatteryExchangeModel> lowBatteryExchangeModelList;
    
    /**
     * 低电量换电配置模式
     */
    private String lowBatteryExchangeModel;
    
    /**
     * 是否自主开仓 0--开启， 1--关闭
     */
    private Integer isEnableSelfOpen;
    /**
     * 是否开启只剩一个格挡退电池
     */
    //private Integer isEnableReturnBoxCheck;
    
    /**
     * 是否开启保险 （0--是 1--否）
     */
    private Integer isOpenInsurance;
    
    /**
     * 是否迁移加盟商 0--关闭 1--开启
     */
    @Deprecated
    private Integer isMoveFranchisee;
    
    /**
     * 迁移加盟商
     */
    @Deprecated
    private FranchiseeMoveInfo franchiseeMoveInfo;
    
    /**
     * 押金缴纳类型 (0：缴纳押金，1：电池免押金，2：租车免押金，3：车辆电池免押金)
     */
    private Integer freeDepositType;
    
    /**
     * 车电关联
     */
    private Integer isOpenCarBatteryBind;
    
    /**
     * 车辆控制  0--开  1--关
     */
    private Integer isOpenCarControl;
    
    /**
     * 是否启用0元退押审核
     */
    private Integer isZeroDepositAuditEnabled;
    
    /**
     * 是否开启电子签名 0--是 1--否 2--功能关闭
     */
    private Integer isEnableEsign;
    
    /**
     * 是否允许租电 0--是  1--否
     */
    //private Integer allowRentEle;
    
    /**
     * 是否允许退电 0--是  1--否
     */
    //private Integer allowReturnEle;
    
    /**
     * 冻结是否强制退资产 0--是 1--否
     */
    private Integer allowFreezeWithAssets;
    
    /**
     * 渠道时限
     */
    private Integer channelTimeLimit;
    
    /**
     * 柜机少电比例
     */
    @Range(min = 0, max = 100, message = "请输入0-100的整数", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer lowChargeRate;
    
    /**
     * 柜机多电比例
     */
    @Range(min = 0, max = 100, message = "请输入0-100的整数", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer fullChargeRate;
    
    
    /**
     * 打开微信客服 0-是 1-否
     */
    private Integer wxCustomer;
    
    /**
     * 打开支付宝客服 0-是 1-否
     */
    private Integer alipayCustomer;
    
    /**
     * 柜机少电多电配置标准:0-统一配置 1-单个柜机配置
     */
    private Integer chargeRateType;
    
    /**
     * 是否开启舒适换电；默认是1是关闭，0是开启
     */
    private Integer isComfortExchange;
    
    /**
     * 优先换电标准
     */
    private Double priorityExchangeNorm;
    
    /**
     * 是否开启美团骑手商城：0--是 1--否
     */
    private Integer isEnableMeiTuanRiderMall;
    
    /**
     * 是否开启对换电套餐购买次数的限制：0--是 1--否
     */
    private Integer eleLimit;
    
    /**
     * 换电套餐购买限制次数，默认1次
     */
    @Range(min = 1, message = "换电套餐购买限制次数必须是大于0的整数", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer eleLimitCount;
    
    /**
     * 套餐冻结自动审核，0-关闭自动审核，其他为自动审核最大天数限制
     */
    @Range(min = 0, max = 9999, message = "请输入1-9999的整数", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer freezeAutoReviewDays;
    
    /**
     * 套餐冻结次数限制，0-不限次，其他为用户端申请冻结最大次数
     */
    @Range(min = 0, max = 9999, message = "请输入1-9999的整数", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer packageFreezeCount;
    
    /**
     * 与套餐冻结次数限制参数 packageFreezeCount 联动
     * 套餐冻结最大天数限制，packageFreezeCount 为 0 时最大天数限制为60，packageFreezeCount 为其他数值时根据 packageFreezeDays 限制申请冻结最大天数
     */
    @Range(min = 1, max = 9999, message = "请输入1-9999的整数", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer packageFreezeDays;
    
    /**
     * 套餐过期保护期，单位:小时，保护期结束后产生套餐过期滞纳金，默认24小时
     */
    @Range(min = 1, max = 99, message = "套餐过期滞纳金起算时间设置范围为1-99", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer expiredProtectionTime;
    
    public static Double MIN_NORM = 50.00;
    
    public static Double MAX_NORM = 100.00;
}



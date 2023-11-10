package com.xiliulou.electricity.query;

import com.xiliulou.electricity.entity.FranchiseeMoveInfo;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
    private Integer isEnableReturnBoxCheck;

    /**
     * 是否开启保险 （0--是 1--否）
     */
    private Integer isOpenInsurance;

    /**
     * 是否迁移加盟商 0--关闭 1--开启
     */
    private Integer isMoveFranchisee;

    /**
     * 迁移加盟商
     */
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
    private Integer allowRentEle;

    /**
     * 是否允许退电 0--是  1--否
     */
    private Integer allowReturnEle;

    /**
     *  冻结是否强制退资产 0--是 1--否
     */
    private Integer allowFreezeWithAssets;
}



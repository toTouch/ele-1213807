package com.xiliulou.electricity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zgw
 * @date 2023/2/13 16:47
 * @mood
 */
@Data
public class DetailsBatteryInfoVo {
    
    private Long uid;
    ////用户电池押金
    
    /**
     * 加盟商Id
     */
    private Long franschiseeId;
    /**
     * 加盟商名
     */
    private String franschiseeName;
    
    /**
     * 门店Id
     */
    private Long storeId;
    /**
     * 门店名
     */
    private String storeName;
    
    /**
     * 电池押金状态
     */
    private Integer batteryDepositStatus;
    
    /**
     * 电池押金
     */
    private BigDecimal batteryDeposit;
    
    /**
     * 缴纳时间
     */
    private Long payDepositTime;
    
    ////用户电池套餐
    
    /**
     * 套餐 id
     */
    private Long memberCardId;
    
    /**
     * 套餐名称
     */
    private String cardName;
    
    /**
     * 换电限制
     */
    private Integer limitCount;
    
    /**
     * 套餐次数
     */
    private Long remainingNumber;
    
    /**
     * 套餐时间
     */
    private Long memberCardCreateTime;
    
    /**
     * 到期时间
     */
    private Long memberCardExpireTime;
    
    /**
     * 剩余天数
     */
    private Long cardDays;
    
    /**
     * 月卡状态
     */
    private Integer memberCardStatus;
    
    /**
     * 电池服务费
     */
    private BigDecimal userBatteryServiceFee;
    
    ////用户电池
    
    /**
     * 电池编号
     */
    private String batterySn;
    
    /**
     * 电池型号
     */
    private String batteryModel;
    
    /**
     * 电量
     */
    private Double power;

    /**
     * 用户绑定的电池型号
     */
    private List<String> batteryModels;
}

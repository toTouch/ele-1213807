package com.xiliulou.electricity.vo.enterprise;

import lombok.Data;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/27 11:42
 */
@Data
public class EnterpriseFreezePackageRecordVO {
    
    private Long id;
    
    /**
     * 用户UID
     */
    private Long uid;
    
    /**
     * 用户名称
     */
    private String userName;
    
    /**
     * 用户手机号
     */
    private String phone;
    
    /**
     * 当前冻结套餐ID
     */
    private Long packageId;
    
    /**
     * 当前冻结套餐名称
     */
    private String packageName;
    
    /**
     * 套餐冻结单号
     */
    private String packageFreezeOrderNo;
    
    /**
     * 停卡状态
     */
    private Integer status;
    
    /**
     * 月卡剩余天数
     */
    private Long cardDays;
    
    /**
     * 用户选择的停卡天数
     */
    private Integer chooseDays;
    
    /**
     * 用户真实的停卡天数
     */
    private Integer realDays;
    
    /**
     * 用户冻结时间
     */
    private Long freezePackageTime;
    
    /**
     * 套餐冻结后又启用时间
     */
    private Long enablePackageTime;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
}

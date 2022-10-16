package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zgw
 * @date 2022/10/14 19:25
 * @mood
 */
@Data
public class EleOtaUpgradeHistoryVo {
    
    private Long id;
    
    private Long electricityName;
    
    private String cellNo;
    
    /**
     * 类型 1--核心板 2--子板
     */
    private Integer type;
    
    /**
     * 升级版本
     */
    private String upgradeVersion;
    
    /**
     * 历史版本
     */
    private String historyVersion;
    
    /**
     * 升级版本sha256值
     */
    private String upgradeSha256Value;
    
    private String status;
    
    /**
     * 升级时间
     */
    private Long upgradeTime;
    
    /**
     * 结束时间（成功，失败）
     */
    private Long finishTime;
    
    private String errMsg;
    
    private Long createTime;
    
    private Long updateTime;
}

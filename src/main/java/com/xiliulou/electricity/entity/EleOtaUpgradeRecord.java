package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (EleOtaUpgradeRecord)实体类
 *
 * @author Eclair
 * @since 2022-10-13 14:49:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_ota_upgrade_record")
public class EleOtaUpgradeRecord {
    
    private Long id;
    
    private Long electricityCabinetId;
    
    private String cellNo;
    
    /**
     * 类型 1--核心板 2--子板
     */
    private Integer type;
    
    /**
     * 类型 1--升级 2--同步
     */
    private Integer changeType;
    
    private String upgradeVersion;
    
    private String historyVersion;
    
    private String upgradeSha256Value;
    
    private String historySha256Value;
    
    private String status;
    
    private Long upgradeTime;
    
    private Long finishTime;
    
    private String errMsg;
    
    private Long createTime;
    
    private Long updateTime;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
}

package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (EleNewOtaUpgradeHistory)实体类
 *
 * @author Eclair
 * @since 2023-02-20 15:48:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_new_ota_upgrade_history")
public class EleNewOtaUpgradeHistory {
    
    private Long id;
    
    private Long electricityCabinetId;
    
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
    
    private String sessionId;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
}

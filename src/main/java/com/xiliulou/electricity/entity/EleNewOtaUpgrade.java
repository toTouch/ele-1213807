package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (EleNewOtaUpgrade)实体类
 *
 * @author Eclair
 * @since 2023-02-20 15:58:53
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_new_ota_upgrade")
public class EleNewOtaUpgrade {
    
    private Long id;
    
    private Long electricityCabinetId;
    
    private String cellNo;
    
    /**
     * 类型 1--核心板 2--子板
     */
    private Integer type;
    
    private String status;
    
    /**
     * 升级版本
     */
    private String upgradeVersion;
    
    private Long createTime;
    
    private Long updateTime;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
}

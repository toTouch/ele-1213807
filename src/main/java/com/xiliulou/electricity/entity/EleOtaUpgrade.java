package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (EleOtaUpgrade)实体类
 *
 * @author Eclair
 * @since 2022-10-14 09:01:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_ota_upgrade")
public class EleOtaUpgrade {
    
    private Long id;
    
    private Long electricityCabinetId;
    
    private String cellNo;
    
    /**
     * 类型 1--核心板 2--子板
     */
    private Integer type;
    
    private String status;
    
    private Long createTime;
    
    private Long updateTime;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
    public static final Integer TYPE_CORE = 1;
    
    public static final Integer TYPE_SUB = 2;
    
    public static final String STATUS_INIT = "INIT";
    
    public static final String STATUS_UPGRADING = "UPGRADING";
    
    public static final String STATUS_UPGRADE_FAIL = "UPGRADE_FAIL";
    
    public static final String STATUS_UPGRADE_SUCCESS = "UPGRADE_SUCCESS";
}

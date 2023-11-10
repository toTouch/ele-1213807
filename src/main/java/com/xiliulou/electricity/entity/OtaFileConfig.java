package com.xiliulou.electricity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (OtaFileConfig)实体类
 *
 * @author zgw
 * @since 2022-10-12 09:24:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ota_file_config")
public class OtaFileConfig {
    
    private Long id;
    
    private String name;
    
    private String downloadLink;
    
    private String version;
    
    private String sha256Value;
    
    /**
     * 文件类型 1--核心板 2--子板 3--旧核心板 4--旧子板
     */
    private Integer type;
    
    private Long createTime;
    
    private Long updateTime;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
    public static final Integer TYPE_CORE_BOARD = 1;
    
    public static final Integer TYPE_SUB_BOARD = 2;
    
    public static final Integer TYPE_OLD_CORE_BOARD = 3;
    
    public static final Integer TYPE_OLD_SUB_BOARD = 4;
    
    /**
     * 六合一核心板类型
     */
    public static final Integer TYPE_SIX_IN_ONE_CORE_BOARD = 5;
    
    /**
     * 六合一子板类型
     */
    public static final Integer TYPE_SIX_IN_ONE_SUB_BOARD = 6;
    
    /**
     * 版本号： 旧版 >= 50
     */
    public static final Double MIN_OLD_BOARD_VERSION = 50.0;
    
    /**
     * 版本号： 10 =< 六合一版 < 20
     */
    public static final Double MAX_SIX_IN_ONE_BOARD_VERSION = 20.0;
    
    /**
     * 版本号： 新版 < 10
     */
    public static final Double MIX_SIX_IN_ONE_BOARD_VERSION = 10.0;
}

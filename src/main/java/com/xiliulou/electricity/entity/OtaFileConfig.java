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
     * 文件类型 1--核心板 2--子板 3--旧核心板 4--旧子板 5--旧六合一 6--新六合一
     */
    private Integer type;
    
    private Long createTime;
    
    private Long updateTime;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
    public static final int TYPE_CORE_BOARD = 1;
    
    public static final int TYPE_SUB_BOARD = 2;
    
    public static final int TYPE_OLD_CORE_BOARD = 3;
    
    public static final int TYPE_OLD_SUB_BOARD = 4;
    
    /**
     * 旧六合一(六合一不区分核心板和子板,以子板文件进行升级)
     */
    public static final int TYPE_SIX_SUB_BOARD = 5;
    
    /**
     * 新六合一(六合一不区分核心板和子板,以子板文件进行升级)
     */
    public static final int TYPE_NEW_SIX_SUB_BOARD = 6;
    
    public static final Integer VERSION_0 = 0;
    
    public static final Integer VERSION_10 = 10;
    
    public static final Integer VERSION_20 = 20;
    
    public static final Integer VERSION_30 = 30;
    
    public static final Integer VERSION_50 = 50;
    
    public static final Integer VERSION_60 = 60;
}

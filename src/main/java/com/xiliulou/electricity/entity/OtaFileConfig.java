package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

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
     * 文件类型 1--核心板 2--子板
     */
    private Integer type;
    
    private Long createTime;
    
    private Long updateTime;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
    public static final Integer TYPE_CORE_BOARD = 1;
    
    public static final Integer TYPE_SUB_BOARD = 2;
}

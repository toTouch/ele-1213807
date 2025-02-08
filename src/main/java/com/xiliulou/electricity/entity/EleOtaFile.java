package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 *
 * @author zgw
 * @since 2022-10-12 17:31:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ele_ota_file")
public class EleOtaFile {
    
    private Long id;
    
    private Integer electricityCabinetId;
    
    private String coreName;
    
    private String subName;
    
    private String coreSha256Value;
    
    private String subSha256Value;
    
    private Long createTime;
    
    private Long updateTime;
    
    /**
     * 文件类型 1--旧 2--新 3--六合一 4--新版六合一
     */
    private Integer fileType;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
    public static final int TYPE_OLD_FILE = 1;
    
    public static final int TYPE_NEW_FILE = 2;
    
    public static final int TYPE_SIX_FILE = 3;
    
    public static final int TYPE_NEW_SIX_FILE = 4;
}

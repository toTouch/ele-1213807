package com.xiliulou.electricity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * (OtaFileEleSha256)实体类
 *
 * @author zgw
 * @since 2022-10-12 17:31:08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("t_ota_file_ele_sha256")
public class OtaFileEleSha256 {
    
    private Long id;
    
    private Long electricityCabinetId;
    
    private String coreSha256Value;
    
    private String subSha256Value;
    
    private Long createTime;
    
    private Long updateTime;
    
    public static final Integer DEL_NORMAL = 0;
    
    public static final Integer DEL_DEL = 1;
    
}

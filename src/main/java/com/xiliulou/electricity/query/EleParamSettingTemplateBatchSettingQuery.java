package com.xiliulou.electricity.query;

import lombok.Data;

import java.util.List;

/**
 * @author zgw
 * @date 2023/3/28 16:29
 * @mood
 */
@Data
public class EleParamSettingTemplateBatchSettingQuery {
    
    private List<Long> eleIds;
    
    private Long id;
}

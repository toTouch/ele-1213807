package com.xiliulou.electricity.vo;

import lombok.Data;

/**
 * @author zgw
 * @date 2023/3/18 14:28
 * @mood
 */
@Data
public class SearchVo {
    
    private Long id;
    
    private String name;

    /**
     * 是否启用；0禁用,1启用
     */
    private Integer enabledState;
}

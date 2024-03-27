package com.xiliulou.electricity.bo.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HeYafeng
 * @description 区域柜机数量统计
 * @date 2024/2/6 16:01:26
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AreaCabinetNumBO {
    
    private Long areaId;
    
    private Integer cabinetNum;
    
}

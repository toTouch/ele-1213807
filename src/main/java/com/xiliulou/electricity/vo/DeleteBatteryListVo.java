package com.xiliulou.electricity.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @ClassName: DeleteBatteryListVo
 * @description:
 * @author: renhang
 * @create: 2024-11-12 17:37
 */
@Data
@Builder
public class DeleteBatteryListVo {
    
    private Integer successCount;
    
    
    private Integer failCount;
    
    
    private List<String> failedSnList;
}

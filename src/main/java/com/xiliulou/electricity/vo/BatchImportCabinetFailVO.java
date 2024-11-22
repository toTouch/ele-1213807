package com.xiliulou.electricity.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @ClassName: BatchImportCabinetFailVO
 * @description:
 * @author: renhang
 * @create: 2024-11-13 11:37
 */
@Data
@Builder
public class BatchImportCabinetFailVO {
    
    private String deviceName;
    
    private String reason;
}

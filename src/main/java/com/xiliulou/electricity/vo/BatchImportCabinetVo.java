package com.xiliulou.electricity.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @ClassName: BatchImportCabinetFailVO
 * @description:
 * @author: renhang
 * @create: 2024-11-13 11:37
 */
@Data
@Builder
public class BatchImportCabinetVo {
    
    private Integer successCount;
    
    private Integer failCount;
    
    private List<BatchImportCabinetFailVO> failVOS;
}

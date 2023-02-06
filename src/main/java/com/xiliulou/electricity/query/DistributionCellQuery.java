package com.xiliulou.electricity.query;

import lombok.Data;

import java.util.List;

/**
 * @author zgw
 * @date 2023/2/6 19:39
 * @mood
 */
@Data
public class DistributionCellQuery {
    
    private List<Integer> allocatedCellNos;
    
    private String preStrategy;
    
    private String nextStrategy;
}

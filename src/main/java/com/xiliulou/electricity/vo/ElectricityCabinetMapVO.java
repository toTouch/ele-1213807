package com.xiliulou.electricity.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @description 柜机地图VO
 * @date 2024/1/17 09:43:07
 */
@Builder
@Data
public class ElectricityCabinetMapVO {
    
    List<ElectricityCabinetListMapVO> electricityCabinetListMapVOList;
    
    private Integer totalCount;
    
    private Integer lowChargeCount;
    
    private Integer fullChargeCount;
    
    private Integer unusableCount;
    
    private Integer offLineCount;

    private Integer reversePowerTypeCount;
}

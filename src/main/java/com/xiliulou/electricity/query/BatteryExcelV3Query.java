package com.xiliulou.electricity.query;

import com.xiliulou.electricity.dto.BatteryExcelV3DTO;
import lombok.Data;

import java.util.List;

/**
 * @author HeYafeng
 * @description 导入电池excel接口参数
 * @date 2023/10/18 17:46:42
 */
@Data
public class BatteryExcelV3Query {
    
    private Long franchiseeId;

    private List<BatteryExcelV3DTO> batteryExcelV3DTOList;
}



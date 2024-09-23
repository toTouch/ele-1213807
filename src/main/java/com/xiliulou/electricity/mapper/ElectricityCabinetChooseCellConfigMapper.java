package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.ElectricityCabinetChooseCellConfig;

/**
 * @ClassName: ElectricityCabinetChooseCellConfigMapper
 * @description:
 * @author: renhang
 * @create: 2024-08-08 09:06
 */
public interface ElectricityCabinetChooseCellConfigMapper {
    
    ElectricityCabinetChooseCellConfig selectConfigByNum(Integer num);
}

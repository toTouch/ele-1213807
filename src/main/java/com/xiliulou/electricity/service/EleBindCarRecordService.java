package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleBindCarRecord;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.query.EleBindCarRecordQuery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;

import java.util.List;

/**
 * 换电柜电池表(EleBindCarRecord)表服务接口
 *
 * @author makejava
 * @since 2022-06-16 14:44:12
 */
public interface EleBindCarRecordService extends IService<EleBindCarRecord> {

    void insert(EleBindCarRecord eleBindCarRecord);

    R queryList(EleBindCarRecordQuery eleBindCarRecordQuery);

    R queryCount(EleBindCarRecordQuery eleBindCarRecordQuery);

}

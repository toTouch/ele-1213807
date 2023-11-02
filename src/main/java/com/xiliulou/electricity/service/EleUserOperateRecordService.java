package com.xiliulou.electricity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleUserOperateRecord;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.query.ElectricityBatteryQuery;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;

/**
 * 换电柜电池表(TEleUserOperateRecord)表服务接口
 *
 * @author makejava
 * @since 2022-07-12 14:44:12
 */
public interface EleUserOperateRecordService extends IService<EleUserOperateRecord> {


    void insert(EleUserOperateRecord eleUserOperateRecord);

    R queryList(Long uid,Long size,Long offset,Long beginTime,Long enTime,Integer operateModel);

    R queryCount(Long uid,Long beginTime,Long enTime,Integer operateModel);
    
    void asyncHandleUserOperateRecord(EleUserOperateRecord eleUserOperateRecord);
}

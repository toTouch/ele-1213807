package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.entity.ElectricityExceptionOrderStatusRecord;
import com.xiliulou.electricity.entity.OffLineElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.mapper.ElectricityCabinetOrderOperHistoryMapper;
import com.xiliulou.electricity.mapper.ElectricityExceptionOrderStatusRecordMapper;
import com.xiliulou.electricity.query.ElectricityCabinetOrderOperHistoryQuery;
import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import com.xiliulou.electricity.service.ElectricityExceptionOrderStatusRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 订单的操作历史记录(TElectricityExceptionOrderStatusRecord)表服务实现类
 *
 * @author makejava
 * @since 2022-07-21 17:57:22
 */
@Service("electricityExceptionOrderStatusRecordService")
public class ElectricityExceptionOrderStatusRecordServiceImpl implements ElectricityExceptionOrderStatusRecordService {

    @Resource
    ElectricityExceptionOrderStatusRecordMapper electricityExceptionOrderStatusRecordMapper;

    @Override
    public void insert(ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord) {
        electricityExceptionOrderStatusRecordMapper.insert(electricityExceptionOrderStatusRecord);
    }

    @Override
    public ElectricityExceptionOrderStatusRecord queryByOrderId(String orderId) {
        return electricityExceptionOrderStatusRecordMapper.selectOne(new LambdaQueryWrapper<ElectricityExceptionOrderStatusRecord>().eq(ElectricityExceptionOrderStatusRecord::getOrderId, orderId));
    }
}

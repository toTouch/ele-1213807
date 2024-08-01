package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.ElectricityExceptionOrderStatusRecord;
import com.xiliulou.electricity.mapper.ElectricityExceptionOrderStatusRecordMapper;
import com.xiliulou.electricity.service.ElectricityExceptionOrderStatusRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;


/**
 * 订单异常状态记录表(TElectricityExceptionOrderStatusRecord)表服务实现类
 *
 * @author makejava
 * @since 2022-07-21 17:57:22
 */
@Service("electricityExceptionOrderStatusRecordService")
@Slf4j
public class ElectricityExceptionOrderStatusRecordServiceImpl implements ElectricityExceptionOrderStatusRecordService {
    
    @Resource
    ElectricityExceptionOrderStatusRecordMapper electricityExceptionOrderStatusRecordMapper;
    
    @Override
    public void insert(ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord) {
        electricityExceptionOrderStatusRecordMapper.insert(electricityExceptionOrderStatusRecord);
    }
    
    @Override
    public ElectricityExceptionOrderStatusRecord queryByOrderId(String orderId) {
        return electricityExceptionOrderStatusRecordMapper.selectOne(
                new LambdaQueryWrapper<ElectricityExceptionOrderStatusRecord>().eq(ElectricityExceptionOrderStatusRecord::getOrderId, orderId));
    }
    
    @Override
    public void update(ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord) {
        electricityExceptionOrderStatusRecordMapper.updateById(electricityExceptionOrderStatusRecord);
    }
    
    @Override
    public void queryRecordAndUpdateStatus(String orderId) {
        ElectricityExceptionOrderStatusRecord statusRecord = this.queryByOrderId(orderId);
        if (Objects.isNull(statusRecord)) {
            log.debug("electricityExceptionOrderStatusRecordService.queryRecordAndUpdateStatus, record is null, orderId is {}", orderId);
            return;
        }
        ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecordUpdate = new ElectricityExceptionOrderStatusRecord();
        electricityExceptionOrderStatusRecordUpdate.setId(statusRecord.getId());
        electricityExceptionOrderStatusRecordUpdate.setUpdateTime(System.currentTimeMillis());
        electricityExceptionOrderStatusRecordUpdate.setIsSelfOpenCell(ElectricityExceptionOrderStatusRecord.SELF_OPEN_CELL);
        this.update(electricityExceptionOrderStatusRecordUpdate);
    }
}

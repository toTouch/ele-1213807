package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.ElectricityCabinetOfflineReportOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.mapper.ElectricityCabinetOfflineReportOrderMapper;
import com.xiliulou.electricity.service.ElectricityCabinetOfflineReportOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("electricityCabinetOfflineReportOrderService")
@Slf4j
public class ElectricityCabinetOfflineReportOrderServiceImpl implements ElectricityCabinetOfflineReportOrderService {

    @Resource
    private ElectricityCabinetOfflineReportOrderMapper electricityCabinetOfflineReportOrderMapper;

    /**
     * 新增离线换电订单上报
     * @param electricityCabinetOfflineReportOrder
     */
    @Override
    public void insertOrder(ElectricityCabinetOfflineReportOrder electricityCabinetOfflineReportOrder) {
        this.electricityCabinetOfflineReportOrderMapper.insert(electricityCabinetOfflineReportOrder);
    }

    /**
     * 根据订单id查询订单
     * @param orderId
     * @return
     */
    @Override
    public ElectricityCabinetOfflineReportOrder queryByOrderId(String orderId) {
        return this.electricityCabinetOfflineReportOrderMapper.selectOne(new LambdaQueryWrapper<ElectricityCabinetOfflineReportOrder>().eq(ElectricityCabinetOfflineReportOrder::getOrderId, orderId));
    }
}

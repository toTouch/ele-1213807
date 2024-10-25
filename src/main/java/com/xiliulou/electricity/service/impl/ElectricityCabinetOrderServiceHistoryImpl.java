package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetOrder;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderHistory;
import com.xiliulou.electricity.mapper.ElectricityCabinetOrderHistoryMapper;
import com.xiliulou.electricity.query.ElectricityCabinetOrderQuery;
import com.xiliulou.electricity.service.ElectricityCabinetOrderHistoryService;
import com.xiliulou.electricity.vo.ElectricityCabinetOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单表(TElectricityCabinetOrder)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
@Service("electricityCabinetOrderHistoryService")
@Slf4j
public class ElectricityCabinetOrderServiceHistoryImpl implements ElectricityCabinetOrderHistoryService {
    
    @Resource
    private ElectricityCabinetOrderHistoryMapper electricityCabinetOrderHistoryMapper;
    
    
    @Override
    public List<ElectricityCabinetOrderVO> queryList(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {
        return electricityCabinetOrderHistoryMapper.queryList(electricityCabinetOrderQuery);
    }
    
    @Override
    public R queryCount(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {
        return R.ok(electricityCabinetOrderHistoryMapper.queryCount(electricityCabinetOrderQuery));
    }
    
    
    @Override
    public Integer queryCountForScreenStatistic(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {
        return electricityCabinetOrderHistoryMapper.queryCount(electricityCabinetOrderQuery);
    }
    
    
    @Override
    public Integer homeOneCount(Long first, Long now, List<Integer> eleIdList, Integer tenantId) {
        return electricityCabinetOrderHistoryMapper.homeOneCount(first, now, eleIdList, tenantId);
    }
    
    @Override
    public Integer homeOneSuccess(Long first, Long now, List<Integer> eleIdList, Integer tenantId) {
        return electricityCabinetOrderHistoryMapper.homeOneSuccess(first, now, eleIdList, tenantId);
    }
    
    @Override
    public Integer homeTotal(Long uid) {
        return electricityCabinetOrderHistoryMapper.selectCount(new LambdaQueryWrapper<ElectricityCabinetOrderHistory>().eq(ElectricityCabinetOrderHistory::getUid, uid));
    }
    
    
    @Override
    public ElectricityCabinetOrderHistory queryByUid(Long uid) {
        return electricityCabinetOrderHistoryMapper.selectOne(new LambdaQueryWrapper<ElectricityCabinetOrderHistory>().eq(ElectricityCabinetOrderHistory::getUid, uid)
                .notIn(ElectricityCabinetOrderHistory::getStatus, ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS, ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL,
                        ElectricityCabinetOrder.ORDER_CANCEL).orderByDesc(ElectricityCabinetOrderHistory::getCreateTime).last("limit 0,1"));
    }
    
    
    @Override
    public ElectricityCabinetOrderHistory selectLatestByUidV2(Long uid) {
        return electricityCabinetOrderHistoryMapper.selectLatestByUidV2(uid);
    }
    
    @Override
    public ElectricityCabinetOrderVO selectLatestOrderAndCabinetInfo(Long uid) {
        return electricityCabinetOrderHistoryMapper.selectLatestOrderAndCabinetInfo(uid);
    }
    
    @Override
    public List<ElectricityCabinetOrderVO> listSuperAdminPage(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {
        return electricityCabinetOrderHistoryMapper.selectListSuperAdminPage(electricityCabinetOrderQuery);
    }
    
}

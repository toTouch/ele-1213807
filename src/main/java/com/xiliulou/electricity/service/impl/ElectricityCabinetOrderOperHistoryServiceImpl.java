package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.mapper.ElectricityCabinetOrderOperHistoryMapper;
import com.xiliulou.electricity.query.ElectricityCabinetOrderOperHistoryQuery;
import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;


/**
 * 订单的操作历史记录(TElectricityCabinetOrderOperHistory)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 10:57:22
 */
@Service("electricityCabinetOrderOperHistoryService")
public class ElectricityCabinetOrderOperHistoryServiceImpl implements ElectricityCabinetOrderOperHistoryService {
    @Resource
    private ElectricityCabinetOrderOperHistoryMapper electricityCabinetOrderOperHistoryMapper;

    /**
     * 新增数据
     *
     * @param electricityCabinetOrderOperHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElectricityCabinetOrderOperHistory insert(ElectricityCabinetOrderOperHistory electricityCabinetOrderOperHistory) {
        this.electricityCabinetOrderOperHistoryMapper.insert(electricityCabinetOrderOperHistory);
        return electricityCabinetOrderOperHistory;
    }

    @Override
    public R queryList(ElectricityCabinetOrderOperHistoryQuery electricityCabinetOrderOperHistoryQuery) {
        return R.ok(electricityCabinetOrderOperHistoryMapper.queryList(electricityCabinetOrderOperHistoryQuery));
    }
}

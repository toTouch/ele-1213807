package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.entity.OffLineElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.mapper.ElectricityCabinetOrderOperHistoryMapper;
import com.xiliulou.electricity.query.ElectricityCabinetOrderOperHistoryQuery;
import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.EleOrderOperHistoryDetailVO;
import com.xiliulou.electricity.vo.ElectricityCabinetVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


/**
 * 订单的操作历史记录(TElectricityCabinetOrderOperHistory)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 10:57:22
 */
@Service("electricityCabinetOrderOperHistoryService")
public class ElectricityCabinetOrderOperHistoryServiceImpl implements ElectricityCabinetOrderOperHistoryService {

    //上报数据类型 0：旧版本，1：新版本
    private static final Integer  TYPE_STATUS_OLD = 0;
    private static final Integer  TYPE_STATUS_NEW = 1;

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

    /**
     * 离线换电新增操作记录
     *
     * @param offLineElectricityCabinetOrderOperHistory
     * @return
     */
    @Override
    public R insertOffLineOperateHistory(OffLineElectricityCabinetOrderOperHistory offLineElectricityCabinetOrderOperHistory) {
        return R.ok(electricityCabinetOrderOperHistoryMapper.insertOffLineEleExchangeOperateHistory(offLineElectricityCabinetOrderOperHistory));
    }

    @Slave
    @Override
    public R queryListByOrderId(ElectricityCabinetOrderOperHistoryQuery electricityCabinetOrderOperHistoryQuery) {
        List<ElectricityCabinetOrderOperHistory> historyList = electricityCabinetOrderOperHistoryMapper.queryListByOrderId(electricityCabinetOrderOperHistoryQuery);

        if (ObjectUtil.isNotEmpty(historyList)) {
            boolean falg = Boolean.FALSE;
            //判断上报的操作记录数据是否有操作顺序及操作结果
            for (ElectricityCabinetOrderOperHistory history : historyList) {
                falg = ObjectUtil.isEmpty(history.getSeq()) || ObjectUtil.isEmpty(history.getResult()) || ObjectUtil.equal(history.getSeq(), -1) || ObjectUtil.equal(history.getResult(), -1);
            }

            //若上报的操作记录数据没有操作顺序或操作结果（即旧数据）
            if (falg) {
                List<ElectricityCabinetOrderOperHistory> operHistoryList = historyList.stream().sorted(Comparator.comparing(ElectricityCabinetOrderOperHistory::getCreateTime).reversed()).collect(Collectors.toList());
                return R.ok(operHistoryList);
            }
        }

        return R.ok(historyList);
    }

    /**
     * 查询操作记录为旧数据还是新数据，方便前端页面跳转判断
     * @param orderId
     * @param type
     * @return
     */
    @Override
    public R selectOperateDataType(String orderId, Integer type) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        ElectricityCabinetOrderOperHistoryQuery historyQuery = ElectricityCabinetOrderOperHistoryQuery.builder().orderId(orderId).type(type).tenantId(tenantId).build();

        List<ElectricityCabinetOrderOperHistory> historyList = electricityCabinetOrderOperHistoryMapper.queryListByOrderId(historyQuery);
        if (ObjectUtil.isNotEmpty(historyList)) {
            boolean falg = Boolean.FALSE;
            //判断上报的操作记录数据是否有操作顺序及操作结果
            for (ElectricityCabinetOrderOperHistory history : historyList) {
                falg = ObjectUtil.isEmpty(history.getSeq()) || ObjectUtil.isEmpty(history.getResult()) || ObjectUtil.equal(history.getSeq(), -1) || ObjectUtil.equal(history.getResult(), -1);
            }

            //若上报的操作记录数据没有操作顺序或操作结果（即旧数据）
            if (falg) {
                return R.ok(TYPE_STATUS_OLD);
            }
        }

        return R.ok(TYPE_STATUS_NEW);
    }

    @Slave
    @Override
    public R queryCountByOrderId(ElectricityCabinetOrderOperHistoryQuery electricityCabinetOrderOperHistoryQuery) {
        return R.ok(electricityCabinetOrderOperHistoryMapper.queryCountByOrderId(electricityCabinetOrderOperHistoryQuery));
    }



}

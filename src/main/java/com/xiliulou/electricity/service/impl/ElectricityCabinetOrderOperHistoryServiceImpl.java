package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.entity.OffLineElectricityCabinetOrderOperHistory;
import com.xiliulou.electricity.mapper.ElectricityCabinetOrderOperHistoryMapper;
import com.xiliulou.electricity.query.ElectricityCabinetOrderOperHistoryQuery;
import com.xiliulou.electricity.service.ElectricityCabinetOrderOperHistoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


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

    @Override
    public R queryListByOrderId(ElectricityCabinetOrderOperHistoryQuery electricityCabinetOrderOperHistoryQuery) {
        List<ElectricityCabinetOrderOperHistory> historyList = electricityCabinetOrderOperHistoryMapper.queryListByOrderId(electricityCabinetOrderOperHistoryQuery);

        if (ObjectUtil.isNotEmpty(historyList)) {
            boolean falg = Boolean.FALSE;
            //判断上报的操作记录数据是否有操作顺序及操作结果
            for (ElectricityCabinetOrderOperHistory history : historyList) {
                falg = ObjectUtil.isEmpty(history.getSeq()) || ObjectUtil.isEmpty(history.getResult());
            }

            //若上报的操作记录数据没有操作顺序或操作结果（即旧数据），手动排序 设置操作结果
            if (falg) {
                for (int i = 0; i < historyList.size(); i++) {
                    historyList.get(i).setSeq(i + 1);

                    historyList.get(i).setResult(checkOrderOperResult(historyList.get(i)) ? ElectricityCabinetOrderOperHistory.OPERATE_RESULT_SUCCESS : ElectricityCabinetOrderOperHistory.OPERATE_RESULT_FAIL);
                }
            }
        }

        return R.ok(historyList);
    }

    @Override
    public R queryCountByOrderId(ElectricityCabinetOrderOperHistoryQuery electricityCabinetOrderOperHistoryQuery) {
        return R.ok(electricityCabinetOrderOperHistoryMapper.queryCountByOrderId(electricityCabinetOrderOperHistoryQuery));
    }


    private boolean checkOrderOperResult(ElectricityCabinetOrderOperHistory history) {
        if (StringUtils.isNotBlank(history.getMsg())) {
            return history.getMsg().contains("不合法") ||
                    history.getMsg().contains("没有关闭") ||
                    history.getMsg().contains("不存在") ||
                    history.getMsg().contains("无法进行") ||
                    history.getMsg().contains("失败") ||
                    history.getMsg().contains("未关闭") ||
                    history.getMsg().contains("不匹配") ||
                    history.getMsg().contains("不属于") ||
                    history.getMsg().contains("超时");
        }

        return Boolean.FALSE;
    }

}

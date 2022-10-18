package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleWarnMsg;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.mapper.EleWarnMsgMapper;
import com.xiliulou.electricity.query.EleWarnMsgQuery;
import com.xiliulou.electricity.service.EleWarnMsgService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.vo.*;
import net.bytebuddy.description.type.TypeList;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 换电柜异常上报信息(TEleWarnMsg)表服务实现类
 *
 * @author makejava
 * @since 2021-03-29 14:12:45
 */
@Service("eleWarnMsgService")
public class EleWarnMsgServiceImpl implements EleWarnMsgService {
    @Resource
    private EleWarnMsgMapper eleWarnMsgMapper;

    @Autowired
    ElectricityCabinetService electricityCabinetService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleWarnMsg queryByIdFromDB(Long id) {
        return this.eleWarnMsgMapper.selectById(id);
    }


    /**
     * 新增数据
     *
     * @param eleWarnMsg 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleWarnMsg insert(EleWarnMsg eleWarnMsg) {
        this.eleWarnMsgMapper.insert(eleWarnMsg);
        return eleWarnMsg;
    }

    /**
     * 修改数据
     *
     * @param eleWarnMsg 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleWarnMsg eleWarnMsg) {
        return this.eleWarnMsgMapper.updateById(eleWarnMsg);

    }

    @Override
    public R queryList(EleWarnMsgQuery eleWarnMsgQuery) {
        return R.ok(eleWarnMsgMapper.queryList(eleWarnMsgQuery));
    }

    @Override
    public R queryAllTenant(EleWarnMsgQuery eleWarnMsgQuery) {
        return R.ok(eleWarnMsgMapper.queryAllTenantList(eleWarnMsgQuery));
    }

    @Override
    public R queryAllTenantCount() {
        return R.ok(eleWarnMsgMapper.queryAllTenantCount());
    }

    @Override
    public R queryCount(EleWarnMsgQuery eleWarnMsgQuery) {
        return R.ok(eleWarnMsgMapper.queryCount(eleWarnMsgQuery));
    }

    @Override
    public void delete(Long id) {
        eleWarnMsgMapper.deleteById(id);
    }

    @Override
    public R queryStatisticsEleWarmMsg(EleWarnMsgQuery eleWarnMsgQuery) {
        return R.ok(eleWarnMsgMapper.queryStatisticsEleWarmMsg(eleWarnMsgQuery));
    }

    @Override
    public R queryStatisticEleWarnMsgByElectricityCabinet(EleWarnMsgQuery eleWarnMsgQuery) {
        return R.ok(eleWarnMsgMapper.queryStatisticEleWarnMsgByElectricityCabinet(eleWarnMsgQuery));
    }

    @Override
    public R queryStatisticEleWarnMsgRanking(EleWarnMsgQuery eleWarnMsgQuery) {
        List<EleWarnMsgVo> eleWarnMsgRankingVos = null;
        if (Objects.nonNull(eleWarnMsgQuery.getElectricityCabinetId())) {
            eleWarnMsgRankingVos=eleWarnMsgMapper.queryStatisticEleWarnMsgRankingByElectricityCabinetId(eleWarnMsgQuery);
            return R.ok(eleWarnMsgRankingVos);
        }else {
            eleWarnMsgRankingVos = eleWarnMsgMapper.queryStatisticEleWarnMsgRanking(eleWarnMsgQuery);
        }
        if (Objects.nonNull(eleWarnMsgRankingVos)) {
            for (EleWarnMsgVo eleWarnMsgVo : eleWarnMsgRankingVos) {
                EleWarnMsgVo eleWarnMsgVoForTenant = eleWarnMsgMapper.queryStatisticEleWarnMsgForTenant(eleWarnMsgVo.getElectricityCabinetId());
                eleWarnMsgVo.setElectricityCabinetName(eleWarnMsgVoForTenant.getElectricityCabinetName());
                eleWarnMsgVo.setTenantName(eleWarnMsgVoForTenant.getTenantName());
            }
        }
        return R.ok(eleWarnMsgRankingVos);
    }

    @Override
    public R queryStatisticEleWarnMsgRankingCount() {
        return R.ok(eleWarnMsgMapper.queryStatisticEleWarnMsgRankingCount());
    }

    @Override
    public void queryElectricityName(List<Object> list) {

        if (Objects.isNull(list)){
            return;
        }

        for (Object object:list){
            if (object instanceof EleBatteryWarnMsgVo){
                ElectricityCabinet electricityCabinet=electricityCabinetService.queryByIdFromCache(Integer.parseInt(((EleBatteryWarnMsgVo) object).getElectricityCabinetId()));
                ((EleBatteryWarnMsgVo) object).setCabinetName(electricityCabinet.getName());
            } else if (object instanceof EleBusinessWarnMsgVo){
                ElectricityCabinet electricityCabinet=electricityCabinetService.queryByIdFromCache(Integer.parseInt(((EleBusinessWarnMsgVo) object).getElectricityCabinetId()));
                ((EleBusinessWarnMsgVo) object).setCabinetName(electricityCabinet.getName());
            }else if (object instanceof EleCabinetWarnMsgVo){
                ElectricityCabinet electricityCabinet=electricityCabinetService.queryByIdFromCache(Integer.parseInt(((EleCabinetWarnMsgVo) object).getElectricityCabinetId()));
                ((EleCabinetWarnMsgVo) object).setCabinetName(electricityCabinet.getName());
            }else if (object instanceof EleCellWarnMsgVo){
                ElectricityCabinet electricityCabinet=electricityCabinetService.queryByIdFromCache(Integer.parseInt(((EleCellWarnMsgVo) object).getElectricityCabinetId()));
                ((EleCellWarnMsgVo) object).setCabinetName(electricityCabinet.getName());
            }
        }

    }
}

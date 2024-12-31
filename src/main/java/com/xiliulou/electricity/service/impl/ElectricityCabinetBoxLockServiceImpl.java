package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBoxLock;
import com.xiliulou.electricity.mapper.ElectricityCabinetBoxLockMapper;
import com.xiliulou.electricity.service.ElectricityCabinetBoxLockService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author renhang
 */
@Slf4j
@Service
public class ElectricityCabinetBoxLockServiceImpl implements ElectricityCabinetBoxLockService {

    @Resource
    private ElectricityCabinetService electricityCabinetService;

    @Resource
    private ElectricityCabinetBoxLockMapper electricityCabinetBoxLockMapper;

    @Override
    public void insertElectricityCabinetBoxLock(ElectricityCabinetBoxLock cabinetBoxLock) {
        if (Objects.isNull(cabinetBoxLock)) {
            log.error("ElectricityCabinetBoxLockService Error! cabinetBoxLock is null");
            return;
        }
        Integer eid = cabinetBoxLock.getElectricityCabinetId();
        if (Objects.isNull(eid)) {
            log.error("ElectricityCabinetBoxLockService Error! eid is null");
            return;
        }
        if (Objects.isNull(cabinetBoxLock.getCellNo())) {
            log.error("ElectricityCabinetBoxLockService Error! cellNo is null, eid is {}", eid);
            return;
        }
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(eid);
        if (Objects.isNull(electricityCabinet)) {
            log.error("ElectricityCabinetBoxLockService Error! electricityCabinet is null, eid is {}", eid);
            return;
        }

        cabinetBoxLock.setAddress(electricityCabinet.getAddress()).setSn(electricityCabinet.getSn())
                .setAreaId(electricityCabinet.getAreaId()).setCreateTime(System.currentTimeMillis())
                .setDelFlag(ElectricityCabinetBoxLock.DEL_NORMAL).setName(electricityCabinet.getName())
                .setUpdateTime(System.currentTimeMillis()).setTenantId(electricityCabinet.getTenantId())
                .setStoreId(electricityCabinet.getStoreId()).setFranchiseeId(electricityCabinet.getFranchiseeId());
        electricityCabinetBoxLockMapper.insertEleLockBox(cabinetBoxLock);
    }

    @Override
    public void updateElectricityCabinetBoxLock(Integer eid, String cellNo) {
        if (Objects.isNull(eid)) {
            log.error("ElectricityCabinetBoxLockService Error! updateElectricityCabinetBoxLock.eid is null");
            return;
        }

        if (StrUtil.isEmpty(cellNo)) {
            log.error("ElectricityCabinetBoxLockService Error! updateElectricityCabinetBoxLock.cellNo is null");
            return;
        }
        ElectricityCabinetBoxLock electricityCabinetBoxLock = electricityCabinetBoxLockMapper.selectBoxLockByEidAndCell(eid, cellNo);
        if (Objects.isNull(electricityCabinetBoxLock)) {
            log.error("ElectricityCabinetBoxLockService Error! electricityCabinetBoxLock is null. eid is {}.cellNo is {}", eid, cellNo);
            return;
        }
        ElectricityCabinetBoxLock updateBoxLock = new ElectricityCabinetBoxLock();
        updateBoxLock.setUpdateTime(System.currentTimeMillis());
        updateBoxLock.setId(electricityCabinetBoxLock.getId());
        updateBoxLock.setDelFlag(ElectricityCabinetBoxLock.DEL_DEL);
        electricityCabinetBoxLockMapper.updateEleLockBox(updateBoxLock);
    }
}

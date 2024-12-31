package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBoxLock;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.ElectricityCabinetBoxLockMapper;
import com.xiliulou.electricity.query.exchange.ElectricityCabinetBoxLockPageQuery;
import com.xiliulou.electricity.service.ElectricityCabinetBoxLockService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetBoxLockPageVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Resource
    private AssertPermissionService assertPermissionService;

    @Resource
    private FranchiseeService franchiseeService;

    @Resource
    private StoreService storeService;

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

    @Override
    public List<ElectricityCabinetBoxLockPageVO> queryList(ElectricityCabinetBoxLockPageQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }

        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(user);
        if (!pair.getLeft()) {
            return new ArrayList<>();
        }
        query.setFranchiseeIds(pair.getRight());

        List<ElectricityCabinetBoxLock> electricityCabinetBoxLocks = electricityCabinetBoxLockMapper.listCabinetBoxLock(query);
        if (CollUtil.isEmpty(electricityCabinetBoxLocks)) {
            return CollUtil.newArrayList();
        }

        return electricityCabinetBoxLocks.stream().map(item -> {
            ElectricityCabinetBoxLockPageVO vo = new ElectricityCabinetBoxLockPageVO();
            BeanUtil.copyProperties(item, vo);

            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            vo.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : null);
            Store store = storeService.queryByIdFromCache(item.getStoreId());
            vo.setStoreName(Objects.nonNull(store) ? store.getName() : null);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Long queryCount(ElectricityCabinetBoxLockPageQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }

        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(user);
        if (!pair.getLeft()) {
            return NumberConstant.ZERO_L;
        }
        query.setFranchiseeIds(pair.getRight());

        return electricityCabinetBoxLockMapper.countCabinetBoxLock(query);
    }
}

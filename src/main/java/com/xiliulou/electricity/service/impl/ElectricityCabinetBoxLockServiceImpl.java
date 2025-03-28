package com.xiliulou.electricity.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.entity.merchant.MerchantArea;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.ElectricityCabinetBoxLockMapper;
import com.xiliulou.electricity.query.EleOuterCommandQuery;
import com.xiliulou.electricity.query.ElectricityCabinetIdByFilterQuery;
import com.xiliulou.electricity.query.exchange.ElectricityCabinetBoxLockPageQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.service.merchant.MerchantAreaService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.ElectricityCabinetBoxLockPageVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
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
    private ElectricityCabinetBoxService boxService;

    @Resource
    private AssertPermissionService assertPermissionService;

    @Resource
    private FranchiseeService franchiseeService;

    @Resource
    private StoreService storeService;

    @Resource
    private RedisService redisService;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private MerchantAreaService merchantAreaService;
    
    @Resource
    private ElectricityCabinetBoxService electricityCabinetBoxService;
    
    @Resource
    private BoxOtherPropertiesService boxOtherPropertiesService;

    @Override
    public void insertElectricityCabinetBoxLock(ElectricityCabinetBoxLock cabinetBoxLock) {
        if (Objects.isNull(cabinetBoxLock)) {
            log.error("ElectricityCabinetBoxLockService Warn! cabinetBoxLock is null");
            return;
        }
        Integer eid = cabinetBoxLock.getElectricityCabinetId();
        if (Objects.isNull(eid)) {
            log.error("ElectricityCabinetBoxLockService Warn! eid is null");
            return;
        }

        Integer cellNo = cabinetBoxLock.getCellNo();
        if (Objects.isNull(cellNo)) {
            log.error("ElectricityCabinetBoxLockService Warn! cellNo is null, eid is {}", eid);
            return;
        }

        ElectricityCabinetBoxLock electricityCabinetBoxLock = applicationContext.getBean(ElectricityCabinetBoxLockService.class).selectBoxLockByEidAndCell(eid, cellNo);
        if (Objects.nonNull(electricityCabinetBoxLock)) {
            return;
        }

        electricityCabinetBoxLockMapper.insertEleLockBox(cabinetBoxLock);
    }

    @Override
    public void updateElectricityCabinetBoxLock(Integer eid, String cellNo) {
        if (Objects.isNull(eid)) {
            log.error("ElectricityCabinetBoxLockService Warn! updateElectricityCabinetBoxLock.eid is null");
            return;
        }

        if (StrUtil.isEmpty(cellNo)) {
            log.error("ElectricityCabinetBoxLockService Warn! updateElectricityCabinetBoxLock.cellNo is null");
            return;
        }
        ElectricityCabinetBoxLock electricityCabinetBoxLock = applicationContext.getBean(ElectricityCabinetBoxLockService.class).selectBoxLockByEidAndCell(eid, Integer.valueOf(cellNo));
        if (Objects.isNull(electricityCabinetBoxLock)) {
            return;
        }

        electricityCabinetBoxLockMapper.deleteEleLockBox(electricityCabinetBoxLock.getId());
    }

    @Override
    @Slave
    public List<ElectricityCabinetBoxLockPageVO> queryList(ElectricityCabinetBoxLockPageQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }

        Triple<List<Long>, List<Long>, Boolean> triple = assertPermissionService.assertPermissionByTriple(SecurityUtils.getUserInfo());
        if (!triple.getRight()) {
            return new ArrayList<>();
        }
        query.setFranchiseeIds(triple.getLeft());
        query.setStoreIds(triple.getMiddle());


        //  电柜信息搜索
        if (Objects.nonNull(query.getAreaId()) || StrUtil.isNotBlank(query.getAddress()) || StrUtil.isNotBlank(query.getName())) {
            List<Integer> cabinetIdByFilterList = electricityCabinetService.queryCabinetIdByFilter(
                    ElectricityCabinetIdByFilterQuery.builder().tenantId(query.getTenantId())
                            .name(query.getName())
                            .address(query.getAddress())
                            .areaId(query.getAreaId())
                            .build()
            );
            if (CollUtil.isNotEmpty(cabinetIdByFilterList)) {
                query.setIdsByLikeName(cabinetIdByFilterList);
            } else {
                return CollUtil.newArrayList();
            }
        }
    
        List<ElectricityCabinetBoxLock> electricityCabinetBoxLocks = electricityCabinetBoxLockMapper.listCabinetBoxLock(query);
        if (CollUtil.isEmpty(electricityCabinetBoxLocks)) {
            return CollUtil.newArrayList();
        }
    
        Map<Integer, List<BoxOtherProperties>> boxOtherPropertiesMap = null;
        Set<Integer> eidSet = electricityCabinetBoxLocks.stream().map(ElectricityCabinetBoxLock::getElectricityCabinetId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(eidSet)) {
            List<BoxOtherProperties> boxOtherProperties = boxOtherPropertiesService.listByEidList(new ArrayList<>(eidSet));
            if (CollectionUtils.isNotEmpty(boxOtherProperties)) {
                boxOtherPropertiesMap = boxOtherProperties.stream().collect(Collectors.groupingBy(BoxOtherProperties::getElectricityCabinetId));
            }
        }
    
        Map<Integer, List<BoxOtherProperties>> finalBoxOtherPropertiesMap = boxOtherPropertiesMap;
        
        Map<Integer, List<String>> eidAndCellNo = new HashMap<>();
        List<ElectricityCabinetBoxLockPageVO> cabinetBoxLockPageVOS = electricityCabinetBoxLocks.stream().map(item -> {
            Integer electricityCabinetId = item.getElectricityCabinetId();
            ElectricityCabinetBoxLockPageVO vo = new ElectricityCabinetBoxLockPageVO();
            BeanUtil.copyProperties(item, vo);
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            vo.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : null);
            Store store = storeService.queryByIdFromCache(item.getStoreId());
            vo.setStoreName(Objects.nonNull(store) ? store.getName() : null);
            
            Optional.ofNullable(electricityCabinetService.queryByIdFromCache(electricityCabinetId)).ifPresent(cabinet -> {
                vo.setName(cabinet.getName());
                vo.setAddress(cabinet.getAddress());
                MerchantArea merchantArea = merchantAreaService.queryById(cabinet.getAreaId());
                vo.setAreaName(Objects.isNull(merchantArea) ? null : merchantArea.getName());
            });
            
            // 收集柜机id与仓门号用于查询锁仓sn
            if (eidAndCellNo.containsKey(electricityCabinetId)) {
                eidAndCellNo.get(electricityCabinetId).add(item.getCellNo().toString());
            } else {
                List<String> cellNoList = new ArrayList<>();
                cellNoList.add(item.getCellNo().toString());
                eidAndCellNo.put(electricityCabinetId, cellNoList);
            }
    
            // 锁仓备注
            if (MapUtils.isNotEmpty(finalBoxOtherPropertiesMap) && finalBoxOtherPropertiesMap.containsKey(electricityCabinetId)) {
                List<BoxOtherProperties> boxOtherPropertiesList = finalBoxOtherPropertiesMap.get(electricityCabinetId);
                if (CollectionUtils.isNotEmpty(boxOtherPropertiesList)) {
                    boxOtherPropertiesList.forEach(boxOtherProperties -> {
                        if (Objects.isNull(boxOtherProperties)) {
                            return;
                        }
                
                        if (Objects.nonNull(item.getCellNo()) && Objects.equals(boxOtherProperties.getCellNo(), item.getCellNo().toString())) {
                            vo.setRemark(boxOtherProperties.getRemark());
                        }
                    });
                }
            }
            return vo;
        }).collect(Collectors.toList());
        
        // 查询拼装锁定在仓sn
        Map<Integer, Map<String, String>> lockSnsMap = electricityCabinetBoxService.listLockSnsByEidAndCellNo(eidAndCellNo);
        if (MapUtils.isEmpty(lockSnsMap)) {
            return cabinetBoxLockPageVOS;
        }
        for (ElectricityCabinetBoxLockPageVO pageVO : cabinetBoxLockPageVOS) {
            if (lockSnsMap.containsKey(pageVO.getElectricityCabinetId())) {
                pageVO.setLockSn(lockSnsMap.get(pageVO.getElectricityCabinetId()).get(pageVO.getCellNo()));
            }
        }
        
        return cabinetBoxLockPageVOS;
    }

    @Override
    @Slave
    public Long queryCount(ElectricityCabinetBoxLockPageQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }


        Triple<List<Long>, List<Long>, Boolean> triple = assertPermissionService.assertPermissionByTriple(SecurityUtils.getUserInfo());
        if (!triple.getRight()) {
            return NumberConstant.ZERO_L;
        }
        query.setFranchiseeIds(triple.getLeft());
        query.setStoreIds(triple.getMiddle());

        //  电柜信息搜索
        if (Objects.nonNull(query.getAreaId()) || StrUtil.isNotBlank(query.getAddress()) || StrUtil.isNotBlank(query.getName())) {
            List<Integer> cabinetIdByFilterList = electricityCabinetService.queryCabinetIdByFilter(
                    ElectricityCabinetIdByFilterQuery.builder().tenantId(query.getTenantId())
                            .name(query.getName())
                            .address(query.getAddress())
                            .areaId(query.getAreaId())
                            .build()
            );
            if (CollUtil.isNotEmpty(cabinetIdByFilterList)) {
                query.setIdsByLikeName(cabinetIdByFilterList);
            } else {
                return NumberConstant.ZERO_L;
            }
        }


        return electricityCabinetBoxLockMapper.countCabinetBoxLock(query);
    }


    @Override
    public R enableBoxCell(EleOuterCommandQuery query) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!redisService.setNx(CacheConstant.LOCK_CELL_ENABLE_KEY + user.getUid(), NumberConstant.ONE.toString(), 5 * 1000L, false)) {
            return R.fail(false, "100002", "点击过于频繁，请稍后再试");
        }

        // 需要校验是否已经启用了
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(query.getProductKey(), query.getDeviceName());
        if (Objects.isNull(electricityCabinet)) {
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        Map<String, Object> data = query.getData();
        if (CollUtil.isEmpty(data)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        Object object = data.get("cell_list");
        if (Objects.isNull(object)) {
            return R.fail("402013", "至少选择一个格挡启用");
        }

        if (object instanceof List<?>) {
            List<?> tempList = (List<?>) object;
            if (!tempList.stream().allMatch(item -> item instanceof Integer)) {
                return R.fail("ELECTRICITY.0007", "不合法的参数");
            }

            @SuppressWarnings("unchecked")
            List<Integer> list = (List<Integer>) tempList;
            // 处理
            if (!Objects.equals(list.size(), NumberConstant.ONE)) {
                return R.fail("402011", "启用仓门只能启用一个");
            }
            Integer eid = electricityCabinet.getId();
            String cellNo = String.valueOf(list.get(0));

            // 先看格挡记录
            ElectricityCabinetBox electricityCabinetBox = boxService.queryByCellNo(eid, cellNo);
            if (Objects.nonNull(electricityCabinetBox) && Objects.equals(electricityCabinetBox.getUsableStatus(), ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_USABLE)) {
                log.info("EnableBoxCell Info! electricityCabinetBox is usAble, eid is {}, cellNo is {}", eid, cellNo);
                return R.fail("402012", "当前仓门已经启用，请刷新后查看");
            }

            // 查看锁仓记录
            ElectricityCabinetBoxLock cabinetBoxLock = electricityCabinetBoxLockMapper.selectBoxLockByEidAndCell(electricityCabinet.getId(), list.get(0));
            if (Objects.isNull(cabinetBoxLock)) {
                log.info("EnableBoxCell Info! cabinetBoxLock is null, eid is {}, cellNo is {}", eid, cellNo);
                return R.fail("402012", "当前仓门已经启用，请刷新后查看");
            }

            return electricityCabinetService.sendCommandToEleForOuter(query);
        } else {
            log.warn("EnableBoxCell Warn! cell_list is not legal，cell_list is {}", JsonUtil.toJson(object));
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

    }

    @Override
    @Slave
    public ElectricityCabinetBoxLock selectBoxLockByEidAndCell(Integer eid, Integer cellNo) {
        return electricityCabinetBoxLockMapper.selectBoxLockByEidAndCell(eid, cellNo);
    }

    @Override
    public void deleteElectricityCabinetBoxLock(Integer electricityCabinetId) {
        electricityCabinetBoxLockMapper.deleteElectricityCabinetBoxLock(electricityCabinetId);
    }
}

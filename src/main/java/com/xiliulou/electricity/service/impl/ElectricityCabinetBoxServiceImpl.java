package com.xiliulou.electricity.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.BatteryModel;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.entity.ElectricityConfig;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.ElectricityCabinetBoxMapper;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.query.ElectricityCabinetBoxQuery;
import com.xiliulou.electricity.query.FreeCellNoQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.ElectricityBatteryVO;
import com.xiliulou.electricity.vo.ElectricityCabinetBoxVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 换电柜仓门表(TElectricityCabinetBox)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 11:00:37
 */
@Service("electricityCabinetBoxService")
@Slf4j
public class ElectricityCabinetBoxServiceImpl implements ElectricityCabinetBoxService {
    
    @Resource
    private ElectricityCabinetBoxMapper electricityCabinetBoxMapper;
    
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;
    
    @Autowired
    UserInfoService userInfoService;
    
    @Autowired
    BatteryOtherPropertiesService batteryOtherPropertiesService;
    
    @Autowired
    ElectricityConfigService electricityConfigService;
    
    @Autowired
    BoxOtherPropertiesService boxOtherPropertiesService;
    
    @Autowired
    BatteryModelService batteryModelService;
    
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetBox queryByIdFromDB(Long id, Integer tenantId) {
        return this.electricityCabinetBoxMapper.selectOne(
                new LambdaQueryWrapper<ElectricityCabinetBox>().eq(ElectricityCabinetBox::getId, id).eq(ElectricityCabinetBox::getTenantId, tenantId));
    }
    
    @Override
    public void batchInsertBoxByModelId(ElectricityCabinetModel electricityCabinetModel, Integer id) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        if (Objects.nonNull(id)) {
            for (int i = 1; i <= electricityCabinetModel.getNum(); i++) {
                ElectricityCabinetBox electricityCabinetBox = new ElectricityCabinetBox();
                electricityCabinetBox.setElectricityCabinetId(id);
                electricityCabinetBox.setCellNo(String.valueOf(i));
                electricityCabinetBox.setCreateTime(System.currentTimeMillis());
                electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
                electricityCabinetBox.setDelFlag(ElectricityCabinetBox.DEL_NORMAL);
                electricityCabinetBox.setTenantId(tenantId);
                electricityCabinetBoxMapper.insert(electricityCabinetBox);
            }
        }
    }
    
    @Override
    public void batchInsertBoxByModelIdV2(ElectricityCabinetModel electricityCabinetModel, Integer id) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        List<ElectricityCabinetBox> boxList = Lists.newArrayList();
        if (Objects.nonNull(id)) {
            for (int i = 1; i <= electricityCabinetModel.getNum(); i++) {
                ElectricityCabinetBox electricityCabinetBox = new ElectricityCabinetBox();
                electricityCabinetBox.setElectricityCabinetId(id);
                electricityCabinetBox.setCellNo(String.valueOf(i));
                electricityCabinetBox.setCreateTime(System.currentTimeMillis());
                electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
                electricityCabinetBox.setDelFlag(ElectricityCabinetBox.DEL_NORMAL);
                electricityCabinetBox.setTenantId(tenantId);
                boxList.add(electricityCabinetBox);
            }
            electricityCabinetBoxMapper.batchInsertEleBox(boxList);
        }
    }
    
    @Override
    public void batchDeleteBoxByElectricityCabinetId(Integer id) {
        electricityCabinetBoxMapper.batchDeleteBoxByElectricityCabinetId(id, System.currentTimeMillis());
    }
    
    @Override
    @Slave
    public R queryList(ElectricityCabinetBoxQuery electricityCabinetBoxQuery) {
        
        List<ElectricityCabinetBoxVO> electricityCabinetBoxVOList = electricityCabinetBoxMapper.queryList(electricityCabinetBoxQuery);
        if (ObjectUtil.isEmpty(electricityCabinetBoxVOList)) {
            return R.ok(electricityCabinetBoxVOList);
        }
        
        List<ElectricityCabinetBoxVO> electricityCabinetBoxVOs = electricityCabinetBoxVOList.parallelStream().peek(item -> {
            if (StringUtils.isNotBlank(item.getSn())) {
                ElectricityBatteryVO electricityBatteryVO = electricityBatteryService.selectBatteryDetailInfoBySN(item.getSn());
                if (Objects.nonNull(electricityBatteryVO)) {
                    item.setPower(electricityBatteryVO.getPower());
                    item.setChargeStatus(electricityBatteryVO.getChargeStatus());
                    item.setBatteryA(electricityBatteryVO.getBatteryChargeA());
                    item.setBatteryV(electricityBatteryVO.getBatteryV());
                }
            }
        }).collect(Collectors.toList());
        
        List<ElectricityCabinetBoxVO> result = electricityCabinetBoxVOs.stream().sorted(Comparator.comparing(item -> Integer.parseInt(item.getCellNo())))
                .collect(Collectors.toList());
        
        return R.ok(result);
    }
    
    /**
     * 电柜详情  查询格挡列表及格挡其它信息
     *
     * @param electricityCabinetBoxQuery
     * @return
     */
    @Slave
    @Override
    public R selectBoxList(ElectricityCabinetBoxQuery electricityCabinetBoxQuery) {
        List<ElectricityCabinetBoxVO> electricityCabinetBoxVOList = electricityCabinetBoxMapper.selectBoxList(electricityCabinetBoxQuery);
        if (ObjectUtil.isEmpty(electricityCabinetBoxVOList)) {
            return R.ok(electricityCabinetBoxVOList);
        }
        
        List<BatteryModel> batteryModels = batteryModelService.queryByTenantIdFromCache(TenantContextHolder.getTenantId());
        Map<String, String> batteryModelMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(batteryModels)) {
            batteryModelMap = batteryModels.stream().collect(Collectors.toMap(BatteryModel::getBatteryType, BatteryModel::getBatteryVShort, (item1, item2) -> item2));
        }
        
        Map<String, String> finalBatteryModelMap = batteryModelMap;
        List<ElectricityCabinetBoxVO> electricityCabinetBoxVOs = electricityCabinetBoxVOList.parallelStream().peek(item -> {
            if (StringUtils.isBlank(item.getSn())) {
                return;
            }
            
            String sn = item.getSn();
            if (item.getSn().contains("UNKNOW")) {
                sn = item.getSn().substring(6);
            }
            ElectricityBatteryVO electricityBatteryVO = electricityBatteryService.selectBatteryDetailInfoBySN(sn);
            if (Objects.nonNull(electricityBatteryVO)) {
                item.setPower(Objects.nonNull(electricityBatteryVO.getPower()) ? electricityBatteryVO.getPower() : 0);
                item.setChargeStatus(Objects.nonNull(electricityBatteryVO.getChargeStatus()) ? electricityBatteryVO.getChargeStatus() : -1);
                item.setBatteryA(Objects.nonNull(electricityBatteryVO.getBatteryChargeA()) ? electricityBatteryVO.getBatteryChargeA() : 0);
                item.setBatteryV(Objects.nonNull(electricityBatteryVO.getBatteryV()) ? electricityBatteryVO.getBatteryV() : 0);
                item.setBatteryTemperature(Objects.nonNull(electricityBatteryVO.getBatteryTemperature()) ? electricityBatteryVO.getBatteryTemperature() : "0.00");
                
                // 租借在仓
                if (Objects.equals(electricityBatteryVO.getBusinessStatus(), ElectricityBattery.BUSINESS_STATUS_LEASE) && Objects.equals(electricityBatteryVO.getPhysicsStatus(),
                        ElectricityBattery.PHYSICS_STATUS_WARE_HOUSE)) {
                    if (Objects.isNull(electricityBatteryVO.getUid())) {
                        log.warn("ListBoxOther Warn! 电池租借在仓，但是uid为空，sn is {}", sn);
                    } else {
                        item.setIsBatteryRentInCell(ElectricityCabinetBoxVO.BATTERY_RENT_IN_CELL);
                        item.setUid(electricityBatteryVO.getUid());
                        UserInfo userInfo = userInfoService.queryByUidFromCache(electricityBatteryVO.getUid());
                        item.setUserName(Objects.nonNull(userInfo) ? userInfo.getName() : null);
                        item.setPhone(Objects.nonNull(userInfo) ? userInfo.getPhone() : null);
                    }
                }
            }

            //设置电池短型号
            if (Objects.nonNull(electricityBatteryVO) && Objects.nonNull(electricityBatteryVO.getModel())) {
                if (finalBatteryModelMap.containsKey(electricityBatteryVO.getModel())) {
                    item.setBatteryShortType(finalBatteryModelMap.getOrDefault(electricityBatteryVO.getModel(), ""));
                }
            }
        }).collect(Collectors.toList());
        
        List<ElectricityCabinetBoxVO> result = electricityCabinetBoxVOs.stream().sorted(Comparator.comparing(item -> Integer.parseInt(item.getCellNo())))
                .collect(Collectors.toList());
        
        return R.ok(result);
    }
    
    @Override
    public R modify(ElectricityCabinetBox electricityCabinetBox) {
        electricityCabinetBoxMapper.updateById(electricityCabinetBox);
        return R.ok();
    }
    
    @Slave
    @Override
    public List<ElectricityCabinetBox> queryBoxByElectricityCabinetId(Integer id) {
        return electricityCabinetBoxMapper.selectList(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId, id)
                .eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL)
                .eq(ElectricityCabinetBox::getUsableStatus, ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_USABLE));
    }
    
    @Override
    public List<ElectricityCabinetBox> selectEleBoxAttrByEid(Integer id) {
        return electricityCabinetBoxMapper.selectEleBoxAttrByEid(id);
    }
    
    @Slave
    @Override
    public List<ElectricityCabinetBox> listByElectricityCabinetIdS(List<Integer> electricityCabinetIdS, Integer tenantId) {
        return electricityCabinetBoxMapper.selectListByElectricityCabinetIdS(electricityCabinetIdS, tenantId);
    }

    @Slave
    @Override
    public List<ElectricityCabinetBox> listCabineBoxByEids(List<Integer> electricityCabinetIdList) {
        return electricityCabinetBoxMapper.selectListByEids(electricityCabinetIdList);
    }
    
    @Override
    @Slave
    public List<ElectricityCabinetBox> queryAllBoxByElectricityCabinetId(Integer id) {
        return electricityCabinetBoxMapper.selectList(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId, id)
                .eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL));
    }
    
    @Override
    @Slave
    public ElectricityCabinetBox queryBySn(String sn, Integer electricityCabinetId) {
        return electricityCabinetBoxMapper.selectOne(
                Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getSn, sn).eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL)
                        .eq(ElectricityCabinetBox::getElectricityCabinetId, electricityCabinetId)
                        .eq(ElectricityCabinetBox::getUsableStatus, ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_USABLE));
    }
    
    @Override
    public List<ElectricityCabinetBox> queryNoElectricityBatteryBox(Integer id) {
        return electricityCabinetBoxMapper.selectList(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId, id)
                .eq(ElectricityCabinetBox::getStatus, ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY).eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL)
                .eq(ElectricityCabinetBox::getUsableStatus, ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_USABLE));
    }
    
    @Override
    public List<ElectricityCabinetBox> queryElectricityBatteryBox(ElectricityCabinet electricityCabinet, String cellNo, String batteryType, Double fullCharged) {
        List<ElectricityCabinetBox> boxes = electricityCabinetBoxMapper.queryElectricityBatteryBox(electricityCabinet.getId(), cellNo, batteryType, fullCharged);
        if (CollUtil.isEmpty(boxes)){
            return new ArrayList<>();
        }
        return boxes;
    }
    
    @Override
    @Slave
    public ElectricityCabinetBox queryByCellNo(Integer electricityCabinetId, String cellNo) {
        return electricityCabinetBoxMapper.selectOne(
                Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId, electricityCabinetId).eq(ElectricityCabinetBox::getCellNo, cellNo)
                        .eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL));
    }
    
    @Override
    public void modifyByCellNo(ElectricityCabinetBox electricityCabinetNewBox) {
        electricityCabinetBoxMapper.modifyByCellNo(electricityCabinetNewBox);
    }
    
    @Override
    public void modifyCellByCellNo(ElectricityCabinetBox electricityCabinetBox) {
        electricityCabinetBoxMapper.modifyCellByCellNo(electricityCabinetBox);
    }
    
    @Override
    public List<ElectricityCabinetBox> queryUsableBatteryCellNo(Integer id, String batteryType, Double fullyCharged) {
        List<ElectricityCabinetBox> boxes = electricityCabinetBoxMapper.queryUsableBatteryCellNo(id, batteryType, fullyCharged);
        if (CollUtil.isEmpty(boxes)) {
            return new ArrayList<>();
        }
        return boxes;
    }
    
    @Override
    public List<FreeCellNoQuery> findUsableEmptyCellNo(Integer eid) {
        return electricityCabinetBoxMapper.queryUsableEmptyCellNo(eid);
    }
    
    @Override
    public List<ElectricityCabinetBox> listUsableEmptyCell(Integer eid) {
        return electricityCabinetBoxMapper.selectUsableEmptyCell(eid);
    }
    
    @Override
    public int selectUsableEmptyCellNumber(Integer eid, Integer tenantId) {
        return electricityCabinetBoxMapper.selectUsableEmptyCellNumber(eid, tenantId);
    }
    
    @Override
    public Integer disableCell(Integer cellNo, Integer electricityCabinetId) {
        return electricityCabinetBoxMapper.modifyCellUsableStatus(cellNo, electricityCabinetId);
    }
    
    @Slave
    @Override
    public R queryBoxCount(Integer electricityCabinet, Integer tenantId) {
        return R.ok(electricityCabinetBoxMapper.queryBoxCount(electricityCabinet, tenantId));
    }
    
    /**
     * 根据电池id查询格挡
     *
     * @param batteryId
     * @return
     */
    @Override
    public ElectricityCabinetBox selectByBatteryId(Long batteryId) {
        return electricityCabinetBoxMapper.selectEleBoxByBatteryId(batteryId);
    }
    
    @Override
    public Triple<Boolean, String, Object> selectAvailableBoxNumber(Integer electricityCabinetId, Integer tenantId) {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(tenantId);
        if (Objects.isNull(electricityConfig)) {
            log.error("ELE ERROR! electricityConfig is null,tenantId={}", tenantId);
            return Triple.of(false, "000001", "系统异常！");
        }
        
        if (!Objects.equals(electricityConfig.getIsEnableReturnBoxCheck(), ElectricityConfig.DISABLE_RETURN_BOX_CHECK)) {
            return Triple.of(true, "", "");
        }
        
        //获取所有启用的格挡
        List<ElectricityCabinetBox> electricityCabinetBoxes = this.queryBoxByElectricityCabinetId(electricityCabinetId);
        if (CollectionUtils.isEmpty(electricityCabinetBoxes)) {
            return Triple.of(true, "", "");
        }
        
        //获取空格挡
        List<ElectricityCabinetBox> haveBatteryBoxs = electricityCabinetBoxes.stream().filter(item -> StringUtils.isBlank(item.getSn())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(haveBatteryBoxs) || haveBatteryBoxs.size() <= 1) {
            log.warn("ELE WARN! emptyCellNumber less than 1,electricityCabinetId={}", electricityCabinetId);
            return Triple.of(false, "100240", "当前无空余格挡可供退电，请联系客服！");
        }
        
        return Triple.of(true, "", "");
    }
    
    
    @Override
    public List<ElectricityCabinetBox> selectHaveBatteryCellId(Integer id) {
        List<ElectricityCabinetBox> boxes = electricityCabinetBoxMapper.selectHaveBatteryCellId(id);
        if (CollUtil.isEmpty(boxes)){
            return new ArrayList<>();
        }
        return boxes;
    }
    
    @Override
    @Slave
    public List<ElectricityCabinetBox> listBySnList(List<String> snList) {
        return electricityCabinetBoxMapper.selectListBySnList(snList);
    }
}

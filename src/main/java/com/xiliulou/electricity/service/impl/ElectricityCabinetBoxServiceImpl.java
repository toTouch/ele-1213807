package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
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
import java.util.Comparator;
import java.util.List;
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


    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetBox queryByIdFromDB(Long id,Integer tenantId) {
        return this.electricityCabinetBoxMapper.selectOne(new LambdaQueryWrapper<ElectricityCabinetBox>()
                .eq(ElectricityCabinetBox::getId, id).eq(ElectricityCabinetBox::getTenantId,tenantId));
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

        List<ElectricityCabinetBoxVO> result = electricityCabinetBoxVOs.stream().sorted(Comparator.comparing(item -> Integer.parseInt(item.getCellNo()))).collect(Collectors.toList());

        return R.ok(result);
    }
    
    /**
     * 电柜详情  查询格挡列表及格挡其它信息
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

        List<ElectricityCabinetBoxVO> electricityCabinetBoxVOs = electricityCabinetBoxVOList.parallelStream().peek(item -> {
            if (StringUtils.isBlank(item.getSn())) {
                return;
            }

            ElectricityBatteryVO electricityBatteryVO = electricityBatteryService.selectBatteryDetailInfoBySN(item.getSn());
            item.setPower(Objects.nonNull(electricityBatteryVO) ? electricityBatteryVO.getPower() : 0);
            item.setChargeStatus(Objects.nonNull(electricityBatteryVO) ? electricityBatteryVO.getChargeStatus() : -1);
            item.setBatteryA(Objects.nonNull(electricityBatteryVO) ? electricityBatteryVO.getBatteryChargeA() : 0);
            item.setBatteryV(Objects.nonNull(electricityBatteryVO) ? electricityBatteryVO.getBatteryV() : 0);

        }).collect(Collectors.toList());
    
        List<ElectricityCabinetBoxVO> result = electricityCabinetBoxVOs.stream().sorted(Comparator.comparing(item -> Integer.parseInt(item.getCellNo()))).collect(Collectors.toList());
    
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
                .eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL).eq(ElectricityCabinetBox::getUsableStatus, ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_USABLE));
    }

    @Override
    public List<ElectricityCabinetBox> selectEleBoxAttrByEid(Integer id) {
        return electricityCabinetBoxMapper.selectEleBoxAttrByEid(id);
    }

    @Override
    @Slave
    public List<ElectricityCabinetBox> queryAllBoxByElectricityCabinetId(Integer id) {
        return electricityCabinetBoxMapper.selectList(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId, id)
                .eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL));
    }

    @Override
    @Slave
    public ElectricityCabinetBox queryBySn(String sn,Integer electricityCabinetId) {
        return electricityCabinetBoxMapper.selectOne(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getSn, sn)
                .eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL).eq(ElectricityCabinetBox::getElectricityCabinetId,electricityCabinetId).eq(ElectricityCabinetBox::getUsableStatus, ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_USABLE));
    }

    @Override
    public List<ElectricityCabinetBox> queryNoElectricityBatteryBox(Integer id) {
        return electricityCabinetBoxMapper.selectList(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId, id)
                .eq(ElectricityCabinetBox::getStatus, ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY).eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL)
                .eq(ElectricityCabinetBox::getUsableStatus, ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_USABLE));
    }
    
    @Override
    public List<ElectricityCabinetBox> queryElectricityBatteryBox(ElectricityCabinet electricityCabinet, String cellNo,
            String batteryType, Double fullCharged) {
        return electricityCabinetBoxMapper
                .queryElectricityBatteryBox(electricityCabinet.getId(), cellNo, batteryType, fullCharged);
    }

    @Override
    @Slave
    public ElectricityCabinetBox queryByCellNo(Integer electricityCabinetId, String cellNo) {
        return electricityCabinetBoxMapper.selectOne(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId, electricityCabinetId)
                .eq(ElectricityCabinetBox::getCellNo, cellNo).eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL));
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
        return electricityCabinetBoxMapper.queryUsableBatteryCellNo(id, batteryType, fullyCharged);
    }

    @Override
    public List<FreeCellNoQuery> findUsableEmptyCellNo(Integer eid) {
        return electricityCabinetBoxMapper.queryUsableEmptyCellNo(eid);
    }

    @Override
    public int selectUsableEmptyCellNumber(Integer eid, Integer tenantId) {
        return electricityCabinetBoxMapper.selectUsableEmptyCellNumber(eid, tenantId);
    }

    @Override
    public Integer disableCell(Integer cellNo, Integer electricityCabinetId) {
        return electricityCabinetBoxMapper.modifyCellUsableStatus(cellNo,electricityCabinetId);
    }

    @Slave
    @Override
    public R queryBoxCount(Integer electricityCabinet, Integer tenantId) {
        return R.ok(electricityCabinetBoxMapper.queryBoxCount(electricityCabinet,tenantId));
    }
    /**
     * 根据电池id查询格挡
     * @param batteryId
     * @return
     */
    @Override
    public ElectricityCabinetBox selectByBatteryId(Long batteryId) {
        return electricityCabinetBoxMapper.selectOne(new LambdaQueryWrapper<ElectricityCabinetBox>().eq(ElectricityCabinetBox::getBId, batteryId));
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
        if (CollectionUtils.isEmpty(haveBatteryBoxs) || haveBatteryBoxs.size()<=1) {
            log.error("ELE ERROR! emptyCellNumber less than 1,electricityCabinetId={}", electricityCabinetId);
            return Triple.of(false, "100240", "当前无空余格挡可供退电，请联系客服！");
        }

        return Triple.of(true, "", "");
    }
}

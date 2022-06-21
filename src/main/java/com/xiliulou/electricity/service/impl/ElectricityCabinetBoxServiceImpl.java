package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.handler.EleHardwareHandlerManager;
import com.xiliulou.electricity.mapper.ElectricityCabinetBoxMapper;
import com.xiliulou.electricity.query.ElectricityCabinetBoxQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.ElectricityCabinetBoxVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 换电柜仓门表(TElectricityCabinetBox)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 11:00:37
 */
@Service("electricityCabinetBoxService")
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
    FranchiseeUserInfoService franchiseeUserInfoService;


    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetBox queryByIdFromDB(Long id) {
        return this.electricityCabinetBoxMapper.selectById(id);
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
    @DS("slave_1")
    public R queryList(ElectricityCabinetBoxQuery electricityCabinetBoxQuery) {

        List<ElectricityCabinetBoxVO> electricityCabinetBoxVOList = electricityCabinetBoxMapper.queryList(electricityCabinetBoxQuery);
        if (ObjectUtil.isEmpty(electricityCabinetBoxVOList)) {
            return R.ok(electricityCabinetBoxVOList);
        }
        List<ElectricityCabinetBoxVO> electricityCabinetBoxVOs = new ArrayList<>();

        if (ObjectUtil.isNotEmpty(electricityCabinetBoxVOList)) {
            for (ElectricityCabinetBoxVO electricityCabinetBoxVO : electricityCabinetBoxVOList) {
                ElectricityBattery electricityBattery = electricityBatteryService.queryBySn(electricityCabinetBoxVO.getSn());
                if (Objects.nonNull(electricityBattery)) {
                    electricityCabinetBoxVO.setPower(electricityBattery.getPower());
                    electricityCabinetBoxVO.setChargeStatus(electricityBattery.getChargeStatus());
                }
                electricityCabinetBoxVOs.add(electricityCabinetBoxVO);
            }
        }
        return R.ok(electricityCabinetBoxVOs);
    }

    @Override
    public R modify(ElectricityCabinetBox electricityCabinetBox) {
        electricityCabinetBoxMapper.updateById(electricityCabinetBox);
        return R.ok();
    }

    @Override
    public List<ElectricityCabinetBox> queryBoxByElectricityCabinetId(Integer id) {
        return electricityCabinetBoxMapper.selectList(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId, id)
                .eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL).eq(ElectricityCabinetBox::getUsableStatus, ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_USABLE));
    }

    @Override
    public List<ElectricityCabinetBox> queryAllBoxByElectricityCabinetId(Integer id) {
        return electricityCabinetBoxMapper.selectList(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId, id)
                .eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL));
    }

    @Override
    public List<ElectricityCabinetBox> queryNoElectricityBatteryBox(Integer id) {
        return electricityCabinetBoxMapper.selectList(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId, id)
                .eq(ElectricityCabinetBox::getStatus, ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY).eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL)
                .eq(ElectricityCabinetBox::getUsableStatus, ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_USABLE));
    }

    @Override
    public List<ElectricityCabinetBox> queryElectricityBatteryBox(ElectricityCabinet electricityCabinet, String cellNo, String batteryType) {
        return electricityCabinetBoxMapper.queryElectricityBatteryBox(electricityCabinet.getId(), cellNo, batteryType);
    }

    @Override
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
    public List<ElectricityCabinetBox> findUsableEmptyCellNo(Integer eid) {
        return electricityCabinetBoxMapper.queryUsableEmptyCellNo(eid);
    }

    @Override
    public Integer disableCell(Integer cellNo, Integer electricityCabinetId) {
        return electricityCabinetBoxMapper.modifyCellUsableStatus(cellNo,electricityCabinetId);
    }

}

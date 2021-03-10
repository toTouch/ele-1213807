package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.entity.ElectricityBattery;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.handler.EleHardwareHandlerManager;
import com.xiliulou.electricity.mapper.ElectricityCabinetBoxMapper;
import com.xiliulou.electricity.query.ElectricityCabinetBoxQuery;
import com.xiliulou.electricity.service.ElectricityBatteryService;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.utils.PageUtil;
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

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetBox queryByIdFromDB(Long id) {
        return this.electricityCabinetBoxMapper.queryById(id);
    }

    @Override
    public void batchInsertBoxByModelId(ElectricityCabinetModel electricityCabinetModel, Integer id) {
        if (Objects.nonNull(id)) {
            for (int i = 1; i <= electricityCabinetModel.getNum(); i++) {
                ElectricityCabinetBox electricityCabinetBox = new ElectricityCabinetBox();
                electricityCabinetBox.setElectricityCabinetId(id);
                electricityCabinetBox.setCellNo(String.valueOf(i));
                electricityCabinetBox.setCreateTime(System.currentTimeMillis());
                electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
                electricityCabinetBox.setDelFlag(ElectricityCabinetBox.DEL_NORMAL);
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
        Page page = PageUtil.getPage(electricityCabinetBoxQuery.getOffset(), electricityCabinetBoxQuery.getSize());

        electricityCabinetBoxMapper.queryList(page, electricityCabinetBoxQuery);
        if (ObjectUtil.isEmpty(page.getRecords())) {
            return R.ok();
        }
        List<ElectricityCabinetBoxVO> electricityCabinetBoxVOList = page.getRecords();
        List<ElectricityCabinetBoxVO> electricityCabinetBoxVOs=new ArrayList<>();

        if (ObjectUtil.isNotEmpty(electricityCabinetBoxVOList)) {
            for (ElectricityCabinetBoxVO electricityCabinetBoxVO:electricityCabinetBoxVOList) {
                ElectricityBattery electricityBattery = electricityBatteryService.queryById(electricityCabinetBoxVO.getElectricityBatteryId());
                if (Objects.nonNull(electricityBattery)) {
                    electricityCabinetBoxVO.setSn(electricityBattery.getSn());
                    electricityCabinetBoxVO.setPower(electricityBattery.getPower());
                }
                electricityCabinetBoxVOs.add(electricityCabinetBoxVO);
            }
        }
        page.setRecords(electricityCabinetBoxVOs);
        return R.ok(page);
    }

    @Override
    public R modify(ElectricityCabinetBox electricityCabinetBox) {
        electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
        electricityCabinetBoxMapper.update(electricityCabinetBox);
        return R.ok();
    }


    @Override
    public List<ElectricityCabinetBox> queryBoxByElectricityCabinetId(Integer id) {
        return electricityCabinetBoxMapper.selectList(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId, id)
                .eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL).eq(ElectricityCabinetBox::getUsableStatus,ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_USABLE));
    }

    @Override
    public List<ElectricityCabinetBox> queryNoElectricityBatteryBox(Integer id) {
        return electricityCabinetBoxMapper.selectList(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId, id)
                .eq(ElectricityCabinetBox::getStatus, ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY).eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL)
                .eq(ElectricityCabinetBox::getUsableStatus,ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_USABLE));
    }

    @Override
    public List<ElectricityCabinetBox> queryElectricityBatteryBox(Integer id, String cellNo) {
        List<ElectricityCabinetBox> electricityCabinetBoxList = electricityCabinetBoxMapper.selectList(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId, id)
                .eq(ElectricityCabinetBox::getStatus, ElectricityCabinetBox.STATUS_ELECTRICITY_BATTERY).eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL)
                .ne(Objects.nonNull(cellNo),ElectricityCabinetBox::getCellNo, cellNo).eq(ElectricityCabinetBox::getUsableStatus,ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_USABLE));
        List<ElectricityCabinetBox> electricityCabinetBoxes = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(electricityCabinetBoxList)) {
            electricityCabinetBoxList.parallelStream().forEach(e -> {
                ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(id);

                //是否满电
                if (Objects.nonNull(electricityCabinet)) {
                    ElectricityBattery electricityBattery = electricityBatteryService.queryById(e.getElectricityBatteryId());
                    if (Objects.nonNull(electricityBattery)) {
                        if (electricityBattery.getPower() >= electricityCabinet.getFullyCharged()) {

                            //该电池是否绑定用户
                            List<UserInfo> userInfoList=userInfoService.queryByBatterySn(electricityBattery.getSn());
                            if(ObjectUtil.isEmpty(userInfoList)){
                                electricityCabinetBoxes.add(e);
                            }
                        }
                    }
                }
            });
        }
        return electricityCabinetBoxes;
    }

    @Override
    public void modifyByCellNo(ElectricityCabinetBox electricityCabinetBox) {
        electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
        electricityCabinetBoxMapper.modifyByCellNo(electricityCabinetBox);
    }

    @Override
    public ElectricityCabinetBox queryByCellNo(Integer electricityCabinetId, String cellNo) {
        return electricityCabinetBoxMapper.selectOne(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId, electricityCabinetId)
                .eq(ElectricityCabinetBox::getCellNo, cellNo).eq(ElectricityCabinetBox::getDelFlag, ElectricityCabinetBox.DEL_NORMAL));
    }


}
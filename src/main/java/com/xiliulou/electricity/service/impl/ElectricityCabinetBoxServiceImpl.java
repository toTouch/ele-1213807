package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ElectricityCabinetBox;
import com.xiliulou.electricity.entity.ElectricityCabinetModel;
import com.xiliulou.electricity.mapper.ElectricityCabinetBoxMapper;
import com.xiliulou.electricity.query.ElectricityCabinetBoxQuery;
import com.xiliulou.electricity.service.ElectricityCabinetBoxService;
import com.xiliulou.electricity.vo.ElectricityCabinetBoxVO;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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


    /**
     * 新增数据
     *
     * @param electricityCabinetBox 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElectricityCabinetBox insert(ElectricityCabinetBox electricityCabinetBox) {
        this.electricityCabinetBoxMapper.insert(electricityCabinetBox);
        return electricityCabinetBox;
    }

    /**
     * 修改数据
     *
     * @param electricityCabinetBox 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElectricityCabinetBox electricityCabinetBox) {
       return this.electricityCabinetBoxMapper.update(electricityCabinetBox);
         
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.electricityCabinetBoxMapper.deleteById(id) > 0;
    }

    @Override
    public void batchInsertBoxByModelId(ElectricityCabinetModel electricityCabinetModel, Integer id) {
        for (int i=1;i<=electricityCabinetModel.getNum();i++) {
            ElectricityCabinetBox electricityCabinetBox = new ElectricityCabinetBox();
            electricityCabinetBox.setElectricityCabinetId(id);
            electricityCabinetBox.setUsableStatus(ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_USABLE);
            electricityCabinetBox.setStatus(ElectricityCabinetBox.STATUS_CLOSE_DOOR);
            electricityCabinetBox.setBoxStatus(ElectricityCabinetBox.STATUS_NO_ELECTRICITY_BATTERY);
            electricityCabinetBox.setCellNo(String.valueOf(i));
            electricityCabinetBox.setCreateTime(System.currentTimeMillis());
            electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
            electricityCabinetBox.setDelFlag(ElectricityCabinetBox.DEL_NORMAL);
            electricityCabinetBoxMapper.insertOne(electricityCabinetBox);
        }
    }

    @Override
    public void batchDeleteBoxByElectricityCabinetId(Integer id) {
        electricityCabinetBoxMapper.batchDeleteBoxByElectricityCabinetId(id);
    }

    @Override
    public R queryList(ElectricityCabinetBoxQuery electricityCabinetBoxQuery) {
        List<ElectricityCabinetBoxVO> electricityCabinetBoxVOList=this.electricityCabinetBoxMapper.queryList(electricityCabinetBoxQuery);
        List<ElectricityCabinetBoxVO> electricityCabinetBoxVOS=new ArrayList<>();
        if(ObjectUtil.isNotEmpty(electricityCabinetBoxVOList)) {
            List<ElectricityCabinetBoxVO> finalElectricityCabinetBoxVOS = electricityCabinetBoxVOS;
            electricityCabinetBoxVOList.parallelStream().forEach(e -> {
                //TODO 查电池信息
                finalElectricityCabinetBoxVOS.add(e);
            });
            electricityCabinetBoxVOS=finalElectricityCabinetBoxVOS.stream().sorted(Comparator.comparing(ElectricityCabinetBoxVO::getId).reversed()).collect(Collectors.toList());
        }
        return R.ok(electricityCabinetBoxVOS);
    }

    @Override
    public R modify(ElectricityCabinetBox electricityCabinetBox) {
        electricityCabinetBox.setUpdateTime(System.currentTimeMillis());
        electricityCabinetBoxMapper.update(electricityCabinetBox);
        return R.ok();
    }

    @Override
    public R modifyByElectricityCabinetId(ElectricityCabinetBox electricityCabinetBox) {
        electricityCabinetBoxMapper.modifyByElectricityCabinetId(electricityCabinetBox);
        return R.ok();
    }

    @Override
    public List<ElectricityCabinetBox> queryBoxByElectricityCabinetId(Integer id) {
        return electricityCabinetBoxMapper.selectList(Wrappers.<ElectricityCabinetBox>lambdaQuery().eq(ElectricityCabinetBox::getElectricityCabinetId,id));
    }
}
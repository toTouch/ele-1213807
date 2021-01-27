package com.xiliulou.electricity.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.ElectricityCabinetBind;
import com.xiliulou.electricity.mapper.ElectricityCabinetBindMapper;
import com.xiliulou.electricity.service.ElectricityCabinetBindService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

/**
 * (ElectricityCabinetBind)表服务实现类
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@Service("electricityCabinetBindService")
@Slf4j
public class ElectricityCabinetBindServiceImpl implements ElectricityCabinetBindService {
    @Resource
    ElectricityCabinetBindMapper electricityCabinetBindMapper;
    @Override
    public void deleteByUid(Long uid) {
        electricityCabinetBindMapper.deleteByUid(uid);
    }

    @Override
    public void insert(ElectricityCabinetBind electricityCabinetBind) {
        electricityCabinetBindMapper.insert(electricityCabinetBind);
    }

    @Override
    public List<ElectricityCabinetBind> queryElectricityCabinetList(Long uid) {
        return electricityCabinetBindMapper.selectList(new LambdaQueryWrapper<ElectricityCabinetBind>().eq(ElectricityCabinetBind::getUid,uid));
    }

}
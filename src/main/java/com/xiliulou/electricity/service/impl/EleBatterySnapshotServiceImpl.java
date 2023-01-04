package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleBatterySnapshot;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.mapper.EleBatterySnapshotMapper;
import com.xiliulou.electricity.service.EleBatterySnapshotService;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
/**
 * (EleBatterySnapshot)表服务实现类
 *
 * @author makejava
 * @since 2023-01-04 09:21:26
 */
@Service("eleBatterySnapshotService")
@Slf4j
public class EleBatterySnapshotServiceImpl implements EleBatterySnapshotService {
    @Resource
    private EleBatterySnapshotMapper eleBatterySnapshotMapper;
    @Autowired
    private ElectricityCabinetService electricityCabinetService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleBatterySnapshot queryByIdFromDB(Long id) {
        return this.eleBatterySnapshotMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public  EleBatterySnapshot queryByIdFromCache(Long id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<EleBatterySnapshot> queryAllByLimit(int offset, int limit) {
        return this.eleBatterySnapshotMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param eleBatterySnapshot 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleBatterySnapshot insert(EleBatterySnapshot eleBatterySnapshot) {
        this.eleBatterySnapshotMapper.insertOne(eleBatterySnapshot);
        return eleBatterySnapshot;
    }

    /**
     * 修改数据
     *
     * @param eleBatterySnapshot 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleBatterySnapshot eleBatterySnapshot) {
       return this.eleBatterySnapshotMapper.update(eleBatterySnapshot);
         
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
        return this.eleBatterySnapshotMapper.deleteById(id) > 0;
    }
    
    @Override
    public Pair<Boolean, Object> queryBatterySnapshot(Integer eId, Integer size, Integer offset, Long startTime,
            Long endTime) {
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(eId);
        if(Objects.isNull(electricityCabinet)) {
            return Pair.of(false,"柜机不存在");
        }
        
        if(!TenantContextHolder.getTenantId().equals(electricityCabinet.getTenantId())) {
            return Pair.of(true,null);
        }
        
        
        return Pair.of(true,eleBatterySnapshotMapper.queryBatterySnapshot(eId,size,offset,startTime,endTime));
    }
}

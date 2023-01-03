package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.BatteryTrackRecord;
import com.xiliulou.electricity.mapper.BatteryTrackRecordMapper;
import com.xiliulou.electricity.service.BatteryTrackRecordService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
/**
 * (BatteryTrackRecord)表服务实现类
 *
 * @author makejava
 * @since 2023-01-03 16:24:37
 */
@Service("batteryTrackRecordService")
@Slf4j
public class BatteryTrackRecordServiceImpl implements BatteryTrackRecordService {
    @Resource
    private BatteryTrackRecordMapper batteryTrackRecordMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BatteryTrackRecord queryByIdFromDB(Long id) {
        return this.batteryTrackRecordMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public  BatteryTrackRecord queryByIdFromCache(Long id) {
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
    public List<BatteryTrackRecord> queryAllByLimit(int offset, int limit) {
        return this.batteryTrackRecordMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param batteryTrackRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatteryTrackRecord insert(BatteryTrackRecord batteryTrackRecord) {
        this.batteryTrackRecordMapper.insertOne(batteryTrackRecord);
        return batteryTrackRecord;
    }

    /**
     * 修改数据
     *
     * @param batteryTrackRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(BatteryTrackRecord batteryTrackRecord) {
       return this.batteryTrackRecordMapper.update(batteryTrackRecord);
         
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
        return this.batteryTrackRecordMapper.deleteById(id) > 0;
    }
}

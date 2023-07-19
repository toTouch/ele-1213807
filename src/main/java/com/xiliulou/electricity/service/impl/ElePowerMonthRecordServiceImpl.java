package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ElePowerMonthRecord;
import com.xiliulou.electricity.mapper.ElePowerMonthRecordMapper;
import com.xiliulou.electricity.query.PowerMonthStatisticsQuery;
import com.xiliulou.electricity.service.ElePowerMonthRecordService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
/**
 * (ElePowerMonthRecord)表服务实现类
 *
 * @author makejava
 * @since 2023-07-18 10:20:44
 */
@Service("elePowerMonthRecordService")
@Slf4j
public class ElePowerMonthRecordServiceImpl implements ElePowerMonthRecordService {
    @Resource
    private ElePowerMonthRecordMapper elePowerMonthRecordMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ElePowerMonthRecord queryByIdFromDB(Long id) {
        return this.elePowerMonthRecordMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public  ElePowerMonthRecord queryByIdFromCache(Long id) {
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
    public List<ElePowerMonthRecord> queryAllByLimit(int offset, int limit) {
        return this.elePowerMonthRecordMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param elePowerMonthRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ElePowerMonthRecord insert(ElePowerMonthRecord elePowerMonthRecord) {
        this.elePowerMonthRecordMapper.insertOne(elePowerMonthRecord);
        return elePowerMonthRecord;
    }

    /**
     * 修改数据
     *
     * @param elePowerMonthRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElePowerMonthRecord elePowerMonthRecord) {
       return this.elePowerMonthRecordMapper.update(elePowerMonthRecord);
         
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
        return this.elePowerMonthRecordMapper.deleteById(id) > 0;
    }

    @Override
    public Pair<Boolean, Object> queryMonthStatistics(PowerMonthStatisticsQuery query) {
        List<ElePowerMonthRecord> list = this.elePowerMonthRecordMapper.queryPartAttrList(query);
        return null;
    }
}

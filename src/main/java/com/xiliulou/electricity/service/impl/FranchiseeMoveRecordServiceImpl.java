package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.FranchiseeMoveRecord;
import com.xiliulou.electricity.mapper.FranchiseeMoveRecordMapper;
import com.xiliulou.electricity.service.FranchiseeMoveRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 加盟商迁移记录(FranchiseeMoveRecord)表服务实现类
 *
 * @author zzlong
 * @since 2023-02-07 17:12:52
 */
@Service("franchiseeMoveRecordService")
@Slf4j
public class FranchiseeMoveRecordServiceImpl implements FranchiseeMoveRecordService {
    @Autowired
    private FranchiseeMoveRecordMapper franchiseeMoveRecordMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public FranchiseeMoveRecord selectByIdFromDB(Integer id) {
        return this.franchiseeMoveRecordMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public FranchiseeMoveRecord selectByIdFromCache(Integer id) {
        return null;
    }


    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<FranchiseeMoveRecord> selectByPage(int offset, int limit) {
        return this.franchiseeMoveRecordMapper.selectByPage(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param franchiseeMoveRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FranchiseeMoveRecord insert(FranchiseeMoveRecord franchiseeMoveRecord) {
        this.franchiseeMoveRecordMapper.insertOne(franchiseeMoveRecord);
        return franchiseeMoveRecord;
    }

    /**
     * 修改数据
     *
     * @param franchiseeMoveRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(FranchiseeMoveRecord franchiseeMoveRecord) {
        return this.franchiseeMoveRecordMapper.update(franchiseeMoveRecord);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Integer id) {
        return this.franchiseeMoveRecordMapper.deleteById(id) > 0;
    }
}

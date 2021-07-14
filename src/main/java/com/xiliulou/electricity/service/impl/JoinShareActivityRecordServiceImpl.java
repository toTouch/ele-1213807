package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.JoinShareActivityRecord;
import com.xiliulou.electricity.mapper.JoinShareActivityRecordMapper;
import com.xiliulou.electricity.service.JoinShareActivityRecordService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
/**
 * 参与邀请活动记录(JoinShareActivityRecord)表服务实现类
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@Service("joinShareActivityRecordService")
@Slf4j
public class JoinShareActivityRecordServiceImpl implements JoinShareActivityRecordService {
    @Resource
    private JoinShareActivityRecordMapper joinShareActivityRecordMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public JoinShareActivityRecord queryByIdFromDB(Long id) {
        return this.joinShareActivityRecordMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public  JoinShareActivityRecord queryByIdFromCache(Long id) {
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
    public List<JoinShareActivityRecord> queryAllByLimit(int offset, int limit) {
        return this.joinShareActivityRecordMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param joinShareActivityRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public JoinShareActivityRecord insert(JoinShareActivityRecord joinShareActivityRecord) {
        this.joinShareActivityRecordMapper.insertOne(joinShareActivityRecord);
        return joinShareActivityRecord;
    }

    /**
     * 修改数据
     *
     * @param joinShareActivityRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(JoinShareActivityRecord joinShareActivityRecord) {
       return this.joinShareActivityRecordMapper.update(joinShareActivityRecord);
         
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
        return this.joinShareActivityRecordMapper.deleteById(id) > 0;
    }
}
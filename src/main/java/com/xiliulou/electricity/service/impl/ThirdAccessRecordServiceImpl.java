package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.ThirdAccessRecord;
import com.xiliulou.electricity.mapper.ThirdAccessRecordMapper;
import com.xiliulou.electricity.query.ThirdAccessRecordQuery;
import com.xiliulou.electricity.service.ThirdAccessRecordService;
import com.xiliulou.electricity.vo.ThirdAccessRecordVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: Miss.Li
 * @Date: 2021/11/5 14:04
 * @Description:
 */
@Service("thirdAccessRecordService")
@Slf4j
public class ThirdAccessRecordServiceImpl implements ThirdAccessRecordService {
    @Resource
    private ThirdAccessRecordMapper thirdAccessRecordMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ThirdAccessRecord queryByIdFromDB(Long id) {
        return this.thirdAccessRecordMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ThirdAccessRecord queryByIdFromCache(Long id) {
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
    public List<ThirdAccessRecord> queryAllByLimit(int offset, int limit) {
        return this.thirdAccessRecordMapper.queryAllByLimit(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param thirdAccessRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ThirdAccessRecord insert(ThirdAccessRecord thirdAccessRecord) {
        this.thirdAccessRecordMapper.insertOne(thirdAccessRecord);
        return thirdAccessRecord;
    }

    /**
     * 修改数据
     *
     * @param thirdAccessRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ThirdAccessRecord thirdAccessRecord) {
        return this.thirdAccessRecordMapper.update(thirdAccessRecord);

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
        return this.thirdAccessRecordMapper.deleteById(id) > 0;
    }

    @Override
    public Pair<Boolean, Object> queryList(ThirdAccessRecordQuery query) {
        return Pair.of(true, this.thirdAccessRecordMapper.queryList(query));
    }

    @Override
    public List<ThirdAccessRecordVo> queryListByRequestId(String requestId) {
        return thirdAccessRecordMapper.queryListByRequestId(requestId);
    }
}

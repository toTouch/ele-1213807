package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.ThirdAccessRecord;
import com.xiliulou.electricity.query.ThirdAccessRecordQuery;
import com.xiliulou.electricity.vo.ThirdAccessRecordVo;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * @author: Miss.Li
 * @Date: 2021/11/5 14:04
 * @Description:
 */
public interface ThirdAccessRecordService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    ThirdAccessRecord queryByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    ThirdAccessRecord queryByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ThirdAccessRecord> queryAllByLimit(int offset, int limit);

    /**
     * 新增数据
     *
     * @param thirdAccessRecord 实例对象
     * @return 实例对象
     */
    ThirdAccessRecord insert(ThirdAccessRecord thirdAccessRecord);

    /**
     * 修改数据
     *
     * @param thirdAccessRecord 实例对象
     * @return 实例对象
     */
    Integer update(ThirdAccessRecord thirdAccessRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    Pair<Boolean, Object> queryList(ThirdAccessRecordQuery query);

    List<ThirdAccessRecordVo> queryListByRequestId(String requestId);
}

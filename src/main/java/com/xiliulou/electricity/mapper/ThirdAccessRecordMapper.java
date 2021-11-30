package com.xiliulou.electricity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.entity.ThirdAccessRecord;
import com.xiliulou.electricity.query.ThirdAccessRecordQuery;
import com.xiliulou.electricity.vo.ThirdAccessRecordVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: Miss.Li
 * @Date: 2021/11/5 14:04
 * @Description:
 */
public interface ThirdAccessRecordMapper extends BaseMapper<ThirdAccessRecord> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    ThirdAccessRecord queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<ThirdAccessRecord> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param thirdAccessRecord 实例对象
     * @return 对象列表
     */
    List<ThirdAccessRecord> queryAll(ThirdAccessRecord thirdAccessRecord);

    /**
     * 新增数据
     *
     * @param thirdAccessRecord 实例对象
     * @return 影响行数
     */
    int insertOne(ThirdAccessRecord thirdAccessRecord);

    /**
     * 修改数据
     *
     * @param thirdAccessRecord 实例对象
     * @return 影响行数
     */
    int update(ThirdAccessRecord thirdAccessRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    List<ThirdAccessRecord> queryList(ThirdAccessRecordQuery query);

    List<ThirdAccessRecordVo> queryListByRequestId(String requestId);
}

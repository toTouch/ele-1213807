package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.DivisionAccountRecord;

import java.util.List;

import com.xiliulou.electricity.query.DivisionAccountRecordQuery;
import com.xiliulou.electricity.vo.DivisionAccountRecordStatisticVO;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (DivisionAccountRecord)表数据库访问层
 *
 * @author zzlong
 * @since 2023-04-24 16:23:44
 */
public interface DivisionAccountRecordMapper extends BaseMapper<DivisionAccountRecord> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    DivisionAccountRecord queryById(Long id);

    /**
     * 查询指定行数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<DivisionAccountRecord> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);


    /**
     * 通过实体作为筛选条件查询
     *
     * @param divisionAccountRecord 实例对象
     * @return 对象列表
     */
    List<DivisionAccountRecord> queryAll(DivisionAccountRecord divisionAccountRecord);

    /**
     * 新增数据
     *
     * @param divisionAccountRecord 实例对象
     * @return 影响行数
     */
    int insertOne(DivisionAccountRecord divisionAccountRecord);

    /**
     * 修改数据
     *
     * @param divisionAccountRecord 实例对象
     * @return 影响行数
     */
    int update(DivisionAccountRecord divisionAccountRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

    Integer selectByPageCount(DivisionAccountRecordQuery query);

    List<DivisionAccountRecord> selectByPage(DivisionAccountRecordQuery query);

    List<DivisionAccountRecordStatisticVO> selectStatisticByPage(DivisionAccountRecordQuery query);

    Integer selectStatisticByPageCount(DivisionAccountRecordQuery query);
    
    DivisionAccountRecord selectByOrderId(@Param("orderId") String orderId);

    List<DivisionAccountRecord> selectDAFreezeStatusRecordsByTime(@Param("time") Long time);

    int updateDAStatus(DivisionAccountRecord divisionAccountRecord);
}

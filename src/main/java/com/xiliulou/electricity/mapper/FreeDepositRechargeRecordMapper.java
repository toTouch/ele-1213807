package com.xiliulou.electricity.mapper;

import com.xiliulou.electricity.entity.FreeDepositRechargeRecord;

import java.util.List;

import com.xiliulou.electricity.query.FreeDepositRechargeRecordQuery;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * (FreeDepositRechargeRecord)表数据库访问层
 *
 * @author zzlong
 * @since 2023-02-20 15:46:57
 */
public interface FreeDepositRechargeRecordMapper extends BaseMapper<FreeDepositRechargeRecord> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    FreeDepositRechargeRecord selectById(Long id);

    /**
     * 查询指定行数据
     *
     * @return 对象列表
     */
    List<FreeDepositRechargeRecord> selectByPage(FreeDepositRechargeRecordQuery query);

    Integer selectByPageCount(FreeDepositRechargeRecordQuery query);

    /**
     * 通过实体作为筛选条件查询
     *
     * @param freeDepositRechargeRecord 实例对象
     * @return 对象列表
     */
    List<FreeDepositRechargeRecord> selectByQuery(FreeDepositRechargeRecord freeDepositRechargeRecord);

    /**
     * 新增数据
     *
     * @param freeDepositRechargeRecord 实例对象
     * @return 影响行数
     */
    int insertOne(FreeDepositRechargeRecord freeDepositRechargeRecord);

    /**
     * 修改数据
     *
     * @param freeDepositRechargeRecord 实例对象
     * @return 影响行数
     */
    int update(FreeDepositRechargeRecord freeDepositRechargeRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);

}

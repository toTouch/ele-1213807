package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.FreeDepositRechargeRecord;
import com.xiliulou.electricity.query.FreeDepositRechargeRecordQuery;
import com.xiliulou.electricity.vo.FreeDepositRechargeRecordVO;

import java.util.List;

/**
 * (FreeDepositRechargeRecord)表服务接口
 *
 * @author zzlong
 * @since 2023-02-20 15:46:57
 */
public interface FreeDepositRechargeRecordService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    FreeDepositRechargeRecord selectByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    FreeDepositRechargeRecord selectByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @return 对象列表
     */
    List<FreeDepositRechargeRecordVO> selectByPage(FreeDepositRechargeRecordQuery query);

    Integer selectByPageCount(FreeDepositRechargeRecordQuery query);

    /**
     * 新增数据
     *
     * @param freeDepositRechargeRecord 实例对象
     * @return 实例对象
     */
    FreeDepositRechargeRecord insert(FreeDepositRechargeRecord freeDepositRechargeRecord);

    /**
     * 修改数据
     *
     * @param freeDepositRechargeRecord 实例对象
     * @return 实例对象
     */
    Integer update(FreeDepositRechargeRecord freeDepositRechargeRecord);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

}

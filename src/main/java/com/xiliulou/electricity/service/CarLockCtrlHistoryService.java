package com.xiliulou.electricity.service;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.CarLockCtrlHistory;
import com.xiliulou.electricity.query.CarLockCtrlHistoryQuery;

import java.util.List;

/**
 * (CarLockCtrlHistory)表服务接口
 *
 * @author Hardy
 * @since 2023-04-04 16:22:28
 */
public interface CarLockCtrlHistoryService {
    
    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    CarLockCtrlHistory queryByIdFromDB(Long id);
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    CarLockCtrlHistory queryByIdFromCache(Long id);
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<CarLockCtrlHistory> queryAllByLimit(int offset, int limit);
    
    /**
     * 新增数据
     *
     * @param carLockCtrlHistory 实例对象
     * @return 实例对象
     */
    CarLockCtrlHistory insert(CarLockCtrlHistory carLockCtrlHistory);
    
    /**
     * 根据更换手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone);
    
    /**
     * 修改数据
     *
     * @param carLockCtrlHistory 实例对象
     * @return 实例对象
     */
    Integer update(CarLockCtrlHistory carLockCtrlHistory);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);
    
    R queryList(CarLockCtrlHistoryQuery query);
    
    R queryCount(CarLockCtrlHistoryQuery query);
}

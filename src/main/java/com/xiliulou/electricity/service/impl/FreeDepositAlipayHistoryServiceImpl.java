package com.xiliulou.electricity.service.impl;

import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.FreeDepositAlipayHistory;
import com.xiliulou.electricity.mapper.FreeDepositAlipayHistoryMapper;
import com.xiliulou.electricity.query.FreeDepositAlipayHistoryQuery;
import com.xiliulou.electricity.service.FreeDepositAlipayHistoryService;
import com.xiliulou.electricity.vo.FreeDepositAlipayHistoryVo;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (FreeDepositAlipayHistory)表服务实现类
 *
 * @author zgw
 * @since 2023-04-13 09:13:01
 */
@Service("freeDepositAlipayHistoryService")
@Slf4j
public class FreeDepositAlipayHistoryServiceImpl implements FreeDepositAlipayHistoryService {
    
    @Resource
    private FreeDepositAlipayHistoryMapper freeDepositAlipayHistoryMapper;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public FreeDepositAlipayHistory queryByIdFromDB(Long id) {
        return this.freeDepositAlipayHistoryMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public FreeDepositAlipayHistory queryByIdFromCache(Long id) {
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
    public List<FreeDepositAlipayHistory> queryAllByLimit(int offset, int limit) {
        return this.freeDepositAlipayHistoryMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param freeDepositAlipayHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FreeDepositAlipayHistory insert(FreeDepositAlipayHistory freeDepositAlipayHistory) {
        this.freeDepositAlipayHistoryMapper.insert(freeDepositAlipayHistory);
        return freeDepositAlipayHistory;
    }
    
    /**
     * 修改数据
     *
     * @param freeDepositAlipayHistory 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(FreeDepositAlipayHistory freeDepositAlipayHistory) {
        return this.freeDepositAlipayHistoryMapper.update(freeDepositAlipayHistory);
        
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
        return this.freeDepositAlipayHistoryMapper.deleteById(id) > 0;
    }
    
    @Slave
    @Override
    public R queryList(FreeDepositAlipayHistoryQuery query) {
        List<FreeDepositAlipayHistoryVo> voList = freeDepositAlipayHistoryMapper.queryList(query);
        return R.ok(voList);
    }
    
    @Slave
    @Override
    public R queryCount(FreeDepositAlipayHistoryQuery query) {
        Long count = freeDepositAlipayHistoryMapper.queryCount(query);
        return R.ok(count);
    }
    
    @Slave
    @Override
    public FreeDepositAlipayHistory queryByOrderId(String orderId) {
        return freeDepositAlipayHistoryMapper.queryByOrderId(orderId);
    }
    
    @Override
    public Integer updateByOrderId(FreeDepositAlipayHistory freeDepositAlipayHistory) {
        return freeDepositAlipayHistoryMapper.updateByOrderId(freeDepositAlipayHistory);
    }
    
    /**
     * 更新用户手机号
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @param newPhone 新号码
     * @return 影响行数
     */
    @Override
    public Integer updatePhoneByUid(Integer tenantId, Long uid, String newPhone) {
        return freeDepositAlipayHistoryMapper.updatePhoneByUid(tenantId, uid, newPhone);
    }
}

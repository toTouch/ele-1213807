package com.xiliulou.electricity.service.impl;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.DivisionAccountBatteryMembercard;
import com.xiliulou.electricity.mapper.DivisionAccountBatteryMembercardMapper;
import com.xiliulou.electricity.service.DivisionAccountBatteryMembercardService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (DivisionAccountBatteryMembercard)表服务实现类
 *
 * @author zzlong
 * @since 2023-04-23 17:59:54
 */
@Service("divisionAccountBatteryMembercardService")
@Slf4j
public class DivisionAccountBatteryMembercardServiceImpl implements DivisionAccountBatteryMembercardService {
    @Resource
    private DivisionAccountBatteryMembercardMapper divisionAccountBatteryMembercardMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public DivisionAccountBatteryMembercard queryByIdFromDB(Long id) {
        return this.divisionAccountBatteryMembercardMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public DivisionAccountBatteryMembercard queryByIdFromCache(Long id) {
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
    public List<DivisionAccountBatteryMembercard> queryAllByLimit(int offset, int limit) {
        return this.divisionAccountBatteryMembercardMapper.queryAllByLimit(offset, limit);
    }

    @Slave
    @Override
    public List<Long> selectByDivisionAccountConfigId(Long id) {
        return this.divisionAccountBatteryMembercardMapper.selectByDivisionAccountConfigId(id);
    }

    @Override
    public List<DivisionAccountBatteryMembercard> selectMemberCardsByDAConfigId(Long divisionAccountId) {
        return this.divisionAccountBatteryMembercardMapper.selectMemberCardsByDAConfigId(divisionAccountId);
    }

    @Slave
    @Override
    public Long selectByBatteryMembercardId(Long membercardId) {
        return this.divisionAccountBatteryMembercardMapper.selectByBatteryMembercardId(membercardId);
    }

    @Slave
    @Override
    public List<Long> selectByTenantId(Integer tenantId) {
        return this.divisionAccountBatteryMembercardMapper.selectByTenantId(tenantId);
    }

    @Override
    public Integer deleteByDivisionAccountId(Long id) {
        return  this.divisionAccountBatteryMembercardMapper.deleteByDivisionAccountId(id);
    }

    /**
     * 新增数据
     *
     * @param divisionAccountBatteryMembercard 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DivisionAccountBatteryMembercard insert(DivisionAccountBatteryMembercard divisionAccountBatteryMembercard) {
        this.divisionAccountBatteryMembercardMapper.insertOne(divisionAccountBatteryMembercard);
        return divisionAccountBatteryMembercard;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchInsert(List<DivisionAccountBatteryMembercard> divisionAccountBatteryMembercardList) {
        return this.divisionAccountBatteryMembercardMapper.batchInsert(divisionAccountBatteryMembercardList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchInsertMemberCards(List<DivisionAccountBatteryMembercard> divisionAccountBatteryMembercardList) {
        return this.divisionAccountBatteryMembercardMapper.batchInsertMemberCards(divisionAccountBatteryMembercardList);
    }

    /**
     * 修改数据
     *
     * @param divisionAccountBatteryMembercard 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(DivisionAccountBatteryMembercard divisionAccountBatteryMembercard) {
        return this.divisionAccountBatteryMembercardMapper.update(divisionAccountBatteryMembercard);

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
        return this.divisionAccountBatteryMembercardMapper.deleteById(id) > 0;
    }
}

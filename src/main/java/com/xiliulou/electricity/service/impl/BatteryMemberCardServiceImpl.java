package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.mapper.BatteryMemberCardMapper;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (BatteryMemberCard)表服务实现类
 *
 * @author zzlong
 * @since 2023-07-07 14:06:31
 */
@Service("batteryMemberCardService")
@Slf4j
public class BatteryMemberCardServiceImpl implements BatteryMemberCardService {
    @Resource
    private BatteryMemberCardMapper batteryMemberCardMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BatteryMemberCard queryByIdFromDB(Integer id) {
        return this.batteryMemberCardMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BatteryMemberCard queryByIdFromCache(Integer id) {
        return null;
    }

    /**
     * 修改数据
     *
     * @param batteryMemberCard 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(BatteryMemberCard batteryMemberCard) {
        return this.batteryMemberCardMapper.update(batteryMemberCard);

    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Integer id) {
        return this.batteryMemberCardMapper.deleteById(id) > 0;
    }
}

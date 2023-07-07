package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.MemberCardBatteryType;
import com.xiliulou.electricity.mapper.MemberCardBatteryTypeMapper;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * (MemberCardBatteryType)表服务实现类
 *
 * @author zzlong
 * @since 2023-07-07 14:07:42
 */
@Service("memberCardBatteryTypeService")
@Slf4j
public class MemberCardBatteryTypeServiceImpl implements MemberCardBatteryTypeService {
    @Resource
    private MemberCardBatteryTypeMapper memberCardBatteryTypeMapper;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public MemberCardBatteryType queryByIdFromDB(Long id) {
        return this.memberCardBatteryTypeMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public MemberCardBatteryType queryByIdFromCache(Long id) {
        return null;
    }

    /**
     * 修改数据
     *
     * @param memberCardBatteryType 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(MemberCardBatteryType memberCardBatteryType) {
        return this.memberCardBatteryTypeMapper.update(memberCardBatteryType);

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
        return this.memberCardBatteryTypeMapper.deleteById(id) > 0;
    }
}

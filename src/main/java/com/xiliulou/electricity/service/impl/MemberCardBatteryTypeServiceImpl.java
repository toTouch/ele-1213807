package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.MemberCardBatteryType;
import com.xiliulou.electricity.entity.UserBatteryType;
import com.xiliulou.electricity.mapper.MemberCardBatteryTypeMapper;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    
    @Qualifier("redisService")
    @Autowired
    private RedisService redisService;
    
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer batchInsert(List<MemberCardBatteryType> memberCardBatteryTypeList) {
        return this.memberCardBatteryTypeMapper.batchInsert(memberCardBatteryTypeList);
    }

    @Override
    public List<String> selectBatteryTypeByMid(Long mid) {
        return this.memberCardBatteryTypeMapper.selectBatteryTypeByMid(mid);
    }
    
    @Override
    public List<String> checkBatteryTypeWithMemberCard(Long uid, String batteryType, BatteryMemberCard memberCard) {
        // 用户当前套餐不分型号，不作处理
        List<String> batteryTypesWithCard = selectBatteryTypeByMid(memberCard.getId());
        if (CollectionUtils.isEmpty(batteryTypesWithCard)) {
            return List.of();
        }
        
        // 用户当前绑定的电池也就是要还的电池和当前套餐匹配，不作处理
        if (batteryTypesWithCard.contains(batteryType)) {
            return List.of();
        }
        
        // 不匹配时，从缓存内获取旧套餐的电池型号
        return redisService.getWithList(String.format(CacheConstant.BATTERY_MEMBER_CARD_TRANSFORM, uid), String.class);
    }
}

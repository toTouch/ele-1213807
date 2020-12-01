package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.mapper.ElectricityMemberCardMapper;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @program: XILIULOU
 * @description:
 * @author: Mr.YG
 * @create: 2020-12-01 15:28
 **/
@Service
@Slf4j
public class ElectricityMemberCardServiceImpl extends ServiceImpl<ElectricityMemberCardMapper, ElectricityMemberCard> implements ElectricityMemberCardService {

    @Autowired
    RedisService redisService;

    /**
     * 新增卡包
     *
     * @param electricityMemberCard
     * @return
     */
    @Override
    public R saveElectricityMemberCard(ElectricityMemberCard electricityMemberCard) {
        electricityMemberCard.setCreateTime(System.currentTimeMillis());
        electricityMemberCard.setUpdateTime(System.currentTimeMillis());
        electricityMemberCard.setStatus(ElectricityMemberCard.STATUS_UN_USEABLE);
        return R.ok(baseMapper.insert(electricityMemberCard));
    }

    /**
     * 修改月卡
     *
     * @param electricityMemberCard
     * @return
     */
    @Override
    public R updateElectricityMemberCard(ElectricityMemberCard electricityMemberCard) {
        electricityMemberCard.setUpdateTime(System.currentTimeMillis());
        return R.ok(baseMapper.updateById(electricityMemberCard));
    }

    /**
     * @param id
     * @return
     */
    @Override
    public R deleteElectricityMemberCard(Integer id) {
        baseMapper.deleteById(id);
        deleteElectricityMemberCardCache(id);

        return R.ok();
    }

    /**
     * 删除套餐缓存
     *
     * @param id
     */
    @Override
    public void deleteElectricityMemberCardCache(Integer id) {
        redisService.deleteKeys(ElectricityCabinetConstant.CACHE_MEMBER_CARD + id);
    }

    /**
     * 分页
     *
     * @param offset
     * @param size
     * @param agentId
     * @return
     */
    @Override
    public R getElectricityMemberCardPage(Long offset, Long size, Integer agentId) {
        return R.ok(baseMapper.getElectricityMemberCardPage(offset, size, agentId));
    }

    /**
     * 获取套餐
     *
     * @param id
     * @return
     */
    @Override
    public ElectricityMemberCard getElectricityMemberCard(Integer id) {
        ElectricityMemberCard electricityMemberCard = null;
        electricityMemberCard = redisService.getWithHash(ElectricityCabinetConstant.CACHE_MEMBER_CARD + id, ElectricityMemberCard.class);
        if (Objects.isNull(electricityMemberCard)) {
            electricityMemberCard = baseMapper.selectById(id);
            if (Objects.nonNull(electricityMemberCard)) {
                redisService.saveWithHash(ElectricityCabinetConstant.CACHE_MEMBER_CARD + id, electricityMemberCard);
            }
        }
        return electricityMemberCard;
    }
}

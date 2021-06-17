package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.ElectricityMemberCardMapper;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.utils.PageUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

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

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    FranchiseeService franchiseeService;

    @Autowired
    UserService userService;

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
        if(Objects.equals(electricityMemberCard.getLimitCount(),ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)){
            electricityMemberCard.setMaxUseCount(ElectricityMemberCard.UN_LIMITED_COUNT);
        }
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
        if(Objects.nonNull(electricityMemberCard.getLimitCount())) {
            if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                electricityMemberCard.setMaxUseCount(ElectricityMemberCard.UN_LIMITED_COUNT);
            }
        }
        baseMapper.updateById(electricityMemberCard);
        redisService.saveWithHash(ElectricityCabinetConstant.CACHE_MEMBER_CARD + electricityMemberCard.getId(), electricityMemberCard);
        return R.ok();
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
        redisService.delete(ElectricityCabinetConstant.CACHE_MEMBER_CARD + id);
    }

    /**
     * 分页
     *
     * @param offset
     * @param size
     * @return
     */
    @Override
    @DS("slave_1")
    public R getElectricityMemberCardPage(Long offset, Long size, Integer status, Integer type) {

        Page page = PageUtil.getPage(offset, size);
        return R.ok(baseMapper.getElectricityMemberCardPage(page, offset, size, status, type));
    }

    @Override
    public R queryElectricityMemberCard(Long offset, Long size) {
        //用户信息
        Long uid = SecurityUtils.getUid();
        if (Objects.isNull(uid)) {
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        User user = userService.queryByUidFromCache(uid);
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user! userId:{}", uid);
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //2.判断用户
        UserInfo userInfo = userInfoService.queryByUid(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELECTRICITY  ERROR! not found user,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELECTRICITY  ERROR! user is unusable!uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
            log.error("ELECTRICITY  ERROR! not auth! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //3、查出套餐
        //根据用户cid找到对应的加盟商
        Franchisee franchisee = franchiseeService.queryByCid(user.getCid());
        if (Objects.isNull(franchisee)) {
            log.error("ELECTRICITY  ERROR! not found franchisee ! cid:{} ", user.getCid());
            //麒迹 未找到加盟商默认郑州，郑州也找不到再提示找不到 其余客服需要换  TODO
            franchisee = franchiseeService.queryByCid(147);
            if (Objects.isNull(franchisee)) {
                return R.fail("ELECTRICITY.0038", "未找到加盟商");
            }
        }
        //查找加盟商下的可用套餐
        Page page = PageUtil.getPage(offset, size);
        return R.ok(baseMapper.queryElectricityMemberCard(page,offset,size,franchisee.getId()));
    }

    @Override
    public List<ElectricityMemberCard> queryByFranchisee(Integer id) {
        return null;
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

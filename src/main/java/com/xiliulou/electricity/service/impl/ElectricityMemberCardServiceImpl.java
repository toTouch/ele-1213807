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
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.PageUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        //校验参数
        if(Objects.equals(electricityMemberCard.getLimitCount(),ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)){
            electricityMemberCard.setMaxUseCount(ElectricityMemberCard.UN_LIMITED_COUNT);
        }

        //填充参数
        electricityMemberCard.setCreateTime(System.currentTimeMillis());
        electricityMemberCard.setUpdateTime(System.currentTimeMillis());
        electricityMemberCard.setStatus(ElectricityMemberCard.STATUS_UN_USEABLE);
        electricityMemberCard.setTenantId(tenantId);
        electricityMemberCard.setDelFlag(ElectricityMemberCard.DEL_DEL);

        Integer insert=baseMapper.insert(electricityMemberCard);
        DbUtils.dbOperateSuccessThen(insert, () -> {
            //新增缓存
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_MEMBER_CARD + electricityMemberCard.getId(), electricityMemberCard);
            return null;
        });

        if (insert > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
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

        Integer update=baseMapper.updateById(electricityMemberCard);

        DbUtils.dbOperateSuccessThen(update, () -> {
            //先删再改
            redisService.delete(ElectricityCabinetConstant.CACHE_MEMBER_CARD + electricityMemberCard.getId());
            redisService.saveWithHash(ElectricityCabinetConstant.CACHE_MEMBER_CARD + electricityMemberCard.getId(), electricityMemberCard);
            return null;
        });

        if (update > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
    }

    /**
     * @param id
     * @return
     */
    @Override
    public R deleteElectricityMemberCard(Integer id) {
        ElectricityMemberCard electricityMemberCard=new ElectricityMemberCard();
        electricityMemberCard.setId(id);
        electricityMemberCard.setDelFlag(ElectricityMemberCard.DEL_DEL);
        Integer update=baseMapper.updateById(electricityMemberCard);

        DbUtils.dbOperateSuccessThen(update, () -> {
            //删除缓存
            redisService.delete(ElectricityCabinetConstant.CACHE_MEMBER_CARD + electricityMemberCard.getId());
            return null;
        });

        if (update > 0) {
            return R.ok();
        }
        return R.fail("ELECTRICITY.0086", "操作失败");
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

        return R.ok(baseMapper.electricityMemberCardList(offset, size, status, type));
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
        //扫码查找换电柜 TODO
        Franchisee franchisee = null;
        if (Objects.isNull(franchisee)) {
            log.error("ELECTRICITY  ERROR! not found franchisee ! ");
            return R.fail("ELECTRICITY.0038", "未找到加盟商");

        }

        //该加盟商是否缴纳押金 TODO


        //查找加盟商下的可用套餐
        return R.ok(baseMapper.queryElectricityMemberCard(offset,size,franchisee.getId()));
    }

    @Override
    public List<ElectricityMemberCard> queryByFranchisee(Integer id) {
        return baseMapper.selectList(new LambdaQueryWrapper<ElectricityMemberCard>().eq(ElectricityMemberCard::getFranchiseeId,id));
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

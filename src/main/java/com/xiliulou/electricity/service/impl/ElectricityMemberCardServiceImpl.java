package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.ElectricityMemberCard;
import com.xiliulou.electricity.entity.FranchiseeUserInfo;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.ElectricityMemberCardMapper;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.ElectricityMemberCardService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.FranchiseeUserInfoService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    @Autowired
    ElectricityCabinetService electricityCabinetService;

    @Autowired
    FranchiseeUserInfoService franchiseeUserInfoService;

    @Autowired
    StoreService storeService;

    /**
     * 新增卡包
     *
     * @param electricityMemberCard
     * @return
     */
    @Override
    public R add(ElectricityMemberCard electricityMemberCard) {
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
        electricityMemberCard.setDelFlag(ElectricityMemberCard.DEL_NORMAL);
        if(StringUtils.isNotEmpty(electricityMemberCard.getBatteryType())) {
            electricityMemberCard.setBatteryType(BatteryConstant.acquireBatteryShort(Integer.valueOf(electricityMemberCard.getBatteryType())));
        }

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
    public R update(ElectricityMemberCard electricityMemberCard) {
        electricityMemberCard.setUpdateTime(System.currentTimeMillis());
        if(Objects.nonNull(electricityMemberCard.getLimitCount())) {
            if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.UN_LIMITED_COUNT_TYPE)) {
                electricityMemberCard.setMaxUseCount(ElectricityMemberCard.UN_LIMITED_COUNT);
            }
        }

        if(StringUtils.isNotEmpty(electricityMemberCard.getBatteryType())) {
            electricityMemberCard.setBatteryType(BatteryConstant.acquireBatteryShort(Integer.valueOf(electricityMemberCard.getBatteryType())));
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
    public R delete(Integer id) {
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
    public R queryList(Long offset, Long size, Integer status, Integer type,Integer tenantId) {
        List<ElectricityMemberCard> electricityMemberCardList= baseMapper.queryList(offset, size, status, type,tenantId);
        if(ObjectUtil.isEmpty(electricityMemberCardList)){
            return R.ok(electricityMemberCardList);
        }

        List<ElectricityMemberCard> electricityMemberCards=new ArrayList<>();
        for (ElectricityMemberCard electricityMemberCard:electricityMemberCardList) {
            if(StringUtils.isNotEmpty(electricityMemberCard.getBatteryType())) {
                electricityMemberCard.setBatteryType(BatteryConstant.acquireBattery(electricityMemberCard.getBatteryType()).toString());
            }
            electricityMemberCards.add(electricityMemberCard);
        }
        return R.ok(electricityMemberCards);
    }

    @Override
    public R queryUserList(Long offset, Long size,String productKey, String deviceName) {
        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("rentBattery  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }



        //换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(productKey,deviceName);
        if (Objects.isNull(electricityCabinet)) {
            log.error("rentBattery  ERROR! not found electricityCabinet ！productKey{},deviceName{}", productKey,deviceName);
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }



        //3、查出套餐
        //查找换电柜门店
        if(Objects.isNull(electricityCabinet.getStoreId())){
            log.error("queryByDevice  ERROR! not found store ！electricityCabinetId{}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
        }
        Store store=storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if(Objects.isNull(store)){
            log.error("queryByDevice  ERROR! not found store ！storeId{}", electricityCabinet.getStoreId());
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }


        //查找门店加盟商
        if(Objects.isNull(store.getFranchiseeId())){
            log.error("queryByDevice  ERROR! not found Franchisee ！storeId{}", store.getId());
            return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
        }


        //判断用户
        UserInfo userInfo = userInfoService.queryByUid(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("rentBattery  ERROR! not found user,uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("rentBattery  ERROR! user is unUsable! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_INIT)) {
            log.error("rentBattery  ERROR! not auth! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }


        //是否缴纳押金，是否绑定电池
        FranchiseeUserInfo franchiseeUserInfo = franchiseeUserInfoService.queryByUserInfoId(userInfo.getId());

        //未找到用户
        if (Objects.isNull(franchiseeUserInfo)) {
            log.error("payDeposit  ERROR! not found user! userId:{}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");

        }

        //判断是否缴纳押金
        if (Objects.equals(franchiseeUserInfo.getServiceStatus(), FranchiseeUserInfo.STATUS_IS_INIT)
                || Objects.isNull(franchiseeUserInfo.getBatteryDeposit()) || Objects.isNull(franchiseeUserInfo.getOrderId())) {
            log.error("rentBattery  ERROR! not pay deposit! uid:{} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        //判断该换电柜加盟商和用户加盟商是否一致
        if(!Objects.equals(store.getFranchiseeId(),franchiseeUserInfo.getFranchiseeId())){
            log.error("queryByDevice  ERROR!FranchiseeId is not equal!uid:{} , FranchiseeId1:{} ,FranchiseeId2:{}", user.getUid(),store.getFranchiseeId(),franchiseeUserInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致，请联系客服处理");
        }


        //多电池型号查询套餐
        if(Objects.equals(franchiseeUserInfo.getModelType(),FranchiseeUserInfo.MEW_MODEL_TYPE)){
            if(Objects.isNull(franchiseeUserInfo.getBatteryType())){
                return R.ok();
            }
            return R.ok(baseMapper.queryUserList(offset,size,store.getFranchiseeId(),franchiseeUserInfo.getBatteryType()));
        }


        //查找加盟商下的可用套餐
        return R.ok(baseMapper.queryUserList(offset,size,store.getFranchiseeId(),null));
    }

    @Override
    public List<ElectricityMemberCard> queryByFranchisee(Long id) {
        return baseMapper.selectList(new LambdaQueryWrapper<ElectricityMemberCard>().eq(ElectricityMemberCard::getFranchiseeId,id));
    }

    @Override
    public R queryCount(Integer status, Integer type, Integer tenantId) {
        return R.ok(baseMapper.queryCount(status, type,tenantId));
    }

    @Override
    public R listByFranchisee(Long offset, Long size, Integer status, Integer type, Integer tenantId, Long franchiseeId) {
       return R.ok(baseMapper.listByFranchisee(offset, size, status, type,tenantId,franchiseeId));
    }

    @Override
    public R listCountByFranchisee(Integer status, Integer type, Integer tenantId, Long franchiseeId) {
        return R.ok(baseMapper.listCountByFranchisee(status, type,tenantId,franchiseeId));
    }

    @Override
    public ElectricityMemberCard queryByStatus(Integer id) {
        return baseMapper.selectOne(new LambdaQueryWrapper<ElectricityMemberCard>().eq(ElectricityMemberCard::getId,id)
                .eq(ElectricityMemberCard::getStatus,ElectricityMemberCard.STATUS_USEABLE));
    }

    /**
     * 获取套餐
     *
     * @param id
     * @return
     */
    @Override
    public ElectricityMemberCard queryByCache(Integer id) {
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

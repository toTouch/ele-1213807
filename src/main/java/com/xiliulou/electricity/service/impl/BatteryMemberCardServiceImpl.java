package com.xiliulou.electricity.service.impl;

import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.MemberCardBatteryType;
import com.xiliulou.electricity.mapper.BatteryMemberCardMapper;
import com.xiliulou.electricity.query.BatteryMemberCardQuery;
import com.xiliulou.electricity.query.BatteryMemberCardStatusQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.MemberCardBatteryTypeService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.vo.BatteryMemberCardSearchVO;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserBatteryMemberCardService userBatteryMemberCardService;

    @Autowired
    private ElectricityMemberCardOrderService electricityMemberCardOrderService;

    @Autowired
    private MemberCardBatteryTypeService memberCardBatteryTypeService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BatteryMemberCard queryByIdFromDB(Long id) {
        return this.batteryMemberCardMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BatteryMemberCard queryByIdFromCache(Long id) {
        BatteryMemberCard cacheBatteryMemberCard = redisService.getWithHash(CacheConstant.CACHE_BATTERY_MEMBERCARD + id, BatteryMemberCard.class);
        if (Objects.nonNull(cacheBatteryMemberCard)) {
            return cacheBatteryMemberCard;
        }

        BatteryMemberCard batteryMemberCard = this.queryByIdFromDB(id);
        if (Objects.isNull(batteryMemberCard)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_BATTERY_MEMBERCARD + id, batteryMemberCard);

        return batteryMemberCard;
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
        int update = this.batteryMemberCardMapper.update(batteryMemberCard);
        DbUtils.dbOperateSuccessThenHandleCache(update, i -> {
            redisService.delete(CacheConstant.CACHE_BATTERY_MEMBERCARD + batteryMemberCard.getId());
        });

        return update;
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteById(Long id) {
        int delete = this.batteryMemberCardMapper.deleteById(id);
        DbUtils.dbOperateSuccessThenHandleCache(delete, i -> {
            redisService.delete(CacheConstant.CACHE_BATTERY_MEMBERCARD + id);
        });

        return delete;
    }

    @Override
    public List<BatteryMemberCardSearchVO> search(BatteryMemberCardQuery query) {
        List<BatteryMemberCard> list = this.batteryMemberCardMapper.selectBySearch(query);

        return list.parallelStream().map(item -> {
            BatteryMemberCardSearchVO batteryMemberCardVO = new BatteryMemberCardSearchVO();
            BeanUtils.copyProperties(item, batteryMemberCardVO);
            return batteryMemberCardVO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<BatteryMemberCardVO> selectByPage(BatteryMemberCardQuery query) {
        List<BatteryMemberCard> list = this.batteryMemberCardMapper.selectByPage(query);

        return list.parallelStream().map(item -> {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BeanUtils.copyProperties(item, batteryMemberCardVO);
            return batteryMemberCardVO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<BatteryMemberCardVO> selectByQuery(BatteryMemberCardQuery query) {
        List<BatteryMemberCard> list = this.batteryMemberCardMapper.selectByQuery(query);

        return list.parallelStream().map(item -> {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BeanUtils.copyProperties(item, batteryMemberCardVO);
            return batteryMemberCardVO;
        }).collect(Collectors.toList());

    }

    @Override
    public List<BatteryMemberCardVO> selectByPageForUser(BatteryMemberCardQuery query) {
        List<BatteryMemberCard> list = this.batteryMemberCardMapper.selectByPageForUser(query);

        return list.parallelStream().map(item -> {
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            BeanUtils.copyProperties(item, batteryMemberCardVO);
            return batteryMemberCardVO;
        }).collect(Collectors.toList());

    }

    @Override
    public List<String> selectMembercardBatteryV(BatteryMemberCardQuery query) {
        List<BatteryMemberCardVO> list = this.batteryMemberCardMapper.selectMembercardBatteryV(query);
        if(CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }

        return list.stream().map(BatteryMemberCardVO::getBatteryV).distinct().collect(Collectors.toList());
    }

    @Override
    public Integer selectByPageCount(BatteryMemberCardQuery query) {
        return this.batteryMemberCardMapper.selectByPageCount(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> updateStatus(BatteryMemberCardStatusQuery batteryModelQuery) {
        BatteryMemberCard batteryMemberCard = this.queryByIdFromCache(batteryModelQuery.getId());
        if (Objects.isNull(batteryMemberCard) || !Objects.equals(batteryMemberCard.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
        }

        BatteryMemberCard batteryMemberCardUpdate = new BatteryMemberCard();
        batteryMemberCardUpdate.setId(batteryMemberCard.getId());
        batteryMemberCardUpdate.setStatus(batteryModelQuery.getStatus());
        batteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());

        this.update(batteryMemberCardUpdate);

        return Triple.of(true, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> delete(Long id) {
        BatteryMemberCard batteryMemberCard = this.queryByIdFromCache(id);
        if (Objects.isNull(batteryMemberCard) || !Objects.equals(batteryMemberCard.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
        }

        if (Objects.nonNull(userBatteryMemberCardService.checkUserByMembercardId(id))) {
            return Triple.of(false, "100100", "删除失败，该套餐已有用户使用");
        }

        if (Objects.nonNull(electricityMemberCardOrderService.checkOrderByMembercardId(id))) {
            return Triple.of(false, "100272", "删除失败，该套餐已生成订单");
        }

        BatteryMemberCard batteryMemberCardUpdate = new BatteryMemberCard();
        batteryMemberCardUpdate.setId(id);
        batteryMemberCardUpdate.setStatus(BatteryMemberCard.DEL_DEL);
        batteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(batteryMemberCardUpdate);

        return Triple.of(true, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> modify(BatteryMemberCardQuery query) {
        BatteryMemberCard batteryMemberCard = this.queryByIdFromCache(query.getId());
        if (Objects.isNull(batteryMemberCard) || !Objects.equals(batteryMemberCard.getTenantId(), TenantContextHolder.getTenantId())) {
            return Triple.of(false, "ELECTRICITY.00121", "套餐不存在");
        }

        if (Objects.equals(query.getStatus(), BatteryMemberCard.STATUS_UP)) {
            return Triple.of(false, "100271", "请先下架套餐再进行编辑操作");
        }

        BatteryMemberCard batteryMemberCardUpdate = new BatteryMemberCard();
        batteryMemberCardUpdate.setId(batteryMemberCard.getId());
        batteryMemberCardUpdate.setName(query.getName());
        batteryMemberCardUpdate.setDeposit(query.getDeposit());
        batteryMemberCardUpdate.setRentPrice(query.getRentPrice());
        batteryMemberCardUpdate.setRentPriceUnit(query.getRentPriceUnit());
// TODO       batteryMemberCardUpdate.setValidDays(query.getValidDays());
        batteryMemberCardUpdate.setRentUnit(query.getRentUnit());
        batteryMemberCardUpdate.setRentType(query.getRentType());
        batteryMemberCardUpdate.setSendCoupon(query.getSendCoupon());
        batteryMemberCardUpdate.setStatus(query.getStatus());
        batteryMemberCardUpdate.setUseCount(query.getUseCount());
        batteryMemberCardUpdate.setCouponId(query.getCouponId());
        batteryMemberCardUpdate.setIsRefund(query.getIsRefund());
        batteryMemberCardUpdate.setRefundLimit(query.getRefundLimit());
        batteryMemberCardUpdate.setFreeDeposite(query.getFreeDeposite());
        batteryMemberCardUpdate.setRefundDepositeAudit(query.getRefundDepositeAudit());
        batteryMemberCardUpdate.setServiceCharge(query.getServiceCharge());
        batteryMemberCardUpdate.setRemark(query.getRemark());
        batteryMemberCardUpdate.setUpdateTime(System.currentTimeMillis());

        this.update(batteryMemberCardUpdate);

        return Triple.of(true, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> save(BatteryMemberCardQuery query) {
        if (Objects.nonNull(this.batteryMemberCardMapper.checkMembercardExist(query.getName(), TenantContextHolder.getTenantId()))) {
            return Triple.of(false, "100104", "套餐名称已存在");
        }

        Triple<Boolean, String, Object> verifyBatteryMemberCardResult = verifyBatteryMemberCardQuery(query);
        if (Boolean.FALSE.equals(verifyBatteryMemberCardResult.getLeft())) {
            return verifyBatteryMemberCardResult;
        }

        BatteryMemberCard batteryMemberCard = new BatteryMemberCard();
        BeanUtils.copyProperties(query, batteryMemberCard);
        batteryMemberCard.setDelFlag(BatteryMemberCard.DEL_NORMAL);
        batteryMemberCard.setCreateTime(System.currentTimeMillis());
        batteryMemberCard.setUpdateTime(System.currentTimeMillis());
        batteryMemberCard.setTenantId(TenantContextHolder.getTenantId());

        this.batteryMemberCardMapper.insert(batteryMemberCard);

        memberCardBatteryTypeService.batchInsert(buildMemberCardBatteryTypeList(query.getBatteryModels(), batteryMemberCard.getId()));

        return Triple.of(true, null, null);
    }

    private List<MemberCardBatteryType> buildMemberCardBatteryTypeList(List<String> batteryModels, Long mid) {

        List<MemberCardBatteryType> memberCardBatteryTypeList = Lists.newArrayList();

        for (String batteryModel : batteryModels) {
            MemberCardBatteryType memberCardBatteryType = new MemberCardBatteryType();
            memberCardBatteryType.setBatteryType(batteryModel);
            memberCardBatteryType.setBatteryV(batteryModel.substring(batteryModel.indexOf("_") + 1).substring(0, batteryModel.substring(batteryModel.indexOf("_") + 1).indexOf("_")));
            memberCardBatteryType.setMid(mid);
            memberCardBatteryType.setTenantId(TenantContextHolder.getTenantId());
            memberCardBatteryType.setCreateTime(System.currentTimeMillis());
            memberCardBatteryType.setUpdateTime(System.currentTimeMillis());

            memberCardBatteryTypeList.add(memberCardBatteryType);
        }

        return memberCardBatteryTypeList;
    }

    private Triple<Boolean, String, Object> verifyBatteryMemberCardQuery(BatteryMemberCardQuery query) {

        List<String> list = query.getBatteryModels().stream().map(item -> item.substring(item.indexOf("_") + 1).substring(0, item.substring(item.indexOf("_") + 1).indexOf("_"))).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list) || list.size() != 1) {
            return Triple.of(false, "100273", "套餐电池型号不合法");
        }

        return Triple.of(true, null, null);
    }
}

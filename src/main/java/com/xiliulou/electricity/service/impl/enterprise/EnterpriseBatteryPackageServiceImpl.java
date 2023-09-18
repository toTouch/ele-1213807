package com.xiliulou.electricity.service.impl.enterprise;

import com.google.api.client.util.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.MemberCardBatteryType;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import com.xiliulou.electricity.mapper.BatteryMemberCardMapper;
import com.xiliulou.electricity.query.enterprise.EnterpriseMemberCardQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import com.xiliulou.electricity.service.enterprise.EnterpriseBatteryPackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author BaoYu
 * @description:
 * @date 2023/9/15 13:58
 */

@Slf4j
public class EnterpriseBatteryPackageServiceImpl implements EnterpriseBatteryPackageService {

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

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserBatteryTypeService userBatteryTypeService;

    @Autowired
    private FranchiseeService franchiseeService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CarRentalPackageService carRentalPackageService;

    @Autowired
    private BatteryModelService batteryModelService;

    @Autowired
    private UserBatteryDepositService userBatteryDepositService;

    @Override
    public Triple<Boolean, String, Object> save(EnterpriseMemberCardQuery query) {

        if (Objects.nonNull(this.batteryMemberCardMapper.checkMembercardExist(query.getName(), TenantContextHolder.getTenantId()))) {
            return Triple.of(false, "100104", "套餐名称已存在");
        }

        Franchisee franchisee = franchiseeService.queryByIdFromCache(query.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            return Triple.of(false, "", "加盟商不存在");
        }

        Triple<Boolean, String, Object> verifyBatteryMemberCardResult = verifyBatteryMemberCardQuery(query, franchisee);
        if (Boolean.FALSE.equals(verifyBatteryMemberCardResult.getLeft())) {
            return verifyBatteryMemberCardResult;
        }

        BatteryMemberCard batteryMemberCard = new BatteryMemberCard();
        BeanUtils.copyProperties(query, batteryMemberCard);

        //设置企业渠道换电套餐类型
        batteryMemberCard.setBusinessType(BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_ENTERPRISE_BATTERY.getCode());
        batteryMemberCard.setDelFlag(BatteryMemberCard.DEL_NORMAL);
        batteryMemberCard.setCreateTime(System.currentTimeMillis());
        batteryMemberCard.setUpdateTime(System.currentTimeMillis());
        batteryMemberCard.setTenantId(TenantContextHolder.getTenantId());

        this.batteryMemberCardMapper.insert(batteryMemberCard);

        if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE) && CollectionUtils.isNotEmpty(query.getBatteryModels())) {
            memberCardBatteryTypeService.batchInsert(buildMemberCardBatteryTypeList(query.getBatteryModels(), batteryMemberCard.getId()));
        }

        return Triple.of(true, null, null);

    }

    private Triple<Boolean, String, Object> verifyBatteryMemberCardQuery(EnterpriseMemberCardQuery query, Franchisee franchisee) {

        if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
            return Triple.of(true, null, null);
        }

        List<String> list = query.getBatteryModels().stream().map(item -> item.substring(item.indexOf("_") + 1).substring(0, item.substring(item.indexOf("_") + 1).indexOf("_"))).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list) || list.size() != 1) {
            return Triple.of(false, "100273", "套餐电池型号电压不一致");
        }

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

}

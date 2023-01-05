package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.BatteryConstant;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.manager.CalcRentCarPriceFactory;
import com.xiliulou.electricity.mapper.CarMemberCardOrderMapper;
import com.xiliulou.electricity.mapper.MemberCardFailureRecordMapper;
import com.xiliulou.electricity.query.CarMemberCardOrderQuery;
import com.xiliulou.electricity.query.RentCarHybridOrderQuery;
import com.xiliulou.electricity.query.RentCarMemberCardOrderQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.FailureMemberCardVo;
import com.xiliulou.electricity.vo.MemberCardFailureRecordVO;
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 失效套餐表(memberCardFailureRecord)表服务实现类
 *
 * @author hrp
 * @since 2022-12-21 09:47:25
 */
@Service("memberCardFailureRecordService")
@Slf4j
public class MemberCardFailureRecordServiceImpl implements MemberCardFailureRecordService {


    @Resource
    MemberCardFailureRecordMapper memberCardFailureRecordMapper;

    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    @Autowired
    UserCarMemberCardService userCarMemberCardService;
    @Autowired
    CarMemberCardOrderService carMemberCardOrderService;
    @Autowired
    UserCarDepositService userCarDepositService;
    @Autowired
    UserCarService userCarService;
    @Autowired
    StoreService storeService;
    @Autowired
    ElectricityCarModelService electricityCarModelService;

    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;

    @Autowired
    UserBatteryDepositService userBatteryDepositService;

    @Override
    public void failureMemberCardTask() {
        //处理失效的租车套餐
        handleUserCarMemberCardExpire();

        //处理失效的换电套餐
        handleUserBatteryMemberCardExpire();
    }


    /**
     * 处理失效的换电套餐
     */
    private void handleUserBatteryMemberCardExpire() {

        int offset = 0;
        int size = 300;
        long nowTime = System.currentTimeMillis();

        while (true) {

            List<FailureMemberCardVo> userBatteryMemberCardList = userBatteryMemberCardService.queryMemberCardExpireUser(offset, size, nowTime);

            log.debug("-----expireUserBatteryMemberCardList>>>>>{}", userBatteryMemberCardList);

            if (!DataUtil.collectionIsUsable(userBatteryMemberCardList)) {
                return;
            }

            userBatteryMemberCardList.parallelStream().forEach(item -> {

                ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.queryLastPayMemberCardTimeByUid(item.getUid(), item.getFranchiseeId(), item.getTenantId());

                List<MemberCardFailureRecord> memberCardFailureRecords = null;
                if (Objects.nonNull(electricityMemberCardOrder)) {
                    item.setOrderId(electricityMemberCardOrder.getOrderId());
                    memberCardFailureRecords = memberCardFailureRecordMapper.selectByCarMemberCardOrderId(electricityMemberCardOrder.getOrderId());
                }
                if (!CollectionUtils.isEmpty(memberCardFailureRecords)) {
                    return;
                }


                MemberCardFailureRecord memberCardFailureRecord = buildBatteryMemberCardFailureRecord(item);
                if (Objects.isNull(memberCardFailureRecord)) {
                    return;
                }

                memberCardFailureRecordMapper.insert(memberCardFailureRecord);
            });

            offset += size;
        }

    }

    /**
     * 处理失效的租车套餐
     */
    private void handleUserCarMemberCardExpire() {

        int offset = 0;
        int size = 300;

        while (true) {
            //租车套餐
            List<FailureMemberCardVo> userCarMemberCardList = userCarMemberCardService.queryMemberCardExpireUser(offset, size, System.currentTimeMillis());
            if (CollectionUtils.isEmpty(userCarMemberCardList)) {
                return;
            }

            userCarMemberCardList.parallelStream().forEach(item -> {
                List<MemberCardFailureRecord> memberCardFailureRecords = memberCardFailureRecordMapper.selectByCarMemberCardOrderId(item.getOrderId());
                if (!CollectionUtils.isEmpty(memberCardFailureRecords)) {
                    return;
                }

                MemberCardFailureRecord memberCardFailureRecord = buildRentCarMemberCardFailureRecord(item);
                if (Objects.isNull(memberCardFailureRecord)) {
                    return;
                }

                memberCardFailureRecordMapper.insert(memberCardFailureRecord);
            });

            offset += size;
        }
    }

    @Override
    public void saveRentCarMemberCardFailureRecord(Long uid) {
        try {
            UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(uid);
            if (Objects.isNull(userCarMemberCard)) {
                log.warn("ELE FAILURE CAR MEMBERCARD WARN! not found userCarMemberCard,uid={}", uid);
                return;
            }

            //若套餐已过期  不添加记录
            if (userCarMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
                return;
            }

            CarMemberCardOrder carMemberCardOrder = carMemberCardOrderService.selectByOrderId(userCarMemberCard.getOrderId());
            if (Objects.isNull(carMemberCardOrder)) {
                log.warn("ELE FAILURE CAR MEMBERCARD WARN! not found carMemberCardOrder,uid={},orderId={}", uid, userCarMemberCard.getOrderId());
                return;
            }

            UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(uid);
            if (Objects.isNull(userCarDeposit)) {
                log.warn("ELE FAILURE CAR MEMBERCARD WARN! not found userCarDeposit,uid={}", uid);
                return;
            }

            UserCar userCar = userCarService.selectByUidFromCache(uid);
            if (Objects.isNull(userCar)) {
                log.warn("ELE FAILURE CAR MEMBERCARD WARN! not found userCar,uid={}", uid);
                return;
            }

            ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(userCar.getCarModel().intValue());

            MemberCardFailureRecord memberCardFailureRecord = new MemberCardFailureRecord();
            memberCardFailureRecord.setUid(uid);
            memberCardFailureRecord.setCardName(carMemberCardOrder.getCardName());
            memberCardFailureRecord.setDeposit(userCarDeposit.getCarDeposit());
            memberCardFailureRecord.setCarMemberCardOrderId(carMemberCardOrder.getOrderId());
            memberCardFailureRecord.setMemberCardExpireTime(userCarMemberCard.getMemberCardExpireTime());
            memberCardFailureRecord.setType(MemberCardFailureRecord.FAILURE_TYPE_FOR_RENT_CAR);
            memberCardFailureRecord.setCarSn(userCar.getSn());
            memberCardFailureRecord.setCarModelName(Objects.nonNull(electricityCarModel) ? electricityCarModel.getName() : "");
            memberCardFailureRecord.setCarMemberCardType(carMemberCardOrder.getMemberCardType());
            memberCardFailureRecord.setValidDays(carMemberCardOrder.getValidDays());
            memberCardFailureRecord.setStoreId(carMemberCardOrder.getStoreId());
            memberCardFailureRecord.setTenantId(carMemberCardOrder.getTenantId());
            memberCardFailureRecord.setCreateTime(System.currentTimeMillis());
            memberCardFailureRecord.setUpdateTime(System.currentTimeMillis());

            this.insert(memberCardFailureRecord);
        } catch (Exception e) {
            log.error("ELE FAILURE CAR MEMBERCARD ERROR!", e);
        }
    }


    private MemberCardFailureRecord buildBatteryMemberCardFailureRecord(FailureMemberCardVo item) {
        ElectricityMemberCardOrder electricityMemberCardOrder = electricityMemberCardOrderService.selectByOrderNo(item.getOrderId());
        if (Objects.isNull(electricityMemberCardOrder)) {
            log.error("ELE FAILURE BATTERY MEMBERCARD ERROR! not found carMemberCardOrder,uid={},orderId={}", item.getUid(), item.getOrderId());
            return null;
        }

        MemberCardFailureRecord memberCardFailureRecord = new MemberCardFailureRecord();
        memberCardFailureRecord.setUid(item.getUid());
        memberCardFailureRecord.setCardName(electricityMemberCardOrder.getCardName());
        memberCardFailureRecord.setDeposit(item.getBatteryDeposit());
        memberCardFailureRecord.setCarMemberCardOrderId(electricityMemberCardOrder.getOrderId());
        memberCardFailureRecord.setMemberCardExpireTime(item.getMemberCardExpireTime());
        memberCardFailureRecord.setType(MemberCardFailureRecord.FAILURE_TYPE_FOR_BATTERY);
        memberCardFailureRecord.setBatteryType(item.getBatteryType());
        memberCardFailureRecord.setTenantId(item.getTenantId());
        memberCardFailureRecord.setCreateTime(System.currentTimeMillis());
        memberCardFailureRecord.setUpdateTime(System.currentTimeMillis());

        return memberCardFailureRecord;
    }


    private MemberCardFailureRecord buildRentCarMemberCardFailureRecord(FailureMemberCardVo item) {
        CarMemberCardOrder carMemberCardOrder = carMemberCardOrderService.selectByOrderId(item.getOrderId());
        if (Objects.isNull(carMemberCardOrder)) {
            log.warn("ELE FAILURE CAR MEMBERCARD WARN! not found carMemberCardOrder,uid={},orderId={}", item.getUid(), item.getOrderId());
            return null;
        }

        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(item.getUid());
        if (Objects.isNull(userCarDeposit)) {
            log.warn("ELE FAILURE CAR MEMBERCARD WARN! not found userCarDeposit,uid={}", item.getUid());
            return null;
        }

        UserCar userCar = userCarService.selectByUidFromCache(item.getUid());
        if (Objects.isNull(userCar)) {
            log.warn("ELE FAILURE CAR MEMBERCARD WARN! not found userCar,uid={}", item.getUid());
            return null;
        }

        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(userCar.getCarModel().intValue());

        MemberCardFailureRecord memberCardFailureRecord = new MemberCardFailureRecord();
        memberCardFailureRecord.setUid(item.getUid());
        memberCardFailureRecord.setCardName(carMemberCardOrder.getCardName());
        memberCardFailureRecord.setDeposit(userCarDeposit.getCarDeposit());
        memberCardFailureRecord.setCarMemberCardOrderId(carMemberCardOrder.getOrderId());
        memberCardFailureRecord.setMemberCardExpireTime(item.getMemberCardExpireTime());
        memberCardFailureRecord.setType(MemberCardFailureRecord.FAILURE_TYPE_FOR_RENT_CAR);
        memberCardFailureRecord.setCarSn(userCar.getSn());
        memberCardFailureRecord.setCarModelName(Objects.nonNull(electricityCarModel) ? electricityCarModel.getName() : "");
        memberCardFailureRecord.setCarMemberCardType(carMemberCardOrder.getMemberCardType());
        memberCardFailureRecord.setValidDays(carMemberCardOrder.getValidDays());
        memberCardFailureRecord.setStoreId(carMemberCardOrder.getStoreId());
        memberCardFailureRecord.setTenantId(carMemberCardOrder.getTenantId());
        memberCardFailureRecord.setCreateTime(System.currentTimeMillis());
        memberCardFailureRecord.setUpdateTime(System.currentTimeMillis());

        return memberCardFailureRecord;
    }

    @Override
    public Integer insert(MemberCardFailureRecord memberCardFailureRecord) {
        return memberCardFailureRecordMapper.insert(memberCardFailureRecord);
    }

    @Override
    public R queryFailureMemberCard(Long uid, Integer offset, Integer size) {

        List<MemberCardFailureRecord> memberCardFailureRecordList = memberCardFailureRecordMapper.queryFailureMemberCard(uid, TenantContextHolder.getTenantId(), offset, size);
        if (CollectionUtils.isEmpty(memberCardFailureRecordList)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        List<MemberCardFailureRecordVO> failureRecords = memberCardFailureRecordList.parallelStream().map(item -> {
            MemberCardFailureRecordVO memberCardFailureRecordVO = new MemberCardFailureRecordVO();
            BeanUtils.copyProperties(item, memberCardFailureRecordVO);

            //换电失效套餐
            if (Objects.equals(MemberCardFailureRecord.FAILURE_TYPE_FOR_BATTERY, item.getType())) {
                if (Objects.nonNull(item.getBatteryType())) {
                    Integer batteryType = BatteryConstant.acquireBattery(item.getBatteryType());
                    memberCardFailureRecordVO.setBatteryType(batteryType.toString());
                }
            }


            //租车失效套餐
            if (Objects.equals(MemberCardFailureRecord.FAILURE_TYPE_FOR_RENT_CAR, item.getType())) {
                Store store = storeService.queryByIdFromCache(item.getStoreId());
                if (Objects.nonNull(store)) {
                    memberCardFailureRecordVO.setStoreName(store.getName());
                }
            }

            return memberCardFailureRecordVO;
        }).collect(Collectors.toList());

        return R.ok(failureRecords);
    }
}

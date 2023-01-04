package com.xiliulou.electricity.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
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
    UserCarMemberCardService  userCarMemberCardService;
    @Autowired
    CarMemberCardOrderService carMemberCardOrderService;
    @Autowired
    UserCarDepositService userCarDepositService;
    @Autowired
    UserCarService userCarService;
    @Autowired
    StoreService storeService;

    @Override
    public void failureMemberCardTask() {
        //处理失效的租车套餐
        handleUserCarMemberCardExpire();

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


                MemberCardFailureRecord memberCardFailureRecord = MemberCardFailureRecord.builder()
                        .uid(item.getUid())
                        .memberCardExpireTime(item.getMemberCardExpireTime())
                        .createTime(nowTime)
                        .updateTime(nowTime)
                        .tenantId(item.getTenantId())
                        .batteryType(item.getBatteryType())
                        .type(MemberCardFailureRecord.FAILURE_TYPE_FOR_BATTERY)
                        .deposit(item.getBatteryDeposit()).build();

                memberCardFailureRecordMapper.insert(memberCardFailureRecord);

            });

            offset += size;
        }
    }

    /**
     * 处理失效的租车套餐
     */
    private void handleUserCarMemberCardExpire(){

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
                if(!CollectionUtils.isEmpty(memberCardFailureRecords)){
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

    private MemberCardFailureRecord buildRentCarMemberCardFailureRecord(FailureMemberCardVo item) {
        CarMemberCardOrder carMemberCardOrder = carMemberCardOrderService.selectByOrderId(item.getOrderId());
        if(Objects.isNull(carMemberCardOrder)){
            log.error("ELE FAILURE CAR MEMBERCARD ERROR! not found carMemberCardOrder,uid={},orderId={}", item.getUid(),item.getOrderId());
            return  null;
        }

        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(item.getUid());
        if(Objects.isNull(userCarDeposit)){
            log.error("ELE FAILURE CAR MEMBERCARD ERROR! not found userCarDeposit,uid={}", item.getUid());
            return  null;
        }

        UserCar userCar = userCarService.selectByUidFromCache(item.getUid());
        if(Objects.isNull(userCar)){
            log.error("ELE FAILURE CAR MEMBERCARD ERROR! not found userCar,uid={}", item.getUid());
            return  null;
        }

        MemberCardFailureRecord memberCardFailureRecord = new MemberCardFailureRecord();
        memberCardFailureRecord.setUid(item.getUid());
        memberCardFailureRecord.setCardName(carMemberCardOrder.getCardName());
        memberCardFailureRecord.setDeposit(userCarDeposit.getCarDeposit());
        memberCardFailureRecord.setCarMemberCardOrderId(carMemberCardOrder.getOrderId());
        memberCardFailureRecord.setMemberCardExpireTime(item.getMemberCardExpireTime());
        memberCardFailureRecord.setType(MemberCardFailureRecord.FAILURE_TYPE_FOR_RENT_CAR);
        memberCardFailureRecord.setCarSn(userCar.getSn());
        memberCardFailureRecord.setCarMemberCardType(carMemberCardOrder.getMemberCardType());
        memberCardFailureRecord.setValidDays(carMemberCardOrder.getValidDays());
        memberCardFailureRecord.setStoreId(carMemberCardOrder.getStoreId());
        memberCardFailureRecord.setTenantId(carMemberCardOrder.getTenantId());
        memberCardFailureRecord.setCreateTime(System.currentTimeMillis());
        memberCardFailureRecord.setUpdateTime(System.currentTimeMillis());

        return memberCardFailureRecord;
    }

    @Override
    public R queryFailureMemberCard(Long uid, Integer type, Integer offset, Integer size) {

        if (Objects.equals(MemberCardFailureRecord.FAILURE_TYPE_FOR_BATTERY, type)) {
            List<MemberCardFailureRecord> memberCardFailureRecordList = memberCardFailureRecordMapper.queryFailureMemberCard(uid, TenantContextHolder.getTenantId(), offset, size);
            if (CollectionUtils.isEmpty(memberCardFailureRecordList)) {
                return R.ok();
            }

            for (MemberCardFailureRecord memberCardFailureRecord : memberCardFailureRecordList) {
                if (Objects.nonNull(memberCardFailureRecord.getBatteryType())) {
                    Integer batteryType = BatteryConstant.acquireBattery(memberCardFailureRecord.getBatteryType());
                    memberCardFailureRecord.setBatteryType(batteryType.toString());
                }
            }
            return R.ok(memberCardFailureRecordList);

        }

        if (Objects.equals(MemberCardFailureRecord.FAILURE_TYPE_FOR_RENT_CAR, type)) {
            List<MemberCardFailureRecord> rentCarMemberCardFailureRecordList = memberCardFailureRecordMapper.selectRentCarFailureMemberCardList(uid, type, TenantContextHolder.getTenantId(), offset, size);
            if (CollectionUtils.isEmpty(rentCarMemberCardFailureRecordList)) {
                return R.ok();
            }

            List<MemberCardFailureRecordVO> failureRecords = rentCarMemberCardFailureRecordList.parallelStream().map(item -> {
                MemberCardFailureRecordVO memberCardFailureRecordVO = new MemberCardFailureRecordVO();
                BeanUtils.copyProperties(item, memberCardFailureRecordVO);

                Store store = storeService.queryByIdFromCache(item.getStoreId());
                if (Objects.nonNull(store)) {
                    memberCardFailureRecordVO.setStoreName(store.getName());
                }

                return memberCardFailureRecordVO;
            }).collect(Collectors.toList());

            return R.ok(failureRecords);
        }

        return R.ok();
    }
}

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
import com.xiliulou.pay.weixinv3.dto.WechatJsapiOrderResultDTO;
import com.xiliulou.pay.weixinv3.exception.WechatPayException;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
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

    @Override
    public void failureMemberCardTask() {
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

    @Override
    public R queryFailureMemberCard(Long uid, Integer offset, Integer size) {
        Integer tenantId = TenantContextHolder.getTenantId();

        List<MemberCardFailureRecord> memberCardFailureRecordList = memberCardFailureRecordMapper.queryFailureMemberCard(uid, tenantId, offset, size);
        if (CollectionUtils.isEmpty(memberCardFailureRecordList)) {
            return R.ok();
        }

        for (MemberCardFailureRecord memberCardFailureRecord : memberCardFailureRecordList) {
            if (Objects.nonNull(memberCardFailureRecord.getBatteryType())) {
                Integer batteryType = BatteryConstant.acquireBattery(memberCardFailureRecord.getBatteryType());
                memberCardFailureRecord.setBatteryType(batteryType.toString());
            }
        }
        return R.ok();
    }
}

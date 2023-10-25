package com.xiliulou.electricity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.EnableMemberCardRecord;
import com.xiliulou.electricity.enums.BatteryMemberCardBusinessTypeEnum;
import com.xiliulou.electricity.mapper.EnableMemberCardRecordMapper;
import com.xiliulou.electricity.query.EnableMemberCardRecordQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.EnableMemberCardRecordService;
import com.xiliulou.electricity.vo.EnableMemberCardRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 启用套餐(TEnableMemberCardRecord)实体类
 *
 * @author makejava
 * @since 2022-11-17 16:00:45
 */
@Service("enableMemberCardRecordService")
@Slf4j
public class EnableMemberCardRecordServiceImpl implements EnableMemberCardRecordService {
    @Resource
    private EnableMemberCardRecordMapper enableMemberCardRecordMapper;

    @Autowired
    RedisService redisService;
    
    @Resource
    private BatteryMemberCardService batteryMemberCardService;


    @Override
    public R insert(EnableMemberCardRecord enableMemberCardRecord) {
        return R.ok(enableMemberCardRecordMapper.insert(enableMemberCardRecord));
    }

    @Override
    public Integer update(EnableMemberCardRecord enableMemberCardRecord) {
        return enableMemberCardRecordMapper.updateById(enableMemberCardRecord);
    }

    @Slave
    @Override
    public R queryList(EnableMemberCardRecordQuery enableMemberCardRecordQuery) {
        List<EnableMemberCardRecordVO> enableMemberCardRecordVOList = Lists.newArrayList();
        List<EnableMemberCardRecord> enableMemberCardRecords = enableMemberCardRecordMapper.queryList(enableMemberCardRecordQuery);
        for(EnableMemberCardRecord enableMemberCardRecord : enableMemberCardRecords){
            EnableMemberCardRecordVO enableMemberCardRecordVO = new EnableMemberCardRecordVO();
            
            if(Objects.isNull(enableMemberCardRecord)){
                continue;
            }
            BeanUtils.copyProperties(enableMemberCardRecord, enableMemberCardRecordVO);
            Long packageId = enableMemberCardRecord.getMemberCardId();
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(packageId);
            
            if (Objects.nonNull(batteryMemberCard)) {
                enableMemberCardRecordVO.setBusinessType(batteryMemberCard.getBusinessType());
            } else {
                enableMemberCardRecordVO.setBusinessType(BatteryMemberCardBusinessTypeEnum.BUSINESS_TYPE_BATTERY.getCode());
            }
           
            enableMemberCardRecordVOList.add(enableMemberCardRecordVO);
        }
        
        return R.ok(enableMemberCardRecordVOList);
    }

    @Slave
    @Override
    public R queryCount(EnableMemberCardRecordQuery enableMemberCardRecordQuery) {
        return R.ok(enableMemberCardRecordMapper.queryCount(enableMemberCardRecordQuery));
    }

    @Override
    public EnableMemberCardRecord queryByDisableCardNO(String disableCardNO, Integer tenantId) {
        return enableMemberCardRecordMapper.selectOne(new LambdaQueryWrapper<EnableMemberCardRecord>().eq(EnableMemberCardRecord::getDisableMemberCardNo, disableCardNO).eq(EnableMemberCardRecord::getTenantId, tenantId));
    }

    @Override
    public EnableMemberCardRecord selectLatestByUid(Long uid) {
        return enableMemberCardRecordMapper.selectLatestByUid(uid);
    }
}

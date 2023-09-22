package com.xiliulou.electricity.service.impl;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.enterprise.CloudBeanUseRecord;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.enterprise.CloudBeanUseRecordMapper;
import com.xiliulou.electricity.query.enterprise.CloudBeanUseRecordQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.CloudBeanUseRecordService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.vo.enterprise.CloudBeanUseRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 云豆使用记录表(CloudBeanUseRecord)表服务实现类
 *
 * @author zzlong
 * @since 2023-09-18 10:35:13
 */
@Service("cloudBeanUseRecordService")
@Slf4j
public class CloudBeanUseRecordServiceImpl implements CloudBeanUseRecordService {
    @Resource
    private CloudBeanUseRecordMapper cloudBeanUseRecordMapper;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private BatteryMemberCardService batteryMemberCardService;

    @Override
    public CloudBeanUseRecord queryByIdFromDB(Long id) {
        return this.cloudBeanUseRecordMapper.queryById(id);
    }

    @Override
    public Integer insert(CloudBeanUseRecord cloudBeanUseRecord) {
        return this.cloudBeanUseRecordMapper.insert(cloudBeanUseRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(CloudBeanUseRecord cloudBeanUseRecord) {
        return this.cloudBeanUseRecordMapper.update(cloudBeanUseRecord);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.cloudBeanUseRecordMapper.deleteById(id) > 0;
    }

    @Override
    public Double selectCloudBeanByUidAndType(Long uid, Integer type) {
        return this.cloudBeanUseRecordMapper.selectCloudBeanByUidAndType(uid, type);
    }

    @Slave
    @Override
    public List<CloudBeanUseRecordVO> selectByUserPage(CloudBeanUseRecordQuery query) {
        List<CloudBeanUseRecord> list = this.cloudBeanUseRecordMapper.selectByUserPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        return list.stream().map(item -> {
            CloudBeanUseRecordVO cloudBeanUseRecordVO = new CloudBeanUseRecordVO();
            BeanUtils.copyProperties(item, cloudBeanUseRecordVO);

            UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
            if (Objects.nonNull(userInfo)) {
                cloudBeanUseRecordVO.setUsername(userInfo.getName());
                cloudBeanUseRecordVO.setPhone(userInfo.getPhone());
            }

            if (!Objects.equals(item.getPackageId(), NumberConstant.ZERO_L)) {
                BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(item.getPackageId());
                cloudBeanUseRecordVO.setBatteryMemberCard(Objects.isNull(batteryMemberCard) ? "" : batteryMemberCard.getName());
            }

            return cloudBeanUseRecordVO;
        }).collect(Collectors.toList());
    }

    @Slave
    @Override
    public CloudBeanUseRecordVO cloudBeanUseStatisticsByUid(CloudBeanUseRecordQuery query) {
        query.setSize(Long.MAX_VALUE);
        query.setOffset(NumberConstant.ZERO_L);

        List<CloudBeanUseRecord> list = this.cloudBeanUseRecordMapper.selectByUserPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        CloudBeanUseRecordVO cloudBeanUseRecordVO = new CloudBeanUseRecordVO();
        //支出
        BigDecimal expend = list.stream().filter(item -> Objects.equals(item.getType(), CloudBeanUseRecord.TYPE_PAY_MEMBERCARD) || Objects.equals(item.getType(), CloudBeanUseRecord.TYPE_ADMIN_DEDUCT)).map(CloudBeanUseRecord::getBeanAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        //收入
        BigDecimal income = list.stream().filter(item -> !(Objects.equals(item.getType(), CloudBeanUseRecord.TYPE_PAY_MEMBERCARD) || Objects.equals(item.getType(), CloudBeanUseRecord.TYPE_ADMIN_DEDUCT))).map(CloudBeanUseRecord::getBeanAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        cloudBeanUseRecordVO.setIncome(income);
        cloudBeanUseRecordVO.setExpend(expend);

        return cloudBeanUseRecordVO;
    }
}

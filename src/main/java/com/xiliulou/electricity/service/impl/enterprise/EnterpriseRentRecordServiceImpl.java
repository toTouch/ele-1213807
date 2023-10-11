package com.xiliulou.electricity.service.impl.enterprise;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.enterprise.EnterpriseRentRecord;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseRentRecordMapper;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseRentRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * 用户使用记录表(EnterpriseRentRecord)表服务实现类
 *
 * @author zzlong
 * @since 2023-10-10 20:03:40
 */
@Service("enterpriseRentRecordService")
@Slf4j
public class EnterpriseRentRecordServiceImpl implements EnterpriseRentRecordService {
    
    @Resource
    private EnterpriseRentRecordMapper enterpriseRentRecordMapper;
    
    @Autowired
    private UserBatteryMemberCardService userBatteryMemberCardService;
    
    @Autowired
    EnterpriseChannelUserService enterpriseChannelUserService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EnterpriseRentRecord queryByIdFromDB(Long id) {
        return this.enterpriseRentRecordMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EnterpriseRentRecord queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 修改数据
     *
     * @param enterpriseRentRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EnterpriseRentRecord enterpriseRentRecord) {
        return this.enterpriseRentRecordMapper.update(enterpriseRentRecord);
    }
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.enterpriseRentRecordMapper.deleteById(id) > 0;
    }
    
    @Override
    public List<EnterpriseRentRecord> selectByUidAndTime(Long uid, long startTime) {
        return this.enterpriseRentRecordMapper.selectByUidAndTime(uid, startTime);
    }
    
    @Override
    public int deleteByUid(Long uid) {
        return this.enterpriseRentRecordMapper.deleteByUid(uid);
    }
    
    @Override
    public List<EnterpriseRentRecord> selectByUid(Long uid) {
        return this.enterpriseRentRecordMapper.selectList(new LambdaQueryWrapper<EnterpriseRentRecord>().eq(EnterpriseRentRecord::getUid,uid));
    }
    
    @Override
    public void saveEnterpriseRentRecord(Long uid) {
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(uid);
        if(Objects.isNull(enterpriseChannelUser)){
            log.warn("SAVE RENT RECORD WARN!not found enterpriseChannelUser,uid={}",uid);
            return;
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if(Objects.isNull(userBatteryMemberCard)){
            log.warn("SAVE RENT RECORD WARN!not found userBatteryMemberCard,uid={}",uid);
            return;
        }
    
        EnterpriseRentRecord enterpriseRentRecord = new EnterpriseRentRecord();
        enterpriseRentRecord.setUid(uid);
        enterpriseRentRecord.setRentMembercardOrderId(userBatteryMemberCard.getOrderId());
//        enterpriseRentRecord.setRentMid(userBatteryMemberCard.getMemberCardId());
//        enterpriseRentRecord.setOrderExpireTime(userBatteryMemberCard.getOrderExpireTime());
        enterpriseRentRecord.setRentTime(System.currentTimeMillis());
        enterpriseRentRecord.setCreateTime(System.currentTimeMillis());
        enterpriseRentRecord.setUpdateTime(System.currentTimeMillis());
        enterpriseRentRecord.setTenantId(userBatteryMemberCard.getTenantId());
        this.enterpriseRentRecordMapper.insert(enterpriseRentRecord);
    }
    
    @Override
    public void saveEnterpriseReturnRecord(Long uid) {
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(uid);
        if(Objects.isNull(enterpriseChannelUser)){
            log.warn("SAVE RENT RECORD WARN!not found enterpriseChannelUser,uid={}",uid);
            return;
        }
    
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if(Objects.isNull(userBatteryMemberCard)){
            log.warn("SAVE RENT RECORD WARN!not found userBatteryMemberCard,uid={}",uid);
            return;
        }
    
        EnterpriseRentRecord enterpriseReturnRecord = this.enterpriseRentRecordMapper.selectLatestRentRecord(uid);
        if(Objects.isNull(enterpriseReturnRecord)){
            log.error("SAVE RENT RECORD WARN!not found enterpriseReturnRecord,uid={}",uid);
            return;
        }
    
        EnterpriseRentRecord enterpriseReturnRecordUpdate = new EnterpriseRentRecord();
        enterpriseReturnRecordUpdate.setId(enterpriseReturnRecord.getId());
        enterpriseReturnRecordUpdate.setReturnMembercardOrderId(userBatteryMemberCard.getOrderId());
//        enterpriseReturnRecordUpdate.setReturnMid(userBatteryMemberCard.getId());
        enterpriseReturnRecordUpdate.setReturnTime(System.currentTimeMillis());
        enterpriseReturnRecordUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(enterpriseReturnRecordUpdate);
    }
}

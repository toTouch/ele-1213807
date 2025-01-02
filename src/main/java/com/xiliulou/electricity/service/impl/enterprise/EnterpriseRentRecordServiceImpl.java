package com.xiliulou.electricity.service.impl.enterprise;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.DateFormatConstant;
import com.xiliulou.electricity.constant.enterprise.EnterpriseRentRecordConstant;
import com.xiliulou.electricity.entity.ElectricityMemberCardOrder;
import com.xiliulou.electricity.entity.UserBatteryMemberCard;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.enterprise.AnotherPayMembercardRecord;
import com.xiliulou.electricity.entity.enterprise.EnterpriseChannelUser;
import com.xiliulou.electricity.entity.enterprise.EnterpriseRentRecord;
import com.xiliulou.electricity.entity.enterprise.EnterpriseRentRecordDetail;
import com.xiliulou.electricity.enums.enterprise.PackageOrderTypeEnum;
import com.xiliulou.electricity.enums.enterprise.RenewalStatusEnum;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseRentRecordMapper;
import com.xiliulou.electricity.service.ElectricityMemberCardOrderService;
import com.xiliulou.electricity.service.UserBatteryMemberCardService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.enterprise.AnotherPayMembercardRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseChannelUserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseRentRecordDetailService;
import com.xiliulou.electricity.service.enterprise.EnterpriseRentRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    
    @Resource
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    
    @Resource
    private AnotherPayMembercardRecordService anotherPayMembercardRecordService;
    
    @Resource
    private EnterpriseRentRecordDetailService enterpriseRentRecordDetailService;
    
    @Resource
    private UserInfoService userInfoService;
    
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
        return this.enterpriseRentRecordMapper.updateById(enterpriseRentRecord);
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
    public int deleteByUid(Long uid) {
        return this.enterpriseRentRecordMapper.deleteByUid(uid);
    }
    
    @Slave
    @Override
    public List<EnterpriseRentRecord> selectByUid(Long uid) {
        return this.enterpriseRentRecordMapper.selectByUid(uid);
    }
    
    @Override
    public int createEnterpriseRecordDetail(Integer tenantId) {
        log.info("SAVE RENT RECORD DETAIL START!");
        // 查询已经退电完成的记录
        List<EnterpriseRentRecord> enterpriseRentRecords = queryListAlreadyReturn(tenantId);
        
        if (ObjectUtils.isEmpty(enterpriseRentRecords)) {
            log.info("SAVE RENT RECORD DETAIL WARN!not found enterpriseRentRecords");
            return 0;
        }
    
        Map<Long, List<EnterpriseRentRecord>> rentRecordMap = enterpriseRentRecords.stream().collect(Collectors.groupingBy(EnterpriseRentRecord::getUid));
        rentRecordMap.forEach((key, value) -> {
            // 根据id进行排序
            List<EnterpriseRentRecord> rentRecords = value.stream().sorted(Comparator.comparing(EnterpriseRentRecord::getId)).collect(Collectors.toList());
            
            for (EnterpriseRentRecord enterpriseReturnRecord : rentRecords) {
                // 检测是否已经存在退电记录
                int count = enterpriseRentRecordDetailService.existsByRentRecordId(enterpriseReturnRecord.getId());
                if (count > 0) {
                    log.info("SAVE RENT RECORD DETAIL WARN!detail exists id={}", enterpriseReturnRecord.getId());
                    continue;
                }
                // 获取退电订单id
                Long uid = enterpriseReturnRecord.getUid();
        
                EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(uid);
                if (Objects.isNull(enterpriseChannelUser)) {
                    log.warn("SAVE RENT RECORD DETAIL WARN!not found enterpriseChannelUser,uid={}", uid);
                    continue;
                }
        
                UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
                if (Objects.isNull(userBatteryMemberCard)) {
                    log.warn("SAVE RENT RECORD DETAIL WARN!not found userBatteryMemberCard,uid={}", uid);
                    continue;
                }
    
                UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
                if (Objects.isNull(userBatteryMemberCard)) {
                    log.warn("SAVE RENT RECORD DETAIL WARN!not found userInfo,uid={}", uid);
                    continue;
                }
    
                enterpriseReturnRecord.setTenantId(userInfo.getTenantId());
                // 退电订单设置为 用户当前生效的订单号
                userBatteryMemberCard.setOrderId(enterpriseReturnRecord.getReturnMembercardOrderId());
        
                EnterpriseRentRecord enterpriseReturnRecordUpdate = new EnterpriseRentRecord();
                enterpriseReturnRecordUpdate.setId(enterpriseReturnRecord.getId());
                enterpriseReturnRecordUpdate.setUpdateTime(System.currentTimeMillis());
                enterpriseReturnRecordUpdate.setTenantId(userInfo.getTenantId());
        
                // 获取组退电对应的套餐类型
                List<EnterpriseRentRecordDetail> enterpriseRentRecordDetailList = new ArrayList<>();
                Integer orderType = getRentOrderType(enterpriseReturnRecord, userBatteryMemberCard, uid, enterpriseRentRecordDetailList, enterpriseChannelUser.getEnterpriseId());
                enterpriseReturnRecordUpdate.setRentOrderType(orderType);
        
                this.enterpriseRentRecordMapper.updateById(enterpriseReturnRecordUpdate);
                
                if (ObjectUtils.isNotEmpty(enterpriseRentRecordDetailList)) {
                    // 根据退电日期进行排序
                    enterpriseRentRecordDetailList = enterpriseRentRecordDetailList.stream().sorted(Comparator.comparing(EnterpriseRentRecordDetail::getReturnTime)).collect(Collectors.toList());
                    // 批量保存详情记录
                    enterpriseRentRecordDetailService.batchInsert(enterpriseRentRecordDetailList);
                }
            }
        });
        
        log.info("SAVE RENT RECORD DETAIL END!");
        
        return 0;
    }
    
    @Slave
    private List<EnterpriseRentRecord> queryListAlreadyReturn(Integer tenantId) {
        return enterpriseRentRecordMapper.selectListAlreadyReturn(tenantId);
    }
    
    @Override
    public void saveEnterpriseRentRecord(Long uid) {
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(uid);
        if (Objects.isNull(enterpriseChannelUser)) {
            log.warn("SAVE RENT RECORD WARN!not found enterpriseChannelUser,uid={}", uid);
            return;
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("SAVE RENT RECORD WARN!not found userBatteryMemberCard,uid={}", uid);
            return;
        }
        
        // 判断自主续费是否打开
        if (!Objects.equals(enterpriseChannelUser.getRenewalStatus(), RenewalStatusEnum.RENEWAL_STATUS_NOT_BY_SELF.getCode())) {
            log.warn("channel user enable member card handler!renewal status is open,uid={}", uid);
            return;
        }
    
        UserInfo userInfo = userInfoService.queryByUidFromCache(uid);
        // 判断自主续费是否打开
        if (Objects.isNull(userInfo)) {
            log.warn("SAVE RENT RECORD WARN!not found userInfo,uid={}", uid);
            return;
        }
        
        EnterpriseRentRecord enterpriseRentRecord = new EnterpriseRentRecord();
        enterpriseRentRecord.setUid(uid);
        enterpriseRentRecord.setRentMembercardOrderId(userBatteryMemberCard.getOrderId());
        enterpriseRentRecord.setRentTime(System.currentTimeMillis());
        enterpriseRentRecord.setCreateTime(System.currentTimeMillis());
        enterpriseRentRecord.setUpdateTime(System.currentTimeMillis());
        enterpriseRentRecord.setTenantId(userInfo.getTenantId());
        this.enterpriseRentRecordMapper.insert(enterpriseRentRecord);
    }
    
    @Override
    public void saveEnterpriseReturnRecord(Long uid) {
        EnterpriseChannelUser enterpriseChannelUser = enterpriseChannelUserService.selectByUid(uid);
        if (Objects.isNull(enterpriseChannelUser)) {
            log.warn("SAVE RENT RECORD WARN!not found enterpriseChannelUser,uid={}", uid);
            return;
        }
        
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(uid);
        if (Objects.isNull(userBatteryMemberCard)) {
            log.warn("SAVE RENT RECORD WARN!not found userBatteryMemberCard,uid={}", uid);
            return;
        }
        
        EnterpriseRentRecord enterpriseReturnRecord = this.enterpriseRentRecordMapper.selectLatestRentRecord(uid);
        if (Objects.isNull(enterpriseReturnRecord)) {
            log.error("SAVE RENT RECORD WARN!not found enterpriseReturnRecord,uid={}", uid);
            return;
        }
        
        EnterpriseRentRecord enterpriseReturnRecordUpdate = new EnterpriseRentRecord();
        enterpriseReturnRecordUpdate.setId(enterpriseReturnRecord.getId());
        enterpriseReturnRecordUpdate.setReturnMembercardOrderId(userBatteryMemberCard.getOrderId());
        enterpriseReturnRecordUpdate.setReturnTime(System.currentTimeMillis());
        enterpriseReturnRecordUpdate.setUpdateTime(System.currentTimeMillis());
        enterpriseReturnRecordUpdate.setTenantId(userBatteryMemberCard.getTenantId());
        enterpriseReturnRecord.setReturnTime(enterpriseReturnRecordUpdate.getReturnTime());
        
        // 获取组退电对应的套餐类型
        List<EnterpriseRentRecordDetail> enterpriseRentRecordDetailList = new ArrayList<>();
        Integer orderType = getRentOrderType(enterpriseReturnRecord, userBatteryMemberCard, uid, enterpriseRentRecordDetailList, enterpriseChannelUser.getEnterpriseId());
        enterpriseReturnRecordUpdate.setRentOrderType(orderType);
        
        this.enterpriseRentRecordMapper.updateById(enterpriseReturnRecordUpdate);
        
        if (ObjectUtils.isNotEmpty(enterpriseRentRecordDetailList)) {
            // 根据退电日期进行排序
            enterpriseRentRecordDetailList = enterpriseRentRecordDetailList.stream().sorted(Comparator.comparing(EnterpriseRentRecordDetail::getReturnTime)).collect(Collectors.toList());
            // 批量保存详情记录
            enterpriseRentRecordDetailService.batchInsert(enterpriseRentRecordDetailList);
        }
    }
    
    private Integer getRentOrderType(EnterpriseRentRecord enterpriseReturnRecord, UserBatteryMemberCard userBatteryMemberCard, Long uid,
            List<EnterpriseRentRecordDetail> enterpriseRentRecordDetailList, Long enterpriseId) {
        List<String> orderIdList = new ArrayList<>();
        Map<String, Integer> orderTypeMap = new HashMap<>();
        
        orderIdList.add(enterpriseReturnRecord.getRentMembercardOrderId());
        orderIdList.add(userBatteryMemberCard.getOrderId());
        List<ElectricityMemberCardOrder> electricityMemberCardOrderList = electricityMemberCardOrderService.queryListByOrderIds(orderIdList);
        
        // 找不到对应的租电订单和退电套餐对应的订单
        if (ObjectUtil.isEmpty(electricityMemberCardOrderList)) {
            log.error("save enterprise return record error!, order not find orderIdList={}, uid={}, recordId={}", orderIdList, enterpriseReturnRecord.getUid(), uid,
                    enterpriseReturnRecord.getId());
            return EnterpriseRentRecordConstant.ORDER_TYPE_OTHER;
        }
        
        orderTypeMap = electricityMemberCardOrderList.stream()
                .collect(Collectors.toMap(ElectricityMemberCardOrder::getOrderId, ElectricityMemberCardOrder::getOrderType, (key, key1) -> key1));
        
        Integer rentOrderType = orderTypeMap.get(enterpriseReturnRecord.getRentMembercardOrderId());
        Integer returnOrderType = orderTypeMap.get(userBatteryMemberCard.getOrderId());
        long currentTimeMillis = System.currentTimeMillis();
        
        // 租电类型为普通换电订单，退电类型为普通换电订单
        if (Objects.equals(rentOrderType, PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_NORMAL.getCode()) && Objects.equals(returnOrderType,
                PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_NORMAL.getCode())) {
            return EnterpriseRentRecordConstant.ORDER_TYPE_ELE;
        }
        
        // 租电订单为普通换电订单，退电订单为企业订单
        // 每个企业套餐对应的开始使用的时间段均为套餐生效时间，不用处理天数券导致的回收芸豆问题
        if (Objects.equals(rentOrderType, PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_NORMAL.getCode()) && Objects.equals(returnOrderType,
                PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_ENTERPRISE.getCode())) {
            return handlerEleRentDetail(enterpriseReturnRecord, userBatteryMemberCard, uid, enterpriseRentRecordDetailList, currentTimeMillis, enterpriseId);
        }
        
        // 租电和换电都为企业订单
        // 使用时间段的开始时间可能为天数券时间范围，需要判断处理
        if (Objects.equals(rentOrderType, PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_ENTERPRISE.getCode()) && Objects.equals(returnOrderType,
                PackageOrderTypeEnum.PACKAGE_ORDER_TYPE_ENTERPRISE.getCode())) {
            return handlerEnterpriseRentDetail(enterpriseReturnRecord, userBatteryMemberCard, uid, enterpriseRentRecordDetailList, currentTimeMillis, enterpriseId);
        }
        
        return EnterpriseRentRecordConstant.ORDER_TYPE_OTHER;
    }
    
    /**
     * 处理企业套餐换电详情
     *
     * @param enterpriseReturnRecord
     * @param userBatteryMemberCard
     * @param uid
     * @param enterpriseRentRecordDetailList
     * @param currentTimeMillis
     * @param enterpriseId
     * @return
     */
    private Integer handlerEnterpriseRentDetail(EnterpriseRentRecord enterpriseReturnRecord, UserBatteryMemberCard userBatteryMemberCard, Long uid, List<EnterpriseRentRecordDetail> enterpriseRentRecordDetailList, long currentTimeMillis,
            Long enterpriseId) {
        Integer orderType = EnterpriseRentRecordConstant.ORDER_TYPE_ENTERPRISE;
        
        // 组退订单的id相同
        if (Objects.equals(enterpriseReturnRecord.getRentMembercardOrderId(), userBatteryMemberCard.getOrderId())) {
            AnotherPayMembercardRecord payMemberCardRecord = anotherPayMembercardRecordService.selectByOrderId(enterpriseReturnRecord.getRentMembercardOrderId());
            if (Objects.isNull(payMemberCardRecord)) {
                log.error("save enterprise return record error!enterprise order pay record not find recordId={}, uid={}, orderId={}", enterpriseReturnRecord.getId(), uid,
                        userBatteryMemberCard.getOrderId());
                return orderType;
            }
            
            // 计算最后一个套餐的详情
            Long endTime = Objects.nonNull(payMemberCardRecord) && Objects.nonNull(payMemberCardRecord.getEndTime()) && Objects.nonNull(enterpriseReturnRecord.getReturnTime())
                    && enterpriseReturnRecord.getReturnTime() > payMemberCardRecord.getEndTime() ? payMemberCardRecord.getEndTime() : enterpriseReturnRecord.getReturnTime();
            
            EnterpriseRentRecordDetail detail = EnterpriseRentRecordDetail.builder().rentTime(enterpriseReturnRecord.getRentTime()).returnTime(endTime).rentRecordId(enterpriseReturnRecord.getId())
                    .returnDate(DateUtil.format(new Date(endTime), DateFormatConstant.MONTH_DAY_DATE_FORMAT)).createTime(currentTimeMillis).updateTime(currentTimeMillis).enterpriseId(enterpriseId)
                    .orderId(payMemberCardRecord.getOrderId()).uid(uid).tenantId(enterpriseReturnRecord.getTenantId()).build();
            
            // payMemberCardRecord中记录的是每一个套餐记录中不包括天数券增加的天数的时间，若详情数据的开始时间晚于其结束时间，则为在天数券时间范围内的详情，不予保存
            if (detail.getRentTime() < payMemberCardRecord.getEndTime()) {
                enterpriseRentRecordDetailList.add(detail);
            }
        } else {
            // 计算第一个生效的企业套餐id, 及开始时间
            List<AnotherPayMembercardRecord> anotherPayMembercardRecords = anotherPayMembercardRecordService.selectByUid(uid);
            if (ObjectUtils.isEmpty(anotherPayMembercardRecords)) {
                log.error("save enterprise return record error!enterprise order pay record is empty recordId={}, uid={}", enterpriseReturnRecord.getId(), uid);
                return orderType;
            }
            
            Optional<AnotherPayMembercardRecord> optional = anotherPayMembercardRecords.stream()
                    .filter(item -> Objects.equals(item.getOrderId(), userBatteryMemberCard.getOrderId())).findFirst();
            AnotherPayMembercardRecord lastPayMemberCardRecord = null;
            
            if (optional.isPresent()) {
                lastPayMemberCardRecord = optional.get();
                // 计算最后一个套餐的详情，租退套餐不同时，最后一个套餐一定不是租开始的时间，不用处理天数券的问题
                Long endTime = Objects.nonNull(lastPayMemberCardRecord) && Objects.nonNull(lastPayMemberCardRecord.getEndTime()) && Objects.nonNull(
                        enterpriseReturnRecord.getReturnTime()) && enterpriseReturnRecord.getReturnTime() > lastPayMemberCardRecord.getEndTime()
                        ? lastPayMemberCardRecord.getEndTime() : enterpriseReturnRecord.getReturnTime();
                
                EnterpriseRentRecordDetail detail = EnterpriseRentRecordDetail.builder().rentTime(lastPayMemberCardRecord.getBeginTime()).returnTime(endTime).rentRecordId(enterpriseReturnRecord.getId())
                        .returnDate(DateUtil.format(new Date(endTime), DateFormatConstant.MONTH_DAY_DATE_FORMAT)).createTime(currentTimeMillis).updateTime(currentTimeMillis).enterpriseId(enterpriseId)
                        .orderId(lastPayMemberCardRecord.getOrderId()).uid(uid).tenantId(enterpriseReturnRecord.getTenantId()).build();
                
                enterpriseRentRecordDetailList.add(detail);
            } else {
                log.error("save enterprise return record error!enterprise last order pay record not find recordId={}, uid={}, orderId={}", enterpriseReturnRecord.getId(), uid,
                        userBatteryMemberCard.getOrderId());
            }
            
            // 判断租电和退电是否在同一天 去最后一个订单的开始和结束时间
            String rentDate = DateUtil.format(new Date(enterpriseReturnRecord.getRentTime()), DateFormatConstant.MONTH_DAY_DATE_FORMAT);
            String returnDate = DateUtil.format(new Date(enterpriseReturnRecord.getReturnTime()), DateFormatConstant.MONTH_DAY_DATE_FORMAT);
            if (Objects.equals(rentDate, returnDate)) {
                return orderType;
            }
            
            Optional<AnotherPayMembercardRecord> optionalFirst = anotherPayMembercardRecords.stream()
                    .filter(item -> Objects.equals(item.getOrderId(), enterpriseReturnRecord.getRentMembercardOrderId())).findFirst();
            AnotherPayMembercardRecord payMemberCardRecordFirst = null;
            
            if (optionalFirst.isPresent()) {
                payMemberCardRecordFirst = optionalFirst.get();
                // 计算第一个套餐的详情
                Long endTime = payMemberCardRecordFirst.getEndTime();
                
                EnterpriseRentRecordDetail detail = EnterpriseRentRecordDetail.builder().rentTime(enterpriseReturnRecord.getRentTime()).returnTime(endTime).rentRecordId(enterpriseReturnRecord.getId())
                        .returnDate(DateUtil.format(new Date(endTime), DateFormatConstant.MONTH_DAY_DATE_FORMAT)).createTime(currentTimeMillis).updateTime(currentTimeMillis).enterpriseId(enterpriseId)
                        .orderId(payMemberCardRecordFirst.getOrderId()).uid(uid).tenantId(enterpriseReturnRecord.getTenantId()).build();
                
                // 第一个套餐开始时可能是天数券时间范围内，需要处理，其他的中间的套餐和结尾的套餐开始时间均为套餐生效时间，不需判断处理
                if (detail.getRentTime() < endTime) {
                    enterpriseRentRecordDetailList.add(detail);
                }
            } else {
                log.error("save enterprise return record error!enterprise first order pay record not find recordId={}, uid={}, orderId={}", enterpriseReturnRecord.getId(), uid,
                        enterpriseReturnRecord.getReturnMembercardOrderId());
            }
            
            // 计算中间的订单数量
            if (Objects.nonNull(payMemberCardRecordFirst) && Objects.nonNull(lastPayMemberCardRecord)) {
                AnotherPayMembercardRecord finalLastPayMemberCardRecord = lastPayMemberCardRecord;
                AnotherPayMembercardRecord finalPayMemberCardRecordFirst = payMemberCardRecordFirst;
                
                List<AnotherPayMembercardRecord> middleRecordList = anotherPayMembercardRecords.stream()
                        .filter(item -> item.getId() < finalLastPayMemberCardRecord.getId() && item.getId() > finalPayMemberCardRecordFirst.getId())
                        .collect(Collectors.toList());
                
                if (ObjectUtils.isNotEmpty(middleRecordList)) {
                    middleRecordList.stream().forEach(firstPayRecord -> {
                        EnterpriseRentRecordDetail detail = EnterpriseRentRecordDetail.builder().rentTime(firstPayRecord.getBeginTime()).returnTime(firstPayRecord.getEndTime()).enterpriseId(enterpriseId)
                                .returnDate(DateUtil.format(new Date(firstPayRecord.getEndTime()), DateFormatConstant.MONTH_DAY_DATE_FORMAT)).createTime(currentTimeMillis).rentRecordId(enterpriseReturnRecord.getId())
                                .updateTime(currentTimeMillis).orderId(firstPayRecord.getOrderId()).uid(uid).tenantId(enterpriseReturnRecord.getTenantId()).build();
                        
                        enterpriseRentRecordDetailList.add(detail);
                    });
                }
            }
        }
        
        return orderType;
    }
    
    /**
     * 处理会员的换电场景
     * @return
     */
    private Integer handlerEleRentDetail(EnterpriseRentRecord enterpriseReturnRecord, UserBatteryMemberCard userBatteryMemberCard, Long uid,
            List<EnterpriseRentRecordDetail> enterpriseRentRecordDetailList, long currentTimeMillis, Long enterpriseId) {
        Integer orderType = EnterpriseRentRecordConstant.ORDER_TYPE_ELE_ENTERPRISE;
        
        // 计算第一个生效的企业套餐id, 及开始时间
        List<AnotherPayMembercardRecord> anotherPayMemberCardRecords = anotherPayMembercardRecordService.selectByUid(uid);
        if (ObjectUtils.isEmpty(anotherPayMemberCardRecords)) {
            log.error("save enterprise return record error! pay record is empty recordId={}, uid={}", enterpriseReturnRecord.getId(), uid);
            return orderType;
        }
        
        Optional<AnotherPayMembercardRecord> optional = anotherPayMemberCardRecords.stream()
                .filter(item -> Objects.equals(item.getOrderId(), userBatteryMemberCard.getOrderId())).findFirst();
        if (!optional.isPresent()) {
            log.error("save enterprise return record error! pay record not find recordId={}, uid={}, orderId={}", enterpriseReturnRecord.getId(), uid,
                    userBatteryMemberCard.getOrderId());
            return orderType;
        }
        
        // 最后一个套餐的购买记录
        AnotherPayMembercardRecord payMemberCardRecordLast = optional.get();
        
        List<AnotherPayMembercardRecord> payMemberCardRecords = anotherPayMemberCardRecords.stream().filter(item -> item.getId() < payMemberCardRecordLast.getId())
                .sorted(Comparator.comparing(AnotherPayMembercardRecord::getId)).collect(Collectors.toList());
        
        if (ObjectUtils.isNotEmpty(payMemberCardRecords)) {
            payMemberCardRecords.stream().forEach(firstPayRecord -> {
                EnterpriseRentRecordDetail detail = EnterpriseRentRecordDetail.builder().rentTime(firstPayRecord.getBeginTime()).returnTime(firstPayRecord.getEndTime()).rentRecordId(enterpriseReturnRecord.getId())
                        .returnDate(DateUtil.format(new Date(firstPayRecord.getEndTime()), DateFormatConstant.MONTH_DAY_DATE_FORMAT)).createTime(currentTimeMillis).enterpriseId(enterpriseId)
                        .updateTime(currentTimeMillis).orderId(firstPayRecord.getOrderId()).uid(uid).tenantId(enterpriseReturnRecord.getTenantId()).build();
                
                enterpriseRentRecordDetailList.add(detail);
            });
        }
        
        // 计算最后一个套餐的详情
        Long endTime =
                Objects.nonNull(payMemberCardRecordLast) && Objects.nonNull(payMemberCardRecordLast.getEndTime()) && Objects.nonNull(enterpriseReturnRecord.getReturnTime())
                        && enterpriseReturnRecord.getReturnTime() > payMemberCardRecordLast.getEndTime() ? payMemberCardRecordLast.getEndTime()
                        : enterpriseReturnRecord.getReturnTime();
        
        EnterpriseRentRecordDetail detail = EnterpriseRentRecordDetail.builder().rentTime(payMemberCardRecordLast.getBeginTime()).returnTime(endTime).enterpriseId(enterpriseId)
                .returnDate(DateUtil.format(new Date(endTime), DateFormatConstant.MONTH_DAY_DATE_FORMAT)).createTime(currentTimeMillis).updateTime(currentTimeMillis).rentRecordId(enterpriseReturnRecord.getId())
                .orderId(payMemberCardRecordLast.getOrderId()).uid(uid).tenantId(enterpriseReturnRecord.getTenantId()).build();
        
        enterpriseRentRecordDetailList.add(detail);
        
        return orderType;
    }
}

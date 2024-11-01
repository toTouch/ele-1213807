package com.xiliulou.electricity.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.dto.ServicePhoneDTO;
import com.xiliulou.electricity.entity.ServicePhone;
import com.xiliulou.electricity.mapper.ServicePhoneMapper;
import com.xiliulou.electricity.request.ServicePhoneRequest;
import com.xiliulou.electricity.service.ServicePhoneService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author HeYafeng
 * @date 2024/10/24 17:40:54
 */
@Slf4j
@Service
public class ServicePhoneServiceImpl implements ServicePhoneService {
    
    @Resource
    private ServicePhoneMapper servicePhoneMapper;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private OperateRecordUtil operateRecordUtil;
    
    @Override
    public R insertOrUpdate(List<ServicePhoneRequest> requestPhoneList) {
        Integer tenantId = TenantContextHolder.getTenantId();
        boolean result = redisService.setNx(CacheConstant.SERVICE_PHONE_LOCK_KEY + tenantId, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            List<ServicePhone> servicePhonesExist = null;
            if (CollectionUtils.isEmpty(requestPhoneList)) {
                // 查询旧的手机号
                servicePhonesExist = this.listByTenantIdFromCache(tenantId);
                
                // 删除所有
                Integer delete = servicePhoneMapper.deleteByTenantId(tenantId);
                if (delete > 0) {
                    // 清除新缓存
                    redisService.delete(CacheConstant.SERVICE_PHONE + tenantId);
                    // 清除旧缓存
                    redisService.delete(CacheConstant.CACHE_SERVICE_PHONE + tenantId);
                    
                    if (CollectionUtils.isNotEmpty(servicePhonesExist)) {
                        servicePhonesExist.forEach(servicePhone -> {
                            this.sendOperateRecord(servicePhone.getPhone(), servicePhone.getRemark(), null);
                        });
                    }
                    
                }
                return R.ok();
            }
            
            List<ServicePhone> insertList = null;
            List<ServicePhone> updateList = null;
            List<Long> deleteList = null;
            Map<Long, ServicePhone> existMap = null;
            ServicePhoneDTO servicePhoneDTO = this.handlePhones(servicePhonesExist, requestPhoneList, tenantId);
            if (Objects.nonNull(servicePhoneDTO)) {
                insertList = servicePhoneDTO.getInsertList();
                updateList = servicePhoneDTO.getUpdateList();
                deleteList = servicePhoneDTO.getDeleteList();
                existMap = servicePhoneDTO.getExistMap();
            }
            
            boolean flag = this.handleInsert(insertList);
            Boolean update = this.handleUpdate(updateList, tenantId);
            if (!flag) {
                flag = update;
            }
            Boolean delete = this.handleDelete(deleteList, existMap, tenantId);
            if (!flag) {
                flag = delete;
            }
            
            // 清除新缓存
            if (flag) {
                redisService.delete(CacheConstant.SERVICE_PHONE + tenantId);
            }
            return R.ok();
        } finally {
            redisService.delete(CacheConstant.SERVICE_PHONE_LOCK_KEY + tenantId);
        }
    }
    
    private Boolean handleInsert(List<ServicePhone> insertList) {
        boolean flag = false;
        if (CollectionUtil.isNotEmpty(insertList)) {
            Integer insert = servicePhoneMapper.batchInsert(insertList);
            if (insert > 0) {
                flag = true;
                String oldPhone = "";
                String oldRemark = "";
                for (ServicePhone servicePhone : insertList) {
                    this.sendOperateRecord(oldPhone, oldRemark, servicePhone);
                }
            }
        }
        
        return flag;
    }
    
    private Boolean handleUpdate(List<ServicePhone> updateList, Integer tenantId) {
        boolean flag = false;
        if (CollectionUtil.isNotEmpty(updateList)) {
            List<Long> updateIds = updateList.stream().map(ServicePhone::getId).collect(Collectors.toList());
            List<ServicePhone> oldServicePhoneList = this.listByIds(updateIds);
            Map<Long, ServicePhone> oldServicePhoneMap = oldServicePhoneList.stream().collect(Collectors.toMap(ServicePhone::getId, servicePhone -> servicePhone));
            
            for (ServicePhone servicePhone : updateList) {
                Integer update = servicePhoneMapper.update(servicePhone);
                if (update > 0) {
                    flag = true;
                    String newPhone = servicePhone.getPhone();
                    String oldPhone = "";
                    String oldRemark = "";
                    if (MapUtils.isNotEmpty(oldServicePhoneMap) && oldServicePhoneMap.containsKey(servicePhone.getId())) {
                        ServicePhone oldServicePhone = oldServicePhoneMap.get(servicePhone.getId());
                        if (Objects.nonNull(oldServicePhone)) {
                            oldPhone = oldServicePhone.getPhone();
                            oldRemark = oldServicePhone.getRemark();
                        }
                    }
                    
                    // 更新旧缓存key的手机号
                    this.updateOldCache(tenantId, oldPhone, newPhone);
                    // 发送操作记录
                    if (Objects.equals(oldPhone, newPhone) && Objects.equals(oldRemark, servicePhone.getRemark())) {
                        continue;
                    }
                    this.sendOperateRecord(oldPhone, oldRemark, servicePhone);
                }
            }
        }
        
        return flag;
    }
    
    private Boolean handleDelete(List<Long> deleteList, Map<Long, ServicePhone> existMap, Integer tenantId) {
        boolean flag = false;
        if (CollectionUtils.isNotEmpty(deleteList)) {
            Integer delete = servicePhoneMapper.deleteByIds(deleteList);
            if (delete > 0) {
                flag = true;
                for (Long delId : deleteList) {
                    if (MapUtils.isNotEmpty(existMap) && existMap.containsKey(delId)) {
                        this.sendOperateRecord(existMap.get(delId).getPhone(), existMap.get(delId).getRemark(), null);
                    }
                }
            }
            // 清除旧缓存
            redisService.delete(CacheConstant.CACHE_SERVICE_PHONE + tenantId);
        }
        
        return flag;
    }
    
    private ServicePhoneDTO handlePhones(List<ServicePhone> servicePhonesExist, List<ServicePhoneRequest> requestPhoneList, Integer tenantId) {
        List<ServicePhone> insertList = new ArrayList<>();
        List<ServicePhone> updateList = new ArrayList<>();
        List<Long> deleteList = new ArrayList<>();
        Map<Long, ServicePhone> existMap = null;
        
        if (CollectionUtil.isNotEmpty(servicePhonesExist)) {
            existMap = servicePhonesExist.stream().collect(Collectors.toMap(ServicePhone::getId, servicePhone -> servicePhone));
            
            List<Long> requestPhoneIds = requestPhoneList.stream().filter(Objects::nonNull).filter(servicePhoneRequest -> Objects.nonNull(servicePhoneRequest.getId()))
                    .map(ServicePhoneRequest::getId).collect(Collectors.toList());
            
            deleteList = servicePhonesExist.stream().map(ServicePhone::getId).filter(id -> !requestPhoneIds.contains(id)).collect(Collectors.toList());
        }
        
        for (ServicePhoneRequest requestPhone : requestPhoneList) {
            Long id = requestPhone.getId();
            String phone = requestPhone.getPhone();
            String remark = requestPhone.getRemark();
            
            // 手机号为空的处理
            if (StringUtils.isBlank(phone)) {
                if (Objects.nonNull(id)) {
                    deleteList.add(id);
                }
                continue;
            }
            
            // 手机号不为空的处理
            if (Objects.nonNull(id)) {
                if (MapUtils.isNotEmpty(existMap) && existMap.containsKey(requestPhone.getId())) {
                    ServicePhone existPhone = existMap.get(requestPhone.getId());
                    // 手机号和文案有变化时才更新
                    if (!(Objects.equals(requestPhone.getPhone(), existPhone.getPhone()) && Objects.equals(requestPhone.getRemark(), existPhone.getRemark()))) {
                        updateList.add(ServicePhone.builder().phone(phone).remark(remark).id(id).tenantId(tenantId).updateTime(System.currentTimeMillis()).build());
                    }
                }
            } else {
                insertList.add(ServicePhone.builder().phone(phone).remark(remark).tenantId(tenantId).delFlag(CommonConstant.DEL_N).createTime(System.currentTimeMillis())
                        .updateTime(System.currentTimeMillis()).build());
            }
        }
        
        return ServicePhoneDTO.builder().insertList(insertList).updateList(updateList).deleteList(deleteList).existMap(existMap).build();
    }
    
    /**
     * 更新旧缓存key的手机号，因为可能别的地方还在调用
     */
    private void updateOldCache(Integer tenantId, String oldPhone, String newPhone) {
        if (redisService.hasKey(CacheConstant.CACHE_SERVICE_PHONE + tenantId)) {
            String oldPhoneCache = redisService.get(CacheConstant.CACHE_SERVICE_PHONE + tenantId);
            
            if (StringUtils.isNotBlank(oldPhone) && StringUtils.isNotBlank(oldPhoneCache) && Objects.equals(oldPhoneCache, oldPhone) && !Objects.equals(oldPhone, newPhone)) {
                redisService.set(CacheConstant.CACHE_SERVICE_PHONE + tenantId, newPhone);
            }
        }
    }
    
    private void sendOperateRecord(String oldPhone, String oldRemark, ServicePhone servicePhone) {
        Map<String, String> oldMap = new HashMap<>(2);
        String oldPhoneValue = StringUtils.isBlank(oldPhone) ? "空" : oldPhone;
        String oldRemarkValue = StringUtils.isBlank(oldRemark) ? "空" : oldRemark;
        oldMap.put("phone", oldPhoneValue);
        oldMap.put("remark", oldRemarkValue);
        Map<String, String> newMap = new HashMap<>(2);
        String newPhoneValue = Objects.isNull(servicePhone) || StringUtils.isBlank(servicePhone.getPhone()) ? "空" : servicePhone.getPhone();
        String newRemarkValue = Objects.isNull(servicePhone) || StringUtils.isBlank(servicePhone.getRemark()) ? "空" : servicePhone.getRemark();
        newMap.put("phone", newPhoneValue);
        newMap.put("remark", newRemarkValue);
        
        operateRecordUtil.record(oldMap, newMap);
    }
    
    @Override
    public List<ServicePhone> listByTenantIdFromCache(Integer tenantId) {
        List<ServicePhone> phoneList = redisService.getWithList(CacheConstant.SERVICE_PHONE + tenantId, ServicePhone.class);
        if (CollectionUtils.isNotEmpty(phoneList)) {
            return phoneList;
        }
        
        List<ServicePhone> servicePhones = servicePhoneMapper.selectByTenantId(tenantId);
        if (CollectionUtil.isEmpty(servicePhones)) {
            return Collections.emptyList();
        }
        
        redisService.saveWithList(CacheConstant.SERVICE_PHONE + tenantId, servicePhones);
        return servicePhones;
    }
    
    @Slave
    @Override
    public List<ServicePhone> listByIds(List<Long> ids) {
        return servicePhoneMapper.selectListByIds(ids);
    }
    
}

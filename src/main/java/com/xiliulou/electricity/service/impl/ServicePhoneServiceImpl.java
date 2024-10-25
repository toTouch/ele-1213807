package com.xiliulou.electricity.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.entity.ServicePhone;
import com.xiliulou.electricity.mapper.ServicePhoneMapper;
import com.xiliulou.electricity.request.ServicePhoneRequest;
import com.xiliulou.electricity.request.ServicePhonesRequest;
import com.xiliulou.electricity.service.ServicePhoneService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.vo.ServicePhoneVO;
import com.xiliulou.electricity.vo.ServicePhonesVO;
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
@Service
public class ServicePhoneServiceImpl implements ServicePhoneService {
    
    @Resource
    private ServicePhoneMapper servicePhoneMapper;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private OperateRecordUtil operateRecordUtil;
    
    @Override
    public R insertOrUpdate(ServicePhonesRequest request) {
        Integer tenantId = TenantContextHolder.getTenantId();
        boolean result = redisService.setNx(CacheConstant.SERVICE_PHONE_LOCK_KEY + tenantId, "1", 3 * 1000L, false);
        if (!result) {
            return R.fail("ELECTRICITY.0034", "操作频繁");
        }
        
        try {
            List<ServicePhoneRequest> requestPhoneList = request.getPhoneList();
            if (requestPhoneList.stream().map(ServicePhoneRequest::getPhone).distinct().count() != requestPhoneList.size()) {
                return R.fail("120149", "电话号码重复，请检查！");
            }
            
            List<ServicePhone> insertList = new ArrayList<>();
            List<ServicePhone> updateList = new ArrayList<>();
            for (ServicePhoneRequest requestPhone : requestPhoneList) {
                Long id = requestPhone.getId();
                String phone = requestPhone.getPhone();
                String remark = requestPhone.getRemark();
                
                if (Objects.isNull(id)) {
                    insertList.add(ServicePhone.builder().phone(phone).remark(remark).tenantId(tenantId).delFlag(CommonConstant.DEL_N).createTime(System.currentTimeMillis())
                            .updateTime(System.currentTimeMillis()).build());
                } else {
                    updateList.add(ServicePhone.builder().phone(phone).remark(remark).id(id).tenantId(tenantId).updateTime(System.currentTimeMillis()).build());
                }
            }
            
            boolean flag = false;
            if (CollectionUtil.isNotEmpty(insertList)) {
                Integer insert = servicePhoneMapper.batchInsert(insertList);
                if (Objects.nonNull(insert) && Objects.equals(insertList.size(), insert)) {
                    flag = true;
                    String oldPhone = "";
                    String oldRemark = "";
                    for (ServicePhone servicePhone : insertList) {
                        this.sendOperateRecord(oldPhone, oldRemark, servicePhone);
                    }
                }
            }
            
            if (CollectionUtil.isNotEmpty(updateList)) {
                List<Long> updateIds = updateList.stream().map(ServicePhone::getId).collect(Collectors.toList());
                List<ServicePhone> oldServicePhoneList = this.listByIds(updateIds);
                Map<Long, ServicePhone> oldServicePhoneMap = oldServicePhoneList.stream().collect(Collectors.toMap(ServicePhone::getId, servicePhone -> servicePhone));
                
                for (ServicePhone servicePhone : updateList) {
                    Integer update = servicePhoneMapper.update(servicePhone);
                    if (Objects.nonNull(update) && update > 0) {
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
            
            if (flag) {
                redisService.delete(CacheConstant.SERVICE_PHONE + tenantId);
            }
            return R.ok();
        } finally {
            redisService.delete(CacheConstant.SERVICE_PHONE_LOCK_KEY + tenantId);
        }
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
        Map<String, String> oldMap = new HashMap<>();
        oldMap.put("phone", oldPhone);
        oldMap.put("remark", oldRemark);
        Map<String, String> newMap = new HashMap<>();
        newMap.put("phone", servicePhone.getPhone());
        newMap.put("remark", servicePhone.getRemark());
        operateRecordUtil.record(oldMap, newMap);
    }
    
    @Override
    public ServicePhonesVO queryByTenantIdFromCache(Integer tenantId) {
        ServicePhonesVO servicePhoneCache = redisService.getWithHash(CacheConstant.SERVICE_PHONE + tenantId, ServicePhonesVO.class);
        if (Objects.nonNull(servicePhoneCache)) {
            return servicePhoneCache;
        }
        
        List<ServicePhone> servicePhones = servicePhoneMapper.selectByTenantId(tenantId);
        if (CollectionUtil.isEmpty(servicePhones)) {
            return null;
        }
        
        ServicePhonesVO servicePhonesVO = ServicePhonesVO.builder().tenantId(tenantId).phoneList(servicePhones.stream()
                .map(servicePhone -> ServicePhoneVO.builder().id(servicePhone.getId()).phone(servicePhone.getPhone()).remark(servicePhone.getRemark()).build())
                .collect(Collectors.toList())).build();
        
        redisService.saveWithHash(CacheConstant.SERVICE_PHONE + tenantId, servicePhonesVO);
        return servicePhonesVO;
    }
    
    @Slave
    @Override
    public List<ServicePhone> listByIds(List<Long> ids) {
        return servicePhoneMapper.selectListByIds(ids);
    }
    
    @Override
    public List<ServicePhoneVO> listByTenantId(Integer tenantId) {
        ServicePhonesVO servicePhonesVO = this.queryByTenantIdFromCache(tenantId);
        if (Objects.isNull(servicePhonesVO) || CollectionUtil.isEmpty(servicePhonesVO.getPhoneList())) {
            return Collections.emptyList();
        }
        
        return servicePhonesVO.getPhoneList();
    }
    
}

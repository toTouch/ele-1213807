package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.MerchantPlaceConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceCabinetBindMapper;
import com.xiliulou.electricity.query.merchant.MerchantPlaceCabinetBindQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantPlaceCabinetBindSaveRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceCabinetPageRequest;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceCabinetBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetBindVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author maxiaodong
 * @date 2024/2/11 9:53
 * @desc
 */
@Service("merchantPlaceCabinetBindService")
@Slf4j
public class MerchantPlaceCabinetBindServiceImpl implements MerchantPlaceCabinetBindService {
    
    @Resource
    private MerchantPlaceCabinetBindMapper merchantPlaceCabinetBindMapper;
    
    @Resource
    private MerchantPlaceService merchantPlaceService;
    
    @Resource
    private ElectricityCabinetService cabinetService;
    
    @Resource
    private RedisService redisService;
    
    @Slave
    @Override
    public List<MerchantPlaceCabinetBind> queryList(MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel) {
        return merchantPlaceCabinetBindMapper.list(placeCabinetBindQueryModel);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceCabinetBindVO> queryListByMerchantId(MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel) {
        return merchantPlaceCabinetBindMapper.listByMerchantId(placeCabinetBindQueryModel);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceCabinetBindVO> queryBindCabinetName(MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel) {
        return merchantPlaceCabinetBindMapper.queryBindCabinetName(placeCabinetBindQueryModel);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> bind(MerchantPlaceCabinetBindSaveRequest placeCabinetBindSaveRequest) {
        // 检测场地是否存在
        TokenUser user = SecurityUtils.getUserInfo();
        
        if (!redisService.setNx(CacheConstant.MERCHANT_PLACE_CABINET_BIND_UID + user.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        // 检测场地是否存在
        MerchantPlace merchantPlace = merchantPlaceService.queryFromCacheById(placeCabinetBindSaveRequest.getPlaceId());
        if (Objects.isNull(merchantPlace) || !Objects.equals(merchantPlace.getTenantId(), tenantId)) {
            log.error("place bind error, place not exists, placeId ={}, tenantId={}", placeCabinetBindSaveRequest.getPlaceId(), tenantId);
            return Triple.of(false, "", "场地不存在");
        }
        
        // 检测开始时间是否小于上个月的月初
        Long lastMonthDaytime = getLastMonthDay();
        if (placeCabinetBindSaveRequest.getBindTime() < lastMonthDaytime) {
            log.error("place bind error, bind time less than last month day time, placeId ={}, bindTime={}, lastMonthDaytime={}", placeCabinetBindSaveRequest.getPlaceId(),
                    placeCabinetBindSaveRequest.getBindTime(), lastMonthDaytime);
            return Triple.of(false, "", "开始时间只可选择当月（包含当前时间，不得晚于当前时间），及上月时间");
        }
        
        long currentTimeMillis = System.currentTimeMillis();
        if (placeCabinetBindSaveRequest.getBindTime() > currentTimeMillis) {
            log.error("place bind error, bind time is after current time, placeId ={}, bindTime={}, currentTimeMillis={}", placeCabinetBindSaveRequest.getPlaceId(),
                    placeCabinetBindSaveRequest.getBindTime(), currentTimeMillis);
            return Triple.of(false, "", "开始时间不得晚于当前时间");
        }
        
        // 检测绑定柜机是否存在
        ElectricityCabinet electricityCabinet = cabinetService.queryByIdFromCache(placeCabinetBindSaveRequest.getCabinetId());
        if (Objects.isNull(electricityCabinet) || !Objects.equals(merchantPlace.getTenantId(), tenantId)) {
            return Triple.of(false, "", "柜机不存在");
        }
        
        if (!Objects.equals(electricityCabinet.getFranchiseeId(), merchantPlace.getFranchiseeId())) {
            log.error("place bind error, franchisee is diff, placeId ={}, merchantFranchiseeId={}, cabinetFranchiseeId={}", placeCabinetBindSaveRequest.getPlaceId(),
                    electricityCabinet.getFranchiseeId(), merchantPlace.getFranchiseeId());
            return Triple.of(false, "", "柜机不存在");
        }
        
        // 检测柜机是否被绑定
        MerchantPlaceCabinetBindQueryModel queryModel = MerchantPlaceCabinetBindQueryModel.builder().cabinetId(placeCabinetBindSaveRequest.getCabinetId())
                .status(MerchantPlaceConstant.BIND).build();
        List<MerchantPlaceCabinetBind> merchantPlaceCabinetBinds = this.queryList(queryModel);
        
        if (ObjectUtils.isNotEmpty(merchantPlaceCabinetBinds)) {
            log.error("place bind error, cabinet is bind, placeId ={}, cabinetId={}, bindPlaceId={}", placeCabinetBindSaveRequest.getPlaceId(), electricityCabinet.getId(),
                    merchantPlaceCabinetBinds.get(0).getPlaceId());
            return Triple.of(false, "", "柜机已经被其他场地绑定");
        }
        
        // 检测场地绑定的柜的数量是否已经大于20
        List<Long> placeIdList = new ArrayList<>();
        placeIdList.add(placeCabinetBindSaveRequest.getPlaceId());
        queryModel.setPlaceIdList(placeIdList);
        queryModel.setCabinetId(null);
        List<MerchantPlaceCabinetBind> bindList = this.queryList(queryModel);
        if (ObjectUtils.isNotEmpty(bindList) && (bindList.size() + 1) > 20) {
            return Triple.of(false, "", "场地绑定柜机数量不能大于20");
        }
        
        // 判断绑定的时间是否与解绑的历史数据存在重叠
        queryModel.setStatus(MerchantPlaceConstant.UN_BIND);
        queryModel.setOverlapTime(placeCabinetBindSaveRequest.getBindTime());
        List<MerchantPlaceCabinetBind> unBindList = this.queryList(queryModel);
        if (ObjectUtils.isNotEmpty(unBindList)) {
            List<Long> ids = unBindList.stream().map(MerchantPlaceCabinetBind::getId).collect(Collectors.toList());
            log.error("place bind error, bind time is overlap, placeId ={}, ids={}", placeCabinetBindSaveRequest.getPlaceId(), ids);
            return Triple.of(false, "", "您选择的开始时间与已有区间存在冲突，请重新选择");
        }
        
        // 新增绑定数据
        MerchantPlaceCabinetBind placeCabinetBind = new MerchantPlaceCabinetBind();
        BeanUtils.copyProperties(placeCabinetBindSaveRequest, placeCabinetBind);
        placeCabinetBind.setStatus(MerchantPlaceConstant.BIND);
        placeCabinetBind.setCreateTime(System.currentTimeMillis());
        placeCabinetBind.setUpdateTime(System.currentTimeMillis());
        placeCabinetBind.setTenantId(tenantId);
        merchantPlaceCabinetBindMapper.insert(placeCabinetBind);
        
        return Triple.of(true, null, null);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> unBind(MerchantPlaceCabinetBindSaveRequest placeCabinetBindSaveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        
        if (!redisService.setNx(CacheConstant.MERCHANT_PLACE_CABINET_UNBIND_UID + user.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        // 检测绑定记录是否存在
        MerchantPlaceCabinetBind cabinetBind = this.queryById(placeCabinetBindSaveRequest.getId());
        if (Objects.isNull(cabinetBind) || !Objects.equals(cabinetBind.getTenantId(), tenantId)) {
            log.error("place un bind error, data not find, id ={}", placeCabinetBindSaveRequest.getId());
            return Triple.of(false, "", "绑定记录不存在");
        }
        
        if (Objects.equals(cabinetBind.getStatus(), MerchantPlaceConstant.UN_BIND)) {
            log.error("place un bind error, cabinet already un bind, id ={}", placeCabinetBindSaveRequest.getId());
            return Triple.of(false, "", "柜机已经解绑了，不能重复解绑");
        }
        
        long currentTimeMillis = System.currentTimeMillis();
        // 检测结束时间是否小于系统当前时间
        if (placeCabinetBindSaveRequest.getUnBindTime() > currentTimeMillis) {
            log.error("place un bind error, cabinet already un bind, id ={}, unBindTime={},curTime={}", placeCabinetBindSaveRequest.getId(),
                    placeCabinetBindSaveRequest.getUnBindTime(), currentTimeMillis);
            return Triple.of(false, "", "结束时间不能晚于当前时间");
        }
        
        // 检测结束时间是否小于绑定时间
        if (placeCabinetBindSaveRequest.getUnBindTime() > cabinetBind.getBindTime()) {
            log.error("place un bind error, cabinet already un bind, id ={}, unBindTime={},bindTime={}", placeCabinetBindSaveRequest.getId(),
                    placeCabinetBindSaveRequest.getUnBindTime(), cabinetBind.getBindTime());
            return Triple.of(false, "", "结束时间不能早于绑定时间");
        }
    
        // 检测开始时间是否小于上个月的月初
        Long lastMonthDaytime = getLastMonthDay();
        if (placeCabinetBindSaveRequest.getUnBindTime() < lastMonthDaytime) {
            log.error("place bind error, bind time less than last month day time, placeId ={}, unBindTime={}, lastMonthDaytime={}", placeCabinetBindSaveRequest.getPlaceId(),
                    placeCabinetBindSaveRequest.getUnBindTime(), lastMonthDaytime);
            return Triple.of(false, "", "开始时间只可选择当月（包含当前时间，不得晚于当前时间），及上月时间");
        }
        
        // 判断绑定的时间是否与解绑的历史数据存在重叠
        List<Long> placeIdList = new ArrayList<>();
        placeIdList.add(cabinetBind.getPlaceId());
        MerchantPlaceCabinetBindQueryModel queryModel = MerchantPlaceCabinetBindQueryModel.builder().overlapTime(placeCabinetBindSaveRequest.getUnBindTime())
                .placeIdList(placeIdList).status(MerchantPlaceConstant.UN_BIND).build();
        List<MerchantPlaceCabinetBind> unBindList = this.queryList(queryModel);
        if (ObjectUtils.isNotEmpty(unBindList)) {
            List<Long> ids = unBindList.stream().map(MerchantPlaceCabinetBind::getId).collect(Collectors.toList());
            log.error("place un bind error, un bind time is overlap, id ={}, ids={}", placeCabinetBindSaveRequest.getId(), ids);
            return Triple.of(false, "", "您选择的开始时间与已有区间存在冲突，请重新选择");
        }
        
        // 修改记录的状态为解绑，维护解绑时间
        MerchantPlaceCabinetBind unBind = new MerchantPlaceCabinetBind();
        BeanUtils.copyProperties(placeCabinetBindSaveRequest, unBind);
        unBind.setStatus(MerchantPlaceConstant.UN_BIND);
        unBind.setUpdateTime(currentTimeMillis);
        merchantPlaceCabinetBindMapper.unBind(unBind);
        
        return Triple.of(true, null, null);
    }
    
    @Override
    public Triple<Boolean, String, Object> remove(Long id) {
        TokenUser user = SecurityUtils.getUserInfo();
        
        if (!redisService.setNx(CacheConstant.MERCHANT_PLACE_CABINET_DELETE_UID + user.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        // 检测绑定记录是否存在
        MerchantPlaceCabinetBind cabinetBind = this.queryById(id);
        if (Objects.isNull(cabinetBind) || !Objects.equals(cabinetBind.getTenantId(), tenantId)) {
            log.error("place bind delete error, data not find, id ={}", id);
            return Triple.of(false, "", "绑定记录不存在");
        }
        
        MerchantPlaceCabinetBind update = new MerchantPlaceCabinetBind();
        update.setId(id);
        update.setDelFlag(MerchantPlaceConstant.DEL_DEL);
        update.setUpdateTime(System.currentTimeMillis());
        merchantPlaceCabinetBindMapper.remove(update);
        
        return Triple.of(true, null, null);
    }
    
    @Slave
    @Override
    public Integer countTotal(MerchantPlaceCabinetPageRequest placeCabinetPageRequest) {
        MerchantPlaceCabinetBindQueryModel merchantQueryModel = new MerchantPlaceCabinetBindQueryModel();
        BeanUtils.copyProperties(placeCabinetPageRequest, merchantQueryModel);
        
        return merchantPlaceCabinetBindMapper.countTotal(merchantQueryModel);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceCabinetBindVO> listByPage(MerchantPlaceCabinetPageRequest placeCabinetPageRequest) {
        List<MerchantPlaceCabinetBindVO> resList = new ArrayList<>();
    
        MerchantPlaceCabinetBindQueryModel merchantQueryModel = new MerchantPlaceCabinetBindQueryModel();
        BeanUtils.copyProperties(placeCabinetPageRequest, merchantQueryModel);
        
        List<MerchantPlaceCabinetBind> merchantPlaceList = this.merchantPlaceCabinetBindMapper.selectListByPage(merchantQueryModel);
    
        if (ObjectUtils.isEmpty(merchantPlaceList)) {
            return Collections.EMPTY_LIST;
        }
    
        for (MerchantPlaceCabinetBind merchantPlace : merchantPlaceList) {
            MerchantPlaceCabinetBindVO merchantPlaceVO = new MerchantPlaceCabinetBindVO();
            BeanUtils.copyProperties(merchantPlace, merchantPlaceVO);
            
            resList.add(merchantPlaceVO);
        }
    
        return resList;
    }
    
    @Slave
    private MerchantPlaceCabinetBind queryById(Long id) {
        return merchantPlaceCabinetBindMapper.selectById(id);
    }
    
    private Long getLastMonthDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1); // 将日历回退到上个月
        calendar.set(Calendar.DAY_OF_MONTH, 1); // 设置为该月的第一天
        calendar.set(Calendar.HOUR_OF_DAY, 0); // 设置为凌晨
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    @Slave
    @Override
    public List<MerchantPlaceCabinetBind> listByConditions(Set<Long> placeIds, Set<Long> cabinetIds, Integer status, Long startTime, Long endTime) {
        return merchantPlaceCabinetBindMapper.selectListByConditions(placeIds, cabinetIds, status, startTime, endTime);
    }
    
}

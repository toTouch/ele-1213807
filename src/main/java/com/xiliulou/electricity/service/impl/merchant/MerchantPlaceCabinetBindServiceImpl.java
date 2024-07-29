package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.constant.merchant.MerchantPlaceBindConstant;
import com.xiliulou.electricity.constant.merchant.MerchantPlaceConstant;
import com.xiliulou.electricity.entity.ElectricityCabinet;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.mapper.merchant.MerchantMapper;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceCabinetBindMapper;
import com.xiliulou.electricity.query.merchant.MerchantPlaceCabinetBindQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantPlaceCabinetBindSaveRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceCabinetConditionRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceCabinetPageRequest;
import com.xiliulou.electricity.service.ElectricityCabinetService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceCabinetBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceMapService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetBindTimeCheckVo;
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
    
    @Resource
    private MerchantPlaceMapService merchantPlaceMapService;
    
    @Resource
    private MerchantMapper merchantMapper;
    
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
        MerchantPlace merchantPlace = merchantPlaceService.queryByIdFromCache(placeCabinetBindSaveRequest.getPlaceId());
        if (Objects.isNull(merchantPlace) || !Objects.equals(merchantPlace.getTenantId(), tenantId)) {
            log.error("place bind error, place not exists, placeId ={}, tenantId={}", placeCabinetBindSaveRequest.getPlaceId(), tenantId);
            return Triple.of(false, "120209", "场地不存在");
        }
        
        // 检测开始时间是否小于上个月的月初
        Long lastMonthDaytime = getLastMonthDay();
        if (placeCabinetBindSaveRequest.getBindTime() < lastMonthDaytime) {
            log.error("place bind error, bind time less than last month day time, placeId ={}, bindTime={}, lastMonthDaytime={}", placeCabinetBindSaveRequest.getPlaceId(),
                    placeCabinetBindSaveRequest.getBindTime(), lastMonthDaytime);
            
            return Triple.of(false, "120221", "开始时间只可选择当月（包含当前时间，不得晚于当前时间），及上月时间");
        }
        
        long currentTimeMillis = System.currentTimeMillis();
        if (placeCabinetBindSaveRequest.getBindTime() > currentTimeMillis) {
            log.error("place bind error, bind time is after current time, placeId ={}, bindTime={}, currentTimeMillis={}", placeCabinetBindSaveRequest.getPlaceId(),
                    placeCabinetBindSaveRequest.getBindTime(), currentTimeMillis);
            return Triple.of(false, "120222", "开始时间不得晚于当前时间");
        }
        
        // 检测绑定柜机是否存在
        ElectricityCabinet electricityCabinet = cabinetService.queryByIdFromCache(placeCabinetBindSaveRequest.getCabinetId());
        if (Objects.isNull(electricityCabinet) || !Objects.equals(merchantPlace.getTenantId(), tenantId)) {
            return Triple.of(false, "120223", "柜机不存在");
        }
        
        if (!Objects.equals(electricityCabinet.getFranchiseeId(), merchantPlace.getFranchiseeId())) {
            log.error("place bind error, franchisee is diff, placeId ={}, merchantFranchiseeId={}, cabinetFranchiseeId={}", placeCabinetBindSaveRequest.getPlaceId(),
                    electricityCabinet.getFranchiseeId(), merchantPlace.getFranchiseeId());
            return Triple.of(false, "120224", "柜机所属加盟商和当前选中的加盟商不一致");
        }
        
        // 检测柜机是否被绑定
        MerchantPlaceCabinetBindQueryModel queryModel = MerchantPlaceCabinetBindQueryModel.builder().cabinetId(placeCabinetBindSaveRequest.getCabinetId())
                .status(MerchantPlaceConstant.BIND).build();
        List<MerchantPlaceCabinetBind> merchantPlaceCabinetBinds = this.queryList(queryModel);
        
        if (ObjectUtils.isNotEmpty(merchantPlaceCabinetBinds)) {
            log.error("place bind error, cabinet is bind, placeId ={}, cabinetId={}, bindPlaceId={}", placeCabinetBindSaveRequest.getPlaceId(), electricityCabinet.getId(),
                    merchantPlaceCabinetBinds.get(0).getPlaceId());
            return Triple.of(false, "120225", "柜机已经被其他场地绑定");
        }
        
        // 检测场地绑定的柜的数量是否已经大于20
        List<Long> placeIdList = new ArrayList<>();
        placeIdList.add(placeCabinetBindSaveRequest.getPlaceId());
        queryModel.setPlaceIdList(placeIdList);
        queryModel.setCabinetId(null);
        Integer count = merchantPlaceCabinetBindMapper.countCabinetBindCount(queryModel);
        
        if (ObjectUtils.isNotEmpty(count) && (count + 1) > MerchantPlaceConstant.BIND_CABINET_COUNT_LIMIT) {
            return Triple.of(false, "120226", String.format("场地绑定柜机数量不能大于%s", MerchantPlaceConstant.BIND_CABINET_COUNT_LIMIT));
        }
        
        // 判断绑定的时间是否与解绑的历史数据存在重叠
        queryModel.setStatus(MerchantPlaceConstant.UN_BIND);
        queryModel.setOverlapTime(placeCabinetBindSaveRequest.getBindTime());
        queryModel.setCabinetId(placeCabinetBindSaveRequest.getCabinetId());
        List<MerchantPlaceCabinetBind> unBindList = this.queryList(queryModel);
        if (ObjectUtils.isNotEmpty(unBindList)) {
            List<Long> ids = unBindList.stream().map(MerchantPlaceCabinetBind::getId).collect(Collectors.toList());
            log.error("place bind error, bind time is overlap, placeId ={}, ids={}", placeCabinetBindSaveRequest.getPlaceId(), ids);
            return Triple.of(false, "120227", "您选择的开始时间与已有区间存在冲突，请重新选择");
        }
        
        // 新增绑定数据
        MerchantPlaceCabinetBind placeCabinetBind = new MerchantPlaceCabinetBind();
        BeanUtils.copyProperties(placeCabinetBindSaveRequest, placeCabinetBind);
        placeCabinetBind.setStatus(MerchantPlaceConstant.BIND);
        placeCabinetBind.setCreateTime(System.currentTimeMillis());
        placeCabinetBind.setUpdateTime(System.currentTimeMillis());
        placeCabinetBind.setTenantId(tenantId);
        placeCabinetBind.setCabinetId(Long.valueOf(placeCabinetBindSaveRequest.getCabinetId()));
        merchantPlaceCabinetBindMapper.insert(placeCabinetBind);
        
        // 检测柜机否存在场地费
        if (Objects.isNull(electricityCabinet.getPlaceFee())) {
            return Triple.of(true, null, null);
        }
        
        // 检测场地当前关联的商户且未设置存在场地费的数据
        List<Long> noExistsPlaceFeeMerchantIdList = merchantPlaceMapService.queryListNoExistsPlaceFeeMerchant(placeCabinetBindSaveRequest.getPlaceId());
        if (ObjectUtils.isEmpty(noExistsPlaceFeeMerchantIdList)) {
            return Triple.of(true, null, null);
        }
        
        // 修改商户对应的场地费为存在
        Merchant merchantUpdate = new Merchant();
        merchantUpdate.setUpdateTime(currentTimeMillis);
        merchantUpdate.setId(noExistsPlaceFeeMerchantIdList.get(0));
        merchantUpdate.setExistPlaceFee(MerchantConstant.EXISTS_PLACE_FEE_YES);
        
        merchantMapper.updateById(merchantUpdate);
        
        return Triple.of(true, null, merchantUpdate);
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
            log.warn("place un bind warn, data not find, id ={}", placeCabinetBindSaveRequest.getId());
            return Triple.of(false, "120228", "绑定记录不存在");
        }
        
        if (Objects.equals(cabinetBind.getStatus(), MerchantPlaceConstant.UN_BIND)) {
            log.warn("place un bind warn, cabinet already un bind, id ={}", placeCabinetBindSaveRequest.getId());
            return Triple.of(false, "120229", "柜机已经解绑了，不能重复解绑");
        }
        
        long currentTimeMillis = System.currentTimeMillis();
        // 检测结束时间是否小于系统当前时间
        if (placeCabinetBindSaveRequest.getUnBindTime() > currentTimeMillis) {
            log.warn("place un bind warn, cabinet already un bind, id ={}, unBindTime={},curTime={}", placeCabinetBindSaveRequest.getId(),
                    placeCabinetBindSaveRequest.getUnBindTime(), currentTimeMillis);
            return Triple.of(false, "120230", "结束时间不能晚于当前时间");
        }
        
        // 检测结束时间是否小于绑定时间
        if (placeCabinetBindSaveRequest.getUnBindTime() < cabinetBind.getBindTime()) {
            log.warn("place un bind warn, cabinet already un bind, id ={}, unBindTime={},bindTime={}", placeCabinetBindSaveRequest.getId(),
                    placeCabinetBindSaveRequest.getUnBindTime(), cabinetBind.getBindTime());
            return Triple.of(false, "120231", "结束时间不能早于开始时间");
        }
        
        // 检测开始时间是否小于上个月的月初
        Long lastMonthDaytime = getLastMonthDay();
        if (placeCabinetBindSaveRequest.getUnBindTime() < lastMonthDaytime) {
            log.warn("place bind warn, bind time less than last month day time, placeId ={}, unBindTime={}, lastMonthDaytime={}", placeCabinetBindSaveRequest.getPlaceId(),
                    placeCabinetBindSaveRequest.getUnBindTime(), lastMonthDaytime);
            return Triple.of(false, "120221", "开始时间只可选择当月（包含当前时间，不得晚于当前时间），及上月时间");
        }
        
        // 判断绑定的时间是否与解绑的历史数据存在重叠
        List<Long> placeIdList = new ArrayList<>();
        placeIdList.add(cabinetBind.getPlaceId());
        MerchantPlaceCabinetBindQueryModel queryModel = MerchantPlaceCabinetBindQueryModel.builder().overlapTime(placeCabinetBindSaveRequest.getUnBindTime())
                .cabinetId(cabinetBind.getCabinetId().intValue())
                .placeIdList(placeIdList).status(MerchantPlaceConstant.UN_BIND).build();
        
        List<MerchantPlaceCabinetBind> unBindList = this.queryList(queryModel);
        if (ObjectUtils.isNotEmpty(unBindList)) {
            List<Long> ids = unBindList.stream().map(MerchantPlaceCabinetBind::getId).collect(Collectors.toList());
            log.warn("place un bind warn, un bind time is overlap, id ={}, ids={}", placeCabinetBindSaveRequest.getId(), ids);
            return Triple.of(false, "120232", "您选择的开始时间与已有区间存在冲突，请重新选择");
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
            return Triple.of(false, "120228", "绑定记录不存在");
        }
    
        long currentTimeMillis = System.currentTimeMillis();
    
        // 检测创建时间是否在当前本月 如果是本月则不允许进行删除
        if (!DateUtils.isSameMonth(cabinetBind.getCreateTime(), currentTimeMillis)) {
            log.error("place bind delete error, data not find, id ={}", id);
            return Triple.of(false, "120236", "仅可删除本月内创建的记录");
        }
        
        MerchantPlaceCabinetBind update = new MerchantPlaceCabinetBind();
        update.setId(id);
        update.setDelFlag(MerchantPlaceConstant.DEL_DEL);
        update.setUpdateTime(currentTimeMillis);
        
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
    
    /**
     * 检测绑定时间是否存在交叉
     *
     * @param placeId
     * @param time
     * @return
     */
    @Slave
    @Override
    public MerchantPlaceCabinetBindTimeCheckVo checkBindTime(Long placeId, Long time, Integer cabinetId) {
        // 判断绑定的时间是否与解绑的历史数据存在重叠
        List<Long> placeIdList = new ArrayList<>();
        placeIdList.add(placeId);
        MerchantPlaceCabinetBindQueryModel queryModel = MerchantPlaceCabinetBindQueryModel.builder().tenantId(TenantContextHolder.getTenantId()).overlapTime(time).placeIdList(placeIdList).cabinetId(cabinetId)
                .status(MerchantPlaceConstant.UN_BIND).build();
        
        List<MerchantPlaceCabinetBind> unBindList = this.queryList(queryModel);
        
        MerchantPlaceCabinetBindTimeCheckVo vo = new MerchantPlaceCabinetBindTimeCheckVo();
        
        // 不存在重叠
        if (ObjectUtils.isEmpty(unBindList)) {
            vo.setStatus(MerchantPlaceBindConstant.BIND_TIME_OVERLAP_NO);
            return vo;
        }
    
        vo.setStatus(MerchantPlaceBindConstant.BIND_TIME_OVERLAP_YES);
        return vo;
    }
    
    @Slave
    @Override
    public List<MerchantPlaceCabinetBind> queryListByPlaceId(List<Long> placeIdList, Integer placeMonthNotSettlement) {
        return merchantPlaceCabinetBindMapper.selectListByPlaceId(placeIdList, placeMonthNotSettlement);
    }
    
    @Slave
    @Override
    public Integer checkIsBindByPlaceId(Long placeId, Long cabinetId) {
        return merchantPlaceCabinetBindMapper.checkIsBindByPlaceId(placeId, cabinetId);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceCabinetBind> listByPlaceIds(Set<Long> placeIds) {
        return merchantPlaceCabinetBindMapper.selectListByPlaceIds(placeIds);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceCabinetBind> listBindRecord(MerchantPlaceCabinetConditionRequest request) {
        return merchantPlaceCabinetBindMapper.selectListBindRecord(request);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceCabinetBind> listUnbindRecord(MerchantPlaceCabinetConditionRequest request) {
        return merchantPlaceCabinetBindMapper.selectListUnbindRecord(request);
    }
    
    @Override
    public Integer removeByPlaceId(Long placeId, long updateTime, Integer delFlag) {
        return merchantPlaceCabinetBindMapper.removeByPlaceId(placeId, updateTime, delFlag);
    }
    
}

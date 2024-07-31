package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.merchant.MerchantPlaceConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.merchant.MerchantArea;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceCabinetBind;
import com.xiliulou.electricity.entity.merchant.MerchantPlaceMap;
import com.xiliulou.electricity.mapper.merchant.MerchantMapper;
import com.xiliulou.electricity.mapper.merchant.MerchantPlaceMapper;
import com.xiliulou.electricity.query.merchant.MerchantPlaceCabinetBindQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPlaceMapQueryModel;
import com.xiliulou.electricity.query.merchant.MerchantPlaceQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantAreaRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlacePageRequest;
import com.xiliulou.electricity.request.merchant.MerchantPlaceSaveRequest;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.merchant.MerchantAreaService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceCabinetBindService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceMapService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetBindVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceCabinetVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceMapVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceUpdateShowVO;
import com.xiliulou.electricity.vo.merchant.MerchantPlaceVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author maxiaodong
 * @date 2024/2/6 14:46
 * @desc
 */
@Service("merchantPlaceService")
@Slf4j
public class MerchantPlaceServiceImpl implements MerchantPlaceService {
    
    @Resource
    private MerchantMapper merchantMapper;
    
    @Resource
    private MerchantPlaceMapper merchantPlaceMapper;
    
    @Resource
    private FranchiseeService franchiseeService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private MerchantPlaceMapService merchantPlaceMapService;
    
    @Resource
    private MerchantPlaceCabinetBindService merchantPlaceCabinetBindService;
    
    XllThreadPoolExecutorService threadPool = XllThreadPoolExecutors.newFixedThreadPool("MERCHANT-PLACE-DATA-SCREEN-THREAD-POOL", 3, "merchantPlaceDataScreenThread:");
    
    
    @Resource
    private MerchantAreaService merchantAreaService;
    
    @Slave
    @Override
    public Integer existsByAreaId(Long areaId) {
        return merchantMapper.existsByAreaId(areaId);
    }
    
    @Slave
    @Override
    public List<MerchantPlace> queryList(MerchantPlaceQueryModel placeQueryModel) {
        return merchantPlaceMapper.list(placeQueryModel);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> save(MerchantPlaceSaveRequest merchantPlaceSaveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        
        if (!redisService.setNx(CacheConstant.MERCHANT_PLACE_SAVE_UID + user.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        // 检测页面上选择的加盟商和当前用户登录的加盟商是否一致
        if (ObjectUtils.isNotEmpty(merchantPlaceSaveRequest.getBindFranchiseeIdList()) && !merchantPlaceSaveRequest.getBindFranchiseeIdList().contains(merchantPlaceSaveRequest.getFranchiseeId())) {
            log.info("merchant place save info, franchisee is not different id={}, franchiseeId={}, bindFranchiseeId={}", merchantPlaceSaveRequest.getId(), merchantPlaceSaveRequest.getFranchiseeId(), merchantPlaceSaveRequest.getBindFranchiseeIdList());
            return Triple.of(false, "120240", "当前加盟商无权限操作");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        MerchantPlaceQueryModel queryModel = MerchantPlaceQueryModel.builder().name(merchantPlaceSaveRequest.getName()).tenantId(tenantId).build();
        
        // 检测场地名称是否重复
        Integer count = merchantPlaceMapper.checkIsExists(queryModel);
        if (count > 0) {
            return Triple.of(false, "120217", "场地名称已存在");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(merchantPlaceSaveRequest.getFranchiseeId());
        if (Objects.isNull(franchisee) || !Objects.equals(franchisee.getTenantId(), tenantId)) {
            return Triple.of(false, "120203", "加盟商不存在");
        }
        
        if (Objects.nonNull(merchantPlaceSaveRequest.getMerchantAreaId())) {
            MerchantArea merchantArea = merchantAreaService.queryById(merchantPlaceSaveRequest.getMerchantAreaId());
            
            if (Objects.isNull(merchantArea) || !Objects.equals(merchantArea.getTenantId(), tenantId)) {
                log.info("merchant place save info, area is null name={}, merchantAreaId={}", merchantPlaceSaveRequest.getName(), merchantPlaceSaveRequest.getMerchantAreaId());
                return Triple.of(false, "120218", "区域不存在");
            }
            
            if (!Objects.equals(merchantPlaceSaveRequest.getFranchiseeId(), merchantArea.getFranchiseeId())) {
                log.info("merchant place save info, area is null franchisee is different ,name={}, merchantAreaId={}", merchantPlaceSaveRequest.getName(), merchantPlaceSaveRequest.getMerchantAreaId());
                return Triple.of(false, "120218", "区域不存在");
            }
        }
        
        // 保存场地
        MerchantPlace merchantPlace = new MerchantPlace();
        BeanUtils.copyProperties(merchantPlaceSaveRequest, merchantPlace);
        
        long timeMillis = System.currentTimeMillis();
        merchantPlace.setCreateTime(timeMillis);
        merchantPlace.setUpdateTime(timeMillis);
        merchantPlace.setTenantId(tenantId);
        merchantPlace.setDelFlag(MerchantPlaceConstant.DEL_NORMAL);
        merchantPlaceMapper.insert(merchantPlace);
        
        return Triple.of(true, null, merchantPlace);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> update(MerchantPlaceSaveRequest merchantPlaceSaveRequest) {
        TokenUser user = SecurityUtils.getUserInfo();
        
        if (!redisService.setNx(CacheConstant.MERCHANT_PLACE_SAVE_UID + user.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        // 检测场地是否存在
        MerchantPlace merchantPlace = merchantPlaceMapper.selectById(merchantPlaceSaveRequest.getId());
        if (Objects.isNull(merchantPlace) || !Objects.equals(merchantPlace.getTenantId(), tenantId)) {
            return Triple.of(false, "120209", "场地不存在");
        }
        
        // 检测场地的加盟商是否发生改变
        if (!Objects.equals(merchantPlace.getFranchiseeId(), merchantPlaceSaveRequest.getFranchiseeId())) {
            log.info("merchant place update info, franchisee not allow change id={}, franchiseeId={}, updateFranchiseeId={}", merchantPlaceSaveRequest.getId(), merchantPlaceSaveRequest.getFranchiseeId(), merchantPlaceSaveRequest.getFranchiseeId());
            return Triple.of(false, "120239", "场地加盟商不允许修改");
        }
    
        // 检测选中的加盟商和当前登录加盟商是否一致
        if (ObjectUtils.isNotEmpty(merchantPlaceSaveRequest.getBindFranchiseeIdList()) && !merchantPlaceSaveRequest.getBindFranchiseeIdList().contains(merchantPlaceSaveRequest.getFranchiseeId())) {
            log.info("merchant place update info, franchisee is not different id={}, franchiseeId={}, bindFranchiseeId={}", merchantPlaceSaveRequest.getId(), merchantPlaceSaveRequest.getFranchiseeId(), merchantPlaceSaveRequest.getBindFranchiseeIdList());
            return Triple.of(false, "120240", "当前加盟商无权限操作");
        }
        
        if (Objects.nonNull(merchantPlaceSaveRequest.getMerchantAreaId())) {
            MerchantArea merchantArea = merchantAreaService.queryById(merchantPlaceSaveRequest.getMerchantAreaId());
            
            if (Objects.isNull(merchantArea) || !Objects.equals(merchantArea.getTenantId(), tenantId)) {
                log.error("merchant place save error, area is null name={}, merchantAreaId={}", merchantPlaceSaveRequest.getName(), merchantPlaceSaveRequest.getMerchantAreaId());
                return Triple.of(false, "120218", "区域不存在");
            }
    
            if (!Objects.equals(merchantPlaceSaveRequest.getFranchiseeId(), merchantArea.getFranchiseeId())) {
                log.error("merchant place save error, area is null franchisee is different ,name={}, merchantAreaId={}", merchantPlaceSaveRequest.getName(), merchantPlaceSaveRequest.getMerchantAreaId());
                return Triple.of(false, "120218", "区域不存在");
            }
        }
        
        MerchantPlaceQueryModel queryModel = MerchantPlaceQueryModel.builder().name(merchantPlaceSaveRequest.getName()).tenantId(tenantId).nqId(merchantPlaceSaveRequest.getId())
                .build();
        
        // 检测场地名称是否重复
        Integer count = merchantPlaceMapper.checkIsExists(queryModel);
        if (count > 0) {
            return Triple.of(false, "", "场地名称已存在");
        }
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(merchantPlaceSaveRequest.getFranchiseeId());
        if (Objects.isNull(franchisee) || !Objects.equals(franchisee.getTenantId(), tenantId)) {
            return Triple.of(false, "120203", "加盟商不存在");
        }
        
        // 修改场地信息
        MerchantPlace merchantPlaceUpdate = new MerchantPlace();
        BeanUtils.copyProperties(merchantPlaceSaveRequest, merchantPlaceUpdate);
        merchantPlaceUpdate.setUpdateTime(System.currentTimeMillis());
        merchantPlaceMapper.update(merchantPlaceUpdate);
        
        return Triple.of(true, null, merchantPlaceUpdate);
    }
    
    @Override
    public void deleteCache(MerchantPlace merchantPlace) {
        redisService.delete(CacheConstant.CACHE_MERCHANT_PLACE + merchantPlace.getId());
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> remove(Long id, List<Long> bindFranchiseeIdList) {
        // 检测场地是否存在
        TokenUser user = SecurityUtils.getUserInfo();
        
        if (!redisService.setNx(CacheConstant.MERCHANT_PLACE_DELETE_UID + user.getUid(), "1", 3 * 1000L, false)) {
            return Triple.of(false, "ELECTRICITY.0034", "操作频繁");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        List<Long> placeIdList = new ArrayList<>();
        placeIdList.add(id);
        MerchantPlaceMapQueryModel placeMapQueryModel = MerchantPlaceMapQueryModel.builder().placeIdList(placeIdList).build();
        List<MerchantPlaceMap> merchantPlaceMaps = merchantPlaceMapService.queryList(placeMapQueryModel);
        
        if (ObjectUtils.isNotEmpty(merchantPlaceMaps)) {
            List<Long> merchantIdList = merchantPlaceMaps.stream().map(MerchantPlaceMap::getMerchantId).collect(Collectors.toList());
            log.info("merchant place remove is bind, placeId={}, merchantIdList={}", id, merchantIdList);
            return Triple.of(false, "120219", "场地被商户绑定，请解绑后操作");
        }
        
        // 检测场地是否存在
        MerchantPlace merchantPlace = merchantPlaceMapper.selectById(id);
        if (Objects.isNull(merchantPlace) || !Objects.equals(merchantPlace.getTenantId(), tenantId)) {
            return Triple.of(false, "120209", "场地不存在");
        }
    
        if (ObjectUtils.isNotEmpty(bindFranchiseeIdList) && !bindFranchiseeIdList.contains(merchantPlace.getFranchiseeId())) {
            log.info("merchant place delete info, franchisee is not different id={}, franchiseeId={}, bindFranchiseeId={}", id, merchantPlace.getFranchiseeId(), bindFranchiseeIdList);
            return Triple.of(false, "120240", "当前加盟商无权限操作");
        }
        
        // 检测场地是否存在绑定的换电柜
        MerchantPlaceCabinetBindQueryModel queryModel = MerchantPlaceCabinetBindQueryModel.builder().status(MerchantPlaceConstant.BIND).placeIdList(placeIdList).build();
        List<MerchantPlaceCabinetBind> merchantPlaceCabinetBinds = merchantPlaceCabinetBindService.queryList(queryModel);
        
        if (ObjectUtils.isNotEmpty(merchantPlaceCabinetBinds)) {
            return Triple.of(false, "120220", "请先解绑换电柜后操作");
        }
        
        // 删除场地
        long currentTimeMillis = System.currentTimeMillis();
        
        MerchantPlace merchantPlaceDel = MerchantPlace.builder().id(id).updateTime(currentTimeMillis).delFlag(MerchantPlaceConstant.DEL_DEL).build();
        merchantPlaceMapper.remove(merchantPlaceDel);
        
        merchantPlaceCabinetBindService.removeByPlaceId(id, currentTimeMillis, MerchantPlaceConstant.DEL_DEL);
        
        return Triple.of(true, "", merchantPlace);
    }
    
    @Slave
    @Override
    public Integer countTotal(MerchantPlacePageRequest merchantPlacePageRequest) {
        MerchantPlaceQueryModel merchantQueryModel = new MerchantPlaceQueryModel();
        BeanUtils.copyProperties(merchantPlacePageRequest, merchantQueryModel);
        
        return merchantPlaceMapper.countTotal(merchantQueryModel);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceVO> listByPage(MerchantPlacePageRequest merchantPlacePageRequest) {
        
        MerchantPlaceQueryModel merchantPlaceQueryModel = new MerchantPlaceQueryModel();
        BeanUtils.copyProperties(merchantPlacePageRequest, merchantPlaceQueryModel);
        List<MerchantPlace> merchantPlaceList = this.merchantPlaceMapper.selectListByPage(merchantPlaceQueryModel);
        
        if (ObjectUtils.isEmpty(merchantPlaceList)) {
            return Collections.emptyList();
        }
        
        List<MerchantPlaceVO> resList = new ArrayList<>();
        
        List<Long> areaIdList = new ArrayList<>();
        List<Long> idList = new ArrayList<>();
        merchantPlaceList.stream().forEach(item -> {
            areaIdList.add(item.getMerchantAreaId());
            idList.add(item.getId());
        });
        
        for (MerchantPlace merchantPlace : merchantPlaceList) {
            MerchantPlaceVO merchantPlaceVO = new MerchantPlaceVO();
            BeanUtils.copyProperties(merchantPlace, merchantPlaceVO);
    
            Franchisee franchisee = franchiseeService.queryByIdFromCache(merchantPlace.getFranchiseeId());
            Optional.ofNullable(franchisee).ifPresent(f -> {
                merchantPlaceVO.setFranchiseeName(f.getName());
            });
            
            // 查询
            resList.add(merchantPlaceVO);
        }
        
        // 区域名称
        CompletableFuture<Void> areaInfo = CompletableFuture.runAsync(() -> {
            if (ObjectUtils.isEmpty(areaIdList)) {
                return;
            }
            
            MerchantAreaRequest areaQuery = MerchantAreaRequest.builder().idList(areaIdList).build();
            List<MerchantArea> merchantAreaList = merchantAreaService.queryList(areaQuery);
            if (ObjectUtils.isEmpty(merchantAreaList)) {
                return;
            }
            
            Map<Long, String> areaNameMap = merchantAreaList.stream().collect(Collectors.toMap(MerchantArea::getId, MerchantArea::getName, (key, key1) -> key1));
            
            resList.stream().forEach(item -> {
                if (ObjectUtils.isNotEmpty(areaNameMap.get(item.getMerchantAreaId()))) {
                    item.setMerchantAreaName(areaNameMap.get(item.getMerchantAreaId()));
                }
            });
        
        }, threadPool).exceptionally(e -> {
            log.error("MERCHANT PLACE QUERY ERROR! query area error!", e);
            return null;
        });
    
        // 查询商户对应的场地数
        CompletableFuture<Void> merchantInfo = CompletableFuture.runAsync(() -> {
            // 查询场地绑定的商户
            MerchantPlaceMapQueryModel placeMapQueryModel = MerchantPlaceMapQueryModel.builder().placeIdList(idList).build();
            List<MerchantPlaceMapVO> merchantPlaceMaps = merchantPlaceMapService.queryBindMerchantName(placeMapQueryModel);
            
            if (ObjectUtils.isEmpty(merchantPlaceMaps)) {
                return;
            }
            
            Map<Long, String> merchantNameMap = merchantPlaceMaps.stream().collect(Collectors.toMap(MerchantPlaceMapVO::getPlaceId, MerchantPlaceMapVO::getMerchantName, (key, key1) -> key1));
            resList.stream().forEach(item -> {
                // 商户名称
                if (ObjectUtils.isNotEmpty(merchantNameMap.get(item.getId()))) {
                    item.setMerchantName(merchantNameMap.get(item.getId()));
                }
            });
            
        }, threadPool).exceptionally(e -> {
            log.error("MERCHANT PLACE QUERY ERROR! query merchant error!", e);
            return null;
        });
    
        // 查询柜机名称
        CompletableFuture<Void> cabinetInfo = CompletableFuture.runAsync(() -> {
            // 查询场地绑定的柜机
            MerchantPlaceCabinetBindQueryModel placeCabinetBindQueryModel = MerchantPlaceCabinetBindQueryModel.builder().placeIdList(idList).status(MerchantPlaceConstant.BIND).build();
            List<MerchantPlaceCabinetBindVO> merchantPlaceCabinetBinds = merchantPlaceCabinetBindService.queryBindCabinetName(placeCabinetBindQueryModel);
            
            if (ObjectUtils.isEmpty(merchantPlaceCabinetBinds)) {
                return;
            }
            
            Map<Long, List<MerchantPlaceCabinetBindVO>> bindCabinetMap = merchantPlaceCabinetBinds.stream().collect(Collectors.groupingBy(MerchantPlaceCabinetBindVO::getPlaceId));
            resList.stream().forEach(item -> {
                List<MerchantPlaceCabinetBindVO> merchantPlaceCabinetBindVos = bindCabinetMap.get(item.getId());
    
                // 柜机
                if (ObjectUtils.isNotEmpty(merchantPlaceCabinetBindVos)) {
                    item.setCabinetList(merchantPlaceCabinetBindVos);
                }
            });
            
        }, threadPool).exceptionally(e -> {
            log.error("MERCHANT PLACE QUERY ERROR! query cabinet error!", e);
            return null;
        });
    
        CompletableFuture<Void> resultFuture = CompletableFuture.allOf(areaInfo, merchantInfo, cabinetInfo);
        try {
            resultFuture.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Data summary browsing error for merchant place query", e);
        }
        
        return resList;
    }
    
    @Slave
    @Override
    public MerchantPlace queryByIdFromCache(Long placeId) {
        MerchantPlace merchantPlace = redisService.getWithHash(CacheConstant.CACHE_MERCHANT_PLACE + placeId, MerchantPlace.class);
        if (Objects.nonNull(merchantPlace)) {
            return merchantPlace;
        }
    
        merchantPlace = merchantPlaceMapper.selectById(placeId);
        if (Objects.isNull(merchantPlace)) {
            return null;
        }
    
        redisService.saveWithHash(CacheConstant.CACHE_MERCHANT_PLACE + placeId, merchantPlace);
        
        return merchantPlace;
    }
    
    /**
     * 场地柜机下拉框数据
     *
     * @param merchantPlacePageRequest
     * @return
     */
    @Slave
    @Override
    public Triple<Boolean, String, Object> queryListCabinet(MerchantPlacePageRequest merchantPlacePageRequest) {
        // 判断场地id是否存在
        MerchantPlace merchantPlace = this.queryByIdFromCache(merchantPlacePageRequest.getPlaceId());
        
        if (Objects.isNull(merchantPlace) || !Objects.equals(merchantPlace.getTenantId(), merchantPlacePageRequest.getTenantId())) {
            log.info("place cabinet info, place is not exists, placeId={}, curTenantId={}", merchantPlacePageRequest.getPlaceId(), merchantPlacePageRequest.getTenantId());
            return Triple.of(false, "120209", "场地不存在");
        }
        
        // 判断加盟商与场地绑定的是否一致
        if (ObjectUtils.isNotEmpty(merchantPlacePageRequest.getBindFranchiseeIdList()) && !merchantPlacePageRequest.getBindFranchiseeIdList().contains(merchantPlace.getFranchiseeId())) {
            log.info("place cabinet info, franchisee is different, placeId={}, franchiseeId={}, bindFranchiseeId={}", merchantPlace.getTenantId(), merchantPlace.getFranchiseeId(), merchantPlacePageRequest.getBindFranchiseeIdList());
            return Triple.of(false, "120209", "场地不存在");
        }
        
        merchantPlacePageRequest.setFranchiseeId(merchantPlace.getFranchiseeId());
        
        MerchantPlaceQueryModel queryModel = new MerchantPlaceQueryModel();
        BeanUtils.copyProperties(merchantPlacePageRequest, queryModel);
        
        // 查询加盟商下的柜机的信息
        List<MerchantPlaceCabinetVO> merchantPlaceCabinetVOS = merchantPlaceMapper.selectCabinetList(queryModel);
        
        merchantPlaceCabinetVOS = merchantPlaceCabinetVOS.stream().filter(item -> Objects.nonNull(item)).collect(Collectors.toList());
        
        if (ObjectUtils.isEmpty(merchantPlaceCabinetVOS)) {
            return Triple.of(true, null, merchantPlaceCabinetVOS);
        }
    
        merchantPlaceCabinetVOS.forEach(item -> {
            if (Objects.nonNull(item.getPlaceId())) {
                item.setDisable(MerchantPlaceCabinetVO.YES);
            } else {
                item.setDisable(MerchantPlaceCabinetVO.NO);
            }
        });
        
        return Triple.of(true, null, merchantPlaceCabinetVOS);
    }
    
    @Slave
    @Override
    public List<MerchantPlaceVO> queryListPlace(MerchantPlacePageRequest merchantPlacePageRequest) {
        MerchantPlaceQueryModel merchantQueryModel = new MerchantPlaceQueryModel();
        BeanUtils.copyProperties(merchantPlacePageRequest, merchantQueryModel);
        
        List<MerchantPlace> merchantPlaceList = merchantPlaceMapper.selectListByPage(merchantQueryModel);
        if (ObjectUtils.isEmpty(merchantPlaceList)) {
            return Collections.EMPTY_LIST;
        }
        
        // 查询场地是否已经被其他商户给绑定了
        List<MerchantPlaceMap> merchantPlaceMaps = merchantPlaceMapService.queryListForBind(merchantPlacePageRequest.getMerchantId(), merchantPlacePageRequest.getFranchiseeId());
        
        List<Long> placeIdList = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(merchantPlaceMaps)) {
            placeIdList = merchantPlaceMaps.stream().map(MerchantPlaceMap::getPlaceId).distinct().collect(Collectors.toList());
        }
        
        List<MerchantPlaceVO> list = new ArrayList<>();
        
        for (MerchantPlace item : merchantPlaceList) {
            MerchantPlaceVO vo = new MerchantPlaceVO();
            BeanUtils.copyProperties(item, vo);
            
            // 如果是存在绑定关系的则禁用不能选择
            if (placeIdList.contains(item.getId())) {
                vo.setDisable(MerchantPlaceConstant.DISABLE);
            } else {
                vo.setDisable(MerchantPlaceConstant.ENABLE);
            }
            
            list.add(vo);
        }
        
        return list;
    }
    
    /**
     * 根据id 获取编辑信息
     *
     * @param id
     * @param bindFranchiseeIdList
     * @return
     */
    @Slave
    @Override
    public Triple<Boolean, String, Object> queryById(Long id, List<Long> bindFranchiseeIdList) {
        Integer tenantId = TenantContextHolder.getTenantId();
        
        MerchantPlace merchantPlace = merchantPlaceMapper.selectById(id);
        if (Objects.isNull(merchantPlace) || !Objects.equals(merchantPlace.getTenantId(), tenantId)) {
            log.info("merchant place info, query by id error, placeId={}", id);
            return Triple.of(false, "", "场地不存在");
        }
        
        if (ObjectUtils.isNotEmpty(bindFranchiseeIdList) && !bindFranchiseeIdList.contains(merchantPlace.getFranchiseeId())) {
            log.info("merchant place info, query by id franchisee is different, placeId={}, bindFranchiseeId={}", id, bindFranchiseeIdList);
            return Triple.of(false, "", "场地不存在");
        }
        
        MerchantPlaceUpdateShowVO merchantPlaceUpdateShowVO = new MerchantPlaceUpdateShowVO();
        BeanUtils.copyProperties(merchantPlace, merchantPlaceUpdateShowVO);
        
        Franchisee franchisee = franchiseeService.queryByIdFromCache(merchantPlaceUpdateShowVO.getFranchiseeId());
        if (Objects.nonNull(franchisee)) {
            merchantPlaceUpdateShowVO.setFranchiseeName(franchisee.getName());
        }
        
        if (Objects.nonNull(merchantPlace.getMerchantAreaId())) {
            MerchantArea merchantArea = merchantAreaService.queryById(merchantPlace.getMerchantAreaId());
            
            Optional.ofNullable(merchantArea).ifPresent(i -> {
                merchantPlaceUpdateShowVO.setMerchantAreaName(merchantArea.getName());
            });
        }
        
        return Triple.of(true, "", merchantPlaceUpdateShowVO);
    }
    
    @Slave
    @Override
    public List<MerchantPlace> queryByIdList(List<Long> placeIdList, Integer tenantId) {
        return merchantPlaceMapper.selectByIdList(placeIdList, tenantId);
    }
    
}

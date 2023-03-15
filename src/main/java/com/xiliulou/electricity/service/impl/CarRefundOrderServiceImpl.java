package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.CarRefundOrder;
import com.xiliulou.electricity.entity.EleBindCarRecord;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.ElectricityCarModel;
import com.xiliulou.electricity.entity.Store;
import com.xiliulou.electricity.entity.UserCar;
import com.xiliulou.electricity.entity.UserCarDeposit;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.CarRefundOrderMapper;
import com.xiliulou.electricity.query.CarRefundOrderQuery;
import com.xiliulou.electricity.service.CarDepositOrderService;
import com.xiliulou.electricity.service.CarRefundOrderService;
import com.xiliulou.electricity.service.ElectricityCarModelService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.StoreService;
import com.xiliulou.electricity.service.UserCarDepositService;
import com.xiliulou.electricity.service.UserCarService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.CarRefundOrderVo;
import com.xiliulou.security.bean.TokenUser;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * (CarRefundOrder)表服务实现类
 *
 * @author Hardy
 * @since 2023-03-15 13:41:59
 */
@Service("carRefundOrderService")
@Slf4j
public class CarRefundOrderServiceImpl implements CarRefundOrderService {
    
    @Resource
    private CarRefundOrderMapper carRefundOrderMapper;
    
    @Autowired
    private RedisService redisService;
    
    @Autowired
    private UserInfoService userInfoService;
    
    
    @Autowired
    private ElectricityCarService electricityCarService;
    
    @Autowired
    private UserCarDepositService userCarDepositService;
    
    @Autowired
    private ElectricityCarModelService electricityCarModelService;
    
    @Autowired
    private StoreService storeService;
    
    @Autowired
    private UserCarService userCarService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CarRefundOrder queryByIdFromDB(Long id) {
        return this.carRefundOrderMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public CarRefundOrder queryByIdFromCache(Long id) {
        return null;
    }
    
    
    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    @Override
    public List<CarRefundOrder> queryAllByLimit(int offset, int limit) {
        return this.carRefundOrderMapper.queryAllByLimit(offset, limit);
    }
    
    /**
     * 新增数据
     *
     * @param carRefundOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CarRefundOrder insert(CarRefundOrder carRefundOrder) {
        this.carRefundOrderMapper.insertOne(carRefundOrder);
        return carRefundOrder;
    }
    
    /**
     * 修改数据
     *
     * @param carRefundOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(CarRefundOrder carRefundOrder) {
        return this.carRefundOrderMapper.update(carRefundOrder);
        
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
        return this.carRefundOrderMapper.deleteById(id) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer queryCountByStatus(Long uid, Integer tenantId, Integer status) {
        return this.carRefundOrderMapper.queryCountByStatus(uid, tenantId, status);
    }
    
    @Override
    public R queryCount(CarRefundOrderQuery query) {
        return R.ok(this.carRefundOrderMapper.queryCount(query));
    }
    
    @Override
    public R carRefundOrderReview(Long id, Integer status, String remark) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("CAR REFUND ORDER ERROR! user is null");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo) || !Objects.equals(userInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("CAR REFUND ORDER ERROR! userInfo is null error! uid={}", user.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        if (!Objects.equals(tenantId, userInfo.getTenantId())) {
            return R.ok();
        }
        
        CarRefundOrder carRefundOrder = this.queryByIdFromDB(id);
        if (Objects.isNull(carRefundOrder)) {
            log.error("CAR REFUND ORDER ERROR! carRefundOrder is null error! uid={}, id={}", user.getUid(), id);
            return R.fail("100263", "未找到还车审核记录");
        }
        
        if (!Objects.equals(carRefundOrder.getStatus(), CarRefundOrder.STATUS_INIT)) {
            return R.fail("100264", "还车审核记录已处理");
        }
        
        UserInfo carRefundUserInfo = userInfoService.queryByUidFromCache(carRefundOrder.getUid());
        if (Objects.isNull(carRefundUserInfo) || !Objects
                .equals(carRefundUserInfo.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("CAR REFUND ORDER ERROR! userInfo is null error! uid={}", carRefundOrder.getUid());
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        //用户是否绑定车辆
        if (!Objects.equals(carRefundUserInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_YES)) {
            log.error("ELE CAR ERROR! user not binding car,uid={}", userInfo.getUid());
            return R.fail("100015", "用户未绑定车辆");
        }
        
        ElectricityCar electricityCar = electricityCarService.queryByIdFromCache(carRefundOrder.getCarId().intValue());
        if (!Objects.equals(carRefundUserInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_YES)) {
            log.error("ELE CAR ERROR! user not binding car,uid={}", userInfo.getUid());
            return R.fail("100007", "未找到车辆");
        }
        
        CarRefundOrder updateCarRefundOrder = new CarRefundOrder();
        updateCarRefundOrder.setId(carRefundOrder.getId());
        updateCarRefundOrder.setStatus(status);
        updateCarRefundOrder.setRemark(remark);
        updateCarRefundOrder.setUpdateTime(System.currentTimeMillis());
        update(updateCarRefundOrder);
        
        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUid(carRefundUserInfo.getUid());
        updateUserInfo.setCarRentStatus(UserInfo.CAR_RENT_STATUS_NO);
        updateUserInfo.setUpdateTime(System.currentTimeMillis());
        userInfoService.updateByUid(updateUserInfo);
        
        UserCar updateUserCar = new UserCar();
        updateUserCar.setUid(carRefundUserInfo.getUid());
        updateUserCar.setCid(null);
        updateUserCar.setSn("");
        userCarService.unBindingCarByUid(updateUserCar);
        
        ElectricityCar updateElectricityCar = new ElectricityCar();
        updateElectricityCar.setId(electricityCar.getId());
        updateElectricityCar.setStatus(ElectricityCar.STATUS_NOT_RENT);
        updateElectricityCar.setUid(null);
        updateElectricityCar.setPhone(null);
        updateElectricityCar.setUserInfoId(null);
        updateElectricityCar.setUserName(null);
        updateElectricityCar.setUpdateTime(System.currentTimeMillis());
        electricityCarService.carUnBindUser(updateElectricityCar);
        return R.ok();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R userCarRefundOrder() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("CAR REFUND ORDER ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!redisService.setNx(CacheConstant.CACHE_USER_RETURN_CAR_LOCK + user.getUid(), "ok", 3000L, false)) {
            return R.fail("ELECTRICITY.000000", "操作频繁,请稍后再试!");
        }
        
        Integer tenantId = TenantContextHolder.getTenantId();
        
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("CAR REFUND ORDER ERROR! not found userInfo,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        
        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("CAR REFUND ORDER ERROR! user is disable! uid={}", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        
        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(user.getUid());
        if (Objects.isNull(userCarDeposit)) {
            log.error("CAR REFUND ORDER ERROR! not found userCarDeposit! uid={}", user.getUid());
            return R.fail("100013", "未缴纳租车押金");
        }
        
        //用户是否绑定车辆
        if (!Objects.equals(userInfo.getCarRentStatus(), UserInfo.CAR_RENT_STATUS_YES)) {
            log.error("CAR REFUND ORDER ERROR! user not binding car,uid={}", userInfo.getUid());
            return R.fail("100015", "用户未绑定车辆");
        }
        
        ElectricityCar electricityCar = electricityCarService.queryInfoByUid(userInfo.getUid());
        if (Objects.isNull(electricityCar)) {
            log.error("CAR REFUND ORDER ERROR! not found electricityCar! uid={}", user.getUid());
            return R.fail("100015", "用户未绑定车辆");
        }
        
        //是否有正在审核的订单
        Integer count = queryCountByStatus(userInfo.getUid(), tenantId, CarRefundOrder.STATUS_INIT);
        if (!Objects.equals(count, 0)) {
            log.warn("CAR REFUND ORDER WARN! return car under review! uid={}", user.getUid());
            return R.fail("100262", "还车审核中，请耐心等待");
        }
        
        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.RETURN_CAR, userInfo.getUid());
        
        //生成审核记录
        CarRefundOrder carRefundOrder = new CarRefundOrder();
        carRefundOrder.setOrderId(orderId);
        carRefundOrder.setUid(userInfo.getUid());
        carRefundOrder.setName(userInfo.getName());
        carRefundOrder.setPhone(userInfo.getPhone());
        carRefundOrder.setCarId(electricityCar.getId().longValue());
        carRefundOrder.setCarSn(electricityCar.getSn());
        carRefundOrder.setCarDeposit(userCarDeposit.getCarDeposit());
        carRefundOrder.setCarModelId(electricityCar.getModelId().longValue());
        carRefundOrder.setStatus(CarRefundOrder.STATUS_INIT);
        carRefundOrder.setStoreId(electricityCar.getStoreId());
        carRefundOrder.setTenantId(TenantContextHolder.getTenantId());
        carRefundOrder.setCreateTime(System.currentTimeMillis());
        carRefundOrder.setUpdateTime(System.currentTimeMillis());
        insert(carRefundOrder);
        
        //等待后台审核后车辆接触绑定
        return R.ok();
    }
    
    @Override
    public R queryList(CarRefundOrderQuery query) {
        List<CarRefundOrder> carRefundOrders = carRefundOrderMapper.queryList(query);
        if (CollectionUtils.isEmpty(carRefundOrders)) {
            return R.ok(new ArrayList<>());
        }
        
        List<CarRefundOrderVo> voList = new ArrayList<>();
        
        carRefundOrders.forEach(item -> {
            CarRefundOrderVo vo = new CarRefundOrderVo();
            BeanUtils.copyProperties(item, vo);
            
            ElectricityCarModel electricityCarModel = electricityCarModelService
                    .queryByIdFromCache(item.getCarModelId().intValue());
            if (Objects.nonNull(electricityCarModel)) {
                vo.setCarModelName(electricityCarModel.getName());
            }
            
            Store store = storeService.queryByIdFromCache(item.getStoreId());
            if (Objects.nonNull(store)) {
                vo.setStoreName(store.getName());
            }
            voList.add(vo);
        });
        return R.ok();
    }
}

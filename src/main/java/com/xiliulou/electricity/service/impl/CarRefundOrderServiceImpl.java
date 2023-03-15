package com.xiliulou.electricity.service.impl;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.CarRefundOrder;
import com.xiliulou.electricity.entity.ElectricityCar;
import com.xiliulou.electricity.entity.UserCar;
import com.xiliulou.electricity.entity.UserCarDeposit;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.mapper.CarRefundOrderMapper;
import com.xiliulou.electricity.service.CarDepositOrderService;
import com.xiliulou.electricity.service.CarRefundOrderService;
import com.xiliulou.electricity.service.ElectricityCarService;
import com.xiliulou.electricity.service.UserCarDepositService;
import com.xiliulou.electricity.service.UserCarService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    
    //    @Autowired
    //    private UserCarService userCarService;
    @Autowired
    private ElectricityCarService electricityCarService;
    
    //    @Autowired
    //    private CarDepositOrderService carDepositOrderService;
    @Autowired
    private UserCarDepositService userCarDepositService;
    
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
    public R userCarRefundOrder() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("CAR REFUND ORDER ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        
        if (!redisService.setNx(CacheConstant.CACHE_USER_RETURN_CAR_LOCK + user.getUid(), "ok", 3000L, false)) {
            return R.fail("ELECTRICITY.000000", "操作频繁,请稍后再试!");
        }
        
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
        
        //OrderIdUtil.generateBusinessOrderId()
        
        //生成审核记录
        //        CarRefundOrder carRefundOrder = new CarRefundOrder();
        //        carRefundOrder
        
        return null;
    }
}

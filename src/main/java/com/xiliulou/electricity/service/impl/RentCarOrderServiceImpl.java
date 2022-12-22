package com.xiliulou.electricity.service.impl;

import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.RentCarOrderMapper;
import com.xiliulou.electricity.query.RentCarHybridOrderQuery;
import com.xiliulou.electricity.query.RentCarOrderQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

/**
 * 租车订单表(RentCarOrder)表服务实现类
 *
 * @author zzlong
 * @since 2022-12-21 09:47:57
 */
@Service("rentCarOrderService")
@Slf4j
public class RentCarOrderServiceImpl implements RentCarOrderService {
    @Autowired
    private RentCarOrderMapper rentCarOrderMapper;
    @Autowired
    private ElectricityCarService electricityCarService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private UserCarMemberCardService userCarMemberCardService;
    @Autowired
    UserCarDepositService userCarDepositService;
    @Autowired
    ElectricityCarModelService electricityCarModelService;
    @Autowired
    UserCarService userCarService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public RentCarOrder selectByIdFromDB(Long id) {
        return this.rentCarOrderMapper.selectById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public RentCarOrder selectByIdFromCache(Long id) {
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
    public List<RentCarOrder> selectByPage(int offset, int limit) {
        return this.rentCarOrderMapper.selectByPage(offset, limit);
    }

    /**
     * 新增数据
     *
     * @param rentCarOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RentCarOrder insert(RentCarOrder rentCarOrder) {
        this.rentCarOrderMapper.insertOne(rentCarOrder);
        return rentCarOrder;
    }

    /**
     * 修改数据
     *
     * @param rentCarOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(RentCarOrder rentCarOrder) {
        return this.rentCarOrderMapper.update(rentCarOrder);

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
        return this.rentCarOrderMapper.deleteById(id) > 0;
    }


    @Override
    public Triple<Boolean, String, Object> rentCarOrder(RentCarOrderQuery query) {

        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE RENT CAR ERROR! not found user,sn={}", query.getSn());
            return Triple.of(false, "100001", "用户不存在");
        }

        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ELE RENT CAR ERROR! not found user,uid={}", user.getUid());
            return Triple.of(false, "100001", "用户不存在");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELE RENT CAR ERROR! user is disable!uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("ELE RENT CAR ERROR! user not auth,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0041", "未实名认证");
        }

        //判断是否缴纳押金
        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(user.getUid());
        if (Objects.isNull(userCarDeposit)) {
            log.error("ELE RENT CAR ERROR! userCarDeposit is null,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        }
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            log.error("ELE RENT CAR ERROR! not pay deposit,uid={}", user.getUid());
            return Triple.of(false, "ELECTRICITY.0042", "未缴纳押金");
        }

        //是否购买套餐
        UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(user.getUid());
        if (Objects.isNull(userCarMemberCard)) {
            log.error("ELE RENT CAR ERROR! not pay rent car memberCard,uid={}", user.getUid());
            return Triple.of(false, "100232", "未购买租车");
        }

        //套餐是否过期
        if (userCarMemberCard.getMemberCardExpireTime() < System.currentTimeMillis()) {
            log.error("ELE RENT CAR ERROR! rent car memberCard expired,uid={}", user.getUid());
            return Triple.of(false, "100233", "租车套餐已过期");
        }

        //车辆是否可用
        ElectricityCar electricityCar = electricityCarService.selectBySn(query.getSn());
        if (Objects.isNull(electricityCar) || !Objects.equals(electricityCar.getTenantId(), TenantContextHolder.getTenantId())) {
            log.error("ORDER ERROR! not found electricityCar,sn={},uid={}", query.getSn(), user.getUid());
            return Triple.of(false, "100007", "车辆不存在");
        }

        if (Objects.equals(electricityCar.getStatus(), ElectricityCar.STATUS_IS_RENT)) {
            log.error("ORDER ERROR! this car has been bound others,sn={},uid={}", query.getSn(), user.getUid());
            return Triple.of(false, "100231", "车辆已绑定其它用户");
        }

        ElectricityCarModel electricityCarModel = electricityCarModelService.queryByIdFromCache(electricityCar.getModelId());
        if (Objects.isNull(electricityCarModel)) {
            log.error("ELE RENT CAR ERROR! electricityCarModel is null,uid={}", user.getUid());
            return Triple.of(false, "100009", "车辆型号不存在");
        }


        String orderId = OrderIdUtil.generateBusinessOrderId(BusinessType.RENT_CAR, user.getUid());
        RentCarOrder rentCarOrder = new RentCarOrder();
        rentCarOrder.setOrderId(orderId);
        rentCarOrder.setCarModelId(electricityCar.getModelId().longValue());
        rentCarOrder.setCarDeposit(userCarDeposit.getCarDeposit().doubleValue());
        rentCarOrder.setStatus(RentCarOrder.STATUS_SUCCESS);
        rentCarOrder.setCarSn(query.getSn());
        rentCarOrder.setType(RentCarOrder.TYPE_RENT);
        rentCarOrder.setUid(user.getUid());
        rentCarOrder.setName(userInfo.getName());
        rentCarOrder.setPhone(userInfo.getPhone());
        rentCarOrder.setStoreId(electricityCarModel.getStoreId());
        rentCarOrder.setFranchiseeId(electricityCarModel.getFranchiseeId());
        rentCarOrder.setTenantId(TenantContextHolder.getTenantId());
        rentCarOrder.setCreateTime(System.currentTimeMillis());
        rentCarOrder.setUpdateTime(System.currentTimeMillis());

        int insert = rentCarOrderMapper.insertOne(rentCarOrder);

        DbUtils.dbOperateSuccessThen(insert, () -> {

            UserCar updateUserCar = new UserCar();
            updateUserCar.setUid(user.getUid());
            updateUserCar.setSn(query.getSn());
            updateUserCar.setUpdateTime(System.currentTimeMillis());
            userCarService.insertOrUpdate(updateUserCar);

            return null;
        });

        return Triple.of(true, "车辆绑定成功", null);
    }


    @Override
    public Triple<Boolean, String, Object> rentCarHybridOrder(RentCarHybridOrderQuery query, HttpServletRequest request) {


        return Triple.of(false, "", "");
    }
}

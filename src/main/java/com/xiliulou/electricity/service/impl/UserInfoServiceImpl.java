package com.xiliulou.electricity.service.impl;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.DS;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.UserInfoMapper;
import com.xiliulou.electricity.query.UserInfoBatteryAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoCarAddAndUpdate;
import com.xiliulou.electricity.query.UserInfoQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.PageUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.OwnMemberCardInfoVo;
import com.xiliulou.electricity.vo.UserInfoVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户列表(TUserInfo)表服务实现类
 *
 * @author makejava
 * @since 2020-12-07 15:00:00
 */
@Service("userInfoService")
@Slf4j
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    @Resource
    private UserInfoMapper userInfoMapper;
    @Autowired
    StoreService storeService;
    @Autowired
    RentBatteryOrderService rentBatteryOrderService;
    @Autowired
    RentCarOrderService rentCarOrderService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    RedisService redisService;
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    @Autowired
    UserService userService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public UserInfo queryByIdFromDB(Long id) {
        return this.userInfoMapper.queryById(id);
    }

    /**
     * @param id 主键
     * @return
     */
    @Override
    public UserInfo selectUserByUid(Long id) {
        UserInfo userInfo =
                this.userInfoMapper.selectOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUid, id));

        return userInfo;
    }

    /**
     * 新增数据
     *
     * @param userInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfo insert(UserInfo userInfo) {
        this.userInfoMapper.insert(userInfo);
        return userInfo;
    }

    /**
     * 修改数据
     *
     * @param userInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(UserInfo userInfo) {
        return this.userInfoMapper.update(userInfo);

    }

    @Override
    @Transactional
    public R bindBattery(UserInfoBatteryAddAndUpdate userInfoBatteryAddAndUpdate) {
        UserInfo oldUserInfo = queryByIdFromDB(userInfoBatteryAddAndUpdate.getId());
        if (Objects.isNull(oldUserInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        if (Objects.nonNull(oldUserInfo.getNowElectricityBatterySn())) {
            return R.fail("ELECTRICITY.0030", "用户已绑定电池，请解绑后再绑定");
        }
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByBindSn(userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
        if (Objects.isNull(oldElectricityBattery)) {
            return R.fail("ELECTRICITY.0020", "未找到电池");
        }
        UserInfo userInfo = new UserInfo();
        BeanUtil.copyProperties(userInfoBatteryAddAndUpdate, userInfo);
        RentBatteryOrder rentBatteryOrder = new RentBatteryOrder();
        if (Objects.nonNull(userInfoBatteryAddAndUpdate.getBatteryStoreId())) {
            Store store = storeService.queryByIdFromCache(userInfoBatteryAddAndUpdate.getBatteryStoreId());
            if (Objects.isNull(store)) {
                return R.fail("ELECTRICITY.0018", "未找到门店");
            }
            rentBatteryOrder.setBatteryStoreId(store.getId());
            rentBatteryOrder.setBatteryStoreName(store.getName());
        }
        userInfo.setNowElectricityBatterySn(userInfoBatteryAddAndUpdate.getInitElectricityBatterySn());
        userInfo.setUpdateTime(System.currentTimeMillis());
        userInfo.setServiceStatus(UserInfo.IS_SERVICE_STATUS);
        Integer update = userInfoMapper.update(userInfo);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //添加租电池记录
            rentBatteryOrder.setUid(oldUserInfo.getUid());
            rentBatteryOrder.setName(userInfo.getName());
            rentBatteryOrder.setPhone(oldUserInfo.getPhone());
            rentBatteryOrder.setIdNumber(userInfo.getIdNumber());
            rentBatteryOrder.setElectricityBatterySn(userInfo.getInitElectricityBatterySn());
            rentBatteryOrder.setBatteryDeposit(userInfo.getBatteryDeposit());
            rentBatteryOrder.setCreateTime(System.currentTimeMillis());
            rentBatteryOrder.setStatus(RentBatteryOrder.IS_USE_STATUS);
            rentBatteryOrderService.insert(rentBatteryOrder);
            //修改电池状态
            ElectricityBattery electricityBattery = new ElectricityBattery();
            electricityBattery.setId(oldElectricityBattery.getId());
            electricityBattery.setStatus(ElectricityBattery.LEASE_STATUS);
            electricityBattery.setUpdateTime(System.currentTimeMillis());
            electricityBatteryService.update(electricityBattery);
            return null;
        });
        return R.ok();
    }


    @Override
    @Transactional
    public R bindCar(UserInfoCarAddAndUpdate userInfoCarAddAndUpdate) {
        UserInfo oldUserInfo = queryByIdFromDB(userInfoCarAddAndUpdate.getId());
        if (Objects.isNull(oldUserInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        if (Objects.nonNull(oldUserInfo.getCarSn())) {
            return R.fail("ELECTRICITY.0031", "用户已绑定车辆，请解绑后再绑定");
        }
        RentCarOrder rentCarOrder = new RentCarOrder();
        if (Objects.nonNull(userInfoCarAddAndUpdate.getCarStoreId())) {
            Store store = storeService.queryByIdFromCache(userInfoCarAddAndUpdate.getCarStoreId());
            if (Objects.isNull(store)) {
                return R.fail("ELECTRICITY.0018", "未找到门店");
            }
            rentCarOrder.setCarStoreId(store.getId());
            rentCarOrder.setCarStoreName(store.getName());
        }
        UserInfo userInfo = new UserInfo();
        BeanUtil.copyProperties(userInfoCarAddAndUpdate, userInfo);
        userInfo.setUpdateTime(System.currentTimeMillis());
        Integer update = userInfoMapper.update(userInfo);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //添加租电池记录
            rentCarOrder.setUid(oldUserInfo.getUid());
            rentCarOrder.setName(userInfo.getName());
            rentCarOrder.setPhone(oldUserInfo.getPhone());
            rentCarOrder.setIdNumber(userInfo.getIdNumber());
            rentCarOrder.setCarSn(userInfo.getCarSn());
            rentCarOrder.setCarDeposit(userInfo.getCarDeposit());
            rentCarOrder.setNumberPlate(userInfo.getNumberPlate());
            rentCarOrder.setCreateTime(System.currentTimeMillis());
            rentCarOrder.setStatus(RentCarOrder.IS_USE_STATUS);
            rentCarOrderService.insert(rentCarOrder);
            return null;
        });
        return R.ok();
    }


    @Override
    @DS("slave_1")
    public R queryList(UserInfoQuery userInfoQuery) {
        Page page = PageUtil.getPage(userInfoQuery.getOffset(), userInfoQuery.getSize());
        page.setSize(userInfoQuery.getSize());
        userInfoMapper.queryList(page, userInfoQuery);
        if (ObjectUtil.isEmpty(page)) {
            return R.ok(new ArrayList<>());
        }
        List<UserInfoVO> UserInfoVOList = page.getRecords();
        page.setRecords(UserInfoVOList.stream().sorted(Comparator.comparing(UserInfoVO::getCreateTime).reversed()).collect(Collectors.toList()));
        return R.ok(page);
    }

    @Override
    @Transactional
    public R disable(Long id) {
        UserInfo oldUserInfo = queryByIdFromDB(id);
        if (Objects.isNull(oldUserInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setUpdateTime(System.currentTimeMillis());
        userInfo.setUsableStatus(UserInfo.USER_UN_USABLE_STATUS);
        userInfoMapper.update(userInfo);
        return R.ok();
    }

    @Override
    @Transactional
    public R reboot(Long id) {
        UserInfo oldUserInfo = queryByIdFromDB(id);
        if (Objects.isNull(oldUserInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setUpdateTime(System.currentTimeMillis());
        userInfo.setUsableStatus(UserInfo.USER_USABLE_STATUS);
        userInfoMapper.update(userInfo);
        return R.ok();
    }

    @Override
    @Transactional
    public R unBindBattery(Long id) {
        UserInfo oldUserInfo = queryByIdFromDB(id);
        if (Objects.isNull(oldUserInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        if (Objects.isNull(oldUserInfo.getNowElectricityBatterySn())) {
            return R.fail("ELECTRICITY.0029", "用户未绑定电池，不能解绑");
        }
        ElectricityBattery oldElectricityBattery = electricityBatteryService.queryByUnBindSn(oldUserInfo.getNowElectricityBatterySn());
        if (Objects.isNull(oldElectricityBattery)) {
            return R.fail("ELECTRICITY.0020", "未找到电池");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setCarSn(oldUserInfo.getCarSn());
        userInfo.setCarDeposit(oldUserInfo.getCarDeposit());
        userInfo.setNumberPlate(oldUserInfo.getNumberPlate());
        userInfo.setInitElectricityBatterySn(null);
        userInfo.setNowElectricityBatterySn(null);
        userInfo.setBatteryDeposit(null);
        userInfo.setServiceStatus(UserInfo.NO_SERVICE_STATUS);
        userInfo.setUpdateTime(System.currentTimeMillis());
        Integer update = userInfoMapper.unBind(userInfo);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //添加租电池记录
            RentBatteryOrder rentBatteryOrder = new RentBatteryOrder();
            rentBatteryOrder.setUid(oldUserInfo.getUid());
            rentBatteryOrder.setName(oldUserInfo.getName());
            rentBatteryOrder.setPhone(oldUserInfo.getPhone());
            rentBatteryOrder.setIdNumber(oldUserInfo.getIdNumber());
            rentBatteryOrder.setElectricityBatterySn(oldUserInfo.getInitElectricityBatterySn());
            rentBatteryOrder.setBatteryDeposit(oldUserInfo.getBatteryDeposit());
            rentBatteryOrder.setCreateTime(System.currentTimeMillis());
            rentBatteryOrder.setStatus(RentBatteryOrder.NO_USE_STATUS);
            rentBatteryOrderService.insert(rentBatteryOrder);
            //修改电池状态
            ElectricityBattery electricityBattery = new ElectricityBattery();
            electricityBattery.setId(oldElectricityBattery.getId());
            electricityBattery.setStatus(ElectricityBattery.STOCK_STATUS);
            electricityBattery.setUpdateTime(System.currentTimeMillis());
            electricityBatteryService.update(electricityBattery);
            return null;
        });
        return R.ok();
    }

    @Override
    @Transactional
    public R unBindCar(Long id) {
        UserInfo oldUserInfo = queryByIdFromDB(id);
        if (Objects.isNull(oldUserInfo)) {
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }
        if (Objects.isNull(oldUserInfo.getCarSn())) {
            return R.fail("ELECTRICITY.0032", "用户未绑定车辆，不能解绑");
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setNowElectricityBatterySn(oldUserInfo.getNowElectricityBatterySn());
        userInfo.setInitElectricityBatterySn(oldUserInfo.getInitElectricityBatterySn());
        userInfo.setBatteryDeposit(oldUserInfo.getBatteryDeposit());
        userInfo.setCarSn(null);
        userInfo.setCarDeposit(null);
        userInfo.setNumberPlate(null);
        userInfo.setUpdateTime(System.currentTimeMillis());
        Integer update = userInfoMapper.unBind(userInfo);
        DbUtils.dbOperateSuccessThen(update, () -> {
            //添加租电池记录
            RentCarOrder rentCarOrder = new RentCarOrder();
            rentCarOrder.setUid(oldUserInfo.getUid());
            rentCarOrder.setName(oldUserInfo.getName());
            rentCarOrder.setPhone(oldUserInfo.getPhone());
            rentCarOrder.setIdNumber(oldUserInfo.getIdNumber());
            rentCarOrder.setCarSn(oldUserInfo.getCarSn());
            rentCarOrder.setCarDeposit(oldUserInfo.getCarDeposit());
            rentCarOrder.setNumberPlate(oldUserInfo.getNumberPlate());
            rentCarOrder.setCreateTime(System.currentTimeMillis());
            rentCarOrder.setStatus(RentCarOrder.NO_USE_STATUS);
            rentCarOrderService.insert(rentCarOrder);
            return null;
        });
        return R.ok();
    }

    @Override
    public UserInfo queryByUid(Long uid) {
        return userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUid, uid).eq(UserInfo::getDelFlag, UserInfo.DEL_NORMAL));
    }

    @Override
    public Integer homeOneTotal(Long first, Long now) {
        return userInfoMapper.selectCount(new LambdaQueryWrapper<UserInfo>().between(UserInfo::getCreateTime, first, now).eq(UserInfo::getDelFlag, UserInfo.DEL_NORMAL));
    }

    @Override
    public Integer homeOneService(Long first, Long now) {
        return userInfoMapper.selectCount(new LambdaQueryWrapper<UserInfo>().between(UserInfo::getCreateTime, first, now).eq(UserInfo::getDelFlag, UserInfo.DEL_NORMAL)
                .eq(UserInfo::getServiceStatus, UserInfo.IS_SERVICE_STATUS));
    }

    @Override
    public Integer homeOneMemberCard(Long first, Long now) {
        return userInfoMapper.homeOneMemberCard(first, now);
    }

    @Override
    public int minCount(Long id) {
        return userInfoMapper.minCount(id, System.currentTimeMillis());
    }

    @Override
    public List<HashMap<String, String>> homeThreeTotal(long startTimeMilliDay, Long endTimeMilliDay) {
        return userInfoMapper.homeThreeTotal(startTimeMilliDay, endTimeMilliDay);
    }

    @Override
    public List<HashMap<String, String>> homeThreeService(long startTimeMilliDay, Long endTimeMilliDay) {
        return userInfoMapper.homeThreeService(startTimeMilliDay, endTimeMilliDay);
    }

    @Override
    public List<HashMap<String, String>> homeThreeMemberCard(long startTimeMilliDay, Long endTimeMilliDay) {
        return userInfoMapper.homeThreeMemberCard(startTimeMilliDay, endTimeMilliDay);
    }

    /**
     * 获取用户套餐信息
     *
     * @param uid
     * @return
     */
    @Override
    @DS("slave_1")

    public R getMemberCardInfo(Long uid) {
        UserInfo userInfo = selectUserByUid(uid);
        if (Objects.isNull(userInfo)) {
            log.error("GET_MEMBER_CARD_INFO ERROR,NOT FOUND USERINFO,UID:{}", uid);
            return R.failMsg("未找到用户信息!");
        }
        if (Objects.isNull(userInfo.getRemainingNumber()) || Objects.isNull(userInfo.getMemberCardExpireTime()) || System.currentTimeMillis() >=
                userInfo.getMemberCardExpireTime() || userInfo.getRemainingNumber() == 0) {
            return R.ok();
        }


        OwnMemberCardInfoVo ownMemberCardInfoVo = new OwnMemberCardInfoVo();
        ownMemberCardInfoVo.setMemberCardExpireTime(userInfo.getMemberCardExpireTime());
        ownMemberCardInfoVo.setRemainingNumber(userInfo.getRemainingNumber());
        ownMemberCardInfoVo.setType(userInfo.getCardType());
        ownMemberCardInfoVo.setDays((long) Math.round((userInfo.getMemberCardExpireTime() - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)));
        return R.ok(ownMemberCardInfoVo);
    }

    @Override
    public void deleteUserInfo(UserInfo oldUserInfo) {
        userInfoMapper.deleteById(oldUserInfo.getId());
    }

    @Override
    public void updateByUid(UserInfo userInfo) {
        userInfoMapper.updateByUid(userInfo);
    }

    @Override
    public void plusCount(Long id) {
        userInfoMapper.plusCount(id, System.currentTimeMillis());
    }

    @Override
    public R queryUserInfo() {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        //2.判断用户是否有电池是否有月卡
        UserInfo userInfo = queryByUid(user.getUid());
        //用户是否可用
        if (Objects.isNull(userInfo) || Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("ELECTRICITY  ERROR! not found userInfo ");
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }
        //判断是否开通服务
        if (Objects.equals(userInfo.getServiceStatus(), UserInfo.NO_SERVICE_STATUS)) {
            log.error("ELECTRICITY  ERROR! not found userInfo ");
            return R.fail("ELECTRICITY.0021", "未开通服务");
        }
        //判断用户是否开通月卡
        if (Objects.isNull(userInfo.getMemberCardExpireTime()) || Objects.isNull(userInfo.getRemainingNumber())) {
            log.error("ELECTRICITY  ERROR! not found memberCard ");
            return R.fail("ELECTRICITY.0022", "未开通月卡");
        }
        Long now = System.currentTimeMillis();
        if (userInfo.getMemberCardExpireTime() < now || userInfo.getRemainingNumber() == 0) {
            log.error("ELECTRICITY  ERROR! not found memberCard ");
            return R.fail("ELECTRICITY.0023", "月卡已过期");
        }
        return R.ok(userInfo);
    }


}
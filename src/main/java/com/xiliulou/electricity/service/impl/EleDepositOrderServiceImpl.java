package com.xiliulou.electricity.service.impl;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.constant.ElectricityCabinetConstant;
import com.xiliulou.electricity.entity.CommonOrder;
import com.xiliulou.electricity.entity.EleDepositOrder;
import com.xiliulou.electricity.entity.ElectricityPayParams;
import com.xiliulou.electricity.entity.ElectricityTradeOrder;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.mapper.EleDepositOrderMapper;
import com.xiliulou.electricity.service.EleDepositOrderService;
import com.xiliulou.electricity.service.ElectricityPayParamsService;
import com.xiliulou.electricity.service.ElectricityTradeOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 缴纳押金订单表(TEleDepositOrder)表服务实现类
 *
 * @author makejava
 * @since 2021-02-22 10:16:44
 */
@Service("eleDepositOrderService")
@Slf4j
public class EleDepositOrderServiceImpl implements EleDepositOrderService {
    @Resource
    EleDepositOrderMapper eleDepositOrderMapper;
    @Autowired
    RedisService redisService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    UserService userService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    ElectricityTradeOrderService electricityTradeOrderService;
    @Autowired
    ElectricityPayParamsService electricityPayParamsService;
    @Autowired
    UserOauthBindService userOauthBindService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleDepositOrder queryByIdFromDB(Long id) {
        return this.eleDepositOrderMapper.queryById(id);
    }
    
        /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EleDepositOrder queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 新增数据
     *
     * @param eleDepositOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EleDepositOrder insert(EleDepositOrder eleDepositOrder) {
        this.eleDepositOrderMapper.insert(eleDepositOrder);
        return eleDepositOrder;
    }

    /**
     * 修改数据
     *
     * @param eleDepositOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EleDepositOrder eleDepositOrder) {
       return this.eleDepositOrderMapper.update(eleDepositOrder);
         
    }

    @Override
    public EleDepositOrder queryByOrderId(String orderNo) {
        return eleDepositOrderMapper.selectOne(new LambdaQueryWrapper<EleDepositOrder>().eq(EleDepositOrder::getOrderId,orderNo));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R payDeposit(HttpServletRequest request) {
            //用户信息
            Long uid = SecurityUtils.getUid();
            if (Objects.isNull(uid)) {
                return R.fail("ELECTRICITY.0001", "未找到用户");
            }
            //限频
            Boolean getLockSuccess = redisService.setNx(ElectricityCabinetConstant.ELE_CACHE_USER_DEPOSIT_LOCK_KEY + uid, IdUtil.fastSimpleUUID(), 3*1000L, false);
            if (!getLockSuccess) {
                return R.fail("ELECTRICITY.0034", "操作频繁");
            }
            User user=userService.queryByUidFromCache(uid);
            if (Objects.isNull(user)) {
                log.error("ELECTRICITY  ERROR! not found user! userId:{}",uid);
                return R.fail("ELECTRICITY.0001", "未找到用户");
            }

            //判断是否实名认证
            UserInfo userInfo = userInfoService.queryByUid(uid);
            if (Objects.isNull(userInfo)) {
                log.error("ELECTRICITY  ERROR! not found userInfo! userId:{}",uid);
                return R.fail("ELECTRICITY.0001", "未找到用户");
            }
            if (!Objects.equals(userInfo.getServiceStatus(), UserInfo.STATUS_IS_AUTH)) {
                return R.fail("ELECTRICITY.0041", "未实名认证");
            }

            //计算押金
            //根据用户cid找到对应的加盟商
            Franchisee franchisee=franchiseeService.queryByCid(user.getCid());
            if (Objects.isNull(franchisee)) {
                log.error("ELECTRICITY  ERROR! not found franchisee ! cid:{} ",user.getCid());
                return R.fail("ELECTRICITY.0038", "未找到加盟商");
            }
            BigDecimal payAmount=franchisee.getBatteryDeposit();
            String orderId=generateOrderId(uid);

            //生成订单
            EleDepositOrder eleDepositOrder = EleDepositOrder.builder()
                    .orderId(orderId)
                    .uid(user.getUid())
                    .phone(userInfo.getPhone())
                    .userName(user.getName())
                    .payAmount(payAmount)
                    .status(1)
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis()).build();

            //支付零元
            if(payAmount.compareTo(BigDecimal.valueOf(0.01))<0){
                eleDepositOrder.setStatus(2);
                eleDepositOrderMapper.insert(eleDepositOrder);
                UserInfo userInfoUpdate = new UserInfo();
                userInfoUpdate.setId(userInfo.getId());
                userInfoUpdate.setServiceStatus(UserInfo.STATUS_IS_DEPOSIT);
                userInfoUpdate.setUpdateTime(System.currentTimeMillis());
                userInfoService.updateById(userInfoUpdate);
                return R.ok();
            }
            eleDepositOrderMapper.insert(eleDepositOrder);

            //调起支付
            CommonOrder commonOrder=CommonOrder.builder()
                    .orderId(orderId)
                    .uid(user.getUid())
                    .payAmount(payAmount)
                    .orderType(ElectricityTradeOrder.ORDER_TYPE_DEPOSIT)
                    .attach(ElectricityTradeOrder.ATTACH_DEPOSIT).build();
            ElectricityPayParams electricityPayParams = electricityPayParamsService.getElectricityPayParams();
            UserOauthBind userOauthBind = userOauthBindService.queryUserOauthBySysId(uid);
            Pair<Boolean, Object> getPayParamsPair =
                    electricityTradeOrderService.commonCreateTradeOrderAndGetPayParams(commonOrder, electricityPayParams, userOauthBind.getThirdId(), request);
            if (!getPayParamsPair.getLeft()) {
                return R.failMsg(getPayParamsPair.getRight().toString());
            }
            return R.ok(getPayParamsPair.getRight());
        }

    @Override
    public Long queryByUid(Long uid) {
        List<EleDepositOrder> eleDepositOrderList =eleDepositOrderMapper.selectList(new LambdaQueryWrapper<EleDepositOrder>().eq(EleDepositOrder::getUid,uid).orderByDesc(EleDepositOrder::getUpdateTime));
        if(ObjectUtil.isNotEmpty(eleDepositOrderList)){
            return eleDepositOrderList.get(0).getUpdateTime();
        }
        return null;
    }


    public String generateOrderId(Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(2) + uid+
                RandomUtil.randomNumbers(6);
    }
}
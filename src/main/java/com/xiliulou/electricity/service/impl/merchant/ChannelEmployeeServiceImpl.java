package com.xiliulou.electricity.service.impl.merchant;

import cn.hutool.core.map.MapUtil;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.i18n.MessageUtils;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserOauthBind;
import com.xiliulou.electricity.entity.merchant.ChannelEmployee;
import com.xiliulou.electricity.entity.merchant.ChannelEmployeeAmount;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantArea;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.merchant.ChannelEmployeeAmountMapper;
import com.xiliulou.electricity.mapper.merchant.ChannelEmployeeMapper;
import com.xiliulou.electricity.request.merchant.ChannelEmployeeRequest;
import com.xiliulou.electricity.request.merchant.MerchantPageRequest;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserRoleService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.ChannelEmployeeService;
import com.xiliulou.electricity.service.merchant.MerchantAreaService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.OperateRecordUtil;
import com.xiliulou.electricity.vo.merchant.ChannelEmployeeVO;
import com.xiliulou.security.authentication.console.CustomPasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author BaoYu
 * @description:
 * @date 2024/1/31 16:13
 */

@Slf4j
@Service("channelEmployeeService")
public class ChannelEmployeeServiceImpl implements ChannelEmployeeService {
    
    @Resource
    CustomPasswordEncoder customPasswordEncoder;
    
    @Resource
    UserService userService;
    
    @Resource
    UserRoleService userRoleService;
    
    @Resource
    FranchiseeService franchiseeService;
    
    @Resource
    ChannelEmployeeMapper channelEmployeeMapper;
    
    @Resource
    ChannelEmployeeAmountMapper channelEmployeeAmountMapper;
    
    @Resource
    RedisService redisService;
    
    @Resource
    MerchantService merchantService;
    
    @Resource
    MerchantAreaService merchantAreaService;
    
    @Resource
    private UserOauthBindService userOauthBindService;
    
    @Resource
    private OperateRecordUtil operateRecordUtil;
    
    @Slave
    @Override
    public ChannelEmployeeVO queryById(Long id, List<Long> bindFranchiseeIdList) {
        ChannelEmployee channelEmployee = channelEmployeeMapper.selectById(id);
        if (Objects.isNull(channelEmployee)) {
            return null;
        }
        
        if (ObjectUtils.isNotEmpty(bindFranchiseeIdList) && !bindFranchiseeIdList.contains(channelEmployee.getFranchiseeId())) {
            log.warn("channel employee query by id warn, franchisee is different, id={}, bindFranchiseeId={}", id, bindFranchiseeIdList);
            return null;
        }
        
        ChannelEmployeeVO channelEmployeeVO = new ChannelEmployeeVO();
        BeanUtils.copyProperties(channelEmployee, channelEmployeeVO);
        if (Objects.nonNull(channelEmployeeVO.getUid())) {
            User user = userService.queryByUidFromCache(channelEmployeeVO.getUid());
            channelEmployeeVO.setName(user.getName());
            channelEmployeeVO.setPhone(user.getPhone());
            channelEmployeeVO.setStatus(user.getLockFlag());
        }
        return channelEmployeeVO;
    }
    
    @Slave
    @Override
    public ChannelEmployeeVO queryByUid(Long uid) {
        ChannelEmployee channelEmployee = channelEmployeeMapper.selectByUid(uid);
        if (Objects.isNull(channelEmployee)) {
            return null;
        }
        ChannelEmployeeVO channelEmployeeVO = new ChannelEmployeeVO();
        BeanUtils.copyProperties(channelEmployee, channelEmployeeVO);
        if (Objects.nonNull(channelEmployeeVO.getUid())) {
            User user = userService.queryByUidFromCache(channelEmployeeVO.getUid());
            channelEmployeeVO.setName(user.getName());
            channelEmployeeVO.setPhone(user.getPhone());
            channelEmployeeVO.setStatus(user.getLockFlag());
        }
        return channelEmployeeVO;
    }
    
    @Slave
    @Override
    public List<ChannelEmployeeVO> listChannelEmployee(ChannelEmployeeRequest channelEmployeeRequest) {
        
        List<ChannelEmployeeVO> channelEmployees = channelEmployeeMapper.selectListByCondition(channelEmployeeRequest);
        //查询openId
        if (!CollectionUtils.isEmpty(channelEmployees)){
            Set<Long> longs = channelEmployees.parallelStream().filter(Objects::nonNull).map(ChannelEmployeeVO::getUid).collect(
                    Collectors.toSet());
            List<UserOauthBind> openIds=userOauthBindService.queryOpenIdListByUidsAndTenantId(new ArrayList<>(longs), TenantContextHolder.getTenantId());
            
            //将查询出来的openid集合与channelEmployees集合进行匹配
            if (!CollectionUtils.isEmpty(openIds)){
                Map<Long, String> openIdMap = openIds.parallelStream().collect(Collectors.toMap(UserOauthBind::getUid, UserOauthBind::getThirdId, (k1, k2) -> k2));
                channelEmployees.parallelStream().forEach(channelEmployeeVO -> {
                    String openId = openIdMap.get(channelEmployeeVO.getUid());
                    if (Objects.nonNull(openId)){
                        channelEmployeeVO.setOpenId(openId);
                    }
                });
            }
        }
        
        
        
        channelEmployees.parallelStream().forEach(item -> {
            
            //设置加盟商名称
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            if (Objects.nonNull(franchisee)) {
                item.setFranchiseeName(franchisee.getName());
            }
            
            //设置区域名称
            MerchantArea merchantArea = merchantAreaService.queryById(item.getAreaId());
            if (Objects.nonNull(merchantArea)) {
                item.setAreaName(merchantArea.getName());
            }
            
            //设置商户数
            MerchantPageRequest merchantPageRequest = new MerchantPageRequest();
            merchantPageRequest.setChannelEmployeeUid(item.getUid());
            Integer countTotal = merchantService.countTotal(merchantPageRequest);
            item.setMerchantTotal(countTotal != null ? countTotal : 0);
            
        });
        
        return channelEmployees;
    }
    
    @Slave
    @Override
    public Integer countChannelEmployee(ChannelEmployeeRequest channelEmployeeRequest) {
        
        return channelEmployeeMapper.countByCondition(channelEmployeeRequest);
    }
    
    @Slave
    @Override
    public List<ChannelEmployeeVO> queryChannelEmployees(ChannelEmployeeRequest channelEmployeeRequest) {
        
        List<ChannelEmployeeVO> channelEmployeeVOList = channelEmployeeMapper.selectChannelEmployees(channelEmployeeRequest);
        channelEmployeeVOList.parallelStream().forEach(item -> {
            if (Objects.nonNull(item.getFranchiseeId())) {
                Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
                item.setFranchiseeName(franchisee.getName());
            }
        });
        return channelEmployeeVOList;
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> saveChannelEmployee(ChannelEmployeeRequest channelEmployeeRequest) {
        if (!redisService.setNx(CacheConstant.CACHE_CHANNEL_EMPLOYEE_SAVE_LOCK + channelEmployeeRequest.getPhone(), "1", 3000L, false)) {
            return Triple.of(false, "000000", "操作频繁,请稍后再试");
        }
    
        // 检测选中的加盟商和当前登录加盟商是否一致
        if (ObjectUtils.isNotEmpty(channelEmployeeRequest.getBindFranchiseeIdList()) && !channelEmployeeRequest.getBindFranchiseeIdList().contains(channelEmployeeRequest.getFranchiseeId())) {
            log.warn("channel employee save error, franchisee is not different id={}, franchiseeId={}, bindFranchiseeId={}", channelEmployeeRequest.getId(), channelEmployeeRequest.getFranchiseeId(), channelEmployeeRequest.getBindFranchiseeIdList());
            return Triple.of(false, "120240", "当前加盟商无权限操作");
        }
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
    
        // 检测手机号
        User userPhone = userService.checkPhoneExist(null, channelEmployeeRequest.getPhone(), User.TYPE_USER_CHANNEL, tenantId, null);
        if (Objects.nonNull(userPhone)) {
            log.warn("current phone has been used by other one, phone = {}, tenant id = {}", channelEmployeeRequest.getPhone(), tenantId);
            return Triple.of(false, "120001", "当前手机号已注册");
        }
        
        User existUser = userService.queryByUserName(channelEmployeeRequest.getName());
        if (Objects.nonNull(existUser)) {
            log.warn("The user name has been used by other one, name = {}, tenant id = {}", channelEmployeeRequest.getName(), tenantId);
            return Triple.of(false, "120009", "用户姓名已存在");
        }
        
        //创建渠道员工账户
        User user = User.builder().updateTime(System.currentTimeMillis()).createTime(System.currentTimeMillis()).phone(channelEmployeeRequest.getPhone())
                .lockFlag(User.USER_UN_LOCK).gender(User.GENDER_MALE).lang(MessageUtils.LOCALE_ZH_CN).userType(User.TYPE_USER_CHANNEL).name(channelEmployeeRequest.getName())
                .salt("").avatar("").tenantId(tenantId).loginPwd(customPasswordEncoder.encode("123456")).delFlag(User.DEL_NORMAL).build();
        
        // 如果是禁用则用户默认锁定
        if (Objects.equals(channelEmployeeRequest.getStatus(), MerchantConstant.DISABLE)) {
            user.setLockFlag(User.USER_LOCK);
        }
        
        User userResult = userService.insert(user);
        
        //TODO 设置角色, 渠道员工角色值待定
       /* Long roleId = 0L;
        
        UserRole userRole = new UserRole();
        userRole.setRoleId(roleId);
        userRole.setUid(userResult.getUid());*/
        //userRoleService.insert(userRole);
        
        //创建渠道员工账户
        ChannelEmployeeAmount channelEmployeeAmount = new ChannelEmployeeAmount();
        channelEmployeeAmount.setUid(userResult.getUid());
        channelEmployeeAmount.setTotalIncome(BigDecimal.ZERO);
        channelEmployeeAmount.setBalance(BigDecimal.ZERO);
        channelEmployeeAmount.setWithdrawAmount(BigDecimal.ZERO);
        channelEmployeeAmount.setDelFlag(CommonConstant.DEL_N);
        channelEmployeeAmount.setFranchiseeId(channelEmployeeRequest.getFranchiseeId());
        channelEmployeeAmount.setTenantId(tenantId);
        channelEmployeeAmount.setCreateTime(System.currentTimeMillis());
        channelEmployeeAmount.setUpdateTime(System.currentTimeMillis());
        
        channelEmployeeAmountMapper.insertOne(channelEmployeeAmount);
        
        //创建渠道员工
        ChannelEmployee channelEmployee = new ChannelEmployee();
        channelEmployee.setUid(userResult.getUid());
        channelEmployee.setTenantId(tenantId);
        channelEmployee.setAreaId(channelEmployeeRequest.getAreaId());
        channelEmployee.setFranchiseeId(channelEmployeeRequest.getFranchiseeId());
        channelEmployee.setDelFlag(CommonConstant.DEL_N);
        channelEmployee.setCreateTime(System.currentTimeMillis());
        channelEmployee.setUpdateTime(System.currentTimeMillis());
        
        Integer result = channelEmployeeMapper.insertOne(channelEmployee);
        
        return Triple.of(true, null, result);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Triple<Boolean, String, Object> updateChannelEmployee(ChannelEmployeeRequest channelEmployeeRequest) {
        if (!redisService.setNx(CacheConstant.CACHE_CHANNEL_EMPLOYEE_UPDATE_LOCK + channelEmployeeRequest.getPhone(), "1", 3000L, false)) {
            return Triple.of(false, "000000", "操作频繁,请稍后再试");
        }
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        ChannelEmployee channelEmployee = channelEmployeeMapper.selectById(channelEmployeeRequest.getId());
        if (Objects.isNull(channelEmployee)) {
            log.error("not found channel employee by id, id = {}", channelEmployeeRequest.getId());
            return Triple.of(false, "120008", "当前渠道员工不存在");
        }
    
        // 判断修改的加盟商是否有改变
        if (!Objects.equals(channelEmployeeRequest.getFranchiseeId(), channelEmployee.getFranchiseeId())) {
            log.error("channel employee update error, franchisee not allow change id={}, franchiseeId={}, updateFranchiseeId={}", channelEmployeeRequest.getId(), channelEmployee.getFranchiseeId(), channelEmployeeRequest.getFranchiseeId());
            return Triple.of(false, "120239", "渠道员加盟商不允许修改");
        }
    
        // 检测选中的加盟商和当前登录加盟商是否一致
        if (ObjectUtils.isNotEmpty(channelEmployeeRequest.getBindFranchiseeIdList()) && !channelEmployeeRequest.getBindFranchiseeIdList().contains(channelEmployeeRequest.getFranchiseeId())) {
            log.error("channel update error, franchisee is not different id={}, franchiseeId={}, bindFranchiseeId={}", channelEmployeeRequest.getId(), channelEmployeeRequest.getFranchiseeId(), channelEmployeeRequest.getBindFranchiseeIdList());
            return Triple.of(false, "120240", "当前加盟商无权限操作");
        }
        
        User user = userService.queryByUidFromCache(channelEmployee.getUid());
        if (Objects.isNull(user)) {
            log.error("not found user for update channel employee by uid, uid = {}", channelEmployee.getUid());
            return Triple.of(false, "120008", "当前渠道员工不存在");
        }
        
        if (!Objects.equals(user.getName(), channelEmployeeRequest.getName())) {
            User existUser = userService.queryByUserName(channelEmployeeRequest.getName());
            if (Objects.nonNull(existUser)) {
                log.error("The user name has been used by other one for update channel employee, name = {}, tenant id = {}", channelEmployeeRequest.getName(), tenantId);
                return Triple.of(false, "120009", "用户姓名已存在");
            }
        }
        
        if (!Objects.equals(user.getPhone(), channelEmployeeRequest.getPhone())) {
            // 检测手机号
            User userPhone = userService.checkPhoneExist(null, channelEmployeeRequest.getPhone(), User.TYPE_USER_CHANNEL, tenantId, channelEmployee.getUid());
            if (Objects.nonNull(userPhone)) {
                log.error("current phone has been used by other one for update channel employee, phone = {}, tenant id = {}", channelEmployeeRequest.getPhone(), tenantId);
                return Triple.of(false, "120001", "当前手机号已注册");
            }
        }
        
        String oldPhone = user.getPhone();
        User updateUser = new User();
        
        // 如果是禁用，则将用户置为锁定
        if (Objects.equals(channelEmployeeRequest.getStatus(), MerchantConstant.DISABLE)) {
            updateUser.setLockFlag(User.USER_LOCK);
        } else {
            updateUser.setLockFlag(User.USER_UN_LOCK);
        }
        
        updateUser.setUid(channelEmployee.getUid());
        updateUser.setName(channelEmployeeRequest.getName());
        updateUser.setPhone(channelEmployeeRequest.getPhone());
        updateUser.setUserType(User.TYPE_USER_CHANNEL);
        updateUser.setTenantId(TenantContextHolder.getTenantId());
        updateUser.setUpdateTime(System.currentTimeMillis());
        userService.updateMerchantUser(updateUser);
        
        ChannelEmployee channelEmployeeUpdate = new ChannelEmployee();
        channelEmployeeUpdate.setId(channelEmployeeRequest.getId());
        channelEmployeeUpdate.setFranchiseeId(channelEmployeeRequest.getFranchiseeId());
        //channelEmployeeUpdate.setUid(channelEmployee.getUid());
        //channelEmployeeUpdate.setTenantId(tenantId);
        if (channelEmployeeRequest.getAreaId() != null) {
            channelEmployeeUpdate.setAreaId(channelEmployeeRequest.getAreaId());
        } else {
            channelEmployeeUpdate.setAreaId(NumberConstant.ZERO_L);
        }
        
        channelEmployeeUpdate.setUpdateTime(System.currentTimeMillis());
        
        Integer result = channelEmployeeMapper.updateOne(channelEmployeeUpdate);
        
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                //清理缓存，避免缓存操作和数据库提交在同一个事务中失效的问题
                redisService.delete(CacheConstant.CACHE_USER_UID + updateUser.getUid());
                redisService.delete(CacheConstant.CACHE_USER_PHONE + updateUser.getTenantId() + ":" + oldPhone + ":" + updateUser.getUserType());
            }
        });
        //记录操作日志
        HashMap<String, Object> newValue = MapUtil.of("status", channelEmployeeRequest.getStatus());
        newValue.put("name",channelEmployeeRequest.getName());
        operateRecordUtil.record(null,newValue);
        return Triple.of(true, null, result);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer removeById(Long id, List<Long> bindFranchiseeIdList) {
        ChannelEmployee channelEmployee = channelEmployeeMapper.selectById(id);
        
        if (Objects.isNull(channelEmployee)) {
            log.error("not found channel employee by id, id = {}", id);
            throw new BizException("120008", "渠道员工不存在");
        }
    
        if (ObjectUtils.isNotEmpty(bindFranchiseeIdList) && !bindFranchiseeIdList.contains(channelEmployee.getFranchiseeId())) {
            log.error("channel employee remove error, franchisee is different, id = {}, bindFranchiseeId={}", id, bindFranchiseeIdList);
            throw new BizException("120008", "渠道员工不存在");
        }
        
        List<Merchant> merchants = merchantService.queryByChannelEmployeeUid(channelEmployee.getUid());
        if (CollectionUtils.isNotEmpty(merchants)) {
            log.error("channel employee was already bind merchant, can't remove, channel employee uid = {}", channelEmployee.getUid());
            throw new BizException("120023", "该渠道员下还有绑定的商户，请先解绑后操作");
        }
        
        channelEmployeeMapper.removeById(id, System.currentTimeMillis());
        
        User user = userService.queryByUidFromCache(channelEmployee.getUid());
        Integer result = 0;
        if (Objects.nonNull(user)) {
            User updateUser = new User();
            updateUser.setUid(user.getUid());
            updateUser.setUpdateTime(System.currentTimeMillis());
            updateUser.setLockFlag(User.USER_LOCK);
            updateUser.setDelFlag(User.DEL_DEL);
            userService.updateMerchantUser(updateUser);
            
            userOauthBindService.deleteByUid(channelEmployee.getUid(), channelEmployee.getTenantId().intValue());
            
            result = userService.removeById(channelEmployee.getUid(), System.currentTimeMillis());
            
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    //清理缓存，避免缓存操作和数据库提交在同一个事务中失效的问题
                    redisService.delete(CacheConstant.CACHE_USER_UID + channelEmployee.getUid());
                    redisService.delete(CacheConstant.CACHE_USER_PHONE + user.getTenantId() + ":" + user.getPhone() + ":" + user.getUserType());
                }
            });
            //添加操作记录
            operateRecordUtil.record(null, MapUtil.of("channelName",user.getName()));
        }
        
        
        return result;
    }
    
    @Slave
    @Override
    public Integer existsByAreaId(Long id) {
        return channelEmployeeMapper.existsByAreaId(id);
    }
}

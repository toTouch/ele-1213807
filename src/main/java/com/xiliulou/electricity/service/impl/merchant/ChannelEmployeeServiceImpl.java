package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.i18n.MessageUtils;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserRole;
import com.xiliulou.electricity.entity.merchant.ChannelEmployee;
import com.xiliulou.electricity.entity.merchant.ChannelEmployeeAmount;
import com.xiliulou.electricity.entity.merchant.MerchantArea;
import com.xiliulou.electricity.entity.merchant.MerchantEmployee;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.merchant.ChannelEmployeeAmountMapper;
import com.xiliulou.electricity.mapper.merchant.ChannelEmployeeMapper;
import com.xiliulou.electricity.request.merchant.ChannelEmployeeRequest;
import com.xiliulou.electricity.request.merchant.MerchantPageRequest;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserRoleService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.ChannelEmployeeService;
import com.xiliulou.electricity.service.merchant.MerchantAreaService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.vo.merchant.ChannelEmployeeVO;
import com.xiliulou.security.authentication.console.CustomPasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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
    
    @Slave
    @Override
    public ChannelEmployeeVO queryById(Long id) {
        ChannelEmployee channelEmployee = channelEmployeeMapper.selectById(id);
        if (Objects.isNull(channelEmployee)) {
            return null;
        }
        ChannelEmployeeVO channelEmployeeVO = new ChannelEmployeeVO();
        BeanUtils.copyProperties(channelEmployee, channelEmployeeVO);
        if (Objects.nonNull(channelEmployeeVO.getUid())) {
            User user = userService.queryByUidFromCache(channelEmployeeVO.getUid());
            channelEmployeeVO.setName(user.getName());
            channelEmployeeVO.setPhone(user.getPhone());
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
        }
        return channelEmployeeVO;
    }
    
    @Slave
    @Override
    public List<ChannelEmployeeVO> listChannelEmployee(ChannelEmployeeRequest channelEmployeeRequest) {
        
        List<ChannelEmployeeVO> channelEmployees = channelEmployeeMapper.selectListByCondition(channelEmployeeRequest);
        
        channelEmployees.parallelStream().forEach(item -> {
            
            //设置加盟商名称
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            if(Objects.nonNull(franchisee)){
                item.setFranchiseeName(franchisee.getName());
            }
            
            //设置区域名称
            MerchantArea merchantArea = merchantAreaService.queryById(item.getAreaId());
            if(Objects.nonNull(merchantArea)){
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
    
        List<ChannelEmployeeVO> channelEmployeeVOList  = channelEmployeeMapper.selectChannelEmployees(channelEmployeeRequest);
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
    
        if (!redisService.setNx(CacheConstant.CACHE_CHANNEL_EMPLOYEE_SAVE_LOCK + channelEmployeeRequest.getPhone(), "1", 5000L, false)) {
            return Triple.of(false, null, "操作频繁,请稍后再试");
        }
        
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();
        
        //创建渠道员工账户
        User user = User.builder().updateTime(System.currentTimeMillis()).createTime(System.currentTimeMillis()).phone(channelEmployeeRequest.getPhone())
                .lockFlag(User.USER_UN_LOCK).gender(User.GENDER_MALE).lang(MessageUtils.LOCALE_ZH_CN).userType(User.TYPE_USER_CHANNEL).name(channelEmployeeRequest.getName()).salt("").avatar("")
                .tenantId(tenantId).loginPwd(customPasswordEncoder.encode("123456")).delFlag(User.DEL_NORMAL).build();
    
        // 如果是禁用则用户默认锁定
        if (Objects.equals(channelEmployeeRequest.getStatus(), MerchantConstant.DISABLE)) {
            user.setLockFlag(User.USER_LOCK);
        }
        
        User userResult = userService.insert(user);
       
        //TODO 设置角色, 渠道员工角色值待定
        Long roleId = 0L;
        
        UserRole userRole = new UserRole();
        userRole.setRoleId(roleId);
        userRole.setUid(userResult.getUid());
        //userRoleService.insert(userRole);
        
        //创建渠道员工账户
        ChannelEmployeeAmount channelEmployeeAmount = new ChannelEmployeeAmount();
        channelEmployeeAmount.setUid(userResult.getUid());
        channelEmployeeAmount.setTotalIncome(BigDecimal.ZERO);
        channelEmployeeAmount.setBalance(BigDecimal.ZERO);
        channelEmployeeAmount.setWithdrawAmount(BigDecimal.ZERO);
        channelEmployeeAmount.setTenantId(tenantId);
        channelEmployeeAmount.setCreateTime(System.currentTimeMillis());
        channelEmployeeAmount.setUpdateTime(System.currentTimeMillis());
        
        channelEmployeeAmountMapper.insertOne(channelEmployeeAmount);
        
        //创建渠道员工
        ChannelEmployee channelEmployee = new ChannelEmployee();
        channelEmployee.setUid(userResult.getUid());
        channelEmployee.setTenantId(tenantId);
        channelEmployee.setAreaId(channelEmployeeRequest.getAreaId());
        //channelEmployee.setStatus(channelEmployeeRequest.getStatus());
        channelEmployee.setDelFlag(CommonConstant.DEL_N);
        channelEmployee.setCreateTime(System.currentTimeMillis());
        channelEmployee.setUpdateTime(System.currentTimeMillis());
        
        Integer result = channelEmployeeMapper.insertOne(channelEmployee);
        
        return Triple.of(true, null, result);
    }
    
    @Override
    public Triple<Boolean, String, Object> updateChannelEmployee(ChannelEmployeeRequest channelEmployeeRequest) {
        
        ChannelEmployee channelEmployee = channelEmployeeMapper.selectById(channelEmployeeRequest.getId());
        if (Objects.isNull(channelEmployee)) {
            log.error("not found channel employee by id, id = {}", channelEmployeeRequest.getId());
            throw new BizException("120001", "当前渠道员工不存在");
        }
        
        User updateUser = new User();
    
        // 如果是禁用，则将用户置为锁定
        if (Objects.equals(channelEmployeeRequest.getStatus(), MerchantConstant.DISABLE)) {
            updateUser.setLockFlag(User.USER_LOCK);
        } else  {
            updateUser.setLockFlag(User.USER_UN_LOCK);
        }
        
        updateUser.setUid(channelEmployee.getUid());
        updateUser.setPhone(channelEmployeeRequest.getPhone());
        updateUser.setUserType(User.TYPE_USER_CHANNEL);
        updateUser.setTenantId(TenantContextHolder.getTenantId());
        updateUser.setUpdateTime(System.currentTimeMillis());
        userService.updateMerchantUser(updateUser);
    
        ChannelEmployee channelEmployeeUpdate = new ChannelEmployee();
        channelEmployeeUpdate.setId(channelEmployeeRequest.getId());
        channelEmployeeUpdate.setUid(channelEmployee.getUid());
        //channelEmployeeUpdate.setTenantId(tenantId);
        channelEmployeeUpdate.setAreaId(channelEmployeeRequest.getAreaId());
    
        channelEmployeeUpdate.setUpdateTime(System.currentTimeMillis());
    
        Integer result = channelEmployeeMapper.updateOne(channelEmployeeUpdate);
        
        return Triple.of(true, null, result);
    }
    
    @Override
    public Integer removeById(Long id) {
        ChannelEmployee channelEmployee = channelEmployeeMapper.selectById(id);
        
        if(Objects.isNull(channelEmployee)) {
            log.error("not found channel employee by id, id = {}", id);
            throw new BizException("120008", "渠道员工不存在");
        }
        User user = userService.queryByUidFromCache(channelEmployee.getUid());
    
        Integer result = 0;
        if(Objects.nonNull(user)){
            //userService.deleteInnerUser(merchantEmployee.getUid());
            result = userService.removeById(channelEmployee.getUid(), System.currentTimeMillis());
            redisService.delete(CacheConstant.CACHE_USER_UID + channelEmployee.getUid());
            redisService.delete(CacheConstant.CACHE_USER_PHONE + user.getTenantId() + ":" + user.getPhone() + ":" + user.getUserType());
        }
        return result;
    }
    
    @Slave
    @Override
    public Integer existsByAreaId(Long id) {
        return channelEmployeeMapper.existsByAreaId(id);
    }
}

package com.xiliulou.electricity.service.impl.merchant;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.i18n.MessageUtils;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.CommonConstant;
import com.xiliulou.electricity.constant.merchant.MerchantConstant;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.merchant.Merchant;
import com.xiliulou.electricity.entity.merchant.MerchantEmployee;
import com.xiliulou.electricity.entity.merchant.MerchantPlace;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.merchant.MerchantEmployeeMapper;
import com.xiliulou.electricity.query.merchant.MerchantPromotionEmployeeDetailQueryModel;
import com.xiliulou.electricity.request.merchant.MerchantEmployeeRequest;
import com.xiliulou.electricity.service.UserOauthBindService;
import com.xiliulou.electricity.service.UserRoleService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.merchant.MerchantEmployeeService;
import com.xiliulou.electricity.service.merchant.MerchantJoinRecordService;
import com.xiliulou.electricity.service.merchant.MerchantPlaceService;
import com.xiliulou.electricity.service.merchant.MerchantService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.QrCodeUtils;
import com.xiliulou.electricity.vo.merchant.MerchantEmployeeQrCodeVO;
import com.xiliulou.electricity.vo.merchant.MerchantEmployeeVO;
import com.xiliulou.security.authentication.console.CustomPasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author BaoYu
 * @description:
 * @date 2024/2/19 9:40
 */

@Slf4j
@Service("merchantEmployeeService")
public class MerchantEmployeeServiceImpl implements MerchantEmployeeService {
    
    @Resource
    private MerchantJoinRecordService merchantJoinRecordService;
    
    @Resource
    private RedisService redisService;
    
    @Resource
    private MerchantEmployeeMapper merchantEmployeeMapper;
    
    @Resource
    private UserService userService;
    
    @Resource
    private UserOauthBindService userOauthBindService;
    
    @Resource
    private CustomPasswordEncoder customPasswordEncoder;
    
    @Resource
    private MerchantService merchantService;
    
    @Resource
    private UserRoleService userRoleService;
    
    @Resource
    private MerchantPlaceService merchantPlaceService;
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer saveMerchantEmployee(MerchantEmployeeRequest merchantEmployeeRequest) {
        if (!redisService.setNx(CacheConstant.CACHE_MERCHANT_EMPLOYEE_SAVE_LOCK + merchantEmployeeRequest.getMerchantUid(), "1", 3000L, false)) {
            throw new BizException("000000", "操作频繁,请稍后再试");
        }
        
        // 检查当前手机号是否已经注册
        String name = merchantEmployeeRequest.getName();
        String phone = merchantEmployeeRequest.getPhone();
        
        //userService.queryByUserName(name);
        //检查用户名是否存在
        User existNameUser = userService.queryByUserName(name);
        if (Objects.nonNull(existNameUser)) {
            log.info("The user name has been used by other one for add merchant employee, name = {}, tenant id = {}", name, merchantEmployeeRequest.getTenantId());
            throw new BizException("120009", "用户姓名已存在");
        }
        
        User existUser = userService.queryByUserPhoneFromDB(phone, User.TYPE_USER_MERCHANT_EMPLOYEE, merchantEmployeeRequest.getTenantId());
        if (Objects.nonNull(existUser)) {
            throw new BizException("120001", "当前手机号已注册");
        }
        
        // 检查商户是否正常，状态为非禁用
        Merchant merchant = merchantService.queryByUid(merchantEmployeeRequest.getMerchantUid());
        if (Objects.isNull(merchant) || MerchantConstant.DISABLE.equals(merchant.getStatus())) {
            throw new BizException("120002", "当前商户不可用");
        }
        
        List<MerchantEmployeeVO> merchantEmployeeVOS = merchantEmployeeMapper.selectMerchantUsers(merchantEmployeeRequest);
        if (CollectionUtils.isNotEmpty(merchantEmployeeVOS) && merchantEmployeeVOS.size() >= MerchantConstant.MERCHANT_EMPLOYEE_MAX_SIZE) {
            throw new BizException("120024", "员工数量已达上限，请删除后再添加");
        }
        
        // 创建商户员工账号
        User user = User.builder().updateTime(System.currentTimeMillis()).createTime(System.currentTimeMillis()).phone(phone).lockFlag(User.USER_UN_LOCK).gender(User.GENDER_MALE)
                .lang(MessageUtils.LOCALE_ZH_CN).userType(User.TYPE_USER_MERCHANT_EMPLOYEE).name(name).salt("").avatar("").tenantId(merchantEmployeeRequest.getTenantId())
                .loginPwd(customPasswordEncoder.encode("123456")).delFlag(User.DEL_NORMAL).build();
        
        // 如果是禁用则用户默认锁定
        if (Objects.equals(merchantEmployeeRequest.getStatus(), MerchantConstant.DISABLE)) {
            user.setLockFlag(User.USER_LOCK);
        }
        
        User userResult = userService.insert(user);
        
        //TODO 设置角色, 商户员工角色值待定
       /* Long roleId = 0L;
        
        UserRole userRole = new UserRole();
        userRole.setRoleId(roleId);
        userRole.setUid(userResult.getUid());*/
        //userRoleService.insert(userRole);
        
        MerchantEmployee merchantEmployee = new MerchantEmployee();
        
        merchantEmployee.setUid(userResult.getUid());
        merchantEmployee.setMerchantUid(merchantEmployeeRequest.getMerchantUid());
        merchantEmployee.setPlaceId(merchantEmployeeRequest.getPlaceId());
        merchantEmployee.setTenantId(merchantEmployeeRequest.getTenantId().longValue());
        merchantEmployee.setDelFlag(CommonConstant.DEL_N);
        merchantEmployee.setCreateTime(System.currentTimeMillis());
        merchantEmployee.setUpdateTime(System.currentTimeMillis());
        
        Integer result = merchantEmployeeMapper.insertOne(merchantEmployee);
        
        return result;
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer updateMerchantEmployee(MerchantEmployeeRequest merchantEmployeeRequest) {
        if (!redisService.setNx(CacheConstant.CACHE_MERCHANT_EMPLOYEE_UPDATE_LOCK + merchantEmployeeRequest.getMerchantUid(), "1", 3000L, false)) {
            throw new BizException("000000", "操作频繁,请稍后再试");
        }
        
        //检查商户是否正常，状态为非禁用
        Merchant merchant = merchantService.queryByUid(merchantEmployeeRequest.getMerchantUid());
        if (Objects.isNull(merchant) || MerchantConstant.DISABLE.equals(merchant.getStatus())) {
            throw new BizException("120003", "当前商户不可用");
        }
        
        MerchantEmployeeVO merchantEmployeeVO = merchantEmployeeMapper.selectById(merchantEmployeeRequest.getId());
        if (Objects.isNull(merchantEmployeeVO)) {
            log.info("not found merchant employee by id, id = {}", merchantEmployeeRequest.getId());
            throw new BizException("120004", "商户员工不存在");
        }
        
        User user = userService.queryByUidFromCache(merchantEmployeeVO.getUid());
        if (Objects.isNull(user)) {
            log.info("not found merchant employee by uid, uid = {}", merchantEmployeeVO.getUid());
            throw new BizException("120004", "商户员工不存在");
        }
        
        //用户名需要限制，否则会导致后台编辑其他类型用户报错
        //userService.queryByUserName(name);
        if (!Objects.equals(user.getName(), merchantEmployeeRequest.getName())) {
            User existNameUser = userService.queryByUserName(merchantEmployeeRequest.getName());
            if (Objects.nonNull(existNameUser)) {
                log.info("The user name has been used by other one for update merchant employee, name = {}, tenant id = {}", merchantEmployeeRequest.getName(),
                        merchantEmployeeRequest.getTenantId());
                throw new BizException("120009", "用户姓名已存在");
            }
        }
        
        //检查当前手机号是否已经注册
        if (!Objects.equals(user.getPhone(), merchantEmployeeRequest.getPhone())) {
            User existUser = userService.queryByUserPhoneFromDB(merchantEmployeeRequest.getPhone(), User.TYPE_USER_MERCHANT_EMPLOYEE, merchantEmployeeRequest.getTenantId());
            if (Objects.nonNull(existUser)) {
                throw new BizException("120002", "当前手机号已注册");
            }
        }
        
        String oldPhone = user.getPhone();
        User updateUser = new User();
        
        // 如果是禁用，则将用户置为锁定
        if (Objects.equals(merchantEmployeeRequest.getStatus(), MerchantConstant.DISABLE)) {
            updateUser.setLockFlag(User.USER_LOCK);
        } else {
            updateUser.setLockFlag(User.USER_UN_LOCK);
        }
        
        updateUser.setUid(merchantEmployeeVO.getUid());
        updateUser.setName(merchantEmployeeRequest.getName());
        updateUser.setPhone(merchantEmployeeRequest.getPhone());
        updateUser.setUserType(User.TYPE_USER_MERCHANT_EMPLOYEE);
        updateUser.setTenantId(TenantContextHolder.getTenantId());
        updateUser.setUpdateTime(System.currentTimeMillis());
        userService.updateMerchantUser(updateUser);
        
        MerchantEmployee merchantEmployeeUpdate = new MerchantEmployee();
        BeanUtils.copyProperties(merchantEmployeeRequest, merchantEmployeeUpdate);
        merchantEmployeeUpdate.setUpdateTime(System.currentTimeMillis());
        
        Integer result = merchantEmployeeMapper.updateOne(merchantEmployeeUpdate);
        
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                //清理缓存，避免缓存操作和数据库提交在同一个事务中失效的问题
                redisService.delete(CacheConstant.CACHE_USER_UID + updateUser.getUid());
                redisService.delete(CacheConstant.CACHE_USER_PHONE + updateUser.getTenantId() + ":" + oldPhone + ":" + updateUser.getUserType());
            }
        });
        
        return result;
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer batchUnbindPlaceId(List<Long> employeeUidList) {
        Long updateTime = System.currentTimeMillis();
        
        return merchantEmployeeMapper.batchUnbindPlaceId(employeeUidList, updateTime);
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer removeMerchantEmployee(Long id) {
        MerchantEmployeeVO merchantEmployeeVO = merchantEmployeeMapper.selectById(id);
        if (Objects.isNull(merchantEmployeeVO)) {
            log.warn("not found merchant employee by id, id = {}", id);
            throw new BizException("120004", "商户员工不存在");
        }
        merchantEmployeeMapper.removeById(id, System.currentTimeMillis());
        
        User user = userService.queryByUidFromCache(merchantEmployeeVO.getUid());
        Integer result = 0;
        if (Objects.nonNull(user)) {
            User updateUser = new User();
            updateUser.setUid(user.getUid());
            updateUser.setUpdateTime(System.currentTimeMillis());
            updateUser.setLockFlag(User.USER_LOCK);
            userService.updateMerchantUser(updateUser);
            
            userService.removeById(user.getUid(), System.currentTimeMillis());
            
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    //清理缓存，避免缓存操作和数据库提交在同一个事务中失效的问题
                    redisService.delete(CacheConstant.CACHE_USER_UID + merchantEmployeeVO.getUid());
                    redisService.delete(CacheConstant.CACHE_USER_PHONE + user.getTenantId() + ":" + user.getPhone() + ":" + user.getUserType());
                }
            });
        }
        return result;
    }
    
    @Slave
    @Override
    public MerchantEmployeeVO queryMerchantEmployeeById(Long id) {
        MerchantEmployeeVO merchantEmployeeVO = merchantEmployeeMapper.selectById(id);
        
        return merchantEmployeeVO;
    }
    
    @Slave
    @Override
    public MerchantEmployeeVO queryMerchantEmployeeByUid(Long uid) {
        MerchantEmployeeVO merchantEmployeeVO = merchantEmployeeMapper.selectByUid(uid);
        
        return merchantEmployeeVO;
    }
    
    @Slave
    @Override
    public MerchantEmployeeQrCodeVO queryEmployeeQrCodeByUid(Long uid) {
        MerchantEmployeeQrCodeVO merchantEmployeeQrCodeVO = new MerchantEmployeeQrCodeVO();
        
        MerchantEmployeeVO merchantEmployeeVO = merchantEmployeeMapper.selectByUid(uid);
        Integer tenantId = TenantContextHolder.getTenantId();
        if (Objects.nonNull(merchantEmployeeVO)) {
            if (!Objects.equals(tenantId.longValue(), merchantEmployeeVO.getTenantId())) {
                log.info("tenant id mismatch for query employee QR code, current tenant id = {},  employee tenant id = {}", tenantId, merchantEmployeeVO.getTenantId());
                return null;
            }
            
            Merchant merchant = merchantService.queryByUid(merchantEmployeeVO.getMerchantUid());
            if (Objects.isNull(merchant)) {
                log.info("merchant is null for query employee QR code, merchant uid = {}", merchant.getUid());
                return null;
            }
            merchantEmployeeQrCodeVO.setUid(merchantEmployeeVO.getUid());
            merchantEmployeeQrCodeVO.setMerchantId(merchantEmployeeVO.getMerchantUid());
            merchantEmployeeQrCodeVO.setName(merchantEmployeeVO.getName());
            merchantEmployeeQrCodeVO.setPhone(merchantEmployeeVO.getPhone());
            merchantEmployeeQrCodeVO.setType(MerchantConstant.MERCHANT_EMPLOYEE_QR_CODE_TYPE);
            merchantEmployeeQrCodeVO.setStatus(merchantEmployeeVO.getStatus());
            String code = merchant.getId() + ":" + merchantEmployeeVO.getUid() + ":" + MerchantConstant.MERCHANT_EMPLOYEE_QR_CODE_TYPE;
            merchantEmployeeQrCodeVO.setCode(QrCodeUtils.codeEnCoder(code));
        }
        
        return merchantEmployeeQrCodeVO;
    }
    
    @Slave
    @Override
    public List<MerchantEmployeeVO> listMerchantEmployee(MerchantEmployeeRequest merchantEmployeeRequest) {
        
        List<MerchantEmployeeVO> merchantEmployeeVOList = merchantEmployeeMapper.selectListByCondition(merchantEmployeeRequest);
        
        merchantEmployeeVOList.forEach(merchantEmployeeVO -> {
            User user = userService.queryByUidFromCache(merchantEmployeeVO.getUid());
            if (Objects.nonNull(user)) {
                merchantEmployeeVO.setName(user.getName());
                merchantEmployeeVO.setPhone(user.getPhone());
                merchantEmployeeVO.setStatus(user.getLockFlag());
            }
            MerchantPlace merchantPlace = merchantPlaceService.queryByIdFromCache(merchantEmployeeVO.getPlaceId());
            if (Objects.nonNull(merchantPlace)) {
                merchantEmployeeVO.setPlaceName(merchantPlace.getName());
            }
            
        });
        
        return merchantEmployeeVOList;
    }
    
    @Slave
    @Override
    public Integer countMerchantEmployee(MerchantEmployeeRequest merchantEmployeeRequest) {
        return merchantEmployeeMapper.countByCondition(merchantEmployeeRequest);
    }
    
    @Slave
    @Override
    public List<MerchantEmployee> queryListByPlaceId(List<Long> placeIdList) {
        return merchantEmployeeMapper.selectListByPlaceId(placeIdList);
    }
    
    /**
     * 查询当前或者历史中存在的场地员工
     * @param queryModel
     * @return
     */
    @Slave
    @Override
    public List<MerchantEmployee> selectByMerchantUid(MerchantPromotionEmployeeDetailQueryModel queryModel) {
        return merchantEmployeeMapper.selectListAllByMerchantUid(queryModel);
    }
    
    @Slave
    @Override
    public List<MerchantEmployeeQrCodeVO> selectMerchantEmployeeQrCodes(MerchantEmployeeRequest merchantEmployeeRequest) {
        Merchant merchant = merchantService.queryByUid(merchantEmployeeRequest.getMerchantUid());
        if (Objects.isNull(merchant)) {
            log.warn("not found merchant by uid, uid = {}", merchantEmployeeRequest.getMerchantUid());
            return Collections.EMPTY_LIST;
        }
        
        List<MerchantEmployeeQrCodeVO> merchantEmployeeQrCodeVOList = new ArrayList<>();
        List<MerchantEmployeeVO> merchantEmployeeVOS = merchantEmployeeMapper.selectMerchantUsers(merchantEmployeeRequest);
        merchantEmployeeVOS.forEach(merchantEmployeeVO -> {
            MerchantEmployeeQrCodeVO merchantEmployeeQrCodeVO = new MerchantEmployeeQrCodeVO();
            
            merchantEmployeeQrCodeVO.setMerchantId(merchant.getId());
            merchantEmployeeQrCodeVO.setUid(merchantEmployeeVO.getUid());
            merchantEmployeeQrCodeVO.setName(merchantEmployeeVO.getName());
            merchantEmployeeQrCodeVO.setPhone(merchantEmployeeVO.getPhone());
            merchantEmployeeQrCodeVO.setType(MerchantConstant.MERCHANT_EMPLOYEE_QR_CODE_TYPE);
            merchantEmployeeQrCodeVO.setStatus(merchantEmployeeVO.getStatus());
            String code = merchant.getId() + ":" + merchantEmployeeVO.getUid() + ":" + MerchantConstant.MERCHANT_EMPLOYEE_QR_CODE_TYPE;
            merchantEmployeeQrCodeVO.setCode(QrCodeUtils.codeEnCoder(code));
            
            merchantEmployeeQrCodeVOList.add(merchantEmployeeQrCodeVO);
        });
        
        return merchantEmployeeQrCodeVOList;
    }
    
    @Slave
    @Override
    public List<MerchantEmployeeVO> selectAllMerchantEmployees(MerchantEmployeeRequest merchantEmployeeRequest) {
        
        Merchant merchant = merchantService.queryByUid(merchantEmployeeRequest.getMerchantUid());
        if (Objects.isNull(merchant)) {
            log.info("not found merchant by uid, uid = {}", merchantEmployeeRequest.getMerchantUid());
            return Collections.EMPTY_LIST;
        }
        
        List<MerchantEmployeeVO> merchantEmployeeVOS = merchantEmployeeMapper.selectMerchantUsers(merchantEmployeeRequest);
        
        return merchantEmployeeVOS;
    }
    
    /**
     * 根据uid批量删除场地员工
     * @param uidList
     * @param updateTime
     * @return
     */
    @Override
    public Integer batchRemoveByUidList(List<Long> uidList, Long updateTime) {
        return merchantEmployeeMapper.batchRemoveByUidList(uidList, updateTime);
    }
    
    @Slave
    @Override
    public List<MerchantEmployee> queryListByMerchantUid(Long merchantUid, Integer tenantId) {
        MerchantPromotionEmployeeDetailQueryModel queryModel = MerchantPromotionEmployeeDetailQueryModel.builder().tenantId(tenantId).uid(merchantUid).build();
        return merchantEmployeeMapper.selectListByMerchantUid(queryModel);
    }
    
}

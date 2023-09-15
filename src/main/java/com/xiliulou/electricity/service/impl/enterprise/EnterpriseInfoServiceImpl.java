package com.xiliulou.electricity.service.impl.enterprise;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.entity.BatteryMemberCard;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanOrder;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.entity.enterprise.EnterprisePackage;
import com.xiliulou.electricity.enums.BusinessType;
import com.xiliulou.electricity.mapper.EnterpriseInfoMapper;
import com.xiliulou.electricity.query.enterprise.EnterpriseCloudBeanRechargeQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseInfoQuery;
import com.xiliulou.electricity.service.BatteryMemberCardService;
import com.xiliulou.electricity.service.EnterpriseCloudBeanOrderService;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.service.enterprise.EnterprisePackageService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.DbUtils;
import com.xiliulou.electricity.utils.OrderIdUtil;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.enterprise.EnterpriseInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 企业用户信息表(EnterpriseInfo)表服务实现类
 *
 * @author zzlong
 * @since 2023-09-14 10:15:08
 */
@Service("enterpriseInfoService")
@Slf4j
public class EnterpriseInfoServiceImpl implements EnterpriseInfoService {
    @Resource
    private EnterpriseInfoMapper enterpriseInfoMapper;

    @Autowired
    private FranchiseeService franchiseeService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private EnterprisePackageService enterprisePackageService;

    @Autowired
    private BatteryMemberCardService batteryMemberCardService;

    @Autowired
    private EnterpriseCloudBeanOrderService enterpriseCloudBeanOrderService;

    @Autowired
    private RedisService redisService;


    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EnterpriseInfo queryByIdFromDB(Long id) {
        return this.enterpriseInfoMapper.queryById(id);
    }

    @Override
    public EnterpriseInfo queryByIdFromCache(Long id) {

        EnterpriseInfo cacheEnterpriseInfo = redisService.getWithHash(CacheConstant.CACHE_ENTERPRISE_INFO + id, EnterpriseInfo.class);
        if (Objects.nonNull(cacheEnterpriseInfo)) {
            return cacheEnterpriseInfo;
        }

        EnterpriseInfo enterpriseInfo = this.queryByIdFromDB(id);
        if (Objects.isNull(enterpriseInfo)) {
            return null;
        }

        redisService.saveWithHash(CacheConstant.CACHE_ENTERPRISE_INFO + id, enterpriseInfo);

        return enterpriseInfo;
    }

    /**
     * 修改数据
     *
     * @param enterpriseInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EnterpriseInfo enterpriseInfo) {
        int update = this.enterpriseInfoMapper.update(enterpriseInfo);

        DbUtils.dbOperateSuccessThen(update, () -> {
            redisService.delete(CacheConstant.CACHE_ENTERPRISE_INFO + enterpriseInfo.getId());
            return null;
        });

        return update;
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteById(Long id) {
        int delete = this.enterpriseInfoMapper.deleteById(id);

        DbUtils.dbOperateSuccessThen(delete, () -> {
            redisService.delete(CacheConstant.CACHE_ENTERPRISE_INFO + id);
            return null;
        });

        return delete;
    }

    @Slave
    @Override
    public List<EnterpriseInfoVO> selectByPage(EnterpriseInfoQuery query) {
        List<EnterpriseInfo> list = this.enterpriseInfoMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }

        return list.stream().map(item -> {
            EnterpriseInfoVO enterpriseInfoVO = new EnterpriseInfoVO();
            BeanUtils.copyProperties(item, enterpriseInfoVO);

            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            enterpriseInfoVO.setFranchiseeName(Objects.isNull(franchisee) ? "" : franchisee.getName());

            UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
            enterpriseInfoVO.setUsername(Objects.isNull(userInfo) ? "" : userInfo.getName());

            enterpriseInfoVO.setMemcardName(getMembercardNames(item.getId()));

            return enterpriseInfoVO;
        }).collect(Collectors.toList());
    }

    @Slave
    @Override
    public Integer selectByPageCount(EnterpriseInfoQuery query) {
        return this.enterpriseInfoMapper.selectByPageCount(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> save(EnterpriseInfoQuery enterpriseInfoQuery) {
        if (CollectionUtils.isEmpty(enterpriseInfoQuery.getPackageIds())) {
            return Triple.of(false, "", "参数不合法");
        }

        EnterpriseInfo enterpriseInfo = new EnterpriseInfo();
        BeanUtils.copyProperties(enterpriseInfoQuery, enterpriseInfo);
        enterpriseInfo.setBusinessId(Long.valueOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + RandomUtil.randomInt(1000, 9999)));
        enterpriseInfo.setRecoveryMode(EnterpriseInfo.RECOVERY_MODE_RETURN);
        enterpriseInfo.setTenantId(TenantContextHolder.getTenantId());
        enterpriseInfo.setCreateTime(System.currentTimeMillis());
        enterpriseInfo.setUpdateTime(System.currentTimeMillis());
        this.enterpriseInfoMapper.insert(enterpriseInfo);

        List<EnterprisePackage> packageList = enterpriseInfoQuery.getPackageIds().stream().map(item -> {
            EnterprisePackage enterprisePackage = new EnterprisePackage();
            enterprisePackage.setEnterpriseId(enterpriseInfo.getId());
            enterprisePackage.setPackageId(item);
            enterprisePackage.setPackageType(enterpriseInfoQuery.getPackageType());
            enterprisePackage.setTenantId(enterpriseInfo.getTenantId());
            enterprisePackage.setCreateTime(System.currentTimeMillis());
            enterprisePackage.setUpdateTime(System.currentTimeMillis());
            return enterprisePackage;
        }).collect(Collectors.toList());

        enterprisePackageService.batchInsert(packageList);

        return Triple.of(true, null, null);
    }

    @Override
    public Triple<Boolean, String, Object> modify(EnterpriseInfoQuery enterpriseInfoQuery) {
        EnterpriseInfo enterpriseInfo = this.queryByIdFromDB(enterpriseInfoQuery.getId());
        if (Objects.isNull(enterpriseInfo)) {
            return Triple.of(false, "", "企业配置不存在");
        }

        EnterpriseInfo enterpriseInfoUpdate = new EnterpriseInfo();
        BeanUtils.copyProperties(enterpriseInfoQuery, enterpriseInfoUpdate);
        enterpriseInfoUpdate.setId(enterpriseInfo.getId());
        enterpriseInfoUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(enterpriseInfoUpdate);

        return Triple.of(true, null, null);
    }

    @Override
    public Triple<Boolean, String, Object> delete(Long id) {
        this.deleteById(id);
        return Triple.of(true, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> recharge(EnterpriseCloudBeanRechargeQuery enterpriseCloudBeanRechargeQuery) {
        EnterpriseInfo enterpriseInfo = this.queryByIdFromCache(enterpriseCloudBeanRechargeQuery.getId());
        if (Objects.isNull(enterpriseInfo)) {
            return Triple.of(false, "", "企业配置不存在");
        }

        EnterpriseInfo enterpriseInfoUpdate = new EnterpriseInfo();
        enterpriseInfoUpdate.setId(enterpriseInfo.getId());
        enterpriseInfoUpdate.setTotalBeanAmount(enterpriseInfo.getTotalBeanAmount() + enterpriseCloudBeanRechargeQuery.getTotalBeanAmount());
        enterpriseInfoUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(enterpriseInfoUpdate);

        EnterpriseCloudBeanOrder enterpriseCloudBeanOrder = new EnterpriseCloudBeanOrder();
        enterpriseCloudBeanOrder.setEnterpriseId(enterpriseInfo.getId());
        enterpriseCloudBeanOrder.setUid(enterpriseInfo.getUid());
        enterpriseCloudBeanOrder.setOperateUid(SecurityUtils.getUid());
        enterpriseCloudBeanOrder.setPayAmount(Objects.isNull(enterpriseCloudBeanRechargeQuery.getTotalBeanAmount()) ? BigDecimal.ZERO : BigDecimal.valueOf(enterpriseCloudBeanRechargeQuery.getTotalBeanAmount()));
        enterpriseCloudBeanOrder.setOrderId(OrderIdUtil.generateBusinessOrderId(BusinessType.CLOUD_BEAN, enterpriseInfo.getUid()));
        enterpriseCloudBeanOrder.setStatus(EnterpriseCloudBeanOrder.STATUS_SUCCESS);
        enterpriseCloudBeanOrder.setPayType(EnterpriseCloudBeanOrder.OFFLINE_PAYMENT);
        enterpriseCloudBeanOrder.setType(enterpriseCloudBeanRechargeQuery.getType());
        enterpriseCloudBeanOrder.setBeanAmount(Objects.isNull(enterpriseCloudBeanRechargeQuery.getTotalBeanAmount()) ? BigDecimal.ZERO : BigDecimal.valueOf(enterpriseCloudBeanRechargeQuery.getTotalBeanAmount()));
        enterpriseCloudBeanOrder.setFranchiseeId(enterpriseInfo.getFranchiseeId());
        enterpriseCloudBeanOrder.setTenantId(enterpriseInfo.getTenantId());
        enterpriseCloudBeanOrder.setCreateTime(System.currentTimeMillis());
        enterpriseCloudBeanOrder.setUpdateTime(System.currentTimeMillis());
        enterpriseCloudBeanOrder.setRemark(enterpriseCloudBeanRechargeQuery.getRemark());
        enterpriseCloudBeanOrderService.insert(enterpriseCloudBeanOrder);

        return Triple.of(true, null, null);
    }

    @Override
    public EnterpriseInfo selectByUid(Long uid) {
        return this.enterpriseInfoMapper.selectOne(new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUid, uid));
    }

    @Override
    public Boolean checkUserType() {
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("ENTERPRISE ERROR! not found user info,uid={} ", SecurityUtils.getUid());
            return Boolean.FALSE;
        }

        EnterpriseInfo enterpriseInfo = this.selectByUid(userInfo.getUid());
        if (Objects.isNull(enterpriseInfo)) {
            log.error("ENTERPRISE ERROR! not found enterpriseInfo,uid={} ", SecurityUtils.getUid());
            return Boolean.FALSE;
        }

        if (Objects.equals(EnterpriseInfo.STATUS_OPEN, enterpriseInfo.getStatus())) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    private List<String> getMembercardNames(Long id) {

        List<String> list = Lists.newArrayList();

        List<Long> membercardIds = enterprisePackageService.selectByEnterpriseId(id);
        if (CollectionUtils.isEmpty(membercardIds)) {
            return list;
        }

        membercardIds.forEach(e -> {
            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(e);
            if (Objects.nonNull(batteryMemberCard)) {
                list.add(batteryMemberCard.getName());
            }
        });

        return list;
    }
}

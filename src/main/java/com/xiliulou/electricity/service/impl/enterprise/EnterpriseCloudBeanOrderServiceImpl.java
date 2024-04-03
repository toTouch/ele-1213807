package com.xiliulou.electricity.service.impl.enterprise;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.NumberConstant;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanOrder;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.mapper.enterprise.EnterpriseCloudBeanOrderMapper;
import com.xiliulou.electricity.query.enterprise.EnterpriseCloudBeanOrderQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserDataScopeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.asset.AssertPermissionService;
import com.xiliulou.electricity.service.enterprise.EnterpriseCloudBeanOrderService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.enterprise.EnterpriseCloudBeanOrderVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 企业云豆充值订单表(EnterpriseCloudBeanOrder)表服务实现类
 *
 * @author zzlong
 * @since 2023-09-15 09:29:15
 */
@Service("enterpriseCloudBeanOrderService")
@Slf4j
public class EnterpriseCloudBeanOrderServiceImpl implements EnterpriseCloudBeanOrderService {
    
    @Resource
    private EnterpriseCloudBeanOrderMapper enterpriseCloudBeanOrderMapper;
    
    @Autowired
    private FranchiseeService franchiseeService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private EnterpriseInfoService enterpriseInfoService;
    
    @Autowired
    private AssertPermissionService assertPermissionService;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EnterpriseCloudBeanOrder queryByIdFromDB(Long id) {
        return this.enterpriseCloudBeanOrderMapper.queryById(id);
    }
    
    @Override
    public Integer insert(EnterpriseCloudBeanOrder enterpriseCloudBeanOrder) {
        return this.enterpriseCloudBeanOrderMapper.insert(enterpriseCloudBeanOrder);
    }
    
    /**
     * 修改数据
     *
     * @param enterpriseCloudBeanOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EnterpriseCloudBeanOrder enterpriseCloudBeanOrder) {
        return this.enterpriseCloudBeanOrderMapper.update(enterpriseCloudBeanOrder);
        
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
        return this.enterpriseCloudBeanOrderMapper.deleteById(id) > 0;
    }
    
    @Slave
    @Override
    public List<EnterpriseCloudBeanOrderVO> selectByPage(EnterpriseCloudBeanOrderQuery query) {
        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
        if (!pair.getLeft()){
            return new ArrayList<>();
        }
        query.setFranchiseeIds(pair.getRight());
        
        List<EnterpriseCloudBeanOrder> list = this.enterpriseCloudBeanOrderMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        
        return list.stream().map(item -> {
            EnterpriseCloudBeanOrderVO enterpriseCloudBeanOrderVO = new EnterpriseCloudBeanOrderVO();
            BeanUtils.copyProperties(item, enterpriseCloudBeanOrderVO);
            
            EnterpriseInfo enterpriseInfo = enterpriseInfoService.queryByIdFromCache(item.getEnterpriseId());
            enterpriseCloudBeanOrderVO.setEnterpriseName(Objects.isNull(enterpriseInfo) ? "" : enterpriseInfo.getName());
            enterpriseCloudBeanOrderVO.setBusinessId(Objects.isNull(enterpriseInfo) ? null : enterpriseInfo.getBusinessId());
            
            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            enterpriseCloudBeanOrderVO.setFranchiseeName(Objects.isNull(franchisee) ? "" : franchisee.getName());
            
            User user = userService.queryByUidFromCache(item.getOperateUid());
            enterpriseCloudBeanOrderVO.setOperateName(Objects.isNull(user) ? "" : user.getName());
            
            UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
            enterpriseCloudBeanOrderVO.setUsername(Objects.isNull(userInfo) ? "" : userInfo.getName());
            
            return enterpriseCloudBeanOrderVO;
        }).collect(Collectors.toList());
    }
    
    @Slave
    @Override
    public Integer selectByPageCount(EnterpriseCloudBeanOrderQuery query) {
        Pair<Boolean, List<Long>> pair = assertPermissionService.assertPermissionByPair(SecurityUtils.getUserInfo());
        if (!pair.getLeft()){
            return NumberConstant.ZERO;
        }
        query.setFranchiseeIds(pair.getRight());
        return this.enterpriseCloudBeanOrderMapper.selectByPageCount(query);
    }
    
    
    @Slave
    @Override
    public BigDecimal selectTotalCloudBean(EnterpriseCloudBeanOrderQuery query) {
        BigDecimal result = BigDecimal.ZERO;
    
        List<EnterpriseCloudBeanOrder> list = this.enterpriseCloudBeanOrderMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }
    
        for (EnterpriseCloudBeanOrder enterpriseCloudBeanOrder : list) {
            if (Objects.equals(enterpriseCloudBeanOrder.getType(), EnterpriseCloudBeanOrder.TYPE_ADMIN_RECHARGE) || Objects
                    .equals(enterpriseCloudBeanOrder.getType(), EnterpriseCloudBeanOrder.TYPE_PRESENT) || Objects
                    .equals(enterpriseCloudBeanOrder.getType(), EnterpriseCloudBeanOrder.TYPE_USER_RECHARGE)) {
                result = result.add(enterpriseCloudBeanOrder.getBeanAmount());
            } else if (Objects.equals(enterpriseCloudBeanOrder.getType(), EnterpriseCloudBeanOrder.TYPE_ADMIN_DEDUCT)) {
                result = result.subtract(enterpriseCloudBeanOrder.getBeanAmount());
            }
        }
    
        return result;
    }
    
    @Override
    public EnterpriseCloudBeanOrder selectByOrderId(String orderNo) {
        return this.enterpriseCloudBeanOrderMapper.selectOne(new LambdaQueryWrapper<EnterpriseCloudBeanOrder>().eq(EnterpriseCloudBeanOrder::getOrderId, orderNo));
    }
}

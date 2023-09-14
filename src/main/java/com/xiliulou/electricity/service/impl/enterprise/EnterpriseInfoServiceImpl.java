package com.xiliulou.electricity.service.impl.enterprise;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanRecord;
import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.mapper.EnterpriseInfoMapper;
import com.xiliulou.electricity.query.enterprise.EnterpriseCloudBeanRechargeQuery;
import com.xiliulou.electricity.query.enterprise.EnterpriseInfoQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserInfoService;
import com.xiliulou.electricity.service.enterprise.EnterpriseCloudBeanRecordService;
import com.xiliulou.electricity.service.enterprise.EnterpriseInfoService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
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
    private EnterpriseCloudBeanRecordService enterpriseCloudBeanRecordService;

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

    /**
     * 修改数据
     *
     * @param enterpriseInfo 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EnterpriseInfo enterpriseInfo) {
        return this.enterpriseInfoMapper.update(enterpriseInfo);
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
        return this.enterpriseInfoMapper.deleteById(id) > 0;
    }

    @Slave
    @Override
    public List<EnterpriseInfoVO> selectByPage(EnterpriseInfoQuery query) {
        List<EnterpriseInfo> list= this.enterpriseInfoMapper.selectByPage(query);
        if(CollectionUtils.isEmpty(list)){
            return Collections.EMPTY_LIST;
        }

        return list.stream().map(item->{
            EnterpriseInfoVO enterpriseInfoVO = new EnterpriseInfoVO();
            BeanUtils.copyProperties(item,enterpriseInfoVO);

            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            enterpriseInfoVO.setFranchiseeName(Objects.isNull(franchisee)?"":franchisee.getName());

            UserInfo userInfo = userInfoService.queryByUidFromCache(item.getUid());
            enterpriseInfoVO.setUsername(Objects.isNull(userInfo)?"":userInfo.getName());

            return enterpriseInfoVO;
        }).collect(Collectors.toList());
    }

    @Slave
    @Override
    public Integer selectByPageCount(EnterpriseInfoQuery query) {
        return this.enterpriseInfoMapper.selectByPageCount(query);
    }

    @Override
    public Triple<Boolean, String, Object> save(EnterpriseInfoQuery enterpriseInfoQuery) {
        //TODO


        EnterpriseInfo enterpriseInfo = new EnterpriseInfo();
        BeanUtils.copyProperties(enterpriseInfoQuery,enterpriseInfo);
        enterpriseInfo.setTenantId(TenantContextHolder.getTenantId());
        enterpriseInfo.setCreateTime(System.currentTimeMillis());
        enterpriseInfo.setUpdateTime(System.currentTimeMillis());
        this.enterpriseInfoMapper.insert(enterpriseInfo);

        return Triple.of(true,null,null);
    }

    @Override
    public Triple<Boolean, String, Object> modify(EnterpriseInfoQuery enterpriseInfoQuery) {
        EnterpriseInfo enterpriseInfo = this.queryByIdFromDB(enterpriseInfoQuery.getId());
        if(Objects.isNull(enterpriseInfo)){
            return Triple.of(false,"","企业配置不存在");
        }

        EnterpriseInfo enterpriseInfoUpdate = new EnterpriseInfo();
        enterpriseInfoUpdate.setId(enterpriseInfo.getId());
        enterpriseInfoUpdate.setUpdateTime(System.currentTimeMillis());

        //TODO



        return Triple.of(true,null,null);
    }

    @Override
    public Triple<Boolean, String, Object> delete(Long id) {
        //TODO



        this.deleteById(id);
        return Triple.of(true,null,null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> recharge(EnterpriseCloudBeanRechargeQuery enterpriseCloudBeanRechargeQuery) {
        EnterpriseInfo enterpriseInfo = this.queryByIdFromDB(enterpriseCloudBeanRechargeQuery.getId());
        if(Objects.isNull(enterpriseInfo)){
            return Triple.of(false,"","企业配置不存在");
        }

        EnterpriseInfo enterpriseInfoUpdate =new EnterpriseInfo();
        enterpriseInfoUpdate.setId(enterpriseInfo.getId());
        enterpriseInfoUpdate.setTotalBeanAmount(enterpriseInfo.getTotalBeanAmount()+enterpriseCloudBeanRechargeQuery.getTotalBeanAmount());
        enterpriseInfoUpdate.setUpdateTime(System.currentTimeMillis());
        this.update(enterpriseInfoUpdate);

        EnterpriseCloudBeanRecord enterpriseCloudBeanRecord = new EnterpriseCloudBeanRecord();
        enterpriseCloudBeanRecord.setUid(SecurityUtils.getUid());
        enterpriseCloudBeanRecord.setEnterpriseId(enterpriseInfo.getId());
        enterpriseCloudBeanRecord.setType(enterpriseCloudBeanRechargeQuery.getType());
        enterpriseCloudBeanRecord.setBeanAmount(enterpriseCloudBeanRechargeQuery.getTotalBeanAmount());
        enterpriseCloudBeanRecord.setFranchiseeId(enterpriseInfo.getFranchiseeId());
        enterpriseCloudBeanRecord.setTenantId(TenantContextHolder.getTenantId());
        enterpriseCloudBeanRecord.setRemark(enterpriseCloudBeanRechargeQuery.getRemark());
        enterpriseCloudBeanRecord.setCreateTime(System.currentTimeMillis());
        enterpriseCloudBeanRecord.setUpdateTime(System.currentTimeMillis());
        enterpriseCloudBeanRecordService.insert(enterpriseCloudBeanRecord);
        return Triple.of(true,null,null);
    }
}

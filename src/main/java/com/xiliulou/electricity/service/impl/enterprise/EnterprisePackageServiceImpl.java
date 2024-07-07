package com.xiliulou.electricity.service.impl.enterprise;

import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.bo.merchant.MerchantEnterprisePackageBO;
import com.xiliulou.electricity.entity.enterprise.EnterprisePackage;
import com.xiliulou.electricity.mapper.enterprise.EnterprisePackageMapper;
import com.xiliulou.electricity.service.enterprise.EnterprisePackageService;
import com.xiliulou.electricity.vo.enterprise.EnterprisePackageVO;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 企业关联套餐表(EnterprisePackage)表服务实现类
 *
 * @author zzlong
 * @since 2023-09-14 10:15:43
 */
@Service("enterprisePackageService")
@Slf4j
public class EnterprisePackageServiceImpl implements EnterprisePackageService {
    @Resource
    private EnterprisePackageMapper enterprisePackageMapper;
    
    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EnterprisePackage queryByIdFromDB(Long id) {
        return this.enterprisePackageMapper.queryById(id);
    }
    
    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EnterprisePackage queryByIdFromCache(Long id) {
        return null;
    }
    
    /**
     * 修改数据
     *
     * @param enterprisePackage 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EnterprisePackage enterprisePackage) {
        return this.enterprisePackageMapper.update(enterprisePackage);
        
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
        return this.enterprisePackageMapper.deleteById(id) > 0;
    }
    
    @Override
    public void batchInsert(List<EnterprisePackage> packageList) {
        if(CollectionUtils.isEmpty(packageList)){
            return;
        }
        
        this.enterprisePackageMapper.batchInsert(packageList);
    }
    
    @Override
    public List<Long> selectByEnterpriseId(Long id) {
        return this.enterprisePackageMapper.selectByEnterpriseId(id);
    }
    
    @Override
    public Integer selectByPackageId(Long packageId) {
        return this.enterprisePackageMapper.selectByPackageId(packageId);
    }
    
    @Override
    public int deleteByEnterpriseId(Long id) {
        return this.enterprisePackageMapper.deleteByEnterpriseId(id);
    }
    
    @Slave
    @Override
    public List<EnterprisePackageVO> queryListByEnterpriseId(Long id) {
        List<MerchantEnterprisePackageBO> enterprisePackageBOS = enterprisePackageMapper.selectListByEnterpriseId(id);
        if (ObjectUtils.isEmpty(enterprisePackageBOS)) {
            return Collections.emptyList();
        }
        
        List<EnterprisePackageVO> list = new ArrayList<>();
        enterprisePackageBOS.stream().forEach(enterprisePackage -> {
            EnterprisePackageVO vo = new EnterprisePackageVO();
            BeanUtils.copyProperties(enterprisePackage, vo);
            list.add(vo);
        });
        
        return list;
    }
}

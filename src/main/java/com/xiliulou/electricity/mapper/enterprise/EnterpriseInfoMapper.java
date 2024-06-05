package com.xiliulou.electricity.mapper.enterprise;

import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiliulou.electricity.query.enterprise.EnterpriseInfoQuery;
import com.xiliulou.electricity.vo.enterprise.EnterpriseInfoPackageVO;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 企业用户信息表(EnterpriseInfo)表数据库访问层
 *
 * @author zzlong
 * @since 2023-09-14 10:15:08
 */
public interface EnterpriseInfoMapper extends BaseMapper<EnterpriseInfo> {
    
    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterpriseInfo queryById(Long id);
    
    /**
     * 修改数据
     *
     * @param enterpriseInfo 实例对象
     * @return 影响行数
     */
    int update(EnterpriseInfo enterpriseInfo);
    
    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Long id);
    
    List<EnterpriseInfoPackageVO> selectByPage(EnterpriseInfoQuery query);
    
    Integer selectByPageCount(EnterpriseInfoQuery query);
    
    EnterpriseInfo selectByUid(Long uid);
    
    int addCloudBean(@Param("id") Long id, @Param("totalCloudBean") BigDecimal totalCloudBean);
    
    Integer updatePhoneByUid(@Param("tenantId") Integer tenantId, @Param("uid") Long uid, @Param("newPhone") String newPhone);
    
    List<EnterpriseInfo> queryListByIdList(@Param("idList") List<Long> enterpriseIdList);
    
    int subtractCloudBean(@Param("id") Long id, @Param("subtractCloudBean") BigDecimal subtract,@Param("updateTime") long updateTime);
    
    List<EnterpriseInfo> selectList(@Param("tenantId") Integer tenantId);
}

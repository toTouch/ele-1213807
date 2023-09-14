package com.xiliulou.electricity.service.enterprise;

import com.xiliulou.electricity.entity.enterprise.EnterpriseInfo;
import com.xiliulou.electricity.query.enterprise.EnterpriseInfoQuery;
import com.xiliulou.electricity.vo.enterprise.EnterpriseInfoVO;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * 企业用户信息表(EnterpriseInfo)表服务接口
 *
 * @author zzlong
 * @since 2023-09-14 10:15:08
 */
public interface EnterpriseInfoService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    EnterpriseInfo queryByIdFromDB(Long id);

    /**
     * 修改数据
     *
     * @param enterpriseInfo 实例对象
     * @return 实例对象
     */
    Integer update(EnterpriseInfo enterpriseInfo);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    List<EnterpriseInfoVO> selectByPage(EnterpriseInfoQuery query);

    Integer selectByPageCount(EnterpriseInfoQuery query);

    Triple<Boolean, String, Object> delete(Long id);

    Triple<Boolean, String, Object> modify(EnterpriseInfoQuery enterpriseInfoQuery);

    Triple<Boolean, String, Object> save(EnterpriseInfoQuery enterpriseInfoQuery);
}

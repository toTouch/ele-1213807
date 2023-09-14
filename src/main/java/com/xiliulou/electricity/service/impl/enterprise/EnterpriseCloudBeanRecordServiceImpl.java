package com.xiliulou.electricity.service.impl.enterprise;

import com.xiliulou.electricity.entity.Franchisee;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.UserInfo;
import com.xiliulou.electricity.entity.enterprise.EnterpriseCloudBeanRecord;
import com.xiliulou.electricity.mapper.EnterpriseCloudBeanRecordMapper;
import com.xiliulou.electricity.query.enterprise.EnterpriseCloudBeanRecordQuery;
import com.xiliulou.electricity.service.FranchiseeService;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.enterprise.EnterpriseCloudBeanRecordService;
import com.xiliulou.electricity.vo.enterprise.EnterpriseCloudBeanRecordVO;
import lombok.extern.slf4j.Slf4j;
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
 * 企业云豆操作记录表(EnterpriseCloudBeanRecord)表服务实现类
 *
 * @author zzlong
 * @since 2023-09-14 10:16:20
 */
@Service("enterpriseCloudBeanRecordService")
@Slf4j
public class EnterpriseCloudBeanRecordServiceImpl implements EnterpriseCloudBeanRecordService {
    @Resource
    private EnterpriseCloudBeanRecordMapper enterpriseCloudBeanRecordMapper;

    @Autowired
    private FranchiseeService franchiseeService;

    @Autowired
    private UserService userService;

    /**
     * 通过ID查询单条数据从DB
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EnterpriseCloudBeanRecord queryByIdFromDB(Long id) {
        return this.enterpriseCloudBeanRecordMapper.queryById(id);
    }

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public EnterpriseCloudBeanRecord queryByIdFromCache(Long id) {
        return null;
    }

    /**
     * 修改数据
     *
     * @param enterpriseCloudBeanRecord 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(EnterpriseCloudBeanRecord enterpriseCloudBeanRecord) {
        return this.enterpriseCloudBeanRecordMapper.update(enterpriseCloudBeanRecord);

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
        return this.enterpriseCloudBeanRecordMapper.deleteById(id) > 0;
    }

    @Override
    public Integer insert(EnterpriseCloudBeanRecord enterpriseCloudBeanRecord) {
        return this.enterpriseCloudBeanRecordMapper.insert(enterpriseCloudBeanRecord);
    }

    @Override
    public List<EnterpriseCloudBeanRecordVO> selectByPage(EnterpriseCloudBeanRecordQuery query) {
        List<EnterpriseCloudBeanRecord> list = this.enterpriseCloudBeanRecordMapper.selectByPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }

        return list.stream().map(item->{
            EnterpriseCloudBeanRecordVO enterpriseCloudBeanRecordVO = new EnterpriseCloudBeanRecordVO();
            BeanUtils.copyProperties(item,enterpriseCloudBeanRecordVO);

            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            enterpriseCloudBeanRecordVO.setFranchiseeName(Objects.isNull(franchisee)?"":franchisee.getName());

            User user = userService.queryByUidFromCache(item.getUid());
            enterpriseCloudBeanRecordVO.setUsername(Objects.isNull(user)?"":user.getName());

            return enterpriseCloudBeanRecordVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Integer selectByPageCount(EnterpriseCloudBeanRecordQuery query) {
        return this.enterpriseCloudBeanRecordMapper.selectByPageCount(query);
    }
}

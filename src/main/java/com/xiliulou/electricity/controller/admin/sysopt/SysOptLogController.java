package com.xiliulou.electricity.controller.admin.sysopt;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.entity.sysopt.SysOptLog;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.model.sysopt.opt.SysOptLogOptModel;
import com.xiliulou.electricity.model.sysopt.query.SysOptLogQryModel;
import com.xiliulou.electricity.query.sysopt.SysOptLogQryReq;
import com.xiliulou.electricity.service.UserService;
import com.xiliulou.electricity.service.sysopt.SysOptLogService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.utils.ServletUtils;
import com.xiliulou.electricity.utils.WebUtils;
import com.xiliulou.electricity.vo.sysopt.SysOptLogVO;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: Ant
 * @Date 2024/3/6
 * @Description: 系统操作日志 Controller
 **/
@Slf4j
@RestController
@RequestMapping("/admin/sysOptLog")
public class SysOptLogController {
    
    @Resource
    private UserService userService;
    
    @Resource
    private SysOptLogService sysOptLogService;
    
    /**
     * 条件查询列表
     *
     * @param qryReq 请求参数类
     * @return 租车订单集
     */
    @PostMapping("/page")
    public R<List<SysOptLogVO>> page(@RequestBody SysOptLogQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new SysOptLogQryReq();
        }
        
        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);
        
        // 转换请求体
        SysOptLogQryModel qryModel = new SysOptLogQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);
        
        // 调用服务
        List<SysOptLog> entities = sysOptLogService.listPageByCondition(qryModel);
        if (CollectionUtils.isEmpty(entities)) {
            return R.ok(Collections.emptyList());
        }
        
        // 模型转换，封装返回
        List<SysOptLogVO> carRentalPackageVOList = entities.stream().map(entity -> {
            SysOptLogVO sysOptLogVO = new SysOptLogVO();
            BeanUtils.copyProperties(entity, sysOptLogVO);
            
            // 获取用户信息
            User user = userService.queryByUidFromCache(entity.getCreateUid());
            sysOptLogVO.setCreatorName(ObjectUtils.isNotEmpty(user) ? user.getName() : null);
            
            return sysOptLogVO;
        }).collect(Collectors.toList());
        
        return R.ok(carRentalPackageVOList);
    }
    
    /**
     * 条件查询总数
     *
     * @param qryReq 请求参数类
     * @return 总数
     */
    @PostMapping("/count")
    public R<Integer> count(@RequestBody SysOptLogQryReq qryReq) {
        if (null == qryReq) {
            qryReq = new SysOptLogQryReq();
        }
        
        // 赋值租户
        Integer tenantId = TenantContextHolder.getTenantId();
        qryReq.setTenantId(tenantId);
        
        // 转换请求体
        SysOptLogQryModel qryModel = new SysOptLogQryModel();
        BeanUtils.copyProperties(qryReq, qryModel);
        
        // 调用服务
        return R.ok(sysOptLogService.countByCondition(qryModel));
    }
    
    /**
     * 新增
     *
     * @param optModel 操作模型
     * @return true(成功)、false(失败)
     */
    @PostMapping("/insert")
    public R<Boolean> insert(@RequestBody @Valid SysOptLogOptModel optModel) {
        
        Integer tenantId = TenantContextHolder.getTenantId();
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            throw new BizException("ELECTRICITY.0001", "未找到用户");
        }
        
        optModel.setTenantId(tenantId);
        optModel.setCreateUid(user.getUid());
        optModel.setOptIp(WebUtils.getIP(ServletUtils.getRequest()));
        
        SysOptLog entity = new SysOptLog();
        BeanUtils.copyProperties(optModel, entity);
        
        return R.ok(sysOptLogService.insert(entity) > 0);
    }
}

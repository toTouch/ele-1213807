/**
 *  Create date: 2024/8/22
 */

package com.xiliulou.electricity.controller.admin.profitsharing;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.admin.base.AbstractFranchiseeDataPermissionController;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingReceiverConfigOptRequest;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingReceiverConfigQryRequest;
import com.xiliulou.electricity.request.profitsharing.ProfitSharingReceiverConfigStatusOptRequest;
import com.xiliulou.electricity.service.profitsharing.ProfitSharingReceiverConfigService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.QueryGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingReceiverConfigDetailsVO;
import com.xiliulou.electricity.vo.profitsharing.ProfitSharingReceiverConfigVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * description:
 *
 * @author caobotao.cbt
 * @date 2024/8/22 16:45
 */
@RestController
public class JsonAdminProfitSharingReceiverConfigController extends AbstractFranchiseeDataPermissionController {
    
    
    @Resource
    private ProfitSharingReceiverConfigService profitSharingReceiverConfigService;
    
    
    /**
     * 根据手机号获取openid
     *
     * @param phone
     * @author caobotao.cbt
     * @date 2024/8/26 11:14
     */
    @GetMapping("/admin/queryWxMiniOpenIdByPhone")
    public R queryWxMiniOpenIdByPhone(@RequestParam("phone") String phone) {
        
        String openId = profitSharingReceiverConfigService.queryWxMiniOpenIdByPhone(phone, TenantContextHolder.getTenantId());
        
        return R.ok(openId);
    }
    
    /**
     * 新增
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/8/26 11:15
     */
    @PostMapping(value = "/admin/profitSharingReceiverConfig")
    public R insert(@Validated(CreateGroup.class) @RequestBody ProfitSharingReceiverConfigOptRequest request) {
        
        request.setTenantId(TenantContextHolder.getTenantId());
        request.setDataPermissionFranchiseeIds(checkFranchiseeDataPermission());
        
        profitSharingReceiverConfigService.insert(request);
        return R.ok();
    }
    
    /**
     * 更新
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/8/26 11:15
     */
    @PutMapping(value = "/admin/profitSharingReceiverConfig")
    public R update(@Validated(UpdateGroup.class) @RequestBody ProfitSharingReceiverConfigOptRequest request) {
        
        request.setTenantId(TenantContextHolder.getTenantId());
        request.setDataPermissionFranchiseeIds(checkFranchiseeDataPermission());
        
        profitSharingReceiverConfigService.update(request);
        return R.ok();
    }
    
    
    /**
     * 逻辑删除
     *
     * @param id
     * @author caobotao.cbt
     * @date 2024/8/26 14:02
     */
    @DeleteMapping(value = "/admin/profitSharingReceiverConfig/{id}")
    public R remove(@PathVariable(value = "id") Long id) {
        
        profitSharingReceiverConfigService.removeById(TenantContextHolder.getTenantId(), id, checkFranchiseeDataPermission());
        return R.ok();
    }
    
    /**
     * 状态更新
     *
     * @param request
     * @author caobotao.cbt
     * @date 2024/8/26 13:48
     */
    @PutMapping(value = "/admin/profitSharingReceiverConfig/updateStatus")
    public R updateStatus(@Validated @RequestBody ProfitSharingReceiverConfigStatusOptRequest request) {
        
        request.setTenantId(TenantContextHolder.getTenantId());
        request.setDataPermissionFranchiseeIds(checkFranchiseeDataPermission());
        
        profitSharingReceiverConfigService.updateStatus(request);
        return R.ok();
    }
    
    
    /**
     * 详情
     *
     * @author caobotao.cbt
     * @date 2024/8/26 14:03
     */
    @GetMapping(value = "/admin/profitSharingReceiverConfig/{id}")
    public R details(@PathVariable(value = "id") Long id) {
        
        ProfitSharingReceiverConfigDetailsVO detailsVO = profitSharingReceiverConfigService.queryDetailsById(TenantContextHolder.getTenantId(), id);
        
        this.checkFranchiseeDataPermissionByFranchiseeId(detailsVO.getFranchiseeId());
        
        return R.ok(detailsVO);
    }
    
    
    /**
     * 分页
     *
     * @author caobotao.cbt
     * @date 2024/8/26 14:03
     */
    @PostMapping(value = "/admin/profitSharingReceiverConfig/pageList")
    public R pageList(@Validated(QueryGroup.class) @RequestBody ProfitSharingReceiverConfigQryRequest request) {
        
        request.setTenantId(TenantContextHolder.getTenantId());
        request.setDataPermissionFranchiseeIds(checkFranchiseeDataPermission());
        
        List<ProfitSharingReceiverConfigVO> configVOList = profitSharingReceiverConfigService.pageList(request);
        
        return R.ok(configVOList);
    }
    
    
    /**
     * 分页
     *
     * @author caobotao.cbt
     * @date 2024/8/26 14:03
     */
    @PostMapping(value = "/admin/profitSharingReceiverConfig/count")
    public R count(@Validated @RequestBody ProfitSharingReceiverConfigQryRequest request) {
        request.setTenantId(TenantContextHolder.getTenantId());
        request.setDataPermissionFranchiseeIds(checkFranchiseeDataPermission());
        
        Integer count = profitSharingReceiverConfigService.count(request);
        return R.ok(count);
    }
}

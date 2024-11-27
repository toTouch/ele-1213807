package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.controller.BasicController;
import com.xiliulou.electricity.query.MutualExchangePageQuery;
import com.xiliulou.electricity.query.MutualExchangeUpdateQuery;
import com.xiliulou.electricity.request.MutualExchangeAddConfigRequest;
import com.xiliulou.electricity.service.TenantFranchiseeMutualExchangeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 租户下加盟商互通换电controller
 *
 * @author renhang
 * @since 2024-11-27 14:59:37
 */
@RestController
@RequestMapping("/admin/tenant/mutualExchange")
public class JsonAdminTenantFranchiseeMutualExchangeController extends BasicController {
    
    
    @Resource
    private TenantFranchiseeMutualExchangeService tenantFranchiseeMutualExchangeService;
    
    /**
     * 新增/编辑
     *
     * @param request request
     * @return R
     */
    @PostMapping("addOrEditConfig")
    public R addOrEditConfig(@RequestBody @Validated MutualExchangeAddConfigRequest request) {
        return tenantFranchiseeMutualExchangeService.addOrEditConfig(request);
    }
    
    /**
     * 回显详情
     *
     * @param id id
     * @return R
     */
    @GetMapping("getMutualExchangeDetailById")
    public R getMutualExchangeDetailById(@RequestParam("id") Long id) {
        return R.ok(tenantFranchiseeMutualExchangeService.getMutualExchangeDetailById(id));
    }
    
    
    /**
     * 分页查询
     *
     * @param query query
     * @return R
     */
    @PostMapping("pageList")
    public R pageList(@RequestBody MutualExchangePageQuery query) {
        return R.ok(tenantFranchiseeMutualExchangeService.pageList(query));
    }
    
    /**
     * 分页count
     *
     * @param query query
     * @return R
     */
    @PostMapping("pageCount")
    public R pageCount(@RequestBody MutualExchangePageQuery query) {
        return R.ok(tenantFranchiseeMutualExchangeService.pageCount(query));
    }
    
    
    /**
     * 删除
     *
     * @param id id
     * @return R
     */
    @GetMapping("deleteById")
    public R deleteById(@RequestParam("id") Long id) {
        return tenantFranchiseeMutualExchangeService.deleteById(id);
    }
    
    
    /**
     * 分页count
     *
     * @param query query
     * @return R
     */
    @PostMapping("updateStatus")
    public R updateStatus(@RequestBody MutualExchangeUpdateQuery query) {
        return tenantFranchiseeMutualExchangeService.updateStatus(query);
    }
    
    
}

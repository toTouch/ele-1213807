package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleWarnMsg;
import com.xiliulou.electricity.entity.NotExistSn;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.query.FaqQuery;
import com.xiliulou.electricity.query.NotExistSnQuery;
import com.xiliulou.electricity.service.FaqService;
import com.xiliulou.electricity.service.NotExistSnService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.validator.CreateGroup;
import com.xiliulou.electricity.validator.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

/**
 * @author : eclair
 * @date : 2021/9/26 4:45 下午
 */
@RestController
public class JsonAdminNotExistSnController extends BaseController {
    @Autowired
    NotExistSnService notExistSnService;

    @GetMapping("/admin/notExistSn/list")
    public R getList(@RequestParam("size") Integer size,
                     @RequestParam("offset") Integer offset,
                     @RequestParam("eId") Integer eId) {
        if (size <= 0 || size > 50) {
            size = 10;
        }
        if (offset < 0) {
            offset = 0;
        }

        NotExistSnQuery notExistSnQuery = NotExistSnQuery.builder()
                .offset(offset)
                .size(size)
                .eId(eId).build();

        return notExistSnService.queryList(notExistSnQuery);
    }


    @GetMapping("/admin/notExistSn/queryCount")
    public R queryCount(@RequestParam("eId") Integer eId) {

        NotExistSnQuery notExistSnQuery = NotExistSnQuery.builder()
                .eId(eId).build();
        return notExistSnService.queryCount(notExistSnQuery);
    }

    //delete notExistSn by Id
    @DeleteMapping (value = "/admin/notExistSn/delete")
    public R delete(@RequestParam("ids") String ids) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        List<Long> idList = JsonUtil.fromJsonArray(ids, Long.class);
        for (Long id : idList) {
            NotExistSn notExistSn = notExistSnService.queryByIdFromDB(id);
            if (Objects.nonNull(notExistSn)) {
                if(Objects.equals(notExistSn.getTenantId(),tenantId)){
                    notExistSnService.delete(id);
                }
            }
        }
        return R.ok();
    }


}

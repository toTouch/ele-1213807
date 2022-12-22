package com.xiliulou.electricity.controller.admin;

import com.xiliulou.core.controller.BaseController;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.Picture;
import com.xiliulou.electricity.query.CallBackQuery;
import com.xiliulou.electricity.query.PictureQuery;
import com.xiliulou.electricity.query.StorePictureQuery;
import com.xiliulou.electricity.service.PictureService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2022-12-14-14:07
 */
@RestController
public class JsonAdminPictureController extends BaseController {

    @Autowired
    private PictureService pictureService;

    /**
     * 查询图片
     */
    @GetMapping("/admin/picture/list")
    public R storePicture(@RequestParam("businessId") Long businessId){

        PictureQuery pictureQuery = new PictureQuery();
        pictureQuery.setBusinessId(businessId);
        pictureQuery.setStatus(Picture.STATUS_ENABLE);
        pictureQuery.setDelFlag(Picture.DEL_NORMAL);
        pictureQuery.setTenantId(TenantContextHolder.getTenantId());

        return R.ok(pictureService.selectByQuery(pictureQuery));
    }

    /**
     * 保存图片
     * @param callBackQuery
     * @return
     */
    @PostMapping(value = "/admin/picture/save/callback")
    public R pictureCallBack(@RequestBody CallBackQuery callBackQuery){
        return R.ok(pictureService.savePictureCallBack(callBackQuery));
    }

}

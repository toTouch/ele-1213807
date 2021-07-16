package com.xiliulou.electricity.controller.user;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.ShareActivityRecord;
import com.xiliulou.electricity.service.JoinShareActivityRecordService;
import com.xiliulou.pay.weixin.entity.SharePicture;
import com.xiliulou.pay.weixin.shareUrl.GenerateShareUrlService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 参与邀请活动记录(JoinShareActivityRecord)表控制层
 *
 * @author makejava
 * @since 2021-07-14 09:44:36
 */
@RestController
public class JsonUserJoinShareActivityRecordController {
    /**
     * 服务对象
     */
    @Resource
    private JoinShareActivityRecordService joinShareActivityRecordService;

    @Autowired
    GenerateShareUrlService generateShareUrlService;


    /**
     * 解密分享图片
     *
     */
    @GetMapping(value = "/outer/joinShareActivityRecord/checkScene")
    public R checkScene(@RequestParam(value = "scene") String scene) {
        return joinShareActivityRecordService.checkScene(scene);
    }


    /**
     * 点击分享链接进入活动
     *
     */
    @PostMapping(value = "/user/joinShareActivityRecord/joinActivity")
    public R joinActivity(@RequestParam(value = "activityId") Integer activityId,@RequestParam(value = "uid") Long uid) {
        return joinShareActivityRecordService.joinActivity(activityId,uid);
    }



    /**
     * 点击分享链接进入活动
     *
     */
    @GetMapping(value = "/outer/test")
    public R test(){
    SharePicture sharePicture = new SharePicture();
		sharePicture.setPage("pages/start/index");
		sharePicture.setScene("uid=15_id=1_code=880894");
		sharePicture.setAppId("wx76159ea6aa7a64bc");
		sharePicture.setAppSecret("b44586ca1b4ff8def2b4c869cdd8ea6a");
        Pair<Boolean, Object> getShareUrlPair = generateShareUrlService.generateSharePicture(sharePicture);
        //分享失败

        return R.ok(getShareUrlPair.getRight());



    }



}

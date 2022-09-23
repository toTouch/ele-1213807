package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.EleWarnMsg;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.EleWarnMsgQuery;
import com.xiliulou.electricity.service.EleWarnMsgService;
import com.xiliulou.electricity.service.UserTypeFactory;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 换电柜表(TElectricityCabinet)表控制层
 *
 * @author makejava
 * @since 2020-11-25 11:00:14
 */
@RestController
@Slf4j
public class JsonAdminEleWarnMsgController {
    /**
     * 服务对象
     */
    @Autowired
    EleWarnMsgService eleWarnMsgService;
    @Autowired
    UserTypeFactory userTypeFactory;

    //列表查询
    @GetMapping(value = "/admin/eleWarnMsg/list")
    public R queryList(@RequestParam("size") Long size,
                       @RequestParam("offset") Long offset,
                       @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
                       @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName,
                       @RequestParam(value = "type", required = false) Integer type,
                       @RequestParam(value = "status", required = false) Integer status,
                       @RequestParam(value = "cellNo", required = false) Integer cellNo,
                       @RequestParam(value = "tenantId") Integer tenantId) {
        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELE ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //如果是查全部则直接跳过
        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByDataType(user);
        }

        if (ObjectUtil.isEmpty(eleIdList)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        EleWarnMsgQuery eleWarnMsgQuery = EleWarnMsgQuery.builder()
                .offset(offset)
                .size(size)
                .electricityCabinetId(electricityCabinetId)
                .type(type)
                .status(status)
                .eleIdList(eleIdList)
                .tenantId(tenantId)
                .cellNo(cellNo)
                .electricityCabinetName(electricityCabinetName)
                .build();

        return eleWarnMsgService.queryList(eleWarnMsgQuery);
    }

    //查询所有租户异常消息
    @GetMapping(value = "/admin/eleWarnMsg/allTenantList")
    public R queryAllTenantList(@RequestParam("size") Long size,
                                @RequestParam("offset") Long offset) {

        if (size < 0 || size > 50) {
            size = 10L;
        }

        if (offset < 0) {
            offset = 0L;
        }

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin()) {
            log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        EleWarnMsgQuery eleWarnMsgQuery = EleWarnMsgQuery.builder()
                .size(size)
                .offset(offset)
                .build();

        return eleWarnMsgService.queryAllTenant(eleWarnMsgQuery);
    }

    @GetMapping(value = "/admin/eleWarnMsg/queryAllTenantCount")
    public R queryAllTenantCount() {

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin()) {
            log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return eleWarnMsgService.queryAllTenantCount();
    }

    //列表查询
    @GetMapping(value = "/admin/eleWarnMsg/queryCount")
    public R queryCount(@RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
                        @RequestParam(value = "electricityCabinetName", required = false) String electricityCabinetName,
                        @RequestParam(value = "type", required = false) Integer type,
                        @RequestParam(value = "status", required = false) Integer status,
                        @RequestParam(value = "cellNo", required = false) Integer cellNo,
                        @RequestParam(value = "tenantId") Integer tenantId) {

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //如果是查全部则直接跳过
        List<Integer> eleIdList = null;
        if (!SecurityUtils.isAdmin() && !Objects.equals(user.getDataType(), User.DATA_TYPE_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByDataType(user);
        }

        if (ObjectUtil.isEmpty(eleIdList)) {
            return R.ok(Collections.EMPTY_LIST);
        }

        EleWarnMsgQuery eleWarnMsgQuery = EleWarnMsgQuery.builder()
                .electricityCabinetId(electricityCabinetId)
                .type(type)
                .status(status)
                .eleIdList(eleIdList)
                .tenantId(tenantId)
                .cellNo(cellNo)
                .electricityCabinetName(electricityCabinetName)
                .build();

        return eleWarnMsgService.queryCount(eleWarnMsgQuery);
    }

    //have read message
    @PostMapping(value = "/admin/eleWarnMsg/haveRead")
    public R haveRead(@RequestParam("ids") String ids, @RequestParam(value = "tenantId") Integer tenantId) {

        List<Long> idList = JsonUtil.fromJsonArray(ids, Long.class);
        for (Long id : idList) {
            EleWarnMsg eleWarnMsg = eleWarnMsgService.queryByIdFromDB(id);
            if (Objects.nonNull(eleWarnMsg) && Objects.equals(eleWarnMsg.getStatus(), EleWarnMsg.STATUS_UNREAD)) {
                if (Objects.equals(eleWarnMsg.getTenantId(), tenantId)) {
                    EleWarnMsg updateEleWarnMsg = new EleWarnMsg();
                    updateEleWarnMsg.setId(eleWarnMsg.getId());
                    updateEleWarnMsg.setStatus(EleWarnMsg.STATUS_HAVE_READ);
                    updateEleWarnMsg.setUpdateTime(System.currentTimeMillis());
                    eleWarnMsgService.update(updateEleWarnMsg);
                }
            }
        }
        return R.ok();
    }

    //delete message by Id
    @DeleteMapping(value = "/admin/eleWarnMsg/delete")
    public R delete(@RequestParam("ids") String ids, @RequestParam(value = "tenantId") Integer tenantId) {

        List<Long> idList = JsonUtil.fromJsonArray(ids, Long.class);
        for (Long id : idList) {
            EleWarnMsg eleWarnMsg = eleWarnMsgService.queryByIdFromDB(id);
            if (Objects.nonNull(eleWarnMsg)) {
                if (Objects.equals(eleWarnMsg.getTenantId(), tenantId)) {
                    eleWarnMsgService.delete(id);
                }
            }
        }
        return R.ok();
    }

    /**
     * 错误消息分类 admin查看所有异常
     *
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping(value = "/admin/statisticsEleWarmMsg/list")
    public R statisticsEleWarmMsg(@RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
                                  @RequestParam(value = "cellNo", required = false) Integer cellNo,
                                  @RequestParam(value = "beginTime", required = false) Long beginTime,
                                  @RequestParam(value = "endTime", required = false) Long endTime) {

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin()) {
            log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        EleWarnMsgQuery eleWarnMsgQuery = EleWarnMsgQuery.builder()
                .electricityCabinetId(electricityCabinetId)
                .cellNo(cellNo)
                .beginTime(beginTime)
                .endTime(endTime).build();

        return eleWarnMsgService.queryStatisticsEleWarmMsg(eleWarnMsgQuery);
    }

    @GetMapping(value = "/admin/statisticsEleWarmMsg/cabinetList")
    public R statisticEleWarnMsgByElectricityCabinet(@RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId,
                                                     @RequestParam(value = "beginTime", required = false) Long beginTime,
                                                     @RequestParam(value = "endTime", required = false) Long endTime) {


        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin()) {
            log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        EleWarnMsgQuery eleWarnMsgQuery = EleWarnMsgQuery.builder()
                .electricityCabinetId(electricityCabinetId)
                .beginTime(beginTime)
                .endTime(endTime).build();


        return eleWarnMsgService.queryStatisticEleWarnMsgByElectricityCabinet(eleWarnMsgQuery);

    }

    @GetMapping(value = "/admin/statisticsEleWarmMsg/ranking")
    public R statisticEleWarnMsgRanking(@RequestParam("size") Long size,
                                        @RequestParam("offset") Long offset,
                                        @RequestParam(value = "electricityCabinetId", required = false) Integer electricityCabinetId) {
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin()) {
            log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        EleWarnMsgQuery eleWarnMsgQuery = EleWarnMsgQuery.builder()
                .size(size)
                .offset(offset)
                .electricityCabinetId(electricityCabinetId).build();
        return eleWarnMsgService.queryStatisticEleWarnMsgRanking(eleWarnMsgQuery);
    }

    @GetMapping(value = "/admin/statisticsEleWarmMsg/rankingCount")
    public R statisticEleWarnMsgRankingCount() {
        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!SecurityUtils.isAdmin()) {
            log.warn("USER TYPE ERROR! not found operate service! userType={}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return eleWarnMsgService.queryStatisticEleWarnMsgRankingCount();
    }


}
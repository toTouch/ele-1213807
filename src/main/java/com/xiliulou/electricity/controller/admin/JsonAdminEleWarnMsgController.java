package com.xiliulou.electricity.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.xiliulou.clickhouse.service.ClickHouseService;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.electricity.entity.BatteryChangeInfo;
import com.xiliulou.electricity.entity.EleWarnMsg;
import com.xiliulou.electricity.entity.User;
import com.xiliulou.electricity.query.EleWarnMsgQuery;
import com.xiliulou.electricity.service.EleWarnMsgService;
import com.xiliulou.electricity.service.UserTypeFactory;
import com.xiliulou.electricity.service.UserTypeService;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.EleBatteryWarnMsgVo;
import com.xiliulou.electricity.vo.EleBusinessWarnMsgVo;
import com.xiliulou.electricity.vo.EleCabinetWarnMsgVo;
import com.xiliulou.electricity.vo.EleCellWarnMsgVo;
import com.xiliulou.security.bean.TokenUser;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    @Autowired
    ClickHouseService clickHouseService;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //如果是查全部则直接跳过
        List<Integer> eleIdList = null;
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByUserType(user);
            if (ObjectUtil.isEmpty(eleIdList)) {
                return R.ok(new ArrayList<>());
            }
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

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
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

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
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
        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)
                && !Objects.equals(user.getType(), User.TYPE_USER_OPERATE)) {
            UserTypeService userTypeService = userTypeFactory.getInstance(user.getType());
            if (Objects.isNull(userTypeService)) {
                log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
                return R.fail("ELECTRICITY.0066", "用户权限不足");
            }
            eleIdList = userTypeService.getEleIdListByUserType(user);
            if (ObjectUtil.isEmpty(eleIdList)) {
                return R.ok(new ArrayList<>());
            }
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

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
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

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
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

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
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

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            log.warn("USER TYPE ERROR! not found operate service! userType:{}", user.getType());
            return R.fail("ELECTRICITY.0066", "用户权限不足");
        }

        return eleWarnMsgService.queryStatisticEleWarnMsgRankingCount();
    }


    //电池故障列表查询
    @GetMapping(value = "/admin/batteryWarnMsg/list")
    public R queryBatteryWarnMsgList(@RequestParam("size") Long size,
                                     @RequestParam("offset") Long offset,
                                     @RequestParam(value = "beginTime") Long beginTime,
                                     @RequestParam(value = "endTime") Long endTime,
                                     @RequestParam(value = "sn", required = false) String sn,
                                     @RequestParam(value = "electricityCabinetId", required = false) String electricityCabinetId) {
        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(beginTime / 1000, 0, ZoneOffset.ofHours(8));
        LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(endTime / 1000, 0, ZoneOffset.ofHours(8));
        String begin = formatter.format(beginLocalDateTime);
        String end = formatter.format(endLocalDateTime);

        if (StrUtil.isNotEmpty(sn) && StrUtil.isEmpty(electricityCabinetId)) {
            String sql = "select * from t_warn_msg_battery where tenantId=? and  sn=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
            List list = clickHouseService.queryList(EleBatteryWarnMsgVo.class, sql, tenantId, sn, begin, end, offset, size);
            eleWarnMsgService.queryElectricityName(list);
            return R.ok(list);
        }

        if (StrUtil.isNotEmpty(electricityCabinetId) && StrUtil.isEmpty(sn)) {
            String sql = "select * from t_warn_msg_battery where tenantId=? and  electricityCabinetId=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
            List list = clickHouseService.queryList(EleBatteryWarnMsgVo.class, sql, tenantId, electricityCabinetId, begin, end, offset, size);
            eleWarnMsgService.queryElectricityName(list);
            return R.ok(list);
        }

        if (StrUtil.isNotEmpty(sn) && StrUtil.isNotEmpty(electricityCabinetId)) {
            String sql = "select * from t_warn_msg_battery where tenantId=? and  electricityCabinetId=? and  sn=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
            List list = clickHouseService.queryList(EleBatteryWarnMsgVo.class, sql, tenantId, electricityCabinetId, sn, begin, end, offset, size);
            eleWarnMsgService.queryElectricityName(list);
            return R.ok(list);
        }

        System.out.println("电柜ID===================="+electricityCabinetId+"=============="+sn);

        String sql = "select * from t_warn_msg_battery where tenantId=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
        List list = clickHouseService.queryList(EleBatteryWarnMsgVo.class, sql, tenantId,  begin, end, offset, size);
        eleWarnMsgService.queryElectricityName(list);
        return R.ok(list);
    }


    //格挡故障列表查询
    @GetMapping(value = "/admin/cellWarnMsg/list")
    public R queryCellWarnMsgList(@RequestParam("size") Long size,
                                  @RequestParam("offset") Long offset, @RequestParam(value = "beginTime") Long beginTime,
                                  @RequestParam(value = "endTime") Long endTime,
                                  @RequestParam(value = "cellNo", required = false) Integer cellNo,
                                  @RequestParam(value = "operateType", required = false) Integer operateType,
                                  @RequestParam(value = "electricityCabinetId", required = false) String electricityCabinetId) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(beginTime / 1000, 0, ZoneOffset.ofHours(8));
        LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(endTime / 1000, 0, ZoneOffset.ofHours(8));
        String begin = formatter.format(beginLocalDateTime);
        String end = formatter.format(endLocalDateTime);

        if (Objects.nonNull(cellNo) && Objects.nonNull(operateType)) {
            String sql = "select * from t_warn_msg_cell where tenantId=? and  electricityCabinetId=? and cellNo=? and operateType=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
            List list = clickHouseService.queryList(EleCellWarnMsgVo.class, sql, tenantId, electricityCabinetId, cellNo, operateType, begin, end, offset, size);
            eleWarnMsgService.queryElectricityName(list);
            return R.ok(list);
        }

        if (Objects.nonNull(cellNo) && Objects.isNull(operateType)) {
            String sql = "select * from t_warn_msg_cell where tenantId=? and  electricityCabinetId=? and cellNo=?  and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
            List list = clickHouseService.queryList(EleCellWarnMsgVo.class, sql, tenantId, electricityCabinetId, cellNo, begin, end, offset, size);
            eleWarnMsgService.queryElectricityName(list);
            return R.ok(list);
        }

        if (Objects.isNull(cellNo) && Objects.nonNull(operateType)) {
            String sql = "select * from t_warn_msg_cell where tenantId=? and  operateType=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
            List list = clickHouseService.queryList(EleCellWarnMsgVo.class, sql, tenantId, operateType, begin, end, offset, size);
            eleWarnMsgService.queryElectricityName(list);
            return R.ok(list);
        }

        if (StrUtil.isNotEmpty(electricityCabinetId) && Objects.nonNull(operateType)) {
            String sql = "select * from t_warn_msg_cell where tenantId=? and electricityCabinetId=? and  operateType=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
            List list = clickHouseService.queryList(EleCellWarnMsgVo.class, sql, tenantId, electricityCabinetId, operateType, begin, end, offset, size);
            eleWarnMsgService.queryElectricityName(list);
            return R.ok(list);
        }

        if (StrUtil.isNotEmpty(electricityCabinetId) && Objects.isNull(operateType)) {
            String sql = "select * from t_warn_msg_cell where tenantId=? and electricityCabinetId=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
            List list = clickHouseService.queryList(EleCellWarnMsgVo.class, sql, tenantId, electricityCabinetId, begin, end, offset, size);
            eleWarnMsgService.queryElectricityName(list);
            return R.ok(list);
        }

        String sql = "select * from t_warn_msg_cell where tenantId=? and  reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
        List list = clickHouseService.queryList(EleCellWarnMsgVo.class, sql, tenantId, begin, end, offset, size);
        eleWarnMsgService.queryElectricityName(list);
        return R.ok(list);
    }


    //柜机故障列表查询
    @GetMapping(value = "/admin/cabinetWarnMsg/list")
    public R queryCabinetWarnMsgList(@RequestParam(value = "beginTime") Long beginTime,
                                     @RequestParam("size") Long size,
                                     @RequestParam("offset") Long offset,
                                     @RequestParam(value = "endTime") Long endTime,
                                     @RequestParam(value = "electricityCabinetId", required = false) String electricityCabinetId) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(beginTime / 1000, 0, ZoneOffset.ofHours(8));
        LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(endTime / 1000, 0, ZoneOffset.ofHours(8));
        String begin = formatter.format(beginLocalDateTime);
        String end = formatter.format(endLocalDateTime);


        if (Objects.nonNull(electricityCabinetId)) {
            String sql = "select * from t_warn_msg_cabinet where tenantId=? and  electricityCabinetId=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
            List list = clickHouseService.queryList(EleCabinetWarnMsgVo.class, sql, tenantId, electricityCabinetId, begin, end, offset, size);
            eleWarnMsgService.queryElectricityName(list);
            return R.ok(list);
        }


        String sql = "select * from t_warn_msg_cabinet where tenantId=? and  reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
        List list = clickHouseService.queryList(EleCabinetWarnMsgVo.class, sql, tenantId, begin, end, offset, size);
        eleWarnMsgService.queryElectricityName(list);
        return R.ok(list);
    }

    //业务故障列表查询
    @GetMapping(value = "/admin/businessWarnMsg/list")
    public R queryBusinessWarnMsgList(@RequestParam(value = "beginTime") Long beginTime,
                                      @RequestParam("size") Long size,
                                      @RequestParam("offset") Long offset,
                                      @RequestParam(value = "endTime") Long endTime,
                                      @RequestParam(value = "electricityCabinetId", required = false) String electricityCabinetId) {

        //租户
        Integer tenantId = TenantContextHolder.getTenantId();

        LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(beginTime / 1000, 0, ZoneOffset.ofHours(8));
        LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(endTime / 1000, 0, ZoneOffset.ofHours(8));
        String begin = formatter.format(beginLocalDateTime);
        String end = formatter.format(endLocalDateTime);


        if (Objects.nonNull(electricityCabinetId)) {
            String sql = "select * from t_warn_msg_business where tenantId=? and  electricityCabinetId=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
            List list = clickHouseService.queryList(EleBusinessWarnMsgVo.class, sql, tenantId, electricityCabinetId, begin, end, offset, size);
            eleWarnMsgService.queryElectricityName(list);
            return R.ok(list);
        }


        String sql = "select * from t_warn_msg_business where tenantId=? and  reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
        List list = clickHouseService.queryList(EleBusinessWarnMsgVo.class, sql, tenantId, begin, end, offset, size);
        eleWarnMsgService.queryElectricityName(list);
        return R.ok(list);
    }


    //超级管理员电池故障列表查询
    @GetMapping(value = "/admin/superAdminBatteryWarnMsg/list")
    public R querySuperAdminBatteryWarnMsgList(@RequestParam(value = "beginTime") Long beginTime,
                                               @RequestParam("size") Long size,
                                               @RequestParam("offset") Long offset,
                                               @RequestParam(value = "endTime") Long endTime,
                                               @RequestParam(value = "sn", required = false) String sn,
                                               @RequestParam(value = "electricityCabinetId", required = false) String electricityCabinetId) {

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }

        LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(beginTime / 1000, 0, ZoneOffset.ofHours(8));
        LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(endTime / 1000, 0, ZoneOffset.ofHours(8));
        String begin = formatter.format(beginLocalDateTime);
        String end = formatter.format(endLocalDateTime);

        if (StringUtil.isNotEmpty(sn) && StringUtil.isEmpty(electricityCabinetId)) {
            String sql = "select * from t_warn_msg_battery where sn=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
            List list = clickHouseService.queryList(EleBatteryWarnMsgVo.class, sql, sn, begin, end, offset, size);
            eleWarnMsgService.queryElectricityName(list);
            return R.ok(list);
        }

        if (StringUtil.isNotEmpty(electricityCabinetId) && StringUtil.isEmpty(sn)) {
            String sql = "select * from t_warn_msg_battery where electricityCabinetId=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
            List list = clickHouseService.queryList(EleBatteryWarnMsgVo.class, sql, electricityCabinetId, begin, end, offset, size);
            eleWarnMsgService.queryElectricityName(list);
            return R.ok(list);
        }

        if (StringUtil.isNotEmpty(sn) && StringUtil.isNotEmpty(electricityCabinetId)) {
            String sql = "select * from t_warn_msg_battery where and  electricityCabinetId=? and  sn=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
            List list = clickHouseService.queryList(EleBatteryWarnMsgVo.class, sql, electricityCabinetId, sn, begin, end, offset, size);
            eleWarnMsgService.queryElectricityName(list);
            return R.ok(list);
        }

        String sql = "select * from t_warn_msg_battery where electricityCabinetId=? and sn=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
        List list = clickHouseService.queryList(EleBatteryWarnMsgVo.class, sql, electricityCabinetId, sn, begin, end, offset, size);
        eleWarnMsgService.queryElectricityName(list);
        return R.ok(list);
    }


    //超级管理员格挡故障列表查询
    @GetMapping(value = "/admin/superAdminCellWarnMsg/list")
    public R querySuperAdminCellWarnMsgList(@RequestParam(value = "beginTime") Long beginTime,
                                            @RequestParam(value = "endTime") Long endTime,
                                            @RequestParam("size") Long size,
                                            @RequestParam("offset") Long offset,
                                            @RequestParam(value = "cellNo", required = false) Integer cellNo,
                                            @RequestParam(value = "operateType", required = false) Integer operateType,
                                            @RequestParam(value = "electricityCabinetId", required = false) String electricityCabinetId) {

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }

        LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(beginTime / 1000, 0, ZoneOffset.ofHours(8));
        LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(endTime / 1000, 0, ZoneOffset.ofHours(8));
        String begin = formatter.format(beginLocalDateTime);
        String end = formatter.format(endLocalDateTime);

        if (Objects.nonNull(cellNo) && Objects.nonNull(operateType)) {
            String sql = "select * from t_warn_msg_cell where  electricityCabinetId=? and cellNo=? and operateType=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
            List list = clickHouseService.queryList(EleCellWarnMsgVo.class, sql, electricityCabinetId, cellNo, operateType, begin, end, offset, size);
            eleWarnMsgService.queryElectricityName(list);
            return R.ok(list);
        }

        if (Objects.nonNull(cellNo) && Objects.isNull(operateType)) {
            String sql = "select * from t_warn_msg_cell where electricityCabinetId=? and cellNo=?  and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
            List list = clickHouseService.queryList(EleCellWarnMsgVo.class, sql, electricityCabinetId, cellNo, begin, end, offset, size);
            eleWarnMsgService.queryElectricityName(list);
            return R.ok(list);
        }

        if (Objects.isNull(cellNo) && Objects.nonNull(operateType)) {
            String sql = "select * from t_warn_msg_cell where  operateType=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
            List list = clickHouseService.queryList(EleCellWarnMsgVo.class, sql, operateType, begin, end, offset, size);
            eleWarnMsgService.queryElectricityName(list);
            return R.ok(list);
        }


        String sql = "select * from t_warn_msg_cell where  reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
        List list = clickHouseService.queryList(EleCellWarnMsgVo.class, sql, begin, end, offset, size);
        eleWarnMsgService.queryElectricityName(list);
        return R.ok(list);
    }


    //超级管理员柜机故障列表查询
    @GetMapping(value = "/admin/superAdminCabinetWarnMsg/list")
    public R querySuperAdminCabinetWarnMsgList(@RequestParam(value = "beginTime") Long beginTime,
                                               @RequestParam(value = "endTime") Long endTime,
                                               @RequestParam("size") Long size,
                                               @RequestParam("offset") Long offset,
                                               @RequestParam(value = "electricityCabinetId", required = false) String electricityCabinetId) {

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }

        LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(beginTime / 1000, 0, ZoneOffset.ofHours(8));
        LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(endTime / 1000, 0, ZoneOffset.ofHours(8));
        String begin = formatter.format(beginLocalDateTime);
        String end = formatter.format(endLocalDateTime);


        if (Objects.nonNull(electricityCabinetId)) {
            String sql = "select * from t_warn_msg_cabinet where electricityCabinetId=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
            List list = clickHouseService.queryList(EleCabinetWarnMsgVo.class, sql, electricityCabinetId, begin, end, offset, size);
            eleWarnMsgService.queryElectricityName(list);
            return R.ok(list);
        }


        String sql = "select * from t_warn_msg_cabinet where  reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
        List list = clickHouseService.queryList(EleCabinetWarnMsgVo.class, sql, begin, end, offset, size);
        eleWarnMsgService.queryElectricityName(list);
        return R.ok(list);
    }

    //超级管理员业务故障列表查询
    @GetMapping(value = "/admin/superAdminBusinessWarnMsg/list")
    public R querySuperAdminBusinessWarnMsgList(@RequestParam(value = "beginTime") Long beginTime,
                                                @RequestParam(value = "endTime") Long endTime,
                                                @RequestParam("size") Long size,
                                                @RequestParam("offset") Long offset,
                                                @RequestParam(value = "electricityCabinetId", required = false) String electricityCabinetId) {

        //用户区分
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        if (!Objects.equals(user.getType(), User.TYPE_USER_SUPER)) {
            return R.fail("AUTH.0002", "没有权限操作！");
        }

        LocalDateTime beginLocalDateTime = LocalDateTime.ofEpochSecond(beginTime / 1000, 0, ZoneOffset.ofHours(8));
        LocalDateTime endLocalDateTime = LocalDateTime.ofEpochSecond(endTime / 1000, 0, ZoneOffset.ofHours(8));
        String begin = formatter.format(beginLocalDateTime);
        String end = formatter.format(endLocalDateTime);


        if (Objects.nonNull(electricityCabinetId)) {
            String sql = "select * from t_warn_msg_business where  electricityCabinetId=? and reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
            List list = clickHouseService.queryList(EleBusinessWarnMsgVo.class, sql, electricityCabinetId, begin, end, offset, size);
            eleWarnMsgService.queryElectricityName(list);
            return R.ok(list);
        }


        String sql = "select * from t_warn_msg_business where reportTime>=? AND reportTime<=? order by  createTime desc limit ?,?";
        List list = clickHouseService.queryList(EleBusinessWarnMsgVo.class, sql, begin, end, offset, size);
        eleWarnMsgService.queryElectricityName(list);
        return R.ok(list);
    }


}
package com.xiliulou.electricity.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Maps;
import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.exception.CustomBusinessException;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.core.utils.DataUtil;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CacheConstant;
import com.xiliulou.electricity.constant.ElectricityIotConstant;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.mapper.ElectricityCabinetOrderMapper;
import com.xiliulou.electricity.mns.EleHardwareHandlerManager;
import com.xiliulou.electricity.query.*;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.tenant.TenantContextHolder;
import com.xiliulou.electricity.utils.SecurityUtils;
import com.xiliulou.electricity.vo.*;
import com.xiliulou.iot.entity.HardwareCommandQuery;
import com.xiliulou.security.bean.TokenUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shaded.org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单表(TElectricityCabinetOrder)表服务实现类
 *
 * @author makejava
 * @since 2020-11-26 10:56:56
 */
@Service("electricityCabinetOrderService")
@Slf4j
public class ElectricityCabinetOrderServiceImpl implements ElectricityCabinetOrderService {
    @Resource
    private ElectricityCabinetOrderMapper electricityCabinetOrderMapper;
    @Autowired
    ElectricityCabinetService electricityCabinetService;
    @Autowired
    ElectricityCabinetBoxService electricityCabinetBoxService;
    @Autowired
    RedisService redisService;
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    EleHardwareHandlerManager eleHardwareHandlerManager;
    @Autowired
    ElectricityConfigService electricityConfigService;
    @Autowired
    RentBatteryOrderService rentBatteryOrderService;
    @Autowired
    StoreService storeService;
    @Autowired
    ElectricityMemberCardService electricityMemberCardService;
    @Autowired
    FranchiseeService franchiseeService;
    @Autowired
    ElectricityBatteryService electricityBatteryService;
    @Autowired
    ElectricityExceptionOrderStatusRecordService electricityExceptionOrderStatusRecordService;
    @Autowired
    ElectricityCabinetOrderOperHistoryService electricityCabinetOrderOperHistoryService;
    @Autowired
    ElectricityMemberCardOrderService electricityMemberCardOrderService;
    @Autowired
    UserBatteryMemberCardService userBatteryMemberCardService;
    @Autowired
    UserBatteryService userBatteryService;
    @Autowired
    ServiceFeeUserInfoService serviceFeeUserInfoService;
    
    @Autowired
    UserCarMemberCardService userCarMemberCardService;
    
    @Autowired
    UserCarDepositService userCarDepositService;

    @Autowired
    UserActiveInfoService userActiveInfoService;

    @Autowired
    BatteryMemberCardService batteryMemberCardService;

    /**
     * 修改数据
     *
     * @param electricityCabinetOrder 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(ElectricityCabinetOrder electricityCabinetOrder) {
        return this.electricityCabinetOrderMapper.updateById(electricityCabinetOrder);

    }

    /**
     * 通过ID查询单条数据从DB
     *
     * @param orderId 主键
     * @return 实例对象
     */
    @Override
    public ElectricityCabinetOrder queryByOrderId(String orderId) {
        return this.electricityCabinetOrderMapper.selectOne(new LambdaQueryWrapper<ElectricityCabinetOrder>().eq(ElectricityCabinetOrder::getOrderId, orderId));
    }

    /**
     * 新增订单
     *
     * @param electricityCabinetOrder
     */
    @Override
    public void insertOrder(ElectricityCabinetOrder electricityCabinetOrder) {
        this.electricityCabinetOrderMapper.insert(electricityCabinetOrder);
    }

    @Deprecated
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R openDoor(OpenDoorQuery openDoorQuery) {
        if (Objects.isNull(openDoorQuery.getOrderId())) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }
        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderMapper.selectOne(Wrappers.<ElectricityCabinetOrder>lambdaQuery().eq(ElectricityCabinetOrder::getOrderId, openDoorQuery.getOrderId()));
        if (Objects.isNull(electricityCabinetOrder)) {
            log.error("ELECTRICITY  ERROR! not found order,orderId={} ", openDoorQuery.getOrderId());
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }


        //开新门开旧门不易前端为准，以订单状态为准
        if (electricityCabinetOrder.getOrderSeq() < ElectricityCabinetOrder.STATUS_CHECK_OLD_AND_NEW) {
            openDoorQuery.setOpenType(OpenDoorQuery.OLD_OPEN_TYPE);
        } else {
            openDoorQuery.setOpenType(OpenDoorQuery.NEW_OPEN_TYPE);
        }

        //旧电池开门
        if (Objects.equals(openDoorQuery.getOpenType(), OpenDoorQuery.OLD_OPEN_TYPE)) {
            if (!Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT)
                    && !Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_CHECK_FAIL)
                    && !Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_CHECK_BATTERY_EXISTS)
                    && !Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_OPEN_FAIL)) {
                return R.fail("ELECTRICITY.0015", "未找到订单");
            }
        }

        //新电池开门
        if (Objects.equals(openDoorQuery.getOpenType(), OpenDoorQuery.NEW_OPEN_TYPE)) {
            if (!Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)
                    && !Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_CHECK_FAIL)
                    && !Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_CHECK_BATTERY_NOT_EXISTS)
                    && !Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_OPEN_FAIL)) {
                return R.fail("ELECTRICITY.0015", "未找到订单");
            }
        }

        //判断开门用户是否匹配
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ELECTRICITY  ERROR! not found user");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }
        if (!Objects.equals(electricityCabinetOrder.getUid(), user.getUid())) {
            return R.fail("ELECTRICITY.0016", "订单用户不匹配，非法开门");
        }

        //查找换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetOrder.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            log.error("ELECTRICITY  ERROR! not found electricityCabinet ！electricityCabinetId={}", electricityCabinetOrder.getElectricityCabinetId());
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("ELECTRICITY  ERROR!  electricityCabinet is offline ！electricityCabinet={}", electricityCabinet);
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

        //校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("order  ERROR! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("order  ERROR! user is unUsable! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
        if (Objects.isNull(franchisee)) {
            log.error("ELE MEMBERCARD ERROR! not found franchisee,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0038", "加盟商不存在");
        }

        ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(user.getUid());
        if (Objects.isNull(electricityBattery)) {
            log.error("ELE ERROR! not found user bind battery,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0020", "未找到电池");
        }

        //旧电池开门
        if (Objects.equals(openDoorQuery.getOpenType(), OpenDoorQuery.OLD_OPEN_TYPE)) {
            //发送命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cell_no", electricityCabinetOrder.getOldCellNo());
            dataMap.put("order_id", electricityCabinetOrder.getOrderId());
            dataMap.put("status", electricityCabinetOrder.getStatus());

            //是否开启电池检测
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(userInfo.getTenantId());
            if (Objects.nonNull(electricityConfig)) {
                if (Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW)) {
                    dataMap.put("is_checkBatterySn", true);
                    dataMap.put("user_binding_battery_sn", electricityBattery.getSn());
                } else {
                    dataMap.put("is_checkBatterySn", false);
                }
            }

            if (Objects.equals(franchisee.getModelType(), Franchisee.OLD_MODEL_TYPE)) {
                dataMap.put("model_type", false);
            } else {
                dataMap.put("model_type", true);
                dataMap.put("multiBatteryModelName", electricityBattery.getModel());
            }

            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + electricityCabinetOrder.getId())
                    .data(dataMap)
                    .productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName())
                    .command(ElectricityIotConstant.ELE_COMMAND_ORDER_OPEN_OLD_DOOR).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        }

        //新电池开门
        if (Objects.equals(openDoorQuery.getOpenType(), OpenDoorQuery.NEW_OPEN_TYPE)) {
            //发送命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("cell_no", electricityCabinetOrder.getNewCellNo());
            dataMap.put("order_id", electricityCabinetOrder.getOrderId());
            dataMap.put("serial_number", electricityCabinetOrder.getNewElectricityBatterySn());
            dataMap.put("status", electricityCabinetOrder.getStatus().toString());
            dataMap.put("old_cell_no", electricityCabinetOrder.getOldCellNo());

            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + electricityCabinetOrder.getId())
                    .data(dataMap)
                    .productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName())
                    .command(ElectricityIotConstant.ELE_COMMAND_ORDER_OPEN_NEW_DOOR).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
        }
        redisService.delete(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + electricityCabinetOrder.getOrderId());
        return R.ok();
    }

    @Slave
    @Override
    public R queryList(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {

        List<ElectricityCabinetOrderVO> electricityCabinetOrderVOList = electricityCabinetOrderMapper.queryList(electricityCabinetOrderQuery);
        if (ObjectUtil.isEmpty(electricityCabinetOrderVOList)) {
            return R.ok(new ArrayList<>());
        }

        if (ObjectUtil.isNotEmpty(electricityCabinetOrderVOList)) {
            electricityCabinetOrderVOList.parallelStream().forEach(e -> {

                if (Objects.nonNull(e.getStatus()) && e.getStatus().equals(ElectricityCabinetOrder.ORDER_CANCEL)
                        || Objects.nonNull(e.getStatus()) && e.getStatus().equals(ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL)) {


                    ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(electricityCabinetOrderQuery.getTenantId());
                    ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord = electricityExceptionOrderStatusRecordService.queryByOrderId(e.getOrderId());
                    if (Objects.nonNull(electricityConfig) && Objects.equals(ElectricityConfig.ENABLE_SELF_OPEN, electricityConfig.getIsEnableSelfOpen()) && Objects.nonNull(electricityExceptionOrderStatusRecord) && Objects.equals(electricityExceptionOrderStatusRecord.getIsSelfOpenCell(), ElectricityExceptionOrderStatusRecord.NOT_SELF_OPEN_CELL)) {
                        if (Objects.equals(electricityExceptionOrderStatusRecord.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL) && (System.currentTimeMillis() - electricityExceptionOrderStatusRecord.getCreateTime()) / 1000 / 60 <= 3) {
                            e.setSelfOpenCell(ElectricityCabinetOrder.SELF_EXCHANGE_ELECTRICITY);
                        }
                    }
                }

            });
        }

        return R.ok(electricityCabinetOrderVOList);
    }

    @Override
    @Slave
    public R queryCount(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {
        return R.ok(electricityCabinetOrderMapper.queryCount(electricityCabinetOrderQuery));
    }

    @Slave
    @Override
    public Integer homepageExchangeOrderSumCount(HomepageElectricityExchangeFrequencyQuery homepageElectricityExchangeFrequencyQuery) {
        return electricityCabinetOrderMapper.homepageExchangeOrderSumCount(homepageElectricityExchangeFrequencyQuery);
    }

    @Slave
    @Override
    public List<HomepageElectricityExchangeFrequencyVo> homepageExchangeFrequency(HomepageElectricityExchangeFrequencyQuery homepageElectricityExchangeFrequencyQuery) {
        return electricityCabinetOrderMapper.homepageExchangeFrequency(homepageElectricityExchangeFrequencyQuery);
    }

    @Slave
    @Override
    public List<HomepageElectricityExchangeFrequencyVo> homepageExchangeFrequencyCount(HomepageElectricityExchangeFrequencyQuery homepageElectricityExchangeFrequencyQuery) {
        return electricityCabinetOrderMapper.homepageExchangeFrequencyCount(homepageElectricityExchangeFrequencyQuery);
    }

    @Slave
    @Override
    public Integer queryCountForScreenStatistic(ElectricityCabinetOrderQuery electricityCabinetOrderQuery) {
        return electricityCabinetOrderMapper.queryCount(electricityCabinetOrderQuery);
    }

    @Slave
    @Override
    public void exportExcel(ElectricityCabinetOrderQuery electricityCabinetOrderQuery, HttpServletResponse response) {
        electricityCabinetOrderQuery.setOffset(0L);
        electricityCabinetOrderQuery.setSize(2000L);
        List<ElectricityCabinetOrderVO> electricityCabinetOrderVOList = electricityCabinetOrderMapper.queryList(electricityCabinetOrderQuery);
        if (ObjectUtil.isEmpty(electricityCabinetOrderVOList)) {
            throw new CustomBusinessException("查不到订单");
        }

        List<ElectricityCabinetOrderExcelVO> electricityCabinetOrderExcelVOS = new ArrayList();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int index = 0;
        for (ElectricityCabinetOrderVO electricityCabinetOrderVO : electricityCabinetOrderVOList) {
            index++;
            ElectricityCabinetOrderExcelVO excelVo = new ElectricityCabinetOrderExcelVO();
            excelVo.setId(index);
            excelVo.setOrderId(electricityCabinetOrderVO.getOrderId());
            excelVo.setPhone(electricityCabinetOrderVO.getPhone());
            excelVo.setOldElectricityBatterySn(electricityCabinetOrderVO.getOldElectricityBatterySn());
            excelVo.setNewElectricityBatterySn(electricityCabinetOrderVO.getNewElectricityBatterySn());

            if (Objects.nonNull(electricityCabinetOrderVO.getCreateTime())) {
                excelVo.setCreateTime(simpleDateFormat.format(new Date(electricityCabinetOrderVO.getCreateTime())));
            }
            if (Objects.nonNull(electricityCabinetOrderVO.getUpdateTime())) {
                excelVo.setUpdateTime(simpleDateFormat.format(new Date(electricityCabinetOrderVO.getUpdateTime())));
            }

            if (Objects.isNull(electricityCabinetOrderVO.getPaymentMethod())) {
                excelVo.setPaymentMethod("");
            }
            if (Objects.equals(electricityCabinetOrderVO.getPaymentMethod(), ElectricityCabinetOrder.PAYMENT_METHOD_MONTH_CARD)) {
                excelVo.setPaymentMethod("月卡");
            }
            if (Objects.equals(electricityCabinetOrderVO.getPaymentMethod(), ElectricityCabinetOrder.PAYMENT_METHOD_SEASON_CARD)) {
                excelVo.setPaymentMethod("季卡");
            }
            if (Objects.equals(electricityCabinetOrderVO.getPaymentMethod(), ElectricityCabinetOrder.PAYMENT_METHOD_YEAR_CARD)) {
                excelVo.setPaymentMethod("年卡");
            }

            //订单状态
            if (Objects.isNull(electricityCabinetOrderVO.getStatus())) {
                excelVo.setStatus("");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.INIT)) {
                excelVo.setStatus("换电订单生成");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.INIT_CHECK_FAIL)) {
                excelVo.setStatus("换电过程放入没电电池检测失败");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.INIT_CHECK_BATTERY_EXISTS)) {
                excelVo.setStatus("换电柜放入没电电池开门发现有电池存在");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.INIT_OPEN_SUCCESS)) {
                excelVo.setStatus("换电柜放入没电电池开门成功");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.INIT_OPEN_FAIL)) {
                excelVo.setStatus("换电柜放入没电电池开门失败");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)) {
                excelVo.setStatus("换电柜检测没电电池成功");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL)) {
                excelVo.setStatus("换电柜检测没电电池失败");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_TIMEOUT)) {
                excelVo.setStatus("换电柜检测没电电池超时");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.COMPLETE_CHECK_FAIL)) {
                excelVo.setStatus("换电柜开满电电池前置检测失败");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.COMPLETE_CHECK_BATTERY_NOT_EXISTS)) {
                excelVo.setStatus("换电柜开满电电池发现电池不存在");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.COMPLETE_OPEN_SUCCESS)) {
                excelVo.setStatus("换电柜开满电电池仓门成功");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.COMPLETE_OPEN_FAIL)) {
                excelVo.setStatus("换电柜开满电电池仓门失败");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
                excelVo.setStatus("换电柜满电电池成功取走，流程结束");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_TIMEOUT)) {
                excelVo.setStatus("换电柜取走满电电池超时");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.ORDER_CANCEL)) {
                excelVo.setStatus("订单取消");
            }
            if (Objects.equals(electricityCabinetOrderVO.getStatus(), ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL)) {
                excelVo.setStatus("订单异常结束");
            }
            electricityCabinetOrderExcelVOS.add(excelVo);
        }

        String fileName = "换电订单报表.xlsx";
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            // 告诉浏览器用什么软件可以打开此文件
            response.setHeader("content-Type", "application/vnd.ms-excel");
            // 下载文件的默认名称
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            EasyExcel.write(outputStream, ElectricityCabinetOrderExcelVO.class).sheet("sheet").doWrite(electricityCabinetOrderExcelVOS);
            return;
        } catch (IOException e) {
            log.error("导出报表失败！", e);
        }
    }


    @Override
    @Transactional
    public R endOrder(String orderId) {
        //结束异常订单只改订单状态，不用考虑其他
        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderMapper.selectOne(Wrappers.<ElectricityCabinetOrder>lambdaQuery().eq(ElectricityCabinetOrder::getOrderId, orderId).eq(ElectricityCabinetOrder::getTenantId, TenantContextHolder.getTenantId())
                .notIn(ElectricityCabinetOrder::getStatus, ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS, ElectricityCabinetOrder.ORDER_CANCEL, ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL));
        if (Objects.isNull(electricityCabinetOrder)) {
            log.error("ELECTRICITY  ERROR! not found order,orderId={} ", orderId);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }

        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.ORDER_CANCEL) || Objects
                .equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL)) {
            return R.fail("100230", "订单状态异常");
        }


        ElectricityCabinetOrder newElectricityCabinetOrder = new ElectricityCabinetOrder();
        newElectricityCabinetOrder.setId(electricityCabinetOrder.getId());
        newElectricityCabinetOrder.setOrderSeq(ElectricityCabinetOrder.STATUS_ORDER_EXCEPTION_CANCEL);
        newElectricityCabinetOrder.setStatus(ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL);
        newElectricityCabinetOrder.setUpdateTime(System.currentTimeMillis());
        electricityCabinetOrderMapper.updateById(newElectricityCabinetOrder);

        //回退月卡
        UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(electricityCabinetOrder.getUid());
        if (Objects.nonNull(userBatteryMemberCard)) {
            if (Objects.nonNull(userBatteryMemberCard.getMemberCardExpireTime()) && Objects.nonNull(userBatteryMemberCard.getRemainingNumber())
                    && userBatteryMemberCard.getMemberCardExpireTime() > System.currentTimeMillis() && userBatteryMemberCard.getRemainingNumber() != -1) {
                //回退月卡次数
                userBatteryMemberCardService.plusCount(userBatteryMemberCard.getUid());
            }
        }


        //删除开门失败缓存
        redisService.delete(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + orderId);

        return R.ok();
    }


    @Override
    public Integer homeOneCount(Long first, Long now, List<Integer> eleIdList, Integer tenantId) {
        return electricityCabinetOrderMapper.homeOneCount(first, now, eleIdList, tenantId);
    }

    @Slave
    @Override
    public BigDecimal homeOneSuccess(Long first, Long now, List<Integer> eleIdList, Integer tenantId) {
        Integer countTotal = homeOneCount(first, now, eleIdList, tenantId);
        Integer successTotal = electricityCabinetOrderMapper.homeOneSuccess(first, now, eleIdList, tenantId);
        if (successTotal == 0 || countTotal == 0) {
            return BigDecimal.valueOf(0);
        }
        return BigDecimal.valueOf(successTotal).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(countTotal), BigDecimal.ROUND_HALF_EVEN);
    }

    @Slave
    @Override
    public List<HashMap<String, String>> homeThree(long startTimeMilliDay, Long endTimeMilliDay, List<Integer> eleIdList, Integer tenantId) {
        return electricityCabinetOrderMapper.homeThree(startTimeMilliDay, endTimeMilliDay, eleIdList, tenantId);
    }

    @Override
    public Integer homeMonth(Long uid, Long first, Long now) {
        return electricityCabinetOrderMapper.selectCount(new LambdaQueryWrapper<ElectricityCabinetOrder>().between(ElectricityCabinetOrder::getCreateTime, first, now).eq(ElectricityCabinetOrder::getUid, uid));
    }

    @Override
    public Integer homeTotal(Long uid) {
        return electricityCabinetOrderMapper.selectCount(new LambdaQueryWrapper<ElectricityCabinetOrder>().eq(ElectricityCabinetOrder::getUid, uid));
    }


    @Override
    public ElectricityCabinetOrder queryByUid(Long uid) {
        return electricityCabinetOrderMapper.selectOne(new LambdaQueryWrapper<ElectricityCabinetOrder>().eq(ElectricityCabinetOrder::getUid, uid)
                .notIn(ElectricityCabinetOrder::getStatus, ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS, ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL, ElectricityCabinetOrder.ORDER_CANCEL)
                .orderByDesc(ElectricityCabinetOrder::getCreateTime).last("limit 0,1"));
    }

    @Override
    public ElectricityCabinetOrder queryByCellNoAndEleId(Integer eleId, Integer cellNo) {
        return electricityCabinetOrderMapper.selectOne(new LambdaQueryWrapper<ElectricityCabinetOrder>()
                .eq(ElectricityCabinetOrder::getElectricityCabinetId, eleId)
                .eq(ElectricityCabinetOrder::getOldCellNo, cellNo).or().eq(ElectricityCabinetOrder::getNewCellNo, cellNo)
                .orderByDesc(ElectricityCabinetOrder::getCreateTime).last("limit 0,1"));
    }

    @Override
    public String findUsableCellNo(Integer id) {
        List<ElectricityCabinetBox> usableBoxes = electricityCabinetBoxService.queryNoElectricityBatteryBox(id);
        if (!DataUtil.collectionIsUsable(usableBoxes)) {
            return null;
        }

        List<Integer> boxes = usableBoxes.stream().map(ElectricityCabinetBox::getCellNo).map(Integer::parseInt).sorted(Integer::compareTo).collect(Collectors.toList());

        //查看有没有初始化过设备的上次操作过的格挡,这里不必关心线程安全，不需要保证原子性
        if (!redisService.hasKey(CacheConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id)) {
            redisService.setNx(CacheConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id, boxes.get(0).toString());
        }

        String lastCellNo = redisService.get(CacheConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id);

        boxes = rebuildByCellCircleForDevice(boxes, Integer.parseInt(lastCellNo));

        for (Integer box : boxes) {
            if (redisService.setNx(CacheConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + id + "_" + box.toString(), "1", 300 * 1000L, false)) {
                redisService.set(CacheConstant.ELECTRICITY_CABINET_DEVICE_LAST_CELL + id, box.toString());
                return box.toString();
            }
        }

        return null;
    }

    @Override
    public R queryNewStatus(String orderId) {

        Map<String, Object> map = new HashMap<>();
        ElectricityCabinetOrder electricityCabinetOrder = electricityCabinetOrderMapper.selectOne(Wrappers.<ElectricityCabinetOrder>lambdaQuery().eq(ElectricityCabinetOrder::getOrderId, orderId));
        if (Objects.isNull(electricityCabinetOrder)) {
            log.error("ELECTRICITY  ERROR! not found order,orderId={} ", orderId);
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }

        String status = electricityCabinetOrder.getStatus();

        //订单状态旧门开门中
        if (electricityCabinetOrder.getOrderSeq() < ElectricityCabinetOrder.STATUS_INIT_BATTERY_CHECK_SUCCESS) {
            status = electricityCabinetOrder.getOldCellNo() + "号仓门开门中";
        }


        //订单状态新门开门中
        if (electricityCabinetOrder.getOrderSeq() > ElectricityCabinetOrder.STATUS_CHECK_OLD_AND_NEW
                && electricityCabinetOrder.getOrderSeq() < ElectricityCabinetOrder.STATUS_COMPLETE_OPEN_SUCCESS) {
            status = electricityCabinetOrder.getNewCellNo() + "号仓门开门中";
        }


        //旧电池开门成功
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_OPEN_SUCCESS)) {
            status = electricityCabinetOrder.getOldCellNo() + "号仓门开门成功，电池检测中";
        }


        //旧电池检测成功
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)) {
            status = "旧电池已存入," + electricityCabinetOrder.getNewCellNo() + "号仓门开门中";
        }

        //订单状态新门成功
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_OPEN_SUCCESS)) {
            status = electricityCabinetOrder.getNewCellNo() + "号仓门开门成功，电池检测中";
        }

        //订单状态新电池取走
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            status = "新电池已取走,订单完成";
        }

        //订单状态
        map.put("status", status);


        //页面图片显示
        Integer picture = 0;

        //return
        if (electricityCabinetOrder.getOrderSeq() < ElectricityCabinetOrder.STATUS_CHECK_OLD_AND_NEW) {
            picture = 1;
        }


        //rent
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)
                || Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_OPEN_SUCCESS)
                || Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_OPEN_SUCCESS)
                || Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            picture = 2;
        }


        //error
        if (electricityCabinetOrder.getOrderSeq().equals(ElectricityCabinetOrder.STATUS_ORDER_CANCEL)
                || electricityCabinetOrder.getOrderSeq().equals(ElectricityCabinetOrder.STATUS_ORDER_EXCEPTION_CANCEL)) {
            ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(electricityCabinetOrder.getTenantId());

            if (Objects.nonNull(electricityConfig) && Objects.equals(ElectricityConfig.ENABLE_SELF_OPEN, electricityConfig.getIsEnableSelfOpen())) {
                ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord = electricityExceptionOrderStatusRecordService.queryByOrderId(orderId);
                if (Objects.nonNull(electricityExceptionOrderStatusRecord) && Objects.equals(electricityExceptionOrderStatusRecord.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL)) {
                    map.put("selfOpenCell", ElectricityCabinetOrder.SELF_EXCHANGE_ELECTRICITY);
                }
            }

            picture = 3;
        }


        //订单状态
        map.put("picture", picture);

        //是否出错 0--未出错 1--出错
        Integer type = 0;
        //是否重试 0--重试  1--不能重试
        Integer isTry = 1;

        String result = redisService.get(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + orderId);
        if (StringUtils.isNotEmpty(result)) {
            WarnMsgVo warnMsgVo = JsonUtil.fromJson(result, WarnMsgVo.class);
            boolean isNeedEndOrder = warnMsgVo.getIsNeedEndOrder();
            if (!isNeedEndOrder) {
                isTry = 0;
            }

            String msg = warnMsgVo.getMsg();

            //出错信息
            map.put("queryStatus", msg);
            type = 1;
        }

        map.put("type", type);
        map.put("isTry", isTry);
        log.info("map is -->{}", map);
        return R.ok(map);
    }

    @Override
    public R selfOpenCell(OrderSelfOpenCellQuery orderSelfOpenCellQuery) {

        //用户
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("self open cell order  ERROR! not found user ");
            return R.fail("ELECTRICITY.0001", "未找到用户");
        }

        //是否存在未完成的租电池订单
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryByUidAndType(user.getUid());
        if (Objects.nonNull(rentBatteryOrder)) {
            if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
                return R.fail((Object) rentBatteryOrder.getOrderId(), "100023", "存在未完成租电订单，不能自助开仓");
            } else if (Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
                return R.fail((Object) rentBatteryOrder.getOrderId(), "100024", "存在未完成还电订单，不能自助开仓");
            }
        }

        ElectricityCabinetOrder oldElectricityCabinetOrder = queryByUid(user.getUid());
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            return R.fail((Object) oldElectricityCabinetOrder.getOrderId(), "100022", "存在未完成换电订单，不能自助开仓");
        }

        ElectricityCabinetOrder electricityCabinetOrder = queryByOrderId(orderSelfOpenCellQuery.getOrderId());
        if (Objects.isNull(electricityCabinetOrder)) {
            log.error("self open cell ERROR! not found order,orderId={} ", orderSelfOpenCellQuery.getOrderId());
            return R.fail("ELECTRICITY.0015", "未找到订单");
        }

        //换电柜
        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(electricityCabinetOrder.getElectricityCabinetId());
        if (Objects.isNull(electricityCabinet)) {
            log.error("self open cell ERROR! not found electricityCabinet ！electricityCabinetId={}", electricityCabinetOrder.getElectricityCabinetId());
            return R.fail("ELECTRICITY.0005", "未找到换电柜");
        }

        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            log.error("self open cell ERROR!  electricityCabinet is offline ！electricityCabinetId={}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0035", "换电柜不在线");
        }

        //换电柜营业时间
        boolean isBusiness = this.isBusiness(electricityCabinet);
        if (isBusiness) {
            return R.fail("ELECTRICITY.0017", "换电柜已打烊");
        }

//        //下单锁住柜机
//        boolean result = redisService.setNx(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId(), "1", 60 * 1000L, false);
//        if (!result) {
//            return R.fail("ELECTRICITY.00105", "该柜机有人正在下单，请稍等片刻");
//        }

        ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecord = electricityExceptionOrderStatusRecordService.queryByOrderId(orderSelfOpenCellQuery.getOrderId());

        Long now = System.currentTimeMillis();
        if (Objects.isNull(electricityExceptionOrderStatusRecord) || !Objects.equals(electricityExceptionOrderStatusRecord.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_FAIL)) {
            log.warn("SELF OPEN CELL WARN! not old cell exception,orderId={}", orderSelfOpenCellQuery.getOrderId());
            return R.fail("100020", "非旧仓门异常无法自主开仓");
        }

        if ((now - electricityExceptionOrderStatusRecord.getCreateTime()) / 1000 / 60 > 3) {
            log.warn("SELF OPEN CELL WARN! self open cell timeout,orderId={}", orderSelfOpenCellQuery.getOrderId());
            return R.fail("100026", "自助开仓已超开仓时间");
        }

        if (Objects.equals(electricityExceptionOrderStatusRecord.getIsSelfOpenCell(), ElectricityExceptionOrderStatusRecord.SELF_OPEN_CELL)) {
            log.warn("SELF OPEN CELL WARN! self open cell fail,orderId={}", orderSelfOpenCellQuery.getOrderId());
            return R.fail("100021", "该订单已进行自助开仓");
        }

        //查找换电柜门店
        if (Objects.isNull(electricityCabinet.getStoreId())) {
            log.error("self open cell order  ERROR! not found store ！electricityCabinetId={}", electricityCabinet.getId());
            return R.fail("ELECTRICITY.0097", "换电柜未绑定门店，不可用");
        }
        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.error("self open cell order  ERROR! not found store ！storeId={}", electricityCabinet.getStoreId());
            return R.fail("ELECTRICITY.0018", "未找到门店");
        }

        //查找门店加盟商
        if (Objects.isNull(store.getFranchiseeId())) {
            log.error("self open cell order  ERROR! not found Franchisee ！storeId={}", store.getId());
            return R.fail("ELECTRICITY.0098", "换电柜门店未绑定加盟商，不可用");
        }

        //校验用户
        UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("self open cell order  ERROR! not found user,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0019", "未找到用户");
        }

        //用户是否可用
        if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
            log.error("self open cell order ERROR! user is unUsable! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0024", "用户已被禁用");
        }

        //未实名认证
        if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
            log.error("self open cell order ERROR! user not auth,uid={}", user.getUid());
            return R.fail("ELECTRICITY.0041", "未实名认证");
        }

        //判断该换电柜加盟商和用户加盟商是否一致
        if (!Objects.equals(store.getFranchiseeId(), userInfo.getFranchiseeId())) {
            log.error("self open cell order  ERROR!FranchiseeId is not equal!uid={} , FranchiseeId1={} ,FranchiseeId2={}", user.getUid(), store.getFranchiseeId(), userInfo.getFranchiseeId());
            return R.fail("ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致，请联系客服处理");
        }

        //判断是否缴纳押金
        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.error("self open cell order  ERROR! not pay deposit,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0042", "未缴纳押金");
        }

        //未租电池
        if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.error("self open cell order  ERROR! user not rent battery,uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0033", "用户未绑定电池");
        }

        ElectricityCabinetBox electricityCabinetBox = electricityCabinetBoxService.queryByCellNo(electricityCabinetOrder.getElectricityCabinetId(), electricityExceptionOrderStatusRecord.getCellNo() + "");
        if (Objects.isNull(electricityCabinetBox)) {
            log.error("self open cell order  ERROR! not find cellNO! uid={} ", user.getUid());
            return R.fail("ELECTRICITY.0006", "未找到此仓门");
        }

        try {
            ElectricityExceptionOrderStatusRecord electricityExceptionOrderStatusRecordUpdate = new ElectricityExceptionOrderStatusRecord();
            electricityExceptionOrderStatusRecordUpdate.setId(electricityExceptionOrderStatusRecord.getId());
            electricityExceptionOrderStatusRecordUpdate.setUpdateTime(System.currentTimeMillis());
            electricityExceptionOrderStatusRecordUpdate.setIsSelfOpenCell(ElectricityExceptionOrderStatusRecord.SELF_OPEN_CELL);
            electricityExceptionOrderStatusRecordService.update(electricityExceptionOrderStatusRecordUpdate);

            ElectricityCabinetOrderOperHistory history = ElectricityCabinetOrderOperHistory.builder()
                    .createTime(System.currentTimeMillis())
                    .orderId(orderSelfOpenCellQuery.getOrderId())
                    .tenantId(electricityCabinet.getTenantId())
                    .msg("旧电池检测失败，自助开仓")
                    .seq(ElectricityCabinetOrderOperHistory.SELF_OPEN_CELL_SEQ)
                    .type(ElectricityCabinetOrderOperHistory.ORDER_TYPE_EXCHANGE)
                    .result(ElectricityCabinetOrderOperHistory.OPERATE_RESULT_SUCCESS).build();
            electricityCabinetOrderOperHistoryService.insert(history);

            ElectricityCabinetOrder electricityCabinetOrderUpdate = new ElectricityCabinetOrder();
            electricityCabinetOrderUpdate.setId(electricityCabinetOrder.getId());
            electricityCabinetOrderUpdate.setUpdateTime(System.currentTimeMillis());
            electricityCabinetOrderUpdate.setRemark("自助开仓");
            update(electricityCabinetOrderUpdate);

            //发送自助开仓命令
            //发送命令
            HashMap<String, Object> dataMap = Maps.newHashMap();
            dataMap.put("orderId", orderSelfOpenCellQuery.getOrderId());
            dataMap.put("cellNo", electricityExceptionOrderStatusRecord.getCellNo());
            dataMap.put("batteryName", electricityCabinetOrder.getOldElectricityBatterySn());

            String sessionId = CacheConstant.ELE_OPERATOR_SESSION_PREFIX + "-" + System.currentTimeMillis() + ":" + electricityCabinetOrder.getId();

            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(sessionId)
                    .data(dataMap)
                    .productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName())
                    .command(ElectricityIotConstant.SELF_OPEN_CELL).build();
            eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
            return R.ok(sessionId);
        } catch (Exception e) {
            log.error("order is error" + e);
            return R.fail("ELECTRICITY.0025", "自助开仓失败");
        } finally {
            redisService.delete(CacheConstant.ELECTRICITY_CABINET_CACHE_OCCUPY_CELL_NO_KEY + electricityCabinetOrder.getElectricityCabinetId() + "_" + electricityExceptionOrderStatusRecord.getCellNo());
        }

    }

    @Override
    public R checkOpenSessionId(String sessionId) {
        String s = redisService.get(CacheConstant.ELE_OPERATOR_SELF_OPEN_CEE_CACHE_KEY + sessionId);
        if (StrUtil.isEmpty(s)) {
            return R.ok("0001");
        }
        if ("true".equalsIgnoreCase(s)) {
            return R.ok("0002");
        } else {
            return R.ok("0003");
        }
    }

    public static List<Integer> rebuildByCellCircleForDevice(List<Integer> cellNos, Integer lastCellNo) {

        if (cellNos.get(0) > lastCellNo) {
            return cellNos;
        }

        int index = 0;

        for (int i = 0; i < cellNos.size(); i++) {
            if (cellNos.get(i) > lastCellNo) {
                index = i;
                break;
            }

            if (cellNos.get(i).equals(lastCellNo)) {
                index = i + 1;
                break;
            }
        }

        List<Integer> firstSegmentList = cellNos.subList(0, index);
        List<Integer> twoSegmentList = cellNos.subList(index, cellNos.size());

        ArrayList<Integer> resultList = com.google.common.collect.Lists.newArrayList();
        resultList.addAll(twoSegmentList);
        resultList.addAll(firstSegmentList);

        return resultList;
    }

    @Deprecated
    public String generateOrderId(Integer id, String cellNo, Long uid) {
        return String.valueOf(System.currentTimeMillis()).substring(2) + id +
                cellNo + uid;
    }

    public Long getTime(Long time) {
        Date date1 = new Date(time);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = dateFormat.format(date1);
        Date date2 = null;
        try {
            date2 = dateFormat.parse(format);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Long ts = date2.getTime();
        return time - ts;
    }

    public boolean isBusiness(ElectricityCabinet electricityCabinet) {
        //营业时间
        if (Objects.nonNull(electricityCabinet.getBusinessTime())) {
            String businessTime = electricityCabinet.getBusinessTime();
            if (!Objects.equals(businessTime, ElectricityCabinetVO.ALL_DAY)) {
                int index = businessTime.indexOf("-");
                if (!Objects.equals(index, -1) && index > 0) {
                    Long firstToday = DateUtil.beginOfDay(new Date()).getTime();
                    long now = System.currentTimeMillis();
                    Long totalBeginTime = Long.valueOf(businessTime.substring(0, index));
                    Long beginTime = getTime(totalBeginTime);
                    Long totalEndTime = Long.valueOf(businessTime.substring(index + 1));
                    Long endTime = getTime(totalEndTime);
                    if (firstToday + beginTime > now || firstToday + endTime < now) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Triple<Boolean, String, Object> orderV2(OrderQueryV2 orderQuery) {
        TokenUser user = SecurityUtils.getUserInfo();
        if (Objects.isNull(user)) {
            log.error("ORDER ERROR!  not found user,eid={}", orderQuery.getEid());
            return Triple.of(false, "100001", "未能找到用户");
        }

        Triple<Boolean, String, Object> checkExistsOrderResult = checkUserExistsUnFinishOrder(user.getUid());
        if (checkExistsOrderResult.getLeft()) {
            log.warn("ORDER WARN! user exists unFinishOrder! uid={}", user.getUid());
            return Triple.of(false, checkExistsOrderResult.getMiddle(), checkExistsOrderResult.getRight());
        }

        ElectricityCabinet electricityCabinet = electricityCabinetService.queryByIdFromCache(orderQuery.getEid());
        if (Objects.isNull(electricityCabinet)) {
            return Triple.of(false, "100003", "柜机不存在");
        }

        //换电柜是否打烊
        boolean isBusiness = this.isBusiness(electricityCabinet);
        if (isBusiness) {
            return Triple.of(false, "100203", "换电柜已打烊");
        }

        //换电柜是否在线
        boolean eleResult = electricityCabinetService.deviceIsOnline(electricityCabinet.getProductKey(), electricityCabinet.getDeviceName());
        if (!eleResult) {
            return Triple.of(false, "100004", "柜机不在线");
        }

        //这里加柜机的缓存，为了限制不同时分配格挡
        if (!redisService.setNx(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId(), "1", 5 * 1000L, false)) {
            return Triple.of(false, "100214", "柜机正在被使用，请稍后");
        }

        if (!redisService.setNx(CacheConstant.ORDER_TIME_UID + user.getUid(), "1", 5 * 1000L, false)) {
            return Triple.of(false, "100002", "下单过于频繁");
        }

        try {
            Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
            if (Objects.isNull(store)) {
                log.error("ORDER ERROR!  not found store ！uid={},eid={},storeId={}", user.getUid(), electricityCabinet.getId(), electricityCabinet.getStoreId());
                return Triple.of(false, "100204", "未找到门店");
            }

            //校验用户
            UserInfo userInfo = userInfoService.queryByUidFromCache(user.getUid());
            if (Objects.isNull(userInfo)) {
                log.error("ORDER ERROR! not found user info,uid={} ", user.getUid());
                return Triple.of(false, "100205", "未找到用户审核信息");
            }

            //用户是否可用
            if (Objects.equals(userInfo.getUsableStatus(), UserInfo.USER_UN_USABLE_STATUS)) {
                log.error("ORDER ERROR! user is unUsable,uid={} ", user.getUid());
                return Triple.of(false, "ELECTRICITY.0024", "用户已被禁用");
            }

            if (!Objects.equals(userInfo.getAuthStatus(), UserInfo.AUTH_STATUS_REVIEW_PASSED)) {
                log.error("ORDER ERROR! userinfo is UN AUTH! uid={}", user.getUid());
                return Triple.of(false, "100206", "用户未审核");
            }

            //校验加盟商是否一致(加盟商迁移)
            if (!Objects.equals(userInfo.getFranchiseeId(), store.getFranchiseeId())) {
                log.error("ORDER ERROR! user franchiseeId not equals store franchiseeId,uid={},storeId={}", user.getUid(), store.getId());
                return Triple.of(false, "ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致");
            }

            Franchisee franchisee = franchiseeService.queryByIdFromCache(userInfo.getFranchiseeId());
            if (Objects.isNull(franchisee)) {
                log.error("ORDER ERROR! not found franchisee,uid={}", user.getUid());
                return Triple.of(false, "ELECTRICITY.0038", "加盟商不存在");
            }

//            ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(user.getUid());
//            if(Objects.isNull(electricityBattery)){
//                log.error("ORDER ERROR! not found franchisee,uid={}", user.getUid());
//                return Triple.of(false, "100292", "用户未绑定电池");
//            }

            //判断用户押金
            Triple<Boolean, String, Object> checkUserDepositResult = checkUserDeposit(userInfo, store, user);
            if (Boolean.FALSE.equals(checkUserDepositResult.getLeft())) {
                return checkUserDepositResult;
            }

            //判断用户套餐
            UserBatteryMemberCard userBatteryMemberCard = userBatteryMemberCardService.selectByUidFromCache(userInfo.getUid());
            if (Objects.isNull(userBatteryMemberCard)) {
                log.warn("ORDER WARN! user haven't memberCard uid={}", userInfo.getUid());
                return Triple.of(false, "100210", "用户未开通套餐");
            }
//            Triple<Boolean, String, Object> checkUserMemberCardResult = checkUserMemberCard(userBatteryMemberCard, user);
//            if (Boolean.FALSE.equals(checkUserMemberCardResult.getLeft())) {
//                return checkUserMemberCardResult;
//            }

            BatteryMemberCard batteryMemberCard = batteryMemberCardService.queryByIdFromCache(userBatteryMemberCard.getMemberCardId());
            if(Objects.isNull(batteryMemberCard)){
                log.error("ORDER ERROR! not found batteryMemberCard,uid={},mid={}", userInfo.getUid(),userBatteryMemberCard.getMemberCardId());
                return Triple.of(false, "ELECTRICITY.00121","套餐不存在");
            }

            if (userBatteryMemberCard.getMemberCardExpireTime() < System.currentTimeMillis() || (Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT) && userBatteryMemberCard.getRemainingNumber() <= 0)) {
                log.error("RENTBATTERY ERROR! battery memberCard is Expire,uid={}", userInfo.getUid());
                return Triple.of(false, "ELECTRICITY.0023", "套餐已过期");
            }

            //判断用户电池服务费
            Triple<Boolean,Integer,BigDecimal> acquireUserBatteryServiceFeeResult = serviceFeeUserInfoService.acquireUserBatteryServiceFee(userInfo, userBatteryMemberCard, batteryMemberCard, serviceFeeUserInfoService.queryByUidFromCache(userInfo.getUid()));
            if (Boolean.TRUE.equals(acquireUserBatteryServiceFeeResult.getLeft())) {
                log.warn("ORDER WARN! user exist battery service fee,uid={}", user.getUid());
                return Triple.of(false,"ELECTRICITY.100000", "存在电池服务费");
            }
    
            //判断车电关联是否可换电
            ElectricityConfig electricityConfig = electricityConfigService
                    .queryFromCacheByTenantId(userInfo.getTenantId());
            if (Objects.nonNull(electricityConfig) && Objects
                    .equals(electricityConfig.getIsOpenCarBatteryBind(), ElectricityConfig.ENABLE_CAR_BATTERY_BIND)) {
                UserCarMemberCard userCarMemberCard = userCarMemberCardService.selectByUidFromCache(userInfo.getUid());
                Triple<Boolean, String, Object> checkUserCarMemberCardResult = checkUserCarMemberCard(userCarMemberCard,
                        userInfo);
                if (Boolean.FALSE.equals(checkUserCarMemberCardResult.getLeft())) {
                    return checkUserCarMemberCardResult;
                }
            }

            //默认是小程序下单
            if (Objects.isNull(orderQuery.getSource())) {
                orderQuery.setSource(OrderQuery.SOURCE_WX_MP);
            }

            Pair<Boolean, Integer> usableEmptyCellNo = electricityCabinetService.findUsableEmptyCellNo(electricityCabinet.getId());
            if (Boolean.FALSE.equals(usableEmptyCellNo.getLeft())) {
                return Triple.of(false, "100215", "当前无空余格挡可供换电，请联系客服！");
            }

            ElectricityBattery electricityBattery = electricityBatteryService.queryByUid(user.getUid());
            Triple<Boolean, String, Object> usableBatteryCellNoResult = electricityCabinetService.findUsableBatteryCellNoV3(electricityCabinet.getId(), franchisee , electricityCabinet.getFullyCharged(), electricityBattery, userInfo.getUid());
            if (Boolean.FALSE.equals(usableBatteryCellNoResult.getLeft())) {
                return Triple.of(false, usableBatteryCellNoResult.getMiddle(), usableBatteryCellNoResult.getRight());
            }

            //修改按此套餐的次数
            Triple<Boolean, String, String> modifyResult = checkAndModifyMemberCardCount(userBatteryMemberCard, batteryMemberCard);
            if (Boolean.FALSE.equals(modifyResult.getLeft())) {
                return Triple.of(false, modifyResult.getMiddle(), modifyResult.getRight());
            }
    
            ElectricityCabinetBox electricityCabinetBox = (ElectricityCabinetBox) usableBatteryCellNoResult.getRight();
    
            ElectricityCabinetOrder electricityCabinetOrder = ElectricityCabinetOrder.builder()
                    .orderId(generateExchangeOrderId(user.getUid()))
                    .uid(user.getUid())
                    .phone(userInfo.getPhone())
                    .electricityCabinetId(orderQuery.getEid())
                    .oldCellNo(usableEmptyCellNo.getRight())
                    .newCellNo(Integer.parseInt(electricityCabinetBox.getCellNo()))
                    .orderSeq(ElectricityCabinetOrder.STATUS_INIT)
                    .status(ElectricityCabinetOrder.INIT)
                    .source(orderQuery.getSource())
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis())
                    .storeId(electricityCabinet.getStoreId())
                    .franchiseeId(store.getFranchiseeId())
                    .tenantId(TenantContextHolder.getTenantId()).build();

            electricityCabinetOrderMapper.insert(electricityCabinetOrder);
    
            //记录活跃时间
            userActiveInfoService.userActiveRecord(userInfo);
            
            HashMap<String, Object> commandData = Maps.newHashMap();
            commandData.put("orderId", electricityCabinetOrder.getOrderId());
            commandData.put("placeCellNo", electricityCabinetOrder.getOldCellNo());
            commandData.put("takeCellNo", electricityCabinetOrder.getNewCellNo());
            commandData.put("phone", user.getPhone());

            if (Objects.nonNull(electricityConfig) && Objects.equals(electricityConfig.getIsBatteryReview(), ElectricityConfig.BATTERY_REVIEW)) {
                commandData.put("userBindingBatterySn", Objects.isNull(electricityBattery) ? "UNKNOWN" : electricityBattery.getSn());
            }

            if (Objects.equals(franchisee.getModelType(), Franchisee.NEW_MODEL_TYPE)) {
                commandData.put("multiBatteryModelName", Objects.isNull(electricityBattery) ? "UNKNOWN" : electricityBattery.getModel());
            }

            HardwareCommandQuery comm = HardwareCommandQuery.builder()
                    .sessionId(CacheConstant.ELE_OPERATOR_SESSION_PREFIX + ":" + electricityCabinetOrder.getOrderId())
                    .data(commandData)
                    .productKey(electricityCabinet.getProductKey())
                    .deviceName(electricityCabinet.getDeviceName())
                    .command(ElectricityIotConstant.ELE_COMMAND_NEW_EXCHANGE_ORDER).build();
            Pair<Boolean, String> result = eleHardwareHandlerManager.chooseCommandHandlerProcessSend(comm);
            if (Boolean.FALSE.equals(result.getLeft())) {
                return Triple.of(false, "100218", "下单消息发送失败");
            }
            return Triple.of(true, null, electricityCabinetOrder.getOrderId());
        } finally {
            redisService.delete(CacheConstant.ORDER_ELE_ID + electricityCabinet.getId());
            redisService.delete(CacheConstant.ORDER_TIME_UID + user.getUid());
        }
    }
    
    private Triple<Boolean, String, Object> checkUserCarMemberCard(UserCarMemberCard userCarMemberCard, UserInfo user) {
    
        //用户未缴纳押金可直接换电
        UserCarDeposit userCarDeposit = userCarDepositService.selectByUidFromCache(user.getUid());
        if (Objects.isNull(userCarDeposit)) {
            return Triple.of(true, null, null);
        }
    
        //用户未缴纳押金可直接换电
        if (!Objects.equals(user.getCarDepositStatus(), UserInfo.CAR_DEPOSIT_STATUS_YES)) {
            return Triple.of(true, null, null);
        }
    
        //用户从未买过车辆套餐则可直接换电
        if (Objects.isNull(userCarMemberCard) || Objects.isNull(userCarMemberCard.getMemberCardExpireTime()) || Objects
                .equals(userCarMemberCard.getMemberCardExpireTime(), 0L)) {
            return Triple.of(false, "100232", "未购买租车套餐");
        }
        
        //套餐是否可用
        long now = System.currentTimeMillis();
        if (userCarMemberCard.getMemberCardExpireTime() < now) {
            log.error("ORDER ERROR! user's carMemberCard is expire! uid={} cardId={}", user.getUid(),
                    userCarMemberCard.getCardId());
            return Triple.of(false, "100233", "租车套餐已过期");
        }
        return Triple.of(true, null, null);
    }
    
    private String generateExchangeOrderId(Long uid) {
        return String.valueOf(uid) + System.currentTimeMillis() / 1000 + RandomUtil.randomNumbers(3);
    }

    @Override
    public Triple<Boolean, String, String> checkAndModifyMemberCardCount(UserBatteryMemberCard userBatteryMemberCard, BatteryMemberCard batteryMemberCard) {

        if (Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER) || Objects.equals(batteryMemberCard.getLimitCount(), BatteryMemberCard.LIMIT)) {
            Integer row = userBatteryMemberCardService.minCount(userBatteryMemberCard);
            if (row < 1) {
                log.error("ORDER ERROR! memberCard's count modify fail, uid={} ,mid={}", userBatteryMemberCard.getUid(), userBatteryMemberCard.getId());
                return Triple.of(false, "100213", "套餐剩余次数不足");
            }
        }
        return Triple.of(true, null, null);
    }


    private Triple<Boolean, String, Object> checkUserDeposit(UserInfo userInfo, Store store, TokenUser user) {
        if (Objects.isNull(userInfo.getFranchiseeId())) {
            log.error("ORDER ERROR! not found franchiseeUser! uid={}", user.getUid());
            return Triple.of(false, "100207", "用户加盟商信息未找到");
        }

        if (!Objects.equals(store.getFranchiseeId(), userInfo.getFranchiseeId())) {
            log.error("ORDER ERROR! store's fId  is not equal franchieseeId uid={} , store's fid={} ,fid={}", user.getUid(), store.getFranchiseeId(), userInfo.getFranchiseeId());
            return Triple.of(false, "100208", "柜机加盟商和用户加盟商不一致，请联系客服处理");
        }

        if (!Objects.equals(userInfo.getBatteryDepositStatus(), UserInfo.BATTERY_DEPOSIT_STATUS_YES)) {
            log.warn("ORDER WARN! user didn't pay a deposit,uid={},fid={}", user.getUid(), userInfo.getFranchiseeId());
            return Triple.of(false, "100209", "用户未缴纳押金");
        }

        if (!Objects.equals(userInfo.getBatteryRentStatus(), UserInfo.BATTERY_RENT_STATUS_YES)) {
            log.warn("ORDER WARN! user not rent battery! uid={}", user.getUid());
            return Triple.of(false, "100222", "用户还没有租借电池");
        }
        return Triple.of(true, null, null);
    }

    @Deprecated
    private Triple<Boolean, String, Object> checkUserMemberCard(UserBatteryMemberCard userBatteryMemberCard, TokenUser user) {
        if (Objects.isNull(userBatteryMemberCard) || Objects.isNull(userBatteryMemberCard.getMemberCardExpireTime())
                || Objects.isNull(userBatteryMemberCard.getRemainingNumber())) {
            log.warn("ORDER WARN! user haven't memberCard uid={}", user.getUid());
            return Triple.of(false, "100210", "用户未开通套餐");
        }

        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE)) {
            log.warn("ORDER WARN! user's member card is stop! uid={}", user.getUid());
            return Triple.of(false, "100211", "用户套餐已暂停");
        }

        //套餐是否可用
        long now = System.currentTimeMillis();
        if (userBatteryMemberCard.getMemberCardExpireTime() < now) {
            log.warn("ORDER WARN! user's member card is expire! uid={} cardId={}", user.getUid(), userBatteryMemberCard.getMemberCardId());
            return Triple.of(false, "100212", "用户套餐已过期");
        }

        //如果用户不是送的套餐
        ElectricityMemberCard electricityMemberCard = electricityMemberCardService.queryByCache(userBatteryMemberCard.getMemberCardId().intValue());
        if (!Objects.equals(userBatteryMemberCard.getMemberCardId(), UserBatteryMemberCard.SEND_REMAINING_NUMBER)) {
            if (Objects.equals(electricityMemberCard.getLimitCount(), ElectricityMemberCard.LIMITED_COUNT_TYPE) && userBatteryMemberCard.getRemainingNumber() < 0) {
                log.warn("ORDER ERROR! user's count < 0 ,uid={},cardId={}", user.getUid(), electricityMemberCard.getType());
                return Triple.of(false, "100213", "用户套餐剩余次数不足");
            }
        }
        return Triple.of(true, null, null);
    }

    private Triple<Boolean, String, Object> checkUserBatteryServiceFee(UserBatteryMemberCard userBatteryMemberCard, UserInfo userInfo, TokenUser user, ServiceFeeUserInfo serviceFeeUserInfo, Franchisee franchisee) {

        if (Objects.isNull(serviceFeeUserInfo) || Objects.isNull(serviceFeeUserInfo.getServiceFeeGenerateTime())) {
            return Triple.of(true, null, null);
        }

        Long now = System.currentTimeMillis();
        //这里开始计费用户套餐过期电池服务费
        long cardDays = (now - serviceFeeUserInfo.getServiceFeeGenerateTime()) / 1000L / 60 / 60 / 24;
        BigDecimal userChangeServiceFee = electricityMemberCardOrderService.checkUserMemberCardExpireBatteryService(userInfo, franchisee, cardDays);

        //判断用户是否产生停卡电池服务费
        if (Objects.equals(userBatteryMemberCard.getMemberCardStatus(), UserBatteryMemberCard.MEMBER_CARD_DISABLE) || Objects.nonNull(userBatteryMemberCard.getDisableMemberCardTime())) {
            cardDays = (now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60 / 24;
            //不足一天按一天计算
            double time = Math.ceil((now - userBatteryMemberCard.getDisableMemberCardTime()) / 1000L / 60 / 60.0);
            if (time < 24) {
                cardDays = 1;
            }
            userChangeServiceFee = electricityMemberCardOrderService.checkUserDisableCardBatteryService(userInfo, user.getUid(), cardDays, null, serviceFeeUserInfo);
        }
        if (BigDecimal.valueOf(0).compareTo(userChangeServiceFee) != 0) {
            return Triple.of(false, "100220", "用户存在电池服务费");
        }
        return Triple.of(true, null, null);


    }


    private Triple<Boolean, String, Object> checkUserExistsUnFinishOrder(Long uid) {
        RentBatteryOrder rentBatteryOrder = rentBatteryOrderService.queryByUidAndType(uid);
        if (Objects.nonNull(rentBatteryOrder) && Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RENT)) {
            return Triple.of(true, "100200", new ExchangeUnFinishOrderVo(rentBatteryOrder.getOrderId()));
        } else if (Objects.nonNull(rentBatteryOrder) && Objects.equals(rentBatteryOrder.getType(), RentBatteryOrder.TYPE_USER_RETURN)) {
            return Triple.of(true, "100202", new ExchangeUnFinishOrderVo(rentBatteryOrder.getOrderId()));
        }

        //是否存在未完成的换电订单
        ElectricityCabinetOrder oldElectricityCabinetOrder = queryByUid(uid);
        if (Objects.nonNull(oldElectricityCabinetOrder)) {
            return Triple.of(true, "100201", new ExchangeUnFinishOrderVo(oldElectricityCabinetOrder.getOrderId()));
        }

        return Triple.of(false, null, null);
    }

    @Override
    public Triple<Boolean, String, Object> queryOrderStatusForShow(String orderId) {
        ElectricityCabinetOrder electricityCabinetOrder = queryByOrderId(orderId);
        if (Objects.isNull(electricityCabinetOrder)) {
            log.error("ORDER ERROR! query order not found,uid={},orderId={}", SecurityUtils.getUid(), orderId);
            return Triple.of(false, "100221", "未能查找到订单");
        }

        String status = electricityCabinetOrder.getStatus();
        ExchangeOrderMsgShowVO showVo = new ExchangeOrderMsgShowVO();
        showVo.setType(ExchangeOrderMsgShowVO.TYPE_SUCCESS);

        if (isOpenPlaceCellStatus(status)) {
            showVo.setStatus(electricityCabinetOrder.getOldCellNo() + "号仓门开门中");
        }

        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_OPEN_SUCCESS)) {
            showVo.setStatus(electricityCabinetOrder.getOldCellNo() + "号仓门开门成功，电池检测中");
        }

        //旧电池检测成功
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)) {
            showVo.setStatus("旧电池已存入," + electricityCabinetOrder.getNewCellNo() + "号仓门开门中");
        }

        //订单状态新门成功
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_OPEN_SUCCESS)) {
            showVo.setStatus(electricityCabinetOrder.getNewCellNo() + "号仓门开门成功，电池检测中");
        }

        //订单状态新电池取走
        if (Objects.equals(electricityCabinetOrder.getStatus(), ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)) {
            showVo.setStatus("新电池已取走,订单完成");
        }

        if (isPlaceBatteryAllStatus(status)) {
            showVo.setPicture(ExchangeOrderMsgShowVO.PLACE_BATTERY_IMG);
        }

        if (isTakeBatteryAllStatus(status)) {
            showVo.setPicture(ExchangeOrderMsgShowVO.TAKE_BATTERY_IMG);
        }

        if (isExceptionOrder(status)) {
            showVo.setPicture(ExchangeOrderMsgShowVO.EXCEPTION_IMG);
            //检查这里是否需要自助开仓
            checkIsNeedSelfOpenCell(electricityCabinetOrder, showVo);
            showVo.setType(ExchangeOrderMsgShowVO.TYPE_FAIL);
            showVo.setStatus(redisService.get(CacheConstant.ELE_ORDER_WARN_MSG_CACHE_KEY + orderId));
        }
        return Triple.of(true, null, showVo);
    }

    @Override
    public ElectricityCabinetOrder selectLatestByUid(Long uid, Integer tenantId) {
        return electricityCabinetOrderMapper.selectLatestByUid(uid, tenantId);
    }

    @Override
    public Triple<Boolean, String, Object> bluetoothExchangeCheck(String productKey, String deviceName) {
        UserInfo userInfo = userInfoService.queryByUidFromCache(SecurityUtils.getUid());
        if (Objects.isNull(userInfo)) {
            log.error("BLUETOOTH EXCHANGE ERROR! not found userInfo,uid={}", SecurityUtils.getUid());
            return Triple.of(false, "100001", "未能找到用户");
        }

        ElectricityCabinet electricityCabinet = electricityCabinetService.queryFromCacheByProductAndDeviceName(productKey, deviceName);
        if (Objects.isNull(electricityCabinet)) {
            log.error("BLUETOOTH EXCHANGE ERROR! not found electricityCabinet,p={},d={}", productKey, deviceName);
            return Triple.of(false, "100003", "柜机不存在");
        }

        Store store = storeService.queryByIdFromCache(electricityCabinet.getStoreId());
        if (Objects.isNull(store)) {
            log.error("BLUETOOTH EXCHANGE ERROR! not found store,eid={}", electricityCabinet.getId());
            return Triple.of(false, "100003", "柜机不存在");
        }

        if (!Objects.equals(store.getFranchiseeId(), userInfo.getFranchiseeId())) {
            log.error("BLUETOOTH EXCHANGE ERROR! user franchiseeId not equals store franchiseeId,uid={},storeId={}", userInfo.getFranchiseeId(), store.getId());
            return Triple.of(false, "ELECTRICITY.0096", "换电柜加盟商和用户加盟商不一致");
        }

        return Triple.of(true, null, null);
    }

    @Slave
    @Override
    public List<ElectricityCabinetOrder> selectTodayExchangeOrder(Integer eid, long todayStartTimeStamp, long todayEndTimeStamp, Integer tenantId) {
        return electricityCabinetOrderMapper.selectTodayExchangeOrder(eid, todayStartTimeStamp, todayEndTimeStamp, tenantId);
    }

    @Slave
    @Override
    public List<ElectricityCabinetOrder> selectMonthExchangeOrders(Integer eid, long todayStartTimeStamp, long todayEndTimeStamp, Integer tenantId) {
        return electricityCabinetOrderMapper.selectMonthExchangeOrders(eid, todayStartTimeStamp, todayEndTimeStamp,tenantId);
    }

    private void checkIsNeedSelfOpenCell(ElectricityCabinetOrder electricityCabinetOrder, ExchangeOrderMsgShowVO showVo) {
        ElectricityConfig electricityConfig = electricityConfigService.queryFromCacheByTenantId(electricityCabinetOrder.getTenantId());
        if (Objects.isNull(electricityConfig) || Objects.equals(ElectricityConfig.DISABLE_SELF_OPEN, electricityConfig.getIsEnableSelfOpen())) {
            return;
        }

        ElectricityExceptionOrderStatusRecord statusRecord = electricityExceptionOrderStatusRecordService.queryByOrderId(electricityCabinetOrder.getOrderId());
        if (Objects.isNull(statusRecord)) {
            return;
        }

        showVo.setSelfOpenCell(ElectricityCabinetOrder.SELF_EXCHANGE_ELECTRICITY);

    }

    private boolean isExceptionOrder(String status) {
        return status.equals(ElectricityCabinetOrder.ORDER_CANCEL)
                || status.equals(ElectricityCabinetOrder.ORDER_EXCEPTION_CANCEL);
    }

    private boolean isTakeBatteryAllStatus(String status) {
        return status.equals(ElectricityCabinetOrder.COMPLETE_OPEN_SUCCESS)
                || status.equals(ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)
                || status.equals(ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS);
    }

    private boolean isPlaceBatteryAllStatus(String status) {
        return status.equals(ElectricityCabinetOrder.INIT)
                || status.equals(ElectricityCabinetOrder.INIT_OPEN_SUCCESS);
    }

    private boolean isOpenPlaceCellStatus(String status) {
        return status.equals(ElectricityCabinetOrder.INIT_BATTERY_CHECK_SUCCESS)
                || status.equals(ElectricityCabinetOrder.COMPLETE_OPEN_SUCCESS)
                || status.equals(ElectricityCabinetOrder.COMPLETE_BATTERY_TAKE_SUCCESS)
                || status.equals(ElectricityCabinetOrder.INIT);
    }
}

package com.xiliulou.electricity.service.impl;

import com.google.api.client.util.Lists;
import com.xiliulou.core.thread.XllThreadPoolExecutorService;
import com.xiliulou.core.thread.XllThreadPoolExecutors;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.query.ElectricityCabinetQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.utils.DateUtils;
import com.xiliulou.electricity.vo.EleCabinetDataAnalyseVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author zzlong
 * @email zhaozhilong@xiliulou.com
 * @date 2023-04-18-9:12
 */
@Service("eleCabinetDataAnalyseService")
@Slf4j
public class EleCabinetDataAnalyseServiceImpl implements EleCabinetDataAnalyseService {

    private static final XllThreadPoolExecutorService DATA_ANALYSE_THREAD_POOL = XllThreadPoolExecutors.newFixedThreadPool("DATA-ANALYSE-THREAD-POOL", 4, "dataAnalyseThread:");

    @Autowired
    private ElectricityCabinetService eleCabinetService;
    @Autowired
    private ElectricityCabinetModelService eleCabinetModelService;
    @Autowired
    private ElectricityCabinetServerService eleCabinetServerService;
    @Autowired
    private EleCabinetCoreDataService eleCabinetCoreDataService;
    @Autowired
    private FranchiseeService franchiseeService;
    @Autowired
    private ElectricityCabinetPowerService eleCabinetPowerService;
    @Autowired
    private ElectricityBatteryService electricityBatteryService;
    @Autowired
    private ElectricityCabinetBoxService electricityCabinetBoxService;
    @Autowired
    private ElectricityCabinetOrderService eleCabinetOrderService;
    @Autowired
    private StoreService storeService;
    @Autowired
    private EleWarnMsgService eleWarnMsgService;


    @Override
    public List<EleCabinetDataAnalyseVO> selectOfflineByPage(ElectricityCabinetQuery cabinetQuery) {
        List<EleCabinetDataAnalyseVO> electricityCabinetList = eleCabinetService.selecteleCabinetVOByQuery(cabinetQuery);
        if (CollectionUtils.isEmpty(electricityCabinetList)) {
            return Collections.emptyList();
        }

        return buildEleCabinetDataAnalyseVOs(electricityCabinetList);
    }

    @Override
    public List<EleCabinetDataAnalyseVO> selectLockPage(ElectricityCabinetQuery cabinetQuery) {
        List<EleCabinetDataAnalyseVO> electricityCabinetList = eleCabinetService.selectLockCellByQuery(cabinetQuery);
        if (CollectionUtils.isEmpty(electricityCabinetList)) {
            return Collections.emptyList();
        }

        return buildEleCabinetDataAnalyseVOs(electricityCabinetList);
    }

    @Override
    public List<EleCabinetDataAnalyseVO> selectFailurePage(ElectricityCabinetQuery cabinetQuery) {
        //故障编码
        List<Integer> failureList = new ArrayList<>();

        //从clickhouse查询所有有故障柜机
        List<Integer> cabinetFailureIds = eleWarnMsgService.selectEidByCabinetFailureList(failureList);
        List<Integer> cellFailureIds = eleWarnMsgService.selectEidByCellFailureList(failureList);

        Set<Integer> totalEid = new HashSet<>();
        totalEid.addAll(cabinetFailureIds);
        totalEid.addAll(cellFailureIds);
        if (CollectionUtils.isEmpty(totalEid)) {
            return Collections.emptyList();
        }

        cabinetQuery.setEleIdList(Lists.newArrayList(totalEid));

        //分页查询MySQL柜机数据
        List<EleCabinetDataAnalyseVO> electricityCabinetList = eleCabinetService.selecteleCabinetVOByQuery(cabinetQuery);
        if (CollectionUtils.isEmpty(electricityCabinetList)) {
            return Collections.emptyList();
        }

        return buildEleCabinetDataAnalyseVOs(electricityCabinetList);
    }

    @Override
    public List<EleCabinetDataAnalyseVO> selectPowerPage(ElectricityCabinetQuery cabinetQuery) {
        List<EleCabinetDataAnalyseVO> electricityCabinetList = eleCabinetService.selectPowerPage(cabinetQuery);
        if (CollectionUtils.isEmpty(electricityCabinetList)) {
            return Collections.emptyList();
        }

        return buildEleCabinetDataAnalyseVOs(electricityCabinetList);
    }

    @Override
    public Integer selectOfflinePageCount(ElectricityCabinetQuery cabinetQuery) {
        return eleCabinetService.selectOfflinePageCount(cabinetQuery);
    }

    @Override
    public Integer selectLockPageCount(ElectricityCabinetQuery cabinetQuery) {
        return eleCabinetService.selectLockPageCount(cabinetQuery);
    }

    @Override
    public Integer selectPowerPageCount(ElectricityCabinetQuery cabinetQuery) {
        return eleCabinetService.selectPowerPageCount(cabinetQuery);
    }

    @Override
    public Integer selectFailurePageCount(ElectricityCabinetQuery cabinetQuery) {
        //故障编码
        List<Integer> failureList = new ArrayList<>();

        //从clickhouse查询所有有故障柜机
        List<Integer> cabinetFailureIds = eleWarnMsgService.selectEidByCabinetFailureList(failureList);
        List<Integer> cellFailureIds = eleWarnMsgService.selectEidByCellFailureList(failureList);

        Set<Integer> totalEid = new HashSet<>();
        totalEid.addAll(cabinetFailureIds);
        totalEid.addAll(cellFailureIds);
        if (CollectionUtils.isEmpty(totalEid)) {
            return 0;
        }

        cabinetQuery.setEleIdList(Lists.newArrayList(totalEid));

        return eleCabinetService.selectOfflinePageCount(cabinetQuery);
    }

    private List<EleCabinetDataAnalyseVO> buildEleCabinetDataAnalyseVOs(List<EleCabinetDataAnalyseVO> electricityCabinetList) {
        CompletableFuture<Void> acquireBasicInfo = CompletableFuture.runAsync(() -> electricityCabinetList.forEach(item -> {

            ElectricityCabinetModel cabinetModel = eleCabinetModelService.queryByIdFromCache(item.getModelId());
            item.setModelName(Objects.nonNull(cabinetModel) ? cabinetModel.getName() : "");

            Franchisee franchisee = franchiseeService.queryByIdFromCache(item.getFranchiseeId());
            item.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");

            EleCabinetCoreData eleCabinetCoreData = eleCabinetCoreDataService.queryByIdFromDB(item.getId().longValue());
            item.setTemp(Objects.nonNull(eleCabinetCoreData) ? eleCabinetCoreData.getTemp() : 0);

            ElectricityCabinetServer eleCabinetServer = eleCabinetServerService.selectByEid(item.getId());
            item.setServiceEndTime(eleCabinetServer.getServerEndTime());

            ElectricityCabinetPower eleCabinetPower = eleCabinetPowerService.selectByEid(item.getId());
            item.setPowerConsumption(eleCabinetPower.getSumPower());

            Store store = storeService.queryByIdFromCache(item.getStoreId());
            item.setStoreName(Objects.nonNull(store) ? store.getName() : "");

        }), DATA_ANALYSE_THREAD_POOL).exceptionally(e -> {
            log.error("ELE ERROR! acquire eleCabinet basic info fail", e);
            return null;
        });

        CompletableFuture<Void> acquireCellInfo = CompletableFuture.runAsync(() -> electricityCabinetList.forEach(item -> {

            Double fullyCharged = item.getFullyCharged();

            List<ElectricityCabinetBox> cabinetBoxList = electricityCabinetBoxService.queryBoxByElectricityCabinetId(item.getId());
            if (CollectionUtils.isEmpty(cabinetBoxList)) {
                return;
            }

            long exchangeableNumber = cabinetBoxList.stream().filter(e -> eleCabinetService.isExchangeable(e, fullyCharged)).count();

            long fullBatteryNumber = cabinetBoxList.stream().filter(e -> Objects.equals(e.getPower(), Double.valueOf(100))).count();

            long emptyCellNumber = cabinetBoxList.stream().filter(eleCabinetService::isNoElectricityBattery).count();

            long disableCellNumber = cabinetBoxList.stream().filter(e -> Objects.equals(e.getUsableStatus(), ElectricityCabinetBox.ELECTRICITY_CABINET_BOX_UN_USABLE)).count();

            long openFanNumber = cabinetBoxList.stream().filter(e -> Objects.equals(e.getIsFan(), ElectricityCabinetBox.OPEN_FAN)).count();

            long chargeCellNumber = cabinetBoxList.stream().filter(e -> StringUtils.isNotBlank(e.getSn())).map(t -> electricityBatteryService.queryBySnFromDb(t.getSn())).filter(battery -> Objects.equals(battery.getChargeStatus(), ElectricityBattery.CHARGE_STATUS_STARTING) || Objects.equals(battery.getChargeStatus(), ElectricityBattery.CHARGE_STATUS_STARTING)).count();

            item.setFullBatteryNumber((int) fullBatteryNumber);
            item.setChargeBatteryNumber((int) chargeCellNumber);
            item.setEmptyCellNumber((int) emptyCellNumber);
            item.setDisableCellNumber((int) disableCellNumber);
            item.setFanOpenNumber((int) openFanNumber);
            item.setExchangeBatteryNumber((int) exchangeableNumber);
        }), DATA_ANALYSE_THREAD_POOL).exceptionally(e -> {
            log.error("ELE ERROR! acquire eleCabinet cell info fail", e);
            return null;
        });

        CompletableFuture<Void> acquireOrderInfo = CompletableFuture.runAsync(() -> electricityCabinetList.forEach(item -> {
            //日均换电次数
            Long monthExchangeCount = eleCabinetOrderService.selectMonthExchangeCount(item.getId(), DateUtils.get30AgoStartTime(), System.currentTimeMillis(), item.getTenantId());
            item.setAverageExchangeNumber(BigDecimal.valueOf(monthExchangeCount).divide(BigDecimal.valueOf(30)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

            //日均活跃度
            Long monthExchangeUser = eleCabinetOrderService.selectMonthExchangeUser(item.getId(), DateUtils.get30AgoStartTime(), System.currentTimeMillis(), item.getTenantId());
            item.setAveragePeopleNumber(BigDecimal.valueOf(monthExchangeUser).divide(BigDecimal.valueOf(30)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

            List<ElectricityCabinetOrder> todayEleCabinetOrders = eleCabinetOrderService.selectTodayExchangeOrder(item.getId(), DateUtils.getTodayStartTimeStamp(), DateUtils.getTodayEndTimeStamp(), item.getTenantId());
            if (CollectionUtils.isEmpty(todayEleCabinetOrders)) {
                return;
            }

            //今日换电数量
            item.setExchangeNumber(todayEleCabinetOrders.size());
            //今日活跃度
            item.setPeopleNumber((int) todayEleCabinetOrders.stream().map(ElectricityCabinetOrder::getUid).distinct().count());
        }), DATA_ANALYSE_THREAD_POOL).exceptionally(e -> {
            log.error("ELE ERROR! acquire eleCabinet order info fail", e);
            return null;
        });

        CompletableFuture.allOf(acquireBasicInfo, acquireCellInfo, acquireOrderInfo);

        return electricityCabinetList;
    }
}

package com.xiliulou.electricity.service.impl;

import com.google.common.collect.Lists;
import com.xiliulou.core.json.JsonUtil;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.dto.EleDivisionAccountOperationRecordDTO;
import com.xiliulou.electricity.entity.*;
import com.xiliulou.electricity.enums.PackageTypeEnum;
import com.xiliulou.electricity.mapper.DivisionAccountOperationRecordMapper;
import com.xiliulou.electricity.query.DivisionAccountOperationRecordQuery;
import com.xiliulou.electricity.service.*;
import com.xiliulou.electricity.vo.BatteryMemberCardVO;
import com.xiliulou.electricity.vo.DivisionAccountOperationRecordVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * (DivisionAccountOperationRecord)�����ʵ����
 *
 * @author zhangyanbo
 * @since 2023-05-08 13:38:49
 */
@Service("divisionAccountOperationRecordService")
@Slf4j
public class DivisionAccountOperationRecordServiceImpl implements DivisionAccountOperationRecordService {
    @Resource
    private DivisionAccountOperationRecordMapper divisionAccountOperationRecordMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private DivisionAccountConfigService divisionAccountConfigService;

    @Autowired
    private FranchiseeService franchiseeService;

    @Override
    public DivisionAccountOperationRecord queryByIdFromDB(Long id) {
        return this.divisionAccountOperationRecordMapper.queryById(id);
    }

    @Override
    public DivisionAccountOperationRecord queryByIdFromCache(Long id) {
        return null;
    }


    @Override
    public List<DivisionAccountOperationRecord> queryAllByLimit(int offset, int limit) {
        return this.divisionAccountOperationRecordMapper.queryAllByLimit(offset, limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DivisionAccountOperationRecord insert(DivisionAccountOperationRecord divisionAccountOperationRecord) {
        this.divisionAccountOperationRecordMapper.insertOne(divisionAccountOperationRecord);
        return divisionAccountOperationRecord;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(DivisionAccountOperationRecord divisionAccountOperationRecord) {
        return this.divisionAccountOperationRecordMapper.update(divisionAccountOperationRecord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteById(Long id) {
        return this.divisionAccountOperationRecordMapper.deleteById(id) > 0;
    }

    @Slave
    @Override
    public List<DivisionAccountOperationRecordVO> queryList(DivisionAccountOperationRecordQuery divisionAccountOperationRecord) {

        List<DivisionAccountOperationRecord> divisionAccountOperationRecords = divisionAccountOperationRecordMapper.queryList(divisionAccountOperationRecord);
        if (CollectionUtils.isEmpty(divisionAccountOperationRecords)) {
            return Collections.emptyList();
        }

        return divisionAccountOperationRecords.parallelStream().map(item -> {
            DivisionAccountOperationRecordVO recordVO = new DivisionAccountOperationRecordVO();
            recordVO.setId(item.getId());
            recordVO.setName(item.getName());
            recordVO.setOperatorRate(item.getCabinetOperatorRate());
            recordVO.setOperatorRateOther(item.getNonCabOperatorRate());
            recordVO.setFranchiseeRate(item.getCabinetFranchiseeRate());
            recordVO.setFranchiseeRateOther(item.getNonCabFranchiseeRate());
            recordVO.setStoreRate(item.getCabinetStoreRate());
            recordVO.setCreateTime(item.getCreateTime());
            recordVO.setUpdateTime(item.getUpdateTime());

            User user = userService.queryByUidFromCache(item.getUid());
            recordVO.setUserName(Objects.nonNull(user) ? user.getName() : "");

            DivisionAccountConfig divisionAccountConfig = divisionAccountConfigService.queryByIdFromCache(item.getDivisionAccountId().longValue());
            recordVO.setHierarchy(Objects.nonNull(divisionAccountConfig) ? divisionAccountConfig.getHierarchy() : -1);
            recordVO.setStatus(Objects.nonNull(divisionAccountConfig) ? divisionAccountConfig.getStatus() : -1);
            recordVO.setType(Objects.nonNull(divisionAccountConfig) ? divisionAccountConfig.getType() : -1);

            Franchisee franchisee = franchiseeService.queryByIdFromCache(Objects.nonNull(divisionAccountConfig) ? divisionAccountConfig.getFranchiseeId() : 0L);
            recordVO.setFranchiseeName(Objects.nonNull(franchisee) ? franchisee.getName() : "");

            if (StringUtils.isBlank(item.getAccountMemberCard())) {
                return recordVO;
            }

            List<EleDivisionAccountOperationRecordDTO> list = JsonUtil.fromJsonArray(item.getAccountMemberCard(), EleDivisionAccountOperationRecordDTO.class);
            if (CollectionUtils.isEmpty(list)) {
                return recordVO;
            }

            List<String> membercardNames = list.stream().map(EleDivisionAccountOperationRecordDTO::getName).collect(Collectors.toList());
            recordVO.setMembercardNames(membercardNames);

            //recordVO.setBatteryPackages(divisionAccountConfigService.getMemberCardVOListByConfigIdAndType(item.getDivisionAccountId().longValue(), DivisionAccountBatteryMembercard.TYPE_BATTERY));
            //recordVO.setCarRentalPackages(divisionAccountConfigService.getMemberCardVOListByConfigIdAndType(item.getDivisionAccountId().longValue(), DivisionAccountBatteryMembercard.TYPE_CAR_RENTAL));
            //recordVO.setCarWithBatteryPackages(divisionAccountConfigService.getMemberCardVOListByConfigIdAndType(item.getDivisionAccountId().longValue(), DivisionAccountBatteryMembercard.TYPE_CAR_BATTERY));

            recordVO.setBatteryPackages(getPackagesByType(list, PackageTypeEnum.PACKAGE_TYPE_BATTERY.getCode()));
            recordVO.setCarRentalPackages(getPackagesByType(list, PackageTypeEnum.PACKAGE_TYPE_CAR_RENTAL.getCode()));
            recordVO.setCarWithBatteryPackages(getPackagesByType(list, PackageTypeEnum.PACKAGE_TYPE_CAR_BATTERY.getCode()));

            //兼容2.0的旧数据，如果三个套餐均为空值，则需要根据分账配置表中的type来设置对应的套餐信息
            if(CollectionUtils.isEmpty(recordVO.getBatteryPackages())
                    && CollectionUtils.isEmpty(recordVO.getCarRentalPackages())
                    && CollectionUtils.isEmpty(recordVO.getCarWithBatteryPackages())){
                if(DivisionAccountConfig.TYPE_BATTERY.equals(divisionAccountConfig.getType())){
                    recordVO.setBatteryPackages(getPackagesFromOperationRecord(list));
                }else if(DivisionAccountConfig.TYPE_CAR.equals(divisionAccountConfig.getType())){
                    recordVO.setCarRentalPackages(getPackagesFromOperationRecord(list));
                }
            }

            return recordVO;
        }).collect(Collectors.toList());
    }

    private List<BatteryMemberCardVO> getPackagesFromOperationRecord(List<EleDivisionAccountOperationRecordDTO> divisionAccountOperationRecordDTOList){
        List<BatteryMemberCardVO> batteryMemberCardVOS = Lists.newArrayList();
        for(EleDivisionAccountOperationRecordDTO eleDivisionAccountOperationRecordDTO : divisionAccountOperationRecordDTOList){
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();

            batteryMemberCardVO.setId(eleDivisionAccountOperationRecordDTO.getId().longValue());
            batteryMemberCardVO.setName(eleDivisionAccountOperationRecordDTO.getName());
            batteryMemberCardVO.setType(eleDivisionAccountOperationRecordDTO.getType());
            batteryMemberCardVOS.add(batteryMemberCardVO);
        }
        return batteryMemberCardVOS;
    }

    private List<BatteryMemberCardVO> getPackagesByType(List<EleDivisionAccountOperationRecordDTO> divisionAccountOperationRecordDTOList, Integer packageType){
        List<BatteryMemberCardVO> batteryMemberCardVOS = Lists.newArrayList();
        for(EleDivisionAccountOperationRecordDTO eleDivisionAccountOperationRecordDTO : divisionAccountOperationRecordDTOList){
            BatteryMemberCardVO batteryMemberCardVO = new BatteryMemberCardVO();
            if(packageType.equals(eleDivisionAccountOperationRecordDTO.getType())){
                batteryMemberCardVO.setId(eleDivisionAccountOperationRecordDTO.getId().longValue());
                batteryMemberCardVO.setName(eleDivisionAccountOperationRecordDTO.getName());
                batteryMemberCardVO.setType(eleDivisionAccountOperationRecordDTO.getType());
                batteryMemberCardVOS.add(batteryMemberCardVO);
            }
        }
        return batteryMemberCardVOS;
    }


    @Slave
    @Override
    public Integer queryCount(DivisionAccountOperationRecordQuery divisionAccountOperationRecord) {
        return this.divisionAccountOperationRecordMapper.queryCount(divisionAccountOperationRecord);
    }
}

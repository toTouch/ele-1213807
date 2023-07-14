package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CarRenalCacheConstant;
import com.xiliulou.electricity.constant.StringConstant;
import com.xiliulou.electricity.entity.car.CarRentalPackagePO;
import com.xiliulou.electricity.enums.DelFlagEnum;
import com.xiliulou.electricity.enums.UpDownEnum;
import com.xiliulou.electricity.enums.basic.BasicEnum;
import com.xiliulou.electricity.enums.car.CarRentalPackageTypeEnum;
import com.xiliulou.electricity.exception.BizException;
import com.xiliulou.electricity.mapper.car.CarRentalPackageMapper;
import com.xiliulou.electricity.model.car.query.CarRentalPackageQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageOrderService;
import com.xiliulou.electricity.service.car.CarRentalPackageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 租车套餐表 ServiceImpl
 *
 * @author xiaohui.song
 **/
@Service
@Slf4j
public class CarRentalPackageServiceImpl implements CarRentalPackageService {

    @Resource
    private RedisService redisService;

    @Resource
    private CarRentalPackageOrderService carRentalPackageOrderService;

    @Resource
    private CarRentalPackageMapper carRentalPackageMapper;

    /**
     * 根据主键ID查询，不区分是否删除
     *
     * @param ids 主键ID集
     * @return
     */
    @Slave
    @Override
    public List<CarRentalPackagePO> selectByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }

        return carRentalPackageMapper.selectByIds(ids);
    }

    /**
     * 根据条件查询<br />
     *
     * @param qryModel 查询条件
     * @return
     */
    @Slave
    @Override
    public List<CarRentalPackagePO> listByCondition(CarRentalPackageQryModel qryModel) {
        if (!ObjectUtils.allNotNull(qryModel, qryModel.getTenantId())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }
        return carRentalPackageMapper.list(qryModel);
    }

    /**
     * 检测唯一：租户ID+套餐名称
     *
     * @param tenantId 租户ID
     * @param name     套餐名称
     * @return
     */
    @Slave
    @Override
    public Boolean uqByTenantIdAndName(Integer tenantId, String name) {
        if (ObjectUtils.allNotNull(tenantId, name)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        int num = carRentalPackageMapper.uqByTenantIdAndName(tenantId, name);

        return num > 0;
    }

    /**
     * 根据ID修改上下架状态
     *
     * @param id 主键ID
     * @param status 上下架状态
     * @param uid 操作人ID
     * @return
     */
    @Override
    public Boolean updateStatusById(Long id, Integer status, Long uid) {
        if (!ObjectUtils.allNotNull(id, status, uid) || !BasicEnum.isExist(status, UpDownEnum.class)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        int num = carRentalPackageMapper.updateStatusById(id, status, uid, System.currentTimeMillis());

        // 删除缓存
        delCache(String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_ID_KEY, id));

        return num >= 0;
    }

    /**
     * 根据ID删除
     *
     * @param id 主键ID
     * @param uid 操作人ID
     * @return
     */
    @Override
    public Boolean delById(Long id, Long uid) {
        if (!ObjectUtils.allNotNull(id, uid)) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 校验能否删除
        if (carRentalPackageOrderService.checkByRentalPackageId(id)) {
            // TODO 错误编码
            throw new BizException("", "已有购买订单记录，不允许删除");
        }

        int num = carRentalPackageMapper.delById(id, uid, System.currentTimeMillis());

        // 删除缓存
        delCache(String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_ID_KEY, id));

        return num >= 0;
    }

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public List<CarRentalPackagePO> list(CarRentalPackageQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            throw  new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        handleQryModel(qryModel);

        return carRentalPackageMapper.list(qryModel);
    }

    /**
     * 条件查询分页
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public List<CarRentalPackagePO> page(CarRentalPackageQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            throw  new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        handleQryModel(qryModel);

        return carRentalPackageMapper.page(qryModel);
    }

    /**
     * 条件查询总数
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public Integer count(CarRentalPackageQryModel qryModel) {
        if (ObjectUtils.allNotNull(qryModel, qryModel.getTenantId())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        handleQryModel(qryModel);

        return carRentalPackageMapper.count(qryModel);
    }

    /**
     * 处理请求参数
     * @param qryModel
     */
    private void handleQryModel(CarRentalPackageQryModel qryModel) {
        // 处理电池型号，保底
        String batteryModelIds = qryModel.getBatteryModelIds();
        String batteryModelIdsLeftLike = qryModel.getBatteryModelIdsLeftLike();
        if (StringUtils.isNotBlank(batteryModelIds)) {
            batteryModelIds = Arrays.asList(batteryModelIds.split(StringConstant.COMMA_EN)).stream().sorted().collect(Collectors.joining(StringConstant.COMMA_EN));
            qryModel.setBatteryModelIds(batteryModelIds);
        }
        if (StringUtils.isNotBlank(batteryModelIdsLeftLike)) {
            batteryModelIdsLeftLike = Arrays.asList(batteryModelIdsLeftLike.split(StringConstant.COMMA_EN)).stream().sorted().collect(Collectors.joining(StringConstant.COMMA_EN));
            qryModel.setBatteryModelIdsLeftLike(batteryModelIdsLeftLike);
        }
    }

    /**
     * 根据ID查询<br />
     * 优先查询缓存，缓存没有查询DB，懒加载缓存<br />
     * 可能返回<code>null</code>
     * @param id 主键ID
     * @return
     */
    @Slave
    @Override
    public CarRentalPackagePO selectById(Long id) {
        if (null == id || id <= 0) {
            return null;
        }

        // 获取缓存
        String cacheKey = String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_ID_KEY, id);
        CarRentalPackagePO cachePO = redisService.getWithHash(cacheKey, CarRentalPackagePO.class);
        if (ObjectUtils.isNotEmpty(cachePO)) {
            return null;
        }

        // 查询 DB
        CarRentalPackagePO dbPO = carRentalPackageMapper.selectById(id);

        // 存入缓存
        redisService.saveWithHash(cacheKey, dbPO);

        return dbPO;
    }

    /**
     * 根据ID修改数据
     * @param entity 实体数据
     * @return
     */
    @Override
    public Boolean updateById(CarRentalPackagePO entity) {
        if (!ObjectUtils.allNotNull(entity, entity.getId(), entity.getUpdateUid(), entity.getTenantId(), entity.getName())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        // 检测原始套餐状态
        CarRentalPackagePO oriEntity = carRentalPackageMapper.selectById(entity.getId());
        if (oriEntity == null || DelFlagEnum.DEL.getCode().equals(oriEntity.getDelFlag())) {
            // TODO 错误编码
            throw new BizException("", "数据有误");
        }
        if (UpDownEnum.UP.getCode().equals(oriEntity.getStatus())) {
            // TODO 错误编码
            throw new BizException("", "请先下架套餐再进行编辑操作");
        }

        Integer tenantId = entity.getTenantId();
        String name = entity.getName();

        // 检测唯一
        if (!oriEntity.getName().equals(name) && carRentalPackageMapper.uqByTenantIdAndName(tenantId, name) > 0) {
            // TODO 错误编码
            throw new BizException("", "套餐名称已存在");
        }

        entity.setUpdateTime(System.currentTimeMillis());

        int num = carRentalPackageMapper.updateById(entity);

        // 删除缓存
        String cacheEky = String.format(CarRenalCacheConstant.CAR_RENAL_PACKAGE_ID_KEY, entity.getId());
        redisService.delete(cacheEky);

        return num >= 0;
    }

    /**
     * 新增数据，返回主键ID<br />
     * 若为车电一体，则会联动调用换电套餐的逻辑
     * @param entity 实体数据
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insert(CarRentalPackagePO entity) {
        if (!ObjectUtils.allNotNull(entity, entity.getId(), entity.getCreateUid(), entity.getTenantId(), entity.getName())) {
            throw new BizException("ELECTRICITY.0007", "不合法的参数");
        }

        Integer tenantId = entity.getTenantId();
        String name = entity.getName();

        // 检测唯一
        if (carRentalPackageMapper.uqByTenantIdAndName(tenantId, name) > 0) {
            // TODO 错误编码
            throw new BizException("", "套餐名称已存在");
        }

        // 保底处理电池型号ID连接字符串
        String batteryModelIds = entity.getBatteryModelIds();
        if (StringUtils.isNotBlank(batteryModelIds)) {
            batteryModelIds = Arrays.asList(batteryModelIds.split(StringConstant.COMMA_EN)).stream().sorted().collect(Collectors.joining(StringConstant.COMMA_EN));
            entity.setBatteryModelIds(batteryModelIds);
        }


        // 赋值操作人、时间、删除标记
        long now = System.currentTimeMillis();
        entity.setUpdateUid(entity.getCreateUid());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setDelFlag(DelFlagEnum.OK.getCode());

        carRentalPackageMapper.insert(entity);

        // 后续处理逻辑
        afterInsert(entity);

        return entity.getId();
    }

    /**
     * 新增之后的后续操作
     * @param entity
     */
    private void afterInsert(CarRentalPackagePO entity) {
        // TODO 若为车电一体的套餐，需要调用换电套餐的接口，志龙
        if (CarRentalPackageTypeEnum.CAR_BATTERY.getCode().equals(entity.getType())) {

        }
    }

    /**
     * 删除缓存
     * @param key
     */
    private void delCache(String key) {
        redisService.delete(key);
    }
}

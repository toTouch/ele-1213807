package com.xiliulou.electricity.service.impl.car;

import com.xiliulou.cache.redis.RedisService;
import com.xiliulou.core.web.R;
import com.xiliulou.db.dynamic.annotation.Slave;
import com.xiliulou.electricity.constant.CarRenalCacheConstant;
import com.xiliulou.electricity.entity.car.CarRentalPackageMemberTermPO;
import com.xiliulou.electricity.mapper.car.CarRentalPackageMemberTermMapper;
import com.xiliulou.electricity.model.car.opt.CarRentalPackageMemberTermOptModel;
import com.xiliulou.electricity.model.car.query.CarRentalPackageMemberTermQryModel;
import com.xiliulou.electricity.service.car.CarRentalPackageMemberTermService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 租车套餐会员期限表 ServiceImpl
 * @author xiaohui.song
 **/
@Slf4j
@Service
public class CarRentalPackageMemberTermServiceImpl implements CarRentalPackageMemberTermService {

    @Resource
    private RedisService redisService;

    @Resource
    private CarRentalPackageMemberTermMapper carRentalPackageMemberTermMapper;

    /**
     * 根据租户ID和用户ID查询租车套餐会员限制信息
     *
     * @param tenantId 租户ID
     * @param uid      用户ID
     * @return
     */
    @Override
    public R<CarRentalPackageMemberTermPO> selectByTenantIdAndUid(Integer tenantId, Long uid) {
        if (!ObjectUtils.allNotNull(tenantId, uid)) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        // 获取缓存
        String cacheKey = String.format(CarRenalCacheConstant.CAR_RENTAL_PACKAGE_MEMBER_TERM_TENANT_UID_KEY, tenantId, uid);
        CarRentalPackageMemberTermPO cachePO = redisService.getWithHash(cacheKey, CarRentalPackageMemberTermPO.class);
        if (ObjectUtils.isNotEmpty(cachePO)) {
            return R.ok(cachePO);
        }

        // 获取 DB
        CarRentalPackageMemberTermPO dbPO = carRentalPackageMemberTermMapper.selectByTenantIdAndUid(tenantId, uid);
        if (ObjectUtils.isNotEmpty(dbPO)) {
            redisService.saveWithHash(cacheKey, dbPO);
        }

        return R.ok(dbPO);
    }

    /**
     * 条件查询列表<br />
     * 全表扫描，慎用
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<List<CarRentalPackageMemberTermPO>> list(CarRentalPackageMemberTermQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageMemberTermMapper.list(qryModel));
    }

    /**
     * 条件查询分页
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<List<CarRentalPackageMemberTermPO>> page(CarRentalPackageMemberTermQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageMemberTermMapper.page(qryModel));
    }

    /**
     * 条件查询总数
     *
     * @param qryModel 查询模型
     * @return
     */
    @Slave
    @Override
    public R<Integer> count(CarRentalPackageMemberTermQryModel qryModel) {
        if (null == qryModel || null == qryModel.getTenantId() || qryModel.getTenantId() <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageMemberTermMapper.count(qryModel));
    }

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return
     */
    @Slave
    @Override
    public R<CarRentalPackageMemberTermPO> selectById(Long id) {
        if (null == id || id <= 0) {
            return R.fail("ELECTRICITY.0007", "不合法的参数");
        }

        return R.ok(carRentalPackageMemberTermMapper.selectById(id));
    }

    /**
     * 新增数据，返回主键ID
     *
     * @param optModel 操作模型
     * @return
     */
    @Override
    public R<Long> insert(CarRentalPackageMemberTermOptModel optModel) {
        CarRentalPackageMemberTermPO entity = new CarRentalPackageMemberTermPO();
        BeanUtils.copyProperties(optModel, entity);

        // 赋值操作人及时间
        long now = System.currentTimeMillis();
        entity.setUpdateUid(entity.getCreateUid());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);

        // 保存入库
        carRentalPackageMemberTermMapper.insert(entity);

        return R.ok(entity.getId());
    }
}

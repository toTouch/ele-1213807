package com.xiliulou.electricity.service;

import com.xiliulou.electricity.entity.Picture;
import com.xiliulou.electricity.query.PictureQuery;
import com.xiliulou.electricity.query.StorePictureQuery;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * 图片表(Picture)表服务接口
 *
 * @author zzlong
 * @since 2022-12-14 13:54:08
 */
public interface PictureService {

    /**
     * 通过ID查询单条数据从数据库
     *
     * @param id 主键
     * @return 实例对象
     */
    Picture selectByIdFromDB(Long id);

    /**
     * 通过ID查询单条数据从缓存
     *
     * @param id 主键
     * @return 实例对象
     */
    Picture selectByIdFromCache(Long id);

    /**
     * 查询多条数据
     *
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     */
    List<Picture> selectByPage(int offset, int limit);

    /**
     * 新增数据
     *
     * @param picture 实例对象
     * @return 实例对象
     */
    Picture insert(Picture picture);

    Integer batchInsert(List<Picture> pictures);

    /**
     * 修改数据
     *
     * @param picture 实例对象
     * @return 实例对象
     */
    Integer update(Picture picture);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(Long id);

    Triple<Boolean,String,Object> saveStroePicture(List<StorePictureQuery> storePictureQueryList);

    List<Picture> selectByQuery(PictureQuery pictureQuery);

    int deleteByBusinessId(Long id);

    List<Picture> selectByByBusinessId(Long id);
}

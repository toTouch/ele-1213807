package com.xiliulou.electricity.vo.car;

import lombok.Data;


@Data
public class PageDataResult<T> {

    private Integer pageCount;

    private Integer pageSize;

    private Integer pageNumber;

    private T data;

    public static <T> PageDataResult<T> result(Integer pageCount, Integer pageSize, Integer pageNumber, T data) {
        PageDataResult<T> pageDataResult = new PageDataResult();
        pageDataResult.setData(data);
        pageDataResult.setPageCount(pageCount);
        pageDataResult.setPageSize(pageSize);
        pageDataResult.setPageNumber(pageNumber);
        return pageDataResult;
    }
}



package com.xiliulou.electricity.vo.car;

import lombok.Data;


@Data
public class CarDataResult<T> {

    private Integer pageCount;

    private Integer pageSize;

    private Integer pageNumber;

    private T  data;

    public static <T>  CarDataResult <T>  result(Integer pageCount, Integer pageSize, Integer pageNumber, T data){
        CarDataResult<T>  carDataResult = new CarDataResult();
        carDataResult.setData(data);
        carDataResult.setPageCount(pageCount);
        carDataResult.setPageSize(pageSize);
        carDataResult.setPageNumber(pageNumber);
        return carDataResult;
    }
}



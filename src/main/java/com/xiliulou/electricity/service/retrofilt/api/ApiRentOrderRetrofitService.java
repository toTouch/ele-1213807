package com.xiliulou.electricity.service.retrofilt.api;

import com.xiliulou.core.web.R;
import com.xiliulou.electricity.web.query.ApiExchangeOrderCallQuery;
import com.xiliulou.electricity.web.query.ApiRentOrderCallQuery;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * @author : eclair
 * @date : 2021/11/11 8:32 上午
 */
public interface ApiRentOrderRetrofitService {
    @POST("/{tenantId}/{urlType}")
    Call<R> apiCall(@Body ApiRentOrderCallQuery apiRentOrderCallQuery,
                    @Path("tenantId") Integer tenantId,
                    @Path("urlType") Integer urlType);
}

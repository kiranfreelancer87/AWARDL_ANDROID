package com.piddlepops.awardl;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface APIInterface {
    public static String BASE_URL = "http://192.168.128.145:5000/";
    @POST("postResult")
    Call<ResponseBody> postResult(@Body PostResultModel postResultModel);
}
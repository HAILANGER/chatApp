package com.study.newintelrobot;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ChatGPTService {

    @Headers({
            "Content-Type: application/json",
            "Authorization: Bearer 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"
    })
//    @POST("v1/models/chatgpt:generate")
    @POST("v1/completions")
    Call<ChatResponse> generateResponse(@Body ChatRequest request);

}

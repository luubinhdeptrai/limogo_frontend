package com.example.login.API;

import com.example.login.LOGIN.LoginRequest;
import com.example.login.LOGIN.LoginResponse;
import com.example.login.SEND_VERIFICATION.EmailRequest;
import com.example.login.SEND_VERIFICATION.SendCodeResponse;
import com.example.login.SIGNUP.SignupRequest;
import com.example.login.SIGNUP.SignupResponse;
import com.example.login.VERIFY_VERIFICATION.VerifyCodeRequest;
import com.example.login.VERIFY_VERIFICATION.VerifyCodeResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PATCH;

public interface ApiService {

    @POST("auth/signup")
    Call<SignupResponse> signup(@Body SignupRequest request);

    @POST("auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request); // Đổi tên để tránh trùng với hàm login tự động

    @PATCH("auth/send-verification-code")
    Call<SendCodeResponse> sendVerificationCode(@Body EmailRequest request);

    @PATCH("auth/verify-verification-code")
    Call<VerifyCodeResponse> verifyVerificationCode(@Body VerifyCodeRequest request);
}
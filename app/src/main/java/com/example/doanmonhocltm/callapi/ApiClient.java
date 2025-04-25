package com.example.doanmonhocltm.callapi;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {
    private static String BASE_URL = "http://10.0.2.2:8087/"; // Dành cho giả lập Android

//    private static String BASE_URL = "http://172.16.100.214:8087/"; // Địa chỉ IP của server

//    private static String BASE_URL = "http://172.16.146.187:8087/"; // Địa chỉ IP của server

    private static Retrofit retrofit;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {

            SessionManager sessionManager = new SessionManager(context);

            // Tạo HttpLoggingInterceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Log toàn bộ request/response


            // Cấu hình OkHttpClient với interceptor
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(loggingInterceptor)  // Thêm interceptor vào client
                    .addInterceptor(new AuthInterceptor(sessionManager)).connectTimeout(30, TimeUnit.MINUTES)  // Chờ kết nối tới 30 phút
                    .readTimeout(3, TimeUnit.MINUTES)     // Chờ đọc dữ liệu tới 30 phút
                    .writeTimeout(3, TimeUnit.MINUTES)    // Chờ ghi dữ liệu tới 30 phút// Interceptor của bạn
                    .build();

            // Tạo Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}

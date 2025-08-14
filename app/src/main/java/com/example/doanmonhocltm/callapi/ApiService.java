// ApiService.java
package com.example.doanmonhocltm.callapi;

import com.example.doanmonhocltm.model.Accident;
import com.example.doanmonhocltm.model.Car;
import com.example.doanmonhocltm.model.CarViolationReport;
import com.example.doanmonhocltm.model.DriverLicense;
import com.example.doanmonhocltm.model.FcmToken;
import com.example.doanmonhocltm.model.LocationResponse;
import com.example.doanmonhocltm.model.LoginHistory;
import com.example.doanmonhocltm.model.LoginRequest;
import com.example.doanmonhocltm.model.Logout;
import com.example.doanmonhocltm.model.Motorcycle;
import com.example.doanmonhocltm.model.MotorcycleViolationReport;
import com.example.doanmonhocltm.model.Person;
import com.example.doanmonhocltm.model.ResponderStatusRequest;
import com.example.doanmonhocltm.model.ResultLogin;
import com.example.doanmonhocltm.model.ScanLog;
import com.example.doanmonhocltm.model.User;
import com.example.doanmonhocltm.model.UserLocationRequest;
import com.example.doanmonhocltm.model.Vehicles;
import com.example.doanmonhocltm.model.ViolationAll;


import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("quet/api/auth/signin")
    Call<ResultLogin> login(@Body LoginRequest loginRequest);

    @GET("quet/api/vehicles/cars/{licensePlate}")
    Call<Car> getCarByLicensePlate(@Path("licensePlate") String licensePlate);

    @GET("quet/api/vehicles/motorcycles/{licensePlate}")
    Call<Vehicles> getMotorcycle(@Path("licensePlate") String licensePlate);

    @GET("quet/api/vehicles/cars/{licensePlate}")
    Call<Vehicles> getCar(@Path("licensePlate") String licensePlate);


    @Multipart
    @POST("quet/api/face-recognition/identify")
    Call<Person> identifyFace(@Part MultipartBody.Part image);

    @GET("/quet/api/person/{id}")
    Call<Person> getPersonById(@Path("id") String id);

    @POST("/quet/api/car-violations")
    Call<CarViolationReport> createCarViolationReport(@Body CarViolationReport carViolationReport);

    @POST("/quet/api/motorcycle-violations")
    Call<MotorcycleViolationReport> createMotorcycleViolationReport(@Body MotorcycleViolationReport motorcycleViolationReport);

    @POST("/quet/api/scan_logs/motorcycles")
    Call<ScanLog> createMotorcycleScanLog(@Body ScanLog scanLog);

    @POST("/quet/api/scan_logs/cars")
    Call<ScanLog> createCarScanLog(@Body ScanLog scanLog);

    @POST("/quet/api/auth/login-history")
    Call<LoginHistory> createLoginHistory(@Body LoginHistory loginHistory);

    @PATCH("/quet/api/auth/logout/{accountId}")
    Call<Logout> logout(@Path("accountId") String accountId);

    @GET("/quet/api/images/{filename}")
    Call<ResponseBody> getImage(@Path("filename") String filename);

    @GET("/quet/api/account/{id}")
    Call<User> getUserMail(@Path("id") String id);

    @GET("quet/api/car-violations/license-plate/{licensePlate}")
    Call<List<ViolationAll>> getCarViolationsByLicensePlate(@Path("licensePlate") String licensePlate);

    @GET("quet/api/motorcycle-violations/license-plate/{licensePlate}")
    Call<List<ViolationAll>> getMotorcycleViolationsByLicensePlate(@Path("licensePlate") String licensePlate);

    @GET("quet/api/car-violations/{id}")
    Call<ViolationAll> getCarViolationById(@Path("id") int id);

    @GET("quet/api/motorcycle-violations/{id}")
    Call<ViolationAll> getMotorcycleViolationById(@Path("id") int id);

    @GET("quet/api/driving-license/person/{personId}")
    Call<List<DriverLicense>> getDriverLicenseByPersonId(@Path("personId") String personId);


    @GET("quet/api/accidents/unit/{unit_id}")
    Call<List<Accident>> getAccidentByUnitId(@Path("unit_id") String unitId);

    @POST("quet/api/fcm-token/token")
    Call<FcmToken> postCreateFcmToken(@Body FcmToken fcmToken);

    @PUT("quet/api/user-location")
//    Call<Map<String, String>> updateUserLocation(@Body UserLocationRequest userLocationRequest);
    Call<Void> updateUserLocation(@Body UserLocationRequest userLocationRequest);


    @GET("/quet/api/images/accident/{filename}")
    Call<ResponseBody> getImageAccident(@Path("filename") String filename);

    @PUT("/quet/api/accidents/responder")
    Call<Map<String, String>> updateResponderStatus(@Body ResponderStatusRequest responderStatusRequest);


    @GET("/quet/api/camera/{accidentId}")
    Call<LocationResponse> getLocationCamere(@Path("accidentId") int accidentId,
                                             @Query("lat") double lat,
                                             @Query("lng") double lng,
                                             @Query("unitId") String unitId);


}


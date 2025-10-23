package tech.id.kasir.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Field;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import tech.id.kasir.response_api.ApiResponse;
import tech.id.kasir.response_api.MenuResponse;
import tech.id.kasir.response_api.Order;
import tech.id.kasir.response_api.OrderRequest;
import tech.id.kasir.response_api.RestoranResponse;
import tech.id.kasir.response_api.login;

public interface api_transakasi_data {

    @FormUrlEncoded
    @POST("login/admin")
    Call<login> loginadmin(
            @Field("username") String username,
            @Field("password") String password
    );


    @GET("restoran/{id}")
    Call<RestoranResponse> getRestoran(@Path("id") int restoranId);


    @GET("menus")
    Call<MenuResponse> getMenus();

    @GET("orders")
    Call<List<Order>> getOrders();

    @POST("orders")
    Call<ApiResponse<Order>> createOrder(@Body OrderRequest orderRequest);

    @PUT("orders/{id}")
    Call<ApiResponse<Order>> updateOrder(@Path("id") int id, @Body OrderRequest orderRequest);

    @DELETE("orders/{id}")
    Call<ApiResponse> deleteOrder(@Path("id") int id);

}

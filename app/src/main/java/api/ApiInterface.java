package api;

import datamodels.GetCategoriesResponse;
import datamodels.GetProvidersResponse;
import datamodels.ResponseObject;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Ahmed on 29-Jun-14.
 */
public interface ApiInterface {

    @GET("/custom-feeds/")
    ResponseObject GetFeed(@Query("user") String user, @Query("pass") String pass, @Query("cat") String cat, @Query("limit") int limit
            , @Query("array") int array, @Query("provider") String provider);

    @GET("/custom-feeds/?getcats=1")
    GetCategoriesResponse GetCategories();

    @GET("/feed-gen/?key=1")
    GetProvidersResponse GetProviders();

}

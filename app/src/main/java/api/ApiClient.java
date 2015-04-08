package api;

import retrofit.RestAdapter;

/**
 * Created by Ahmed on 29-Jun-14.
 */
public class ApiClient {
    private static ApiInterface sService;
    public static String endpoint = "http://elkholasa.net";
    private static String blueEndpoint = "http://demo.t-publisher.com";

    public static ApiInterface getApiClient() {
        if (sService == null) {
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(
                            endpoint).build();

            sService = restAdapter.create(ApiInterface.class);
        }
        return sService;
    }

}

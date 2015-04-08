package api;

import java.util.ArrayList;

import datamodels.CategoryObject;
import datamodels.ProviderObject;
import datamodels.ResponseObject;

/**
 * Created by Ahmed on 04-Jul-14.
 */
public class API {

    static ApiInterface service = ApiClient.getApiClient();

    public static ResponseObject GetFeed(ArrayList categoryList, ArrayList providerList) {

        StringBuilder categories = new StringBuilder();
        for (int i = 0; i < categoryList.size(); i++) {
            if (i != 0)
                categories.append(',');
            categories.append(categoryList.get(i));
        }
        categories.delete(categories.length(), categories.length());

        StringBuilder providers = new StringBuilder();
        for (int i = 0; i < providerList.size(); i++) {
            if (i != 0)
                providers.append(',');
            providers.append(providerList.get(i));
        }
        providers.delete(providers.length(), providers.length());

        return service.GetFeed("admin", "121314", categories.toString(), 10, 1, providers.toString());

    }

    public static ArrayList<CategoryObject> GetCategoriesList() {
        return service.GetCategories().getData();
    }

    public static ArrayList<ProviderObject> GetProvidersList() {
        return service.GetProviders().getData();
    }
}

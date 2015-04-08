package datamodels;

import java.util.ArrayList;

/**
 * Created by Ahmed on 11-Jul-14.
 */
public class GetProvidersResponse {

    String status;
    ArrayList<ProviderObject> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<ProviderObject> getData() {
        return data;
    }

    public void setData(ArrayList<ProviderObject> data) {
        this.data = data;
    }
}

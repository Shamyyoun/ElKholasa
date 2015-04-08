package datamodels;

import java.util.ArrayList;

/**
 * Created by Ahmed on 11-Jul-14.
 */
public class GetCategoriesResponse {

    String status;
    ArrayList<CategoryObject> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<CategoryObject> getData() {
        return data;
    }

    public void setData(ArrayList<CategoryObject> data) {
        this.data = data;
    }
}

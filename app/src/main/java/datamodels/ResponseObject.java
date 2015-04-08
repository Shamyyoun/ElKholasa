package datamodels;

import java.util.ArrayList;

/**
 * Created by Ahmed on 30-Jun-14.
 */
public class ResponseObject {
    String status;
    ArrayList<GetFeedResponse> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<GetFeedResponse> getData() {
        return data;
    }

    public void setData(ArrayList<GetFeedResponse> data) {
        this.data = data;
    }
}

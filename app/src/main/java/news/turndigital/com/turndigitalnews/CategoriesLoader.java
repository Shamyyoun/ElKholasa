package news.turndigital.com.turndigitalnews;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;

import api.API;
import datamodels.AsyncResult;
import datamodels.CategoryObject;

/**
 * Created by Ahmed on 08-Sep-14.
 */
public class CategoriesLoader extends
        AsyncTaskLoader<AsyncResult<ArrayList<CategoryObject>>> {
    Context context;
    ArrayList<CategoryObject> feedItems;
    AsyncResult<ArrayList<CategoryObject>> data;

    public CategoriesLoader(Context context) {
        super(context);
        this.context = context;
        data = new AsyncResult<ArrayList<CategoryObject>>();
        feedItems = new ArrayList<CategoryObject>();
    }

    @Override
    public void deliverResult(AsyncResult<ArrayList<CategoryObject>> data) {
        if (isReset()) {
            // a query came in while the loader is stopped
            return;
        }
        this.data = data;
        super.deliverResult(data);
    }

    @Override
    public AsyncResult<ArrayList<CategoryObject>> loadInBackground() {
        try {

            feedItems = API.GetCategoriesList();

        } catch (Exception e) {
            data.setException(e);
        }
        data.setData(feedItems);
        return data;
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        data = null;
    }

}
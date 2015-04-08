package news.turndigital.com.turndigitalnews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import api.API;
import datamodels.AsyncResult;
import datamodels.CategoryObject;
import utils.Constants;
import views.DynamicListView;
import views.StableArrayAdapter;

public class EditTopicsFragment extends Fragment {
    public static final String TAG = "edit_topics_fragment";

    ArrayList<Pair<Integer, String>> listItems;
    public ArrayList<Integer> checkedItems;
    DynamicListView mListView;
    protected View mMainView;
    protected View mProgressView;
    protected View mErrorView;
    private EditActivity activity;
    private Context context;
    ArrayList<Integer> ids;
    ArrayList<String> names;
    StableArrayAdapter adapter;
    ArrayAdapter<String> dockedListAdapter;
    ArrayList<Integer> dockedIds;
    ArrayList<String> dockedStringItems;

    private AsyncResult<ArrayList<CategoryObject>> categories;
    private boolean fragmentCreated;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit, container,
                false);
        initComponents(rootView);
        return rootView;
    }

    private void initComponents(View rootView) {
        activity = (EditActivity) getActivity();
        context = activity.getApplicationContext();

        mMainView = rootView.findViewById(R.id.main_view);
        mProgressView = rootView.findViewById(R.id.progress_view);
        mErrorView = rootView.findViewById(R.id.error_view);

        mListView = (DynamicListView) rootView.findViewById(R.id.list);
        mListView.setOrdering();

        rootView.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                packageCheckedItemsAndFinish();
            }
        });

        if (fragmentCreated) {
            FillUI(categories);
        } else {
            loadCategories();
            fragmentCreated = true;
        }

        activity.editTopicsFragment = this;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!packageCheckedItemsAndFinish()) {
                activity.setResult(Activity.RESULT_CANCELED, null);
                activity.finish();
            }
        } else if (item.getItemId() == R.id.action_clear_all) {
            int dockedItemsSize = 0;
            if (dockedIds != null) {
                dockedItemsSize = dockedIds.size();
            }
            int listItemsSize = 0;
            if (listItems != null) {
                listItemsSize = listItems.size();
            }

            for (int i = dockedItemsSize; i < listItemsSize; i++) {
                mListView.setItemChecked(i, false);
            }
        } else if (item.getItemId() == R.id.action_select_all) {
            int dockedItemsSize = 0;
            if (dockedIds != null) {
                dockedItemsSize = dockedIds.size();
            }
            int listItemsSize = 0;
            if (listItems != null) {
                listItemsSize = listItems.size();
            }

            for (int i = dockedItemsSize; i < listItemsSize; i++) {
                mListView.setItemChecked(i, true);
            }
        }
        return true;

    }

    public boolean packageCheckedItemsAndFinish() {
        boolean flag = false;
        if (!ApplicationController.getInstance().IsNetworkAvaliable()) {
            flag = false;
            Toast.makeText(activity, "No internet connection", Toast.LENGTH_LONG).show();
            return flag;
        }

        ArrayList<Integer> checkedCategoriesIds = new ArrayList<Integer>();
        ArrayList<Integer> checkedProvidersIds = new ArrayList<Integer>();

        Intent data = new Intent();

        if (!listItems.isEmpty()) {
            SparseBooleanArray checkedItemPositions = mListView.getCheckedItemPositions();
            for (int i = 0; i < checkedItemPositions.size(); i++) {
                if (checkedItemPositions.valueAt(i)) {
                    int id = (int) adapter.getItemId(checkedItemPositions.keyAt(i));
                    checkedCategoriesIds.add(id);
                }
            }

            data.putExtra(Constants.EXTRA_CHECKED_IDS_CATEGORIES, checkedCategoriesIds);
            activity.setResult(Activity.RESULT_OK, data);
            flag = true;
        }

        if (activity.editProvidersFragment.listItems != null) {
            if (!activity.editProvidersFragment.listItems.isEmpty()) {
                SparseBooleanArray checkedItemPositions = activity.editProvidersFragment.mListView.getCheckedItemPositions();
                for (int i = 0; i < checkedItemPositions.size(); i++) {
                    if (checkedItemPositions.valueAt(i)) {
                        int id = (int) activity.editProvidersFragment.adapter.getItemId(checkedItemPositions.keyAt(i));
                        checkedProvidersIds.add(id);
                    }
                }

                // check if users selected all providers, so don't send providers to request
                if (checkedProvidersIds.size() == activity.editProvidersFragment.providersSize) {
                    data.putExtra(Constants.EXTRA_ALL_SELECTED, true);
                } else {
                    data.putExtra(Constants.EXTRA_ALL_SELECTED, false);
                }

                data.putExtra(Constants.EXTRA_CHECKED_IDS_PROVIDERS, checkedProvidersIds);
                activity.setResult(Activity.RESULT_OK, data);
                flag = true;
            }
        }

        activity.finish();
        return flag;
    }

    private void loadCategories() {
        if (!activity.isFinishing()) {
            showProgress(true);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<CategoryObject> feedItems = new ArrayList<CategoryObject>();
                final AsyncResult<ArrayList<CategoryObject>> data = new AsyncResult<ArrayList<CategoryObject>>();
                try {
                    feedItems = API.GetCategoriesList();
                    data.setData(feedItems);
                    if (!activity.isFinishing()) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                                FillUI(data);
                            }
                        });
                    }
                } catch (Exception e) {
                    data.setException(e);
                    if (!activity.isFinishing()) {
                        showError();
                    }
                }

                Exception exception = data.getException();
                if (exception != null) {
                    if (!activity.isFinishing()) {
                        showError();
                    }
                }
            }
        }).start();
    }

    class CategoryCallback implements
            LoaderManager.LoaderCallbacks<AsyncResult<ArrayList<CategoryObject>>> {

        @Override
        public Loader<AsyncResult<ArrayList<CategoryObject>>> onCreateLoader(int arg0,
                                                                             Bundle arg1) {
            showProgress(true);
            return new CategoriesLoader(context);
        }

        @Override
        public void onLoadFinished(
                Loader<AsyncResult<ArrayList<CategoryObject>>> loader,
                AsyncResult<ArrayList<CategoryObject>> response) {
            Exception exception = response.getException();
            if (exception != null) {
                showError();
            } else {
                FillUI(response);
            }


        }

        @Override
        public void onLoaderReset(Loader<AsyncResult<ArrayList<CategoryObject>>> arg0) {
            // TODO Auto-generated method stub

        }

    }

    private void FillUI(AsyncResult<ArrayList<CategoryObject>> response) {
        try {
            ArrayList<CategoryObject> categoryListResponse = response.getData();
            dockedIds = new ArrayList<Integer>();
            dockedStringItems = new ArrayList<String>();
            listItems = new ArrayList<Pair<Integer, String>>();
            ids = new ArrayList<Integer>();
            names = new ArrayList<String>();


            if (activity.getIntent().getExtras() != null) {
                checkedItems = activity.getIntent().getIntegerArrayListExtra((Constants.EXTRA_CATEGORIES));
            }
            categoryListResponse = ReorderCategories(response.getData(), checkedItems);


            for (int i = 0; i < categoryListResponse.size(); i++) {
                CategoryObject categoryObject = categoryListResponse.get(i);
                if (categoryObject.isDock() == 0) {
                    ids.add(categoryObject.getId());
                    names.add(categoryObject.getName());

                } else if (categoryObject.isDock() == 1) {
                    dockedIds.add(categoryObject.getId());
                    dockedStringItems.add(categoryObject.getName());

                }
                listItems.add(new Pair<Integer, String>(categoryObject.getId(), categoryObject.getName()));

            }

            ArrayList<Integer> combinedIds = new ArrayList<Integer>(dockedIds);
            combinedIds.addAll(ids);
            ArrayList<String> combinedStrings = new ArrayList<String>(dockedStringItems);
            combinedStrings.addAll(names);

            adapter = new StableArrayAdapter(activity, R.layout.list_categories_item, combinedStrings, combinedIds, dockedIds.size());

            mListView.setAdapter(adapter);
            mListView.setCheeseList(combinedStrings);
            mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);


            for (int i = 0; i < checkedItems.size(); i++) {
                int position;
                for (int j = 0; j < listItems.size(); j++) {
                    if (checkedItems.get(i).equals(listItems.get(j).first)) {
                        position = j;
                        mListView.setItemChecked(position, true);
                        break;
                    }
                }
            }
            showProgress(false);
        } catch (Exception e) {

        }
    }

    private ArrayList<CategoryObject> ReorderCategories(ArrayList<CategoryObject> data, ArrayList<Integer> checkedItems) {
        ArrayList<CategoryObject> orderedList = new ArrayList<CategoryObject>();

        for (int i = 0; i < checkedItems.size(); i++) {
            CategoryObject toBeAdded = null;
            int positionToBeAdded = 0;
            for (int j = 0; j < data.size(); j++) {
                if (checkedItems.get(i).equals(data.get(j).getId())) {
                    toBeAdded = data.get(j);
                    positionToBeAdded = j;
                    break;
                }
            }
            if (toBeAdded != null) {
                orderedList.add(toBeAdded);
                data.remove(positionToBeAdded);
            }
        }

        orderedList.addAll(data);

        return orderedList;

    }

    static class CategoriesLoader extends
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

    public void showProgress(final boolean show) {
        try {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
                    // for very easy animations. If available, use these APIs to fade-in
                    // the progress spinner.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        int shortAnimTime = getResources().getInteger(
                                android.R.integer.config_shortAnimTime);

                        mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
                        mMainView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        mMainView.setVisibility(show ? View.GONE
                                                : View.VISIBLE);
                                    }
                                });

                        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                        mProgressView.animate().setDuration(shortAnimTime)
                                .alpha(show ? 1 : 0)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        mProgressView.setVisibility(show ? View.VISIBLE
                                                : View.GONE);
                                    }
                                });
                    } else {
                        // The ViewPropertyAnimator APIs are not available, so simply show
                        // and hide the relevant UI components.
                        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                        mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                }
            });
        } catch (Exception e) {

        }
    }

    public void showError() {
        try {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
                    // for very easy animations. If available, use these APIs to fade-in
                    // the progress spinner.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        int shortAnimTime = getResources().getInteger(
                                android.R.integer.config_shortAnimTime);

                        mMainView.setVisibility(View.GONE);
                        mMainView.animate().setDuration(shortAnimTime).alpha(0)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        mMainView.setVisibility(View.GONE);
                                    }
                                });

                        mProgressView.setVisibility(View.GONE);
                        mProgressView.animate().setDuration(shortAnimTime).alpha(0)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        mProgressView.setVisibility(View.GONE);
                                    }
                                });

                        mErrorView.setVisibility(View.VISIBLE);
                        mErrorView.animate().setDuration(shortAnimTime).alpha(1)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        mErrorView.setVisibility(View.VISIBLE);
                                    }
                                });

                    } else {
                        // The ViewPropertyAnimator APIs are not available, so simply show
                        // and hide the relevant UI components.
                        mProgressView.setVisibility(View.GONE);
                        mMainView.setVisibility(View.GONE);
                        mErrorView.setVisibility(View.VISIBLE);
                    }
                }
            });
        } catch (Exception e) {

        }
    }
}

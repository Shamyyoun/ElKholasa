package news.turndigital.com.turndigitalnews;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.lucasr.twowayview.TwoWayView;

import java.lang.Exception;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import api.API;
import api.ApiClient;
import datamodels.AsyncResult;
import datamodels.CategoryObject;
import datamodels.FeedItem;
import datamodels.FeedItemsHolder;
import datamodels.GetFeedResponse;
import datamodels.ProviderObject;
import json.FeedItemsHandler;
import json.JSONParser;
import json.ProvidersHandler;
import utils.Constants;
import utils.ViewUtil;
import views.PostsAdapter;
import views.ProgressFragment;

/**
 * Created by Ahmed on 24-Jun-14.
 */
public class MainFragment extends ProgressFragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final String TAG = "main_fragment";

    private MainActivity activity;

    private static final int DEFAULT_CATEGORY_NUMBER = 7;
    ArrayList<GetFeedResponse> feedList;
    private View rootView;

    boolean firstLoad;
    static ArrayList<Integer> categoryList;
    static ArrayList<Integer> providersList;

    protected EditText textSearch;
    private ImageButton buttonSearch;
    private ListView listPosts;
    private TextView textNoResults;
    private ArrayList<FeedItem> searchFeedItems;
    private SearchTask searchTask;

    TextView mLastUpdatedTime;
    private Menu mOptionsMenu;
    private MenuItem refreshItem;
    private TextSwitcher mSwitcher;
    private int mInterval = 10 * 1000; // 10 seconds by default, can be changed later
    private Handler mHandler;
    private Runnable mStatusChecker;
    // Array of String to Show In TextSwitcher
    List<String> textToShow;
    List<Intent> tickerIntents;
    int messageCount;
    // to keep current Index of text
    int currentIndex = -1;

    boolean tickerStarted = false;

    // google analytics objects
    private Tracker tracker;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        activity = (MainActivity) getActivity();
        activity.mainFragment = this;

        // google analytics objects
        tracker = ApplicationController.getInstance()
                .getTracker(ApplicationController.TrackerName.APP_TRACKER);

        // customize activity
        setHasOptionsMenu(true);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);


        context = getActivity();

        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mMainView = rootView.findViewById(R.id.main_view);
        mCurrentView = mMainView;
        mProgressView = rootView.findViewById(R.id.progress_view);
        mErrorView = rootView.findViewById(R.id.error_view);

        rootView.findViewById(R.id.btn_edit_topic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToEditActivity();
            }
        });

        // ---- init search objects ----
        textSearch = (EditText) rootView.findViewById(R.id.text_search);
        buttonSearch = (ImageButton) rootView.findViewById(R.id.button_search);
        listPosts = (ListView) rootView.findViewById(R.id.list_posts);
        textNoResults = (TextView) rootView.findViewById(R.id.text_noResults);

        if (savedInstanceState != null) {
            FeedItemsHolder feedItemsHolder = (FeedItemsHolder) savedInstanceState.getSerializable(Constants.KEY_FEED_ITEMS_HOLDER);
            if (feedItemsHolder != null) {
                searchFeedItems = feedItemsHolder.feedItems;
            }

            if (searchFeedItems != null) {

                // check list size
                if (searchFeedItems.size() == 0) {
                    ViewUtil.showView(textNoResults, true);
                    ViewUtil.showView(listPosts, false);
                } else {
                    PostsAdapter adapter = new PostsAdapter(getActivity(), R.layout.list_posts_item, searchFeedItems);
                    listPosts.setAdapter(adapter);
                    ViewUtil.showView(textNoResults, false);
                    ViewUtil.showView(listPosts, true);
                }
            }
        }

        textSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                onTextSearchHasFocus(hasFocus, savedInstanceState);
            }
        });

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!textSearch.getText().toString().isEmpty()) {
                    if (searchTask != null) {
                        searchTask.cancel(true);
                    }
                    searchTask = new SearchTask();
                    searchTask.execute();
                }
            }
        });

        listPosts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // goto details activity
                Intent detailsIntent = new Intent(getActivity(), DetailsActivity.class);
                detailsIntent.putExtra(Constants.ARTICLE_LIST, searchFeedItems);
                detailsIntent.putExtra(Constants.ARTICLE_NUMBER, position);
                detailsIntent.putExtra(Constants.ARTICLE_SECTION_TITLE, "Search results for \"" + textSearch.getText() + "\"");
                detailsIntent.putExtra(Constants.KEY_DETAILS_SCREEN_NAME, "Search");

                startActivity(detailsIntent);
            }
        });

        mLastUpdatedTime = (TextView) rootView.findViewById(R.id.txt_last_updated);
        mSwitcher = (TextSwitcher) rootView.findViewById(R.id.textSwitcher);

        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        mSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                // create new textView and set the properties like clolr, size etc
                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                TextView view = (TextView) layoutInflater.inflate(R.layout.ticker_item, null, false);
                return view;
            }
        });

        // Declare the in and out animations and initialize them
        Animation in = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);

        // set the animation type of textSwitcher
        mSwitcher.setInAnimation(in);
        mSwitcher.setOutAnimation(out);

        mHandler = new Handler();

        tickerIntents = new ArrayList<Intent>();


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        firstLoad = sp.getBoolean(Constants.PREF_FIRST_LOAD, true);

        return rootView;
    }

    private void onTextSearchHasFocus(boolean hasFocus, Bundle savedInstanceState) {
        if (hasFocus) {
            // hide main view
            ViewUtil.showView(mMainView, false);
            ViewUtil.showView(mProgressView, false);
            ViewUtil.showView(mErrorView, false);

            if (savedInstanceState == null) {
                // send screen view
                tracker.setScreenName(Constants.GA_SCREEN_SEARCH);
                HitBuilders.AppViewBuilder builder = new HitBuilders.AppViewBuilder();
                tracker.send(builder.build());
            }
        } else {
            // cancel search task if running
            if (searchTask != null) {
                searchTask.cancel(true);
                // clear list items
                if (searchFeedItems != null) {
                    searchFeedItems = null;
                    ((PostsAdapter) listPosts.getAdapter()).notifyDataSetChanged();
                }
            }

            // hide keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(textSearch.getWindowToken(), 0);

            // show main view
            textSearch.setText("");
            ViewUtil.showView(listPosts, false);
            ViewUtil.showView(textNoResults, false);
            ViewUtil.showView(mErrorView, false);
            ViewUtil.showView(mProgressView, false);
            ViewUtil.showView(mCurrentView, true);

            if (savedInstanceState == null) {
                // send screen view
                tracker.setScreenName(Constants.GA_SCREEN_HOME);
                HitBuilders.AppViewBuilder builder = new HitBuilders.AppViewBuilder();
                tracker.send(builder.build());
            }
        }
    }

    private class SearchTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // hide keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(textSearch.getWindowToken(), 0);

            // show loading view
            ViewUtil.showView(textNoResults, false);
            ViewUtil.showView(listPosts, false);
            ViewUtil.showView(mCurrentView, false);
            ViewUtil.showOneView(mProgressView, listPosts, mErrorView);
        }

        @Override
        protected Void doInBackground(Void... params) {
            String url = ApiClient.endpoint + "/custom-feeds/?action=search_by_keyword&limit=" + ApplicationController.SEARCH_LIMIT + "&keyword=" + textSearch.getText();
            JSONParser jsonParser = new JSONParser(url);
            String response = jsonParser.parse();

            FeedItemsHandler handler = new FeedItemsHandler(response);
            searchFeedItems = handler.handle();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            ViewUtil.showView(mCurrentView, false);

            // check result
            if (searchFeedItems != null) {
                // check list size
                if (searchFeedItems.size() == 0) {
                    ViewUtil.showView(textNoResults, true);
                    ViewUtil.showView(listPosts, false);
                    ViewUtil.showView(mProgressView, false);
                    ViewUtil.showView(mErrorView, false);
                } else {
                    PostsAdapter adapter = new PostsAdapter(getActivity(), R.layout.list_posts_item, searchFeedItems);
                    listPosts.setAdapter(adapter);

                    // show main view
                    ViewUtil.showOneView(listPosts, mErrorView, mProgressView);
                }
            } else {
                // show error view
                ViewUtil.showOneView(mErrorView, listPosts, mProgressView);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRepeatingTask();

        if (searchTask != null) {
            searchTask.cancel(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startRepeatingTask();
    }


    private void updateNewsTitles() {
        currentIndex++;
        // If index reaches maximum reset it
        if (currentIndex == messageCount)
            currentIndex = 0;
        if (textToShow.size() > 0 && textToShow.size() >= currentIndex)
            mSwitcher.setText(textToShow.get(currentIndex));
        mSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivityForResult(tickerIntents.get(currentIndex), Constants.REQUEST_CODE_DETAILS);
                } catch (Exception e) {
                }
            }
        });
    }

    void startRepeatingTask() {
        if (mStatusChecker != null) {
            currentIndex = 0;
            mStatusChecker.run();
        }
    }

    void stopRepeatingTask() {
        if (mStatusChecker != null) {
            mHandler.removeCallbacks(mStatusChecker);
        }
    }

    void saveFeedList() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(feedList);
        editor.putString(Constants.PREF_FEED_LIST, json).commit();
    }

    ArrayList<GetFeedResponse> getCachedFeedList() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Gson gson = new Gson();
        String json = sp.getString(Constants.PREF_FEED_LIST, "");
        Type type = new TypeToken<ArrayList<GetFeedResponse>>() {
        }.getType();
        ArrayList<GetFeedResponse> mObject = gson.fromJson(json, type);
        return mObject;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        categoryList = new ArrayList();
        providersList = new ArrayList();

        if (firstLoad) {
            getLoaderManager().initLoader(1, null, new CategoryCallback()).forceLoad();
        } else {
            FillUI(getCachedFeedList());
            mLastUpdatedTime.setText(getString(R.string.updated) + getLastUpdated());
            categoryList = ApplicationController.getCategoryList(getActivity());
            providersList = ApplicationController.getProvidersList(getActivity());
            getLoaderManager().initLoader(0, null, new FeedLoaderCallback()).forceLoad();
        }
    }

    private void clearViews() {
        mSwitcher.setText("");

        ViewGroup mainView = (ViewGroup) rootView.findViewById(R.id.main_view);
        LinearLayout categoriesView = (LinearLayout) mainView.findViewById(R.id.categories_view);
        categoriesView.removeAllViews();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (ApplicationController.getInstance().IsNetworkAvaliable()) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == Constants.REQUEST_CODE_EDIT) {
                    // save checked categories
                    try {
                        ArrayList<Integer> list1 = new ArrayList<Integer>(data.getIntegerArrayListExtra(Constants.EXTRA_CHECKED_IDS_CATEGORIES));

                        categoryList = new ArrayList();
                        for (int i = 0; i < list1.size(); i++) {
                            categoryList.add(list1.get(i));
                        }

                        ApplicationController.saveCategoryList(categoryList, getActivity());
                    } catch (Exception e) {
                    }

                    // save checked providers
                    try {
                        ArrayList<Integer> list2 = new ArrayList<Integer>(data.getIntegerArrayListExtra(Constants.EXTRA_CHECKED_IDS_PROVIDERS));
                        boolean allSelected = data.getBooleanExtra(Constants.EXTRA_ALL_SELECTED, false);
                        providersList = new ArrayList();
                        for (int i = 0; i < list2.size(); i++) {
                            providersList.add(list2.get(i));
                        }

                        ApplicationController.saveAllProvidersSelected(allSelected, getActivity());
                        ApplicationController.saveProvidersList(providersList, getActivity());
                    } catch (Exception e) {
                    }
                }
                clearViews();
                showProgress();
                getLoaderManager().restartLoader(0, null, new FeedLoaderCallback()).forceLoad();
            }
        }
    }

    @Override
    public void onRefresh() {
        Refresh();
    }

    void Refresh() {
        if (ApplicationController.getInstance().IsNetworkAvaliable()) {
            if (firstLoad) {
                getLoaderManager().restartLoader(0, null, new CategoryCallback()).forceLoad();

            } else
                getLoaderManager().restartLoader(0, null, new FeedLoaderCallback()).forceLoad();
        } else {
            if (refreshItem != null)
                refreshItem.setActionView(null);
            Toast.makeText(getActivity(), R.string.error_check_internet, Toast.LENGTH_LONG).show();
        }
    }

    private void onTags() {
        Intent intent = new Intent(getActivity(), TagsActivity.class);
        getActivity().startActivity(intent);
    }


    class FeedLoaderCallback implements
            LoaderManager.LoaderCallbacks<AsyncResult<ArrayList<GetFeedResponse>>> {

        @Override
        public Loader<AsyncResult<ArrayList<GetFeedResponse>>> onCreateLoader(int arg0,
                                                                              Bundle arg1) {

            textSearch.clearFocus();

            //showProgress(true);
            if (refreshItem != null)
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            return new FeedLoader(getActivity());
        }

        @Override
        public void onLoadFinished(
                Loader<AsyncResult<ArrayList<GetFeedResponse>>> loader,
                AsyncResult<ArrayList<GetFeedResponse>> response) {
            Exception exception = response.getException();
            if (exception != null) {
                if (refreshItem != null)
                    refreshItem.setActionView(null);

            } else {
                Calendar calendar = new GregorianCalendar(Locale.ENGLISH);
                Date now = calendar.getTime();
                SimpleDateFormat format = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
                String time = format.format(now);

                mLastUpdatedTime.setText(getString(R.string.updated) + time);

                FillUI(response.getData());
                saveFeedList();
                saveLastUpdated(time);
            }


        }

        @Override
        public void onLoaderReset(Loader<AsyncResult<ArrayList<GetFeedResponse>>> arg0) {
        }

    }

    static class FeedLoader extends
            AsyncTaskLoader<AsyncResult<ArrayList<GetFeedResponse>>> {
        Context context;
        ArrayList<GetFeedResponse> feedItems;
        AsyncResult<ArrayList<GetFeedResponse>> data;

        public FeedLoader(Context context) {
            super(context);
            this.context = context;
            data = new AsyncResult<ArrayList<GetFeedResponse>>();
            feedItems = new ArrayList<GetFeedResponse>();
        }

        @Override
        public void deliverResult(AsyncResult<ArrayList<GetFeedResponse>> data) {
            if (isReset()) {
                // a query came in while the loader is stopped
                return;
            }
            this.data = data;
            super.deliverResult(data);
        }

        @Override
        public AsyncResult<ArrayList<GetFeedResponse>> loadInBackground() {
            try {
                // check if all selected
                boolean allSelected = ApplicationController.isAllProvidersSelected(getContext());

                if (allSelected) {
                    feedItems = API.GetFeed(categoryList, new ArrayList()).getData();
                } else {
                    feedItems = API.GetFeed(categoryList, providersList).getData();
                }

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

    private void FillUI(ArrayList<GetFeedResponse> response) {
        clearViews();
        textSearch.clearFocus();

        feedList = response;

        ViewGroup mainView = (ViewGroup) rootView.findViewById(R.id.main_view);
        LinearLayout categoriesView = (LinearLayout) mainView.findViewById(R.id.categories_view);

        LayoutInflater vi = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        textToShow = new ArrayList<String>();

        tickerIntents = new ArrayList<Intent>();

        if (feedList != null) {
            int viewIndex = 0;
            for (int i = 0; i < feedList.size(); i++) {
                int size = feedList.get(i).getPosts().size();

                if (size != 0) {
                    View v = vi.inflate(R.layout.horizontal_listview, null);
                    TwoWayView mListView = (TwoWayView) v.findViewById(R.id.list);
                    final ImageView imageLeftArrow = (ImageView) v.findViewById(R.id.image_galleryArrowLeft);
                    final ImageView imageRightArrow = (ImageView) v.findViewById(R.id.image_galleryArrowRight);

                    mListView.setItemMargin(10);

                    mListView.setAdapter(new NewsListAdapter(getActivity(), R.layout.gallery_item, feedList.get(i).getPosts(), i));
                    mListView.setOnItemClickListener(new OnNewsItemClickListener(i));

                    // add scroll listener
//                    mListView.setOnScrollListener(new TwoWayView.OnScrollListener() {
//                        @Override
//                        public void onScrollStateChanged(TwoWayView twoWayView, int i) {
//                        }
//
//                        @Override
//                        public void onScroll(TwoWayView twoWayView, int firstItemIndex, int itemsDisplayedNum, int itemsNum) {
//                            if (firstItemIndex == 0) {
//                                // moved to most left >> hide left arrow
//                                ViewUtil.showView(imageLeftArrow, false);
//                            } else {
//                                // show left arrow
//                                ViewUtil.showView(imageLeftArrow, true);
//                            }
//
//                            if ((firstItemIndex + itemsDisplayedNum) == itemsNum) {
//                                // moved to most right >> hide right arrow
//                                ViewUtil.showView(imageRightArrow, false);
//                            } else {
//                                // show right arrow
//                                ViewUtil.showView(imageRightArrow, true);
//                            }
//                        }
//                    });

                    int heightPixels = (int) getResources().getDimension(R.dimen.news_item_height);

                    categoriesView.addView(v, viewIndex, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPixels));

                    TextView categoryTitle = (TextView) v.findViewById(R.id.txt_title);
                    categoryTitle.setText(feedList.get(i).getName());

                    // add listener to title text
                    final int currentIndex = i;
                    categoryTitle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // goto posts activity TODO
                            Intent intent = new Intent(getActivity(), PostsActivity.class);
                            intent.putExtra(Constants.KEY_FEED_ITEMS, feedList.get(currentIndex).getPosts());
                            intent.putExtra(Constants.KEY_TITLE, feedList.get(currentIndex).getName());
                            intent.putExtra(Constants.KEY_CATEGORY_ID, feedList.get(currentIndex).getId());

                            startActivity(intent);
                        }
                    });

                    for (int j = 0; j < size; j++) {
                        FeedItem feedItem = feedList.get(i).getPosts().get(j);
                        if (feedItem.isTicker()) {
                            textToShow.add(feedItem.getTitle());
                            Intent detailsIntent = new Intent(getActivity(), DetailsActivity.class);
                            detailsIntent.putExtra(Constants.ARTICLE_LIST, feedList.get(i).getPosts());
                            detailsIntent.putExtra(Constants.ARTICLE_NUMBER, j);
                            detailsIntent.putExtra(Constants.ARTICLE_SECTION_TITLE, feedList.get(i).getName());
                            detailsIntent.putExtra(Constants.KEY_DETAILS_SCREEN_NAME, feedList.get(i).getName());
                            tickerIntents.add(detailsIntent);
                        }
                    }
                    viewIndex++;
                }
            }
        }

        messageCount = textToShow.size() > 0 ? textToShow.size() : 0;
        if (refreshItem != null)
            refreshItem.setActionView(null);
        mLastUpdatedTime.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

        // check if ticker intents size
        if (tickerIntents.size() != 0) {
            mSwitcher.setVisibility(View.VISIBLE);

            mStatusChecker = new Runnable() {
                @Override
                public void run() {
                    updateNewsTitles(); //this function can change value of mInterval.
                    mHandler.postDelayed(mStatusChecker, mInterval);
                }
            };
            startRepeatingTask();
        } else {
            mSwitcher.setVisibility(View.GONE);
        }

        showMainView();
    }

    private void initializeTicker() {
    }

    void saveLastUpdated(String time) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.PREF_LAST_UPDATED, time).commit();
    }

    String getLastUpdated() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return sp.getString(Constants.PREF_LAST_UPDATED, "");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        mOptionsMenu = menu;
        refreshItem = mOptionsMenu.findItem(R.id.action_refresh);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit_topic) {
            goToEditActivity();
        } else if (item.getItemId() == R.id.action_refresh) {
            Refresh();
        } else if (item.getItemId() == R.id.action_tags) {
            onTags();
        }
        return super.onOptionsItemSelected(item);
    }

    void goToEditActivity() {
        Intent intent = new Intent(getActivity(), EditActivity.class);
        intent.putExtra(Constants.EXTRA_CATEGORIES, categoryList);
        intent.putExtra(Constants.EXTRA_PROVIDERS, providersList);
        mStatusChecker = null;
        startActivityForResult(intent, Constants.REQUEST_CODE_EDIT);
    }

    class OnNewsItemClickListener implements AdapterView.OnItemClickListener {
        int index;

        public OnNewsItemClickListener(int index) {
            this.index = index;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent detailsIntent = new Intent(getActivity(), DetailsActivity.class);
            detailsIntent.putExtra(Constants.ARTICLE_LIST, feedList.get(index).getPosts());
            detailsIntent.putExtra(Constants.ARTICLE_NUMBER, position);
            detailsIntent.putExtra(Constants.ARTICLE_SECTION_TITLE, feedList.get(index).getName());
            detailsIntent.putExtra(Constants.KEY_DETAILS_SCREEN_NAME, feedList.get(index).getName());

            startActivityForResult(detailsIntent, Constants.REQUEST_CODE_DETAILS);
        }
    }


    public class NewsListAdapter extends ArrayAdapter<FeedItem> {
        int index;

        public NewsListAdapter(Context context, int resource, ArrayList<FeedItem> objects, int index) {
            super(context, resource, objects);
            this.index = index;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.gallery_item, null);

            TextView textView = (TextView) view.findViewById(R.id.txt_title);
            textView.setText(feedList.get(index).getPosts().get(position).getTitle());

            ImageView imageView = (ImageView) view.findViewById(R.id.imageView);

            String imageLink = feedList.get(index).getPosts().get(position).getThumbnail();
            if (!imageLink.isEmpty()) {
                Picasso.with(context).load(imageLink).error(R.drawable.not_found_list).resize(540, 244).into(imageView);
            } else {
                Picasso.with(context).load(R.drawable.not_found_list).into(imageView);
            }

            return view;
        }
    }

    class CategoryCallback implements
            LoaderManager.LoaderCallbacks<AsyncResult<ArrayList<CategoryObject>>> {

        @Override
        public Loader<AsyncResult<ArrayList<CategoryObject>>> onCreateLoader(int arg0,
                                                                             Bundle arg1) {
            showProgress();
            return new CategoriesLoader(getActivity());
        }

        @Override
        public void onLoadFinished(
                Loader<AsyncResult<ArrayList<CategoryObject>>> loader,
                AsyncResult<ArrayList<CategoryObject>> response) {
            Exception exception = response.getException();
            if (exception != null) {
                showError();
            } else {
                LoadCategoriesAndProvidersFirstTime(response);
                getLoaderManager().initLoader(0, null, new FeedLoaderCallback()).forceLoad();
            }
        }

        @Override
        public void onLoaderReset(Loader<AsyncResult<ArrayList<CategoryObject>>> arg0) {
        }

    }

    private void LoadCategoriesAndProvidersFirstTime(AsyncResult<ArrayList<CategoryObject>> response) {
        firstLoad = false;

        ArrayList<CategoryObject> categoryListResponse = response.getData();

        for (int i = 0; i < categoryListResponse.size(); i++) {
            CategoryObject categoryObject = categoryListResponse.get(i);
            categoryList.add(categoryObject.getId());
        }

        // load providers
        ArrayList<ProviderObject> providerObjects = new ArrayList<ProviderObject>();
        try {
            JSONParser jsonParser = new JSONParser("http://alkholasa.net/feed-gen/?key=1");
            String response2 = jsonParser.execute().get();

            if (response2 == null) {
                showError();
            } else {
                ProvidersHandler providersHandler = new ProvidersHandler(response2);
                providerObjects = providersHandler.handle();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < providerObjects.size(); i++) {
            ProviderObject providerObject = providerObjects.get(i);
            providersList.add(providerObject.getId());
        }

        // save data to SP
        ApplicationController.saveCategoryList(categoryList, getActivity());
        ApplicationController.saveProvidersList(providersList, getActivity());

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(Constants.PREF_FIRST_LOAD, false).commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        FeedItemsHolder feedItemsHolder = new FeedItemsHolder();
        feedItemsHolder.feedItems = searchFeedItems;

        outState.putSerializable(Constants.KEY_FEED_ITEMS_HOLDER, feedItemsHolder);
    }
}

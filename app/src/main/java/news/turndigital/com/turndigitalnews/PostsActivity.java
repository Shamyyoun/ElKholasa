package news.turndigital.com.turndigitalnews;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

import api.ApiClient;
import datamodels.FeedItem;
import datamodels.FeedItemsHolder;
import json.FeedItemsHandler;
import json.JSONParser;
import json.NewFeedItemsHandler;
import utils.Constants;
import utils.ViewUtil;
import views.PostsAdapter;


public class PostsActivity extends BaseActivity {
    private String title;

    // arguments if arrived from main fragment
    private ArrayList<FeedItem> feedItems;
    private int categoryId = -1; // default value indicating there is no category id

    // arguments if arrived from tags activity
    private String[] tagsIds;
    private String tagsTitles;

    private ListView listPosts;
    private PostsAdapter postsAdapter;
    private View viewFooter;
    private ProgressBar progressBarLoadMore;

    private View loadingView;
    private View errorView;

    private LoadMoreTask loadMoreTask;

    private ArrayList<AsyncTask<Void, Void, Void>> tasks;

    // google analytics objects
    private Tracker tracker;

    private String detailsScreenName; // used to pass it to details activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        initComponents(savedInstanceState);
    }

    private void initComponents(Bundle savedInstanceState) {
        title = getIntent().getExtras().getString(Constants.KEY_TITLE);
        categoryId = getIntent().getExtras().getInt(Constants.KEY_CATEGORY_ID, -1);
        tagsIds = getIntent().getExtras().getStringArray(Constants.KEY_TAGS_IDS);
        tagsTitles = getIntent().getExtras().getString(Constants.KEY_TAGS_TITLES);

        // google analytics objects
        tracker = ApplicationController.getInstance().getTracker(ApplicationController.TrackerName.APP_TRACKER);

        // check arrived from home or from tags to send suitable screen name to GA
        String screenName = "";
        if (tagsIds == null) {
            // arrived from home
            screenName = title;
            detailsScreenName = title;
        } else {
            // arrived from tags
            screenName = tagsTitles;
            detailsScreenName = "#Tags";
        }

        if(savedInstanceState == null) {
            // send screen view
            tracker.setScreenName(screenName);
            HitBuilders.AppViewBuilder builder = new HitBuilders.AppViewBuilder();
            tracker.send(builder.build());
        }

        // try to get feed items from saved instance bundle
        if (savedInstanceState != null) {
            FeedItemsHolder feedItemsHolder = (FeedItemsHolder) savedInstanceState.getSerializable(Constants.KEY_FEED_ITEMS_HOLDER);
            if (feedItemsHolder != null)
                feedItems = feedItemsHolder.feedItems;
        }

        // if null >> no saved instance, so try to get it from passed intent
        if (feedItems == null) {
            feedItems = (ArrayList<FeedItem>) getIntent().getExtras().get(Constants.KEY_FEED_ITEMS);
        }

        listPosts = (ListView) findViewById(R.id.list_posts);
        viewFooter = getLayoutInflater().inflate(R.layout.list_posts_footer, null);
        progressBarLoadMore = (ProgressBar) viewFooter.findViewById(R.id.progressView_loadMore);
        listPosts.addFooterView(viewFooter);

        loadingView = findViewById(R.id.view_loading);
        errorView = findViewById(R.id.view_error);

        tasks = new ArrayList<AsyncTask<Void, Void, Void>>();

        // check if category id exists, so scroll listener to the listview
        if (categoryId != -1) {
            listPosts.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (((firstVisibleItem + visibleItemCount) == totalItemCount) && totalItemCount != 0) {
                        // reached the last item >> check to see load more
                        if (loadMoreTask == null) {
                            int lastId = feedItems.get(feedItems.size() - 1).getId();
                            loadMoreTask = new LoadMoreTask(categoryId, lastId);
                            tasks.add(loadMoreTask);

                            loadMoreTask.execute();
                        }
                    }
                }
            });
        }

        // customize activity
        setTitle(title);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // check if feed items is null
        if (feedItems != null) {
            fillUi();
        } else {
            FeedItemsTask feedItemsTask = new FeedItemsTask();
            tasks.add(feedItemsTask);

            feedItemsTask.execute();
        }
    }

    /*
     * used to fetch feed items from server
     */
    private class FeedItemsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ViewUtil.showOneView(loadingView, listPosts, errorView);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // prepare url
            String url = ApiClient.endpoint + "/custom-feeds/?action=search_by_tag&limit=" + ApplicationController.ARTICLES_IN_TAGS_LIMIT + "&id=";
            for (int i = 0; i < tagsIds.length; i++) {
                url += (tagsIds[i] + ",");
            }

            JSONParser jsonParser = new JSONParser(url);
            String response = jsonParser.parse();

            FeedItemsHandler handler = new FeedItemsHandler(response);
            feedItems = handler.handle();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (feedItems != null) {
                fillUi();
            } else {
                ViewUtil.showOneView(errorView, listPosts, loadingView);
            }
        }
    }

    private void fillUi() {
        // set list view adapter
        postsAdapter = new PostsAdapter(this, R.layout.list_posts_item, feedItems);
        listPosts.setAdapter(postsAdapter);

        // show main view
        ViewUtil.showOneView(listPosts, errorView, loadingView);


        // add item click listener
        listPosts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // goto details activity
                Intent detailsIntent = new Intent(PostsActivity.this, DetailsActivity.class);
                detailsIntent.putExtra(Constants.ARTICLE_LIST, feedItems);
                detailsIntent.putExtra(Constants.ARTICLE_NUMBER, position);
                detailsIntent.putExtra(Constants.ARTICLE_SECTION_TITLE, title);
                detailsIntent.putExtra(Constants.KEY_DETAILS_SCREEN_NAME, detailsScreenName);

                startActivity(detailsIntent);
            }
        });

    }

    private void onLoadMore() {
        int lastId = feedItems.get(feedItems.size() - 1).getId();
        LoadMoreTask loadMoreTask = new LoadMoreTask(categoryId, lastId);
        tasks.add(loadMoreTask);

        loadMoreTask.execute();
    }

    private class LoadMoreTask extends AsyncTask<Void, Void, Void> {
        private int categoryId;
        private int lastId;

        private ArrayList<FeedItem> newFeedItems;

        public LoadMoreTask(int categoryId, int lastId) {
            this.categoryId = categoryId;
            this.lastId = lastId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ViewUtil.showView(progressBarLoadMore, true);

//            // check to add loading footer or not
//            if (listPosts.getFooterViewsCount() == 0) {
//                // add loading footer
//                listPosts.addFooterView(viewFooter);
//            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            String url = ApiClient.endpoint + "/custom-feeds/?user=admin&pass=121314&limit=7&array=1&cat=" + categoryId + "&lastid=" + lastId;
            JSONParser jsonParser = new JSONParser(url);
            String response = jsonParser.parse();

            NewFeedItemsHandler feedItemsHandler = new NewFeedItemsHandler(response);
            newFeedItems = feedItemsHandler.handle();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // hide progress
            ViewUtil.showView(progressBarLoadMore, false);

//            // remove loading footer
//            listPosts.removeFooterView(viewFooter);

            // check if there is data or not
            if (newFeedItems != null) {
                // check list size to load more next time or not
                // add data to listview
                feedItems.addAll(newFeedItems);
                postsAdapter.notifyDataSetChanged();

                // check data size
                if (newFeedItems.size() != 0) {
                    // set load more task by null to load next time
                    loadMoreTask = null;
                }
            } else {
                // set load more task by null to load next time
                loadMoreTask = null;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result;
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                result = true;
                break;

            default:
                result = super.onOptionsItemSelected(item);
        }

        return result;
    }

    @Override
    protected void onPause() {
        // cancel running tasks
        for (AsyncTask<Void, Void, Void> task : tasks) {
            task.cancel(true);
        }

        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        FeedItemsHolder feedItemsHolder = new FeedItemsHolder();
        feedItemsHolder.feedItems = feedItems;
        outState.putSerializable(Constants.KEY_FEED_ITEMS_HOLDER, feedItemsHolder);
    }

}

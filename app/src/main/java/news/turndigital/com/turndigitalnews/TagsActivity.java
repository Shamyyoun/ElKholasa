package news.turndigital.com.turndigitalnews;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;

import api.ApiClient;
import datamodels.TagItem;
import json.JSONParser;
import json.TagsHandler;
import utils.Constants;
import utils.ViewUtil;
import views.TagsAdapter;

public class TagsActivity extends Activity {
    private View mainView;
    private View loadingView;
    private View errorView;

    private ListView listTags;
    private Button buttonOk;

    private ArrayList<TagItem> tags;

    private TagsTask tagsTask;

    // google analytics objects
    private Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);

        initComponents(savedInstanceState);
    }

    private void initComponents(Bundle savedInstanceState) {
        mainView = findViewById(R.id.view_main);
        loadingView = findViewById(R.id.view_loading);
        errorView = findViewById(R.id.view_error);

        listTags = (ListView) findViewById(R.id.list_tags);
        buttonOk = (Button) findViewById(R.id.btn_ok);

        // google analytics objects
        tracker = ApplicationController.getInstance().getTracker(ApplicationController.TrackerName.APP_TRACKER);

        if (savedInstanceState == null) {
            // send screen view
            HitBuilders.AppViewBuilder builder = new HitBuilders.AppViewBuilder();
            tracker.setScreenName(Constants.GA_SCREEN_TAGS);
            tracker.send(builder.build());
        }

        // customize components
        listTags.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // add listeners
        listTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listTags.setItemChecked(position, listTags.isItemChecked(position));
            }
        });
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get checked ids
                SparseBooleanArray checkedItemsPositions = listTags.getCheckedItemPositions();

                // check if no items selected
                if (checkedItemsPositions.size() != 0) {
                    String[] checkedIds = new String[checkedItemsPositions.size()];
                    String checkedTitles = "";
                    for (int i = 0; i < checkedItemsPositions.size(); i++) {
                        if (checkedItemsPositions.valueAt(i)) {
                            TagItem tagItem = tags.get(checkedItemsPositions.keyAt(i));
                            checkedIds[i] = tagItem.getId();

                            if (i != 0) {
                                checkedTitles += ", ";
                            }
                            checkedTitles += ("#" + tagItem.getName());
                        }
                    }

                    // open posts activity
                    Intent intent = new Intent(TagsActivity.this, PostsActivity.class);
                    intent.putExtra(Constants.KEY_TITLE, "# Results");
                    intent.putExtra(Constants.KEY_TAGS_IDS, checkedIds);
                    intent.putExtra(Constants.KEY_TAGS_TITLES, checkedTitles);

                    startActivity(intent);
                }
            }
        });


        // ----set data----
        // try to get tags from saved instance bundle
        if (savedInstanceState != null) {
            TagsHolder tagsHolder = (TagsHolder) savedInstanceState.getSerializable(Constants.KEY_TAGS_HOLDER);
            if (tagsHolder != null)
                tags = tagsHolder.tags;
        }

        // check if tags null
        if (tags == null) {
            // fetch and load tags
            tagsTask = new TagsTask();
            tagsTask.execute();
        } else {
            // just fill ui
            fillUi();
        }
    }

    private class TagsTask extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
            super.onPreExecute();
            ViewUtil.showOneView(loadingView, mainView, errorView);
        }

        protected Void doInBackground(Void... params) {
            String url = ApiClient.endpoint + "/tpub_feed/?key=5";
            JSONParser parser = new JSONParser(url);
            String response = parser.parse();

            TagsHandler handler = new TagsHandler(response);
            tags = handler.handle();

            return null;
        }

        protected void onPostExecute(Void paramVoid) {
            super.onPostExecute(paramVoid);
            if (tags != null) {
                fillUi();
            } else {
                ViewUtil.showOneView(errorView, mainView, loadingView);
            }
        }
    }

    private void fillUi() {
        // set list adapter
        TagsAdapter adapter = new TagsAdapter(this, R.layout.list_tags_item, tags);
        listTags.setAdapter(adapter);

        // show main view
        ViewUtil.showOneView(mainView, errorView, loadingView);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    protected void onPause() {
        if (tagsTask != null) {
            tagsTask.cancel(true);
        }

        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        TagsHolder tagsHolder = new TagsHolder();
        tagsHolder.tags = tags;
        outState.putSerializable(Constants.KEY_TAGS_HOLDER, tagsHolder);
    }

}
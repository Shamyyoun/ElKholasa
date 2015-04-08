package news.turndigital.com.turndigitalnews;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.widget.TextView;

import com.viewpagerindicator.LinePageIndicator;

import java.util.ArrayList;

import datamodels.FeedItem;
import utils.Constants;

public class DetailsActivity extends BaseActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    ArrayList<FeedItem> newsList;
    int articlePosition;
    String sectionTitle;

    String screenName; // for GA only
    int lastPageIndex = -1;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    LinePageIndicator mIndicator;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // customize activity
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // get last page index from intent as the current post index
        if (getIntent().getExtras() != null) {
            newsList = (ArrayList<FeedItem>) getIntent().getSerializableExtra(Constants.ARTICLE_LIST);
            articlePosition = getIntent().getIntExtra(Constants.ARTICLE_NUMBER, 0);
            sectionTitle = getIntent().getStringExtra(Constants.ARTICLE_SECTION_TITLE);
            screenName = getIntent().getStringExtra(Constants.KEY_DETAILS_SCREEN_NAME);
            TextView vNewsTitle = (TextView) findViewById(R.id.txt_title);
            vNewsTitle.setText(sectionTitle);
        }

        if (savedInstanceState != null) {
            lastPageIndex = savedInstanceState.getInt(Constants.KEY_LAST_PAGE_INDEX);
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(articlePosition);

        mIndicator = (LinePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mViewPager);
        mIndicator.setCurrentItem(articlePosition);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return DetailsFragment.newInstance(position + 1, newsList.get(position));
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return newsList.size();
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

    protected int getCurrentPageIndex() {
        return mViewPager.getCurrentItem();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.KEY_LAST_PAGE_INDEX, lastPageIndex);
    }
}

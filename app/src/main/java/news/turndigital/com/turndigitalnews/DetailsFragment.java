package news.turndigital.com.turndigitalnews;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import datamodels.FeedItem;

/**
 * Created by Ahmed on 27-Jun-14.
 */
public class DetailsFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_NEWS_OBJECT = "news_object";

    private DetailsActivity activity;

    private ShareActionProvider mShareActionProvider;
    private String newsTitle;
    private String newsProvider;
    private String imageLink;
    private String newsLink;
    private TextView vNewsContent;
    private TextView vNewsProvider;
    private TextView vNewsDate;
    private ImageView imageView;

    // google analytics objects
    private Tracker tracker;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DetailsFragment newInstance(int sectionNumber, FeedItem newsObject) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putSerializable(ARG_NEWS_OBJECT, newsObject);
        fragment.setArguments(args);
        return fragment;
    }

    public DetailsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (DetailsActivity) getActivity();

        FeedItem feedItem = (FeedItem) getArguments().getSerializable(ARG_NEWS_OBJECT);

        newsLink = feedItem.getUrl();

        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        vNewsContent = (TextView) rootView.findViewById(R.id.txt_content);
        String newsContent = Html.fromHtml(feedItem.getContent()).toString();
        vNewsContent.setText(newsContent);

        TextView vNewsTitle = (TextView) rootView.findViewById(R.id.txt_title);
        newsTitle = Html.fromHtml(feedItem.getTitle()).toString();
        vNewsTitle.setText(newsTitle);

        vNewsProvider = (TextView) rootView.findViewById(R.id.txt_provider);
        newsProvider = feedItem.getProvider();
        vNewsProvider.setText(newsProvider);

        vNewsDate = (TextView) rootView.findViewById(R.id.txt_date);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date newsDate = new Date();
        try {
            newsDate = df.parse(feedItem.getDate());
            Calendar calendar = new GregorianCalendar(Locale.ENGLISH);
            SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.ENGLISH);

            String date = format.format(newsDate);

            vNewsDate.setText(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        imageView = (ImageView) rootView.findViewById(R.id.imageView);
        imageLink = feedItem.getThumbnail();
        if (!imageLink.isEmpty()) {
            Picasso.with(getActivity()).load(imageLink).error(R.drawable.not_found_details).into(imageView);
        } else
            Picasso.with(getActivity()).load(R.drawable.not_found_details).into(imageView);

        // init google analytics
        tracker = ApplicationController.getInstance().getTracker(ApplicationController.TrackerName.APP_TRACKER);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.details, menu);

        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        mShareActionProvider.setShareIntent(getDefaultIntent());

        // Return true to display menu
        super.onCreateOptionsMenu(menu, inflater);

        // ---page has focus >> so send screen view to GA---
        if (activity.lastPageIndex != activity.getCurrentPageIndex()) {
            // decode url to send it in readable format
            String decodedUrl = newsLink;
            try {
                decodedUrl = java.net.URLDecoder.decode(newsLink, "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }
            String screenName = activity.screenName + " - " + decodedUrl;
            tracker.setScreenName(screenName);
            HitBuilders.AppViewBuilder builder = new HitBuilders.AppViewBuilder();
            tracker.send(builder.build());

            // store last page index
            activity.lastPageIndex = activity.getCurrentPageIndex();
        }
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    /**
     * Defines a default (dummy) share intent to initialize the action provider.
     * However, as soon as the actual content to be used in the intent
     * is known or changes, you must update the share intent by again calling
     * mShareActionProvider.setShareIntent()
     */
    private Intent getDefaultIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, newsTitle);
        intent.putExtra(Intent.EXTRA_TEXT, newsLink);
        return intent;
    }
}

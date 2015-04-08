package news.turndigital.com.turndigitalnews;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.viewpagerindicator.CirclePageIndicator;

public class TutorialFragment extends Fragment {
    public static final String TAG = "tutorial_fragment";
    private static final String KEY_INDEX = "key_index";

    private MainActivity activity;
    private View rootView;

    private ViewPager pager;
    private CirclePageIndicator indicator;

    @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_tutorial, container,
                false);
        initComponents(rootView);
        return rootView;
    }

    private void initComponents(View rootView) {
        activity = (MainActivity) getActivity();

        pager = (ViewPager) rootView.findViewById(R.id.pager);
        indicator = (CirclePageIndicator) rootView.findViewById(R.id.indicator);

        // customize activity
        setHasOptionsMenu(true);
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // set initials
        PagerAdapter adapter = new PagerAdapter();
        pager.setAdapter(adapter);
        indicator.setViewPager(pager);

            // create dialog
            String[] options = new String[] {"Go on with tutorial.", "Don't show this again."};
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity, android.R.style.Theme_Holo_Light));

            builder.setTitle("Welcome");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            onGoOn();
                            break;

                        case 1:
                            ApplicationController.saveHideTutorial(true, activity);
                            gotoMain();
                            break;
                    }
                }
            });
            builder.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tutorial, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result;
        switch (item.getItemId()) {
            case R.id.menu_item_skip:
                gotoMain();
                result = true;
                break;

            default:
                result = super.onOptionsItemSelected(item);
                break;
        }
        return result;
    }

    private void onGoOn() {
        unregisterForContextMenu(rootView);
    }

    private void gotoMain() {
        unregisterForContextMenu(rootView);

        getFragmentManager().beginTransaction()
                .replace(R.id.container, new MainFragment())
                .commit();
    }

    /*
     * view pager adapter
     */
    private class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter() {
            super(activity.getFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putInt(KEY_INDEX, position);

            PagerFragment fragment = new PagerFragment();
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    public static class PagerFragment extends Fragment {
        private ImageView image;
        private int index;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_tutorialscreen, container,
                    false);
            initComponents(rootView);
            return rootView;
        }

        private void initComponents(View rootView) {
            index = getArguments().getInt(KEY_INDEX);
            image = (ImageView) rootView.findViewById(R.id.image_tutorial);

            switch (index) {
                case 0:
                    image.setImageResource(R.drawable.tut1);
                    break;

                case 1:
                    image.setImageResource(R.drawable.tut2);
                    break;

                case 2:
                    image.setImageResource(R.drawable.tut3);
                    break;

                default:
                    image.setImageResource(R.drawable.tut1);
            }
        }
    }
}

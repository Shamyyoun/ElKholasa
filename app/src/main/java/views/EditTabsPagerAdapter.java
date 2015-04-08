package views;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import news.turndigital.com.turndigitalnews.EditProvidersFragment;
import news.turndigital.com.turndigitalnews.EditTopicsFragment;

public class EditTabsPagerAdapter extends FragmentPagerAdapter {
    public static final int TAG_TOPICS = 1;
    public static final int TAG_PROVIDERS = 2;

    public static int fragmentTag;
    public static Fragment currentFragment;

	public EditTabsPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int index) {

		switch (index) {
		case 0:
            EditTopicsFragment topicsFragment = new EditTopicsFragment();
            fragmentTag = TAG_TOPICS;
            currentFragment = topicsFragment;
			return topicsFragment;
		case 1:
            EditProvidersFragment providersFragment = new EditProvidersFragment();
            fragmentTag = TAG_PROVIDERS;
            currentFragment = providersFragment;
            return providersFragment;
		}

		return null;
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return 2;
	}

}

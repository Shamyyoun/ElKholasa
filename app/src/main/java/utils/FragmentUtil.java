package utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class FragmentUtil {
	private FragmentActivity activity;

	public FragmentUtil(FragmentActivity activity) {
		this.activity = activity;
	}

	public void gotoFragment(int containerLayout, Fragment fragment,
			String tag, Bundle bundle) {
		gotoTheFragment(containerLayout, fragment, tag, bundle);
	}

	public void gotoFragment(int containerLayout, Fragment fragment, String tag) {
		gotoTheFragment(containerLayout, fragment, tag, null);
	}

	private void gotoTheFragment(int containerLayout, Fragment fragment,
			String tag, Bundle bundle) {
		if (bundle != null) {
			fragment.setArguments(bundle);
		}

		if (!isFragmentInLayout(fragment, tag)) {
			FragmentTransaction ft = activity.getSupportFragmentManager()
					.beginTransaction();
			ft.replace(containerLayout, fragment, tag);
			ft.commit();
		}
	}

	private boolean isFragmentInLayout(Fragment fragment, String tag) {
		Fragment myFragment = activity.getSupportFragmentManager()
				.findFragmentByTag(tag);
		if (myFragment == null || !myFragment.isVisible()) {
			return false;
		} else {
			return true;
		}
	}
}

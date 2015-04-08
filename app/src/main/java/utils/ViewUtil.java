package utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

public class ViewUtil {
	public static void showView(final View view, boolean show) {
		int shortAnimTime = 200;

		if (show) {
			view.setVisibility(View.VISIBLE);
			view.animate().setDuration(shortAnimTime).alpha(1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							view.setVisibility(View.VISIBLE);
						}
					});
		} else {
			view.setVisibility(View.GONE);
			view.animate().setDuration(shortAnimTime).alpha(0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							view.setVisibility(View.GONE);
						}
					});
		}
	}

    /*
	 * shows one view and hides the other two views
	 */
    public static void showOneView(View firstView, View secondView, View thirdView) {
        showView(secondView, false);
        showView(thirdView, false);

        showView(firstView, true);
    }
}

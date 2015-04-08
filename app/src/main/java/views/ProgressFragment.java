package views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.view.View;

/**
 * Created by Ahmed on 04-Jul-14.
 */
public class ProgressFragment extends Fragment {

    protected Context context;

    protected View mMainView;
    protected View mProgressView;
    protected View mErrorView;
    protected View mCurrentView;

    public void showMainView() {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mMainView.setVisibility(View.VISIBLE);
            mMainView.animate().setDuration(shortAnimTime).alpha(1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mMainView.setVisibility(View.VISIBLE);
                        }
                    });

            mErrorView.setVisibility(View.GONE);
            mErrorView.animate().setDuration(shortAnimTime).alpha(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mErrorView.setVisibility(View.GONE);
                        }
                    });

            mProgressView.setVisibility(View.GONE);
            mProgressView.animate().setDuration(shortAnimTime)
                    .alpha(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mProgressView.setVisibility(View.GONE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(View.GONE);
            mMainView.setVisibility(View.VISIBLE);
            mErrorView.setVisibility(View.GONE);
        }

        mCurrentView = mMainView;
    }

    public void showProgress() {
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

            mErrorView.setVisibility(View.GONE);
            mErrorView.animate().setDuration(shortAnimTime).alpha(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mErrorView.setVisibility(View.GONE);
                        }
                    });

            mProgressView.setVisibility(View.VISIBLE);
            mProgressView.animate().setDuration(shortAnimTime)
                    .alpha(1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mProgressView.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(View.VISIBLE);
            mMainView.setVisibility(View.GONE);
            mErrorView.setVisibility(View.GONE);
        }

        mCurrentView = mProgressView;
    }

    public void showError() {
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


        mCurrentView = mErrorView;
    }

}

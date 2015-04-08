package news.turndigital.com.turndigitalnews;

import android.app.Fragment;
import android.os.Bundle;


public class MainActivity extends BaseActivity {
    protected MainFragment mainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set content view
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            // check to show tutorial_context or not
            boolean hideTutorial = ApplicationController.getHideTutorial(this);
            if (hideTutorial) {
                mainFragment = new MainFragment();

                getFragmentManager().beginTransaction()
                        .replace(R.id.container, mainFragment, MainFragment.TAG)
                        .commit();

            } else {
                TutorialFragment fragment = new TutorialFragment();

                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment, TutorialFragment.TAG)
                        .commit();
            }

        } else {
           try {
               mainFragment = (MainFragment) getFragmentManager().findFragmentByTag(MainFragment.TAG);
           } catch (Exception e) {}
        }

    }

    @Override
    public void onBackPressed() {
        if (mainFragment != null) {
            // check to hide search view or not
            if (mainFragment.textSearch.hasFocus()) {
                mainFragment.textSearch.clearFocus();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
}

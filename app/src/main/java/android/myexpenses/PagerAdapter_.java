package android.myexpenses;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class PagerAdapter_ extends FragmentPagerAdapter {

    private int count;
    private MainActivity activity;

    public PagerAdapter_(FragmentManager fm, int count, MainActivity activity) {
        super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.count = count;
        this.activity=activity;
    }


    @Override
    public Fragment getItem(int position) {
        if(position == 0){
            CurrentFragment curFr = new CurrentFragment();
            curFr.setActivity(activity);
            return curFr;
        }else if(position == 1){
            HistoryFragment hisFr = new HistoryFragment();
            hisFr.setActivity(activity);
            return hisFr;
        }else{
            return null;
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Object o = super.instantiateItem(container, position);
        if (o instanceof CurrentFragment) {
            activity.registerObserver((CurrentFragment)o);
        }
        return o;
    }

    @Override
    public int getCount() {
        return count;
    }

}

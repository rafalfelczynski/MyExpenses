package android.myexpenses;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;

public abstract class MyAbstractFragment extends Fragment {

    public MyAbstractFragment(){ }

    protected MainActivity activity;

    protected void setActivity(MainActivity activity){
        this.activity = activity;
    }

    protected double[] mCalculateChartData(ArrayList<Expense> list) {
            String[] cats = MainActivity.CATEGORIES();
            double[] values = new double[cats.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = 0;
            }
            for (int i = 0; i < list.size(); i++) {
                Expense e = list.get(i);
                double value = e.getValue();
                String cat = e.getCategory();
                int index = 0;
                while (index < cats.length && !cat.equals(cats[index])) {
                    index++;
                }
                values[index] += value;
            }
            return values;
    }

}

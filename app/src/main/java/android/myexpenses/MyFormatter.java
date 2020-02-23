package android.myexpenses;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.formatter.PercentFormatter;

public class MyFormatter extends PercentFormatter {


    public MyFormatter(PieChart pieChart) {
        super(pieChart);
    }

    @Override
    public String getFormattedValue(float value) {
        return "6" +" a %";
    }
}

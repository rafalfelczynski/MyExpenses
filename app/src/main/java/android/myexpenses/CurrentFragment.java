package android.myexpenses;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CurrentFragment extends MyAbstractFragment implements Observer {

    private ListView last3historyListView;
    private TextView todaysLimitTextView;
    private PieChart todaysChart;
    private ArrayList<Expense> expenselist2;
    private ArrayAdapter adapter;
    private PieDataSet set;
    private PieData pieData;
    private ArrayList<PieEntry> entries;
    private ArrayList<Integer> col;
    private TextView last3HistoryTitle;
    private Button addBtn;


    public CurrentFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.current_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        postViewPagerCreated(view);
    }

    public void postViewPagerCreated(final View view) {
        expenselist2 = new ArrayList<>();
        last3HistoryTitle = view.findViewById(R.id.last3HistoryTitle);
        last3historyListView = view.findViewById(R.id.last3historyLinLay);
        todaysLimitTextView = view.findViewById(R.id.todaysLimitTextView);
        todaysChart = view.findViewById(R.id.todayChart);
        addBtn = view.findViewById(R.id.addBtn);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                MainActivity.setViewLayoutParams(last3HistoryTitle, -1, MainActivity.viewPagerHeight / 20, 0, 0);
                MainActivity.setViewLayoutParams(last3historyListView, -1, 3 * (int) (0.08 * MainActivity.viewPagerHeight), 0, (int) last3HistoryTitle.getY() + last3HistoryTitle.getLayoutParams().height);
                MainActivity.setViewLayoutParams(todaysLimitTextView, -1, MainActivity.viewPagerHeight / 10, 0, (int) last3historyListView.getY() + last3historyListView.getLayoutParams().height);
                MainActivity.setViewLayoutParams(todaysChart, -1, (int) (0.51 * MainActivity.viewPagerHeight), 0, (int) todaysLimitTextView.getY() + todaysLimitTextView.getLayoutParams().height);
                MainActivity.setViewLayoutParams(addBtn, 7*MainActivity.SCREEN_WIDTH/10, 8*MainActivity.viewPagerHeight / 100, 15*MainActivity.SCREEN_WIDTH/100, (int) todaysChart.getY() + todaysChart.getLayoutParams().height);
                Thread thread2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        adapter = new ListViewAdapter(last3historyListView.getContext(), R.layout.listview_oneitem, expenselist2);
                        last3historyListView.setAdapter(adapter);
                        AdapterView.OnItemLongClickListener listener = last3historyListView.getOnItemLongClickListener();
                        if (listener == null) {
                            last3historyListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                @Override
                                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                    final Expense e = (Expense) last3historyListView.getItemAtPosition(position);
                                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                                    AlertDialog dialog = dialogBuilder.create();
                                    dialog.setTitle("Czy na pewno usunąć wydatek?");
                                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Usuń", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            System.out.println("size exp list " + expenselist2.size());
                                            expenselist2.remove(e);
                                            System.out.println("size po removie " + expenselist2.size());
                                            MainActivity.getDatabase().deleteFromExpenseTable(e.getDate(), e.getValue(), e.getCategory());
                                            MainActivity.setDoublePreference("left", MainActivity.getDoublePreference("left") + e.getValue());
                                            MainActivity.setDoublePreference("currentWeek", MainActivity.getDoublePreference("currentWeek") + e.getValue());
                                            dialog.dismiss();
                                            refresh();
                                        }
                                    });
                                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Wróć", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    dialog.show();
                                    return false;
                                }

                            });
                        }
                        last3HistoryTitle.setText("Twoje dzisiejsze wydatki");
                        last3HistoryTitle.setGravity(Gravity.CENTER);
                    }
                });
                thread2.start();
                addBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addBtnClicked(v);
                    }
                });
                Thread thread3 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        todaysChart.setTouchEnabled(false);
                        todaysChart.getDescription().setEnabled(false);
                        todaysChart.setDrawEntryLabels(false);
                        todaysChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
                        todaysChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
                        todaysChart.getLegend().setOrientation(Legend.LegendOrientation.VERTICAL);
                        todaysChart.getLegend().setTextSize(12);
                        todaysChart.getLegend().setWordWrapEnabled(true);
                        entries = new ArrayList<>();
                        col = new ArrayList<>();
                        set = new PieDataSet(entries, "");
                        pieData = new PieData(set);
                        pieData.setDrawValues(true);
                        pieData.setValueTextSize(10);
                        todaysChart.setData(pieData);
                        set.setValueFormatter(new PercentFormatter());
                        set.setColors(col);
                    }
                });
                thread3.start();
                todaysLimitTextView.setGravity(Gravity.CENTER);
                refresh();
            }
        });
        thread.start();
    }


    public void addBtnClicked(View v) {
        DialogWindow dialogWindow = new DialogWindow(v.getContext());
        dialogWindow.registerObserver(this);
        dialogWindow.show();
        activity.checkIfDateChanged();
    }

    public void refresh() {
        if (MainActivity.getDatabase() != null) {
            String[] args = CalendarHelp.calendarCurrentDay();
            MainActivity.getDatabase().getRecordsFromExpensesTable(expenselist2, null, "date_ between ? and ?", args, null, null, "date_ desc");
            if(adapter!= null) {
                adapter.notifyDataSetChanged();
            }
            calculateChartData(expenselist2);
        }
    }

    @Override
    public void fetchNewData() {
        refresh();
    }


    private void calculateChartData(final ArrayList<Expense> list) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                double limit = MainActivity.getDoublePreference("limit");
                double currentWeek = MainActivity.getDoublePreference("currentWeek");
                double weekly = MainActivity.getDoublePreference("weekly");
                double divide = MainActivity.getDoublePreference("divide");
                double left = MainActivity.getDoublePreference("left");
                if (list != null) {
                    todaysChart.setVisibility(View.VISIBLE);
                    if (limit != -1) {
                        double[] values = mCalculateChartData(list);
                        String[] cats = MainActivity.CATEGORIES();
                        int[] colors = MainActivity.COLORS();
                        entries.clear();
                        col.clear();
                        float sum2 = 0;
                        float sumProcent=0;
                        for (int i = 0; i < values.length; i++) {
                            if (values[i] != 0) {
                                sum2 += values[i];
                            }
                        }
                        for (int i = 0; i < values.length; i++) {
                            if (values[i] != 0) {
                                String category = cats[i];
                                values[i] = (values[i] / sum2 * 100);
                                PieEntry pieEntry = new PieEntry((float) (values[i]), category);
                                entries.add(pieEntry);
                                col.add(colors[i]);
                                sumProcent += pieEntry.getValue();
                            }
                        }
                        if (sumProcent < 100) {
                            float leftVal = 100 - sumProcent;
                            entries.add(new PieEntry(leftVal, "Wolne srodki"));
                            col.add(getResources().getColor(R.color.ltgray));
                        }
                        todaysChart.setData(pieData);
                        todaysChart.setCenterText(divide == 1 ? "% dzisiejszych wydatków \n("+sum2+"zł)" : "% limitu miesięcznego");
                        todaysChart.setCenterTextSize(12);
                        todaysChart.setCenterTextColor(getResources().getColor(R.color.white));
                        todaysChart.setHoleColor(getResources().getColor(R.color.dark_gray));
                        todaysChart.getLegend().setTextColor(getResources().getColor(R.color.white));
                        todaysChart.invalidate();
                        double v = divide == 1 ? currentWeek : left;
                        if (v > 0) {
                            String txt = (divide == 1) ? (" w tym tygodniu: " + String.format("%6.2f%s", currentWeek, "zł")) : (": " + String.format("%6.2f%s", left, "zł"));
                            todaysLimitTextView.setText("Pozostało do wydania" + txt);
                            todaysLimitTextView.setTextColor(getResources().getColor(R.color.white));
                            todaysLimitTextView.getPaint().setUnderlineText(true);
                        } else {
                            String txt = (divide == 1) ? (String.format("%6.2f%s", -currentWeek, "zł")) : (String.format("%6.2f%s", -left, "zł"));
                            todaysLimitTextView.setText("Limit przekroczony o " + txt);
                            todaysLimitTextView.setTextColor(getResources().getColor(R.color.red));
                            todaysLimitTextView.getPaint().setUnderlineText(true);
                        }
                    } else {
                        todaysLimitTextView.setText("Ustaw limit");
                        todaysChart.setVisibility(View.INVISIBLE);
                    }
                } else {
                    if (limit != -1) {
                        todaysChart.setNoDataText("Brak dzisiejszych wydatków");
                        String txt = (divide == 1) ? (" w tym tygodniu: " + String.format("%6.2f%s", currentWeek, "zł")) : (": " + String.format("%6.2f%s", left, "zł"));
                        todaysLimitTextView.setText("Pozostało do wydania" + txt);
                    } else {
                        todaysLimitTextView.setText("Ustaw limit");
                        todaysChart.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
        thread.start();
    }


}

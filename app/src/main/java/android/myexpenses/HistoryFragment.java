package android.myexpenses;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HistoryFragment extends MyAbstractFragment {

    private ListView historyListView;
    private TabLayout tabLayout;
    private PieChart historyChart;
    private ArrayList<Expense> expenses;
    private ArrayAdapter adapter;
    private PieDataSet set;
    private PieData pieData;
    private ArrayList<PieEntry> entries;
    private ArrayList<Integer> col;
    private Dialog dialog;
    private CalendarView calendarView;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.history_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        postViewCreated(view);
    }


    private void postViewCreated(final View v) {
        dialog = new Dialog(v.getContext());
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                dialog.setContentView(R.layout.calendar_dialog);
                calendarView = dialog.findViewById(R.id.calendarView);
                dialog.setCancelable(true);

                calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                        String[] dates = CalendarHelp.calendarWholeSpecificDay(dayOfMonth, month, year);
                        MainActivity.getDatabase().getRecordsFromExpensesTable(expenses, null, "date_ between ? and ?", dates, null, null, "date_ desc");
                        if (expenses.size() > 0) {
                            adapter.notifyDataSetChanged();
                            updateChartData(v, expenses);
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                }
                            });
                        } else {
                            Toast.makeText(v.getContext(), "Brak wydatków z danego dnia", Toast.LENGTH_SHORT).show();
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                });
            }
        });

        t.start();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                expenses = new ArrayList<>();
                historyListView = v.findViewById(R.id.historyListView);
                tabLayout = v.findViewById(R.id.historyTabLayout);
                historyChart = v.findViewById(R.id.historyChart);
                Thread thread2 = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        adapter = new ListViewAdapter(v.getContext(), R.layout.listview_oneitem, expenses);
                        historyListView.setAdapter(adapter);
                        MainActivity.setViewLayoutParams(tabLayout, -1, MainActivity.viewPagerHeight / 10, 0, 0);
                        MainActivity.setViewLayoutParams(historyListView, -1, (int) (MainActivity.viewPagerHeight * 0.5), 0, (int) tabLayout.getY() + tabLayout.getLayoutParams().height);
                        MainActivity.setViewLayoutParams(historyChart, -1, (int) (MainActivity.viewPagerHeight * 0.4), 0, (int) historyListView.getY() + historyListView.getLayoutParams().height);
                        tabLayout.addTab(tabLayout.newTab().setText("W tym tygodniu"), 0);
                        tabLayout.addTab(tabLayout.newTab().setText("W tym miesiącu"), 1);
                        tabLayout.addTab(tabLayout.newTab().setText("Wybierz Date"), 2);
                        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                            @Override
                            public void onTabSelected(TabLayout.Tab tab) {
                                if (tab.getPosition() == 2) {
                                    pickDateClicked(v);
                                } else if (tab.getPosition() == 1) {
                                    fetchRecordsFromDatabase(v, 1, expenses);
                                    adapter.notifyDataSetChanged();
                                    updateChartData(v, expenses);
                                } else if (tab.getPosition() == 0) {
                                    fetchRecordsFromDatabase(v, 0, expenses);
                                    adapter.notifyDataSetChanged();
                                    updateChartData(v, expenses);
                                }
                            }

                            @Override
                            public void onTabUnselected(TabLayout.Tab tab) {
                            }

                            @Override
                            public void onTabReselected(TabLayout.Tab tab) {
                                onTabSelected(tab);
                            }
                        });
                    }
                });
                thread2.start();
                historyChart.getDescription().setEnabled(false);
                historyChart.setTouchEnabled(false);
                historyChart.getDescription().setEnabled(false);
                historyChart.setDrawEntryLabels(false);
                historyChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
                historyChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
                historyChart.getLegend().setOrientation(Legend.LegendOrientation.VERTICAL);
                historyChart.getLegend().setTextSize(12);
                historyChart.getLegend().setWordWrapEnabled(true);
                entries = new ArrayList<>();
                col = new ArrayList<>();
                set = new PieDataSet(entries, "");
                pieData = new PieData(set);
                pieData.setDrawValues(true);
                pieData.setValueTextSize(10);
                historyChart.setData(pieData);
                set.setValueFormatter(new PercentFormatter());
                set.setColors(col);
            }
        });
        thread.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        //listener.onTabSelected(tabLayout.getTabAt(0));
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        if (tab != null) {
            tab.select();
            historyChart.invalidate();
        }
    }

    private void fetchRecordsFromDatabase(View v, int weekOrMonth, ArrayList<Expense> list) { // week = 0, month = 1
        String[] dates;
        if (weekOrMonth == 0) {
            dates = CalendarHelp.calendarCurrentWeek();
        } else {
            dates = CalendarHelp.calendarCurrentMonth();
        }
        MainActivity.getDatabase().getRecordsFromExpensesTable(list, null, "date_ between ? and ?", dates, null, null, "date_ desc");
    }

    private void updateChartData(final View v, final ArrayList<Expense> list) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (list != null) {
                    double limit = MainActivity.getDoublePreference("limit");
                    historyChart.setVisibility(View.VISIBLE);
                    double[] values = mCalculateChartData(list);
                    String[] cats = MainActivity.CATEGORIES();
                    int[] colors = MainActivity.COLORS();
                    entries.clear();
                    col.clear();
                    float sum = 0;
                    for (int i = 0; i < values.length; i++) {
                        if (values[i] != 0) {
                            String category = cats[i];
                            PieEntry pieEntry = new PieEntry((float) (values[i] / limit * 100), category);
                            entries.add(pieEntry);
                            col.add(colors[i]);
                            sum += pieEntry.getValue();
                        }
                    }
                    if (sum < 100) {
                        float leftVal = 100 - sum;
                        entries.add(new PieEntry(leftVal, "Wolne srodki"));
                        col.add(getResources().getColor(R.color.ltgray));
                    }
                    historyChart.setData(pieData);
                    historyChart.setCenterText("% limitu miesięcznego");
                    historyChart.setCenterTextSize(12);
                    historyChart.setCenterTextColor(getResources().getColor(R.color.white));
                    historyChart.setHoleColor(getResources().getColor(R.color.dark_gray));
                    historyChart.getLegend().setTextColor(getResources().getColor(R.color.white));
                    historyChart.invalidate();
                } else {
                    Toast.makeText(v.getContext(), "Brak wydatków w danym okresie", Toast.LENGTH_SHORT).show();
                    historyChart.setVisibility(View.INVISIBLE);
                }

            }
        });
        thread.start();
    }


    private void pickDateClicked(final View v) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
    }

}

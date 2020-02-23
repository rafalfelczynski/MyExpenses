package android.myexpenses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements Observable {

    private final float WIDTH_DIVISION = 0.05f;// 5% margin
    private final float HEIGHT_DIVISION = 0.25f; // 25%
    private final float _10PROC_DIVISION = 0.1f; // 10%

    public static int PERMISSION_CODE = 10;
    public static int SCREEN_HEIGHT;
    public static int SCREEN_WIDTH;
    public static int STATUS_BAR_HEIGHT;
    public static int APP_SCREEN_HEIGHT;
    public static Typeface consolas;
    private static DatabaseConnection database;
    private static final String PREF_FILE_NAME = "pref_file";
    private boolean noPermissionFlag;
    private static String[] CATEGORIES;// = {"Jedzenie", "Alkohol", "Elektronika", "Paliwo", "Rachunki", "Rozrywka", "Inne"};
    private static int[] COLORS;// = {Color.YELLOW, Color.BLUE, Color.GREEN, Color.LTGRAY, Color.DKGRAY, Color.RED, Color.MAGENTA};

    private static SharedPreferences preferences;

    private ArrayList<Observer> observers;

    private ViewPager viewPager;
    public static int viewPagerHeight;

    public static DatabaseConnection getDatabase() {
        return database;
    }

    public static SharedPreferences getUserPreferences() {
        return preferences;
    }

    public static String getStringPreference(String key) {
        return preferences.getString(key, "");
    }

    public static String[] CATEGORIES(){
        return CATEGORIES;
    }

    public static int[] COLORS(){
        return COLORS;
    }

    public static void setStringPreference(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static double getDoublePreference(String key) {
        return preferences.getFloat(key, -1);
    }

    public static void setDoublePreference(String key, double val) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(key, (float) val);
        editor.commit();
    }

    public static int getIntPreference(String key) {
        return preferences.getInt(key, -1);
    }

    public static void setIntPreference(String key, int val) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, val);
        editor.commit();
    }

    public static void setMultipleDoublePreferences(String[] keys, double[] values) {
        if (keys.length == values.length) {
            SharedPreferences.Editor editor = preferences.edit();
            int index = 0;
            while (index < keys.length) {
                editor.putFloat(keys[index], (float) values[index]);
                index++;
            }
            editor.commit();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CATEGORIES = new String[]{"Jedzenie", "Używki", "Elektronika", "Paliwo", "Rachunki", "Rozrywka", "Inne"};
        COLORS = new int[]{ContextCompat.getColor(this,R.color.yellow), ContextCompat.getColor(this,R.color.dkblue), ContextCompat.getColor(this,R.color.dkgreen),
                ContextCompat.getColor(this,R.color.green), ContextCompat.getColor(this,R.color.red),
                ContextCompat.getColor(this,R.color.orange), ContextCompat.getColor(this,R.color.blue)};
        preferences = this.getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        observers = new ArrayList<>();
        noPermissionFlag = true;
        consolas = Typeface.createFromAsset(getAssets(), "Fonts/Consolas.ttf");
        finishCreating();
        requestMemoryPermission();
    }

    private void initDatabase() {
        database = new DatabaseConnection(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void finishCreating() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Point size = new Point();
                Display display = getWindowManager().getDefaultDisplay();
                display.getRealSize(size);
                STATUS_BAR_HEIGHT = getStatusBarHeight();
                SCREEN_HEIGHT = size.y;
                SCREEN_WIDTH = size.x;
                APP_SCREEN_HEIGHT = SCREEN_HEIGHT - STATUS_BAR_HEIGHT;
                viewPager = findViewById(R.id.viewPager);
                final TabLayout tabLayout = findViewById(R.id.tabLayout);
                final Toolbar mainToolbar = findViewById(R.id.mainToolbar);
                setSupportActionBar(mainToolbar);
                getSupportActionBar().setDisplayShowTitleEnabled(false);

                Thread thread3 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TabLayout.Tab hisTabItem = tabLayout.newTab().setText("Dzisiejsze");
                        TabLayout.Tab curTabItem = tabLayout.newTab().setText("Historia");
                        tabLayout.addTab(hisTabItem);
                        tabLayout.addTab(curTabItem);
                        PagerAdapter pagerAdapter = new PagerAdapter_(getSupportFragmentManager(), tabLayout.getTabCount(), MainActivity.this);
                        viewPager.setAdapter(pagerAdapter);
                        if (tabLayout.getTabCount() > 0) {
                            tabLayout.getTabAt(0).select();
                        }

                        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
                    }
                });
                thread3.start();
                Thread thread4 = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                            @Override
                            public void onTabSelected(TabLayout.Tab tab) {
                                int pos = tab.getPosition();
                                viewPager.setCurrentItem(pos);
                            }
                            @Override
                            public void onTabUnselected(TabLayout.Tab tab) { }
                            @Override
                            public void onTabReselected(TabLayout.Tab tab) {
                                onTabSelected(tab);
                            }
                        });
                    }
                });
                thread4.start();
                Thread thread2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        setViewLayoutParams(mainToolbar, -1, (int) (0.08 * APP_SCREEN_HEIGHT), 0, 0);
                        setViewLayoutParams(tabLayout, -1, (int) (0.08 * APP_SCREEN_HEIGHT), 0, mainToolbar.getLayoutParams().height);
                        viewPagerHeight = (int) (APP_SCREEN_HEIGHT -tabLayout.getLayoutParams().height - mainToolbar.getLayoutParams().height);
                        setViewLayoutParams(viewPager, -1, viewPagerHeight, 0, (int) tabLayout.getY() + tabLayout.getLayoutParams().height);
                    }
                });
                thread2.start();
            }
        });
        thread.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settingsIcon) {
            openSettingsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSettingsDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.settings_menu_dialog);
        ConstraintLayout settingConstr = dialog.findViewById(R.id.settingsConstr);
        int parheight = APP_SCREEN_HEIGHT / 3;
        int parwidth = (int) (3 * SCREEN_WIDTH * HEIGHT_DIVISION);// 3/4 ekranu
        settingConstr.setMinHeight(parheight);
        settingConstr.setMaxHeight(parheight);
        settingConstr.setMaxWidth((int)(3 * SCREEN_WIDTH * HEIGHT_DIVISION));
        settingConstr.setMinWidth((int)(3 * SCREEN_WIDTH * HEIGHT_DIVISION));
        Button btn = dialog.findViewById(R.id.limitOkBtn);
        MainActivity.setViewLayoutParams(btn, (int)(parwidth/2 - parwidth * _10PROC_DIVISION), (int) (parheight * 0.4), parwidth/2f + parwidth * WIDTH_DIVISION, parheight * HEIGHT_DIVISION);
        TextView t = dialog.findViewById(R.id.settingsTitle);
        MainActivity.setViewLayoutParams(t, parwidth, (int)(parheight * HEIGHT_DIVISION), 0, 0);
        t.setGravity(Gravity.CENTER);
        t.setText("Podaj swój miesieczny limit wydatków");
        final EditText input = dialog.findViewById(R.id.inputLimit);
        MainActivity.setViewLayoutParams(input, (int)(parwidth/2 - parwidth *_10PROC_DIVISION), (int)(parheight * 0.4), parwidth * WIDTH_DIVISION, parheight * HEIGHT_DIVISION);
        input.setGravity(Gravity.CENTER);
        input.setHint("Kwota");
        final Switch settingSwitch = dialog.findViewById(R.id.settingSwitch);
        MainActivity.setViewLayoutParams(settingSwitch, (int)(parwidth - parwidth * _10PROC_DIVISION), (int)(parheight * HEIGHT_DIVISION),parwidth * WIDTH_DIVISION, (0.7f * parheight));
        settingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonView.setText("Podziel limit na tygodnie");
                } else {
                    buttonView.setText("Nie dziel limitu na tygodnie");
                }
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double limit;
                if (input.getText().toString().length() > 0 && (limit = Double.parseDouble(input.getText().toString())) > 0) {
                    double weekly = 7 * limit / CalendarHelp.getCurrentMaxDayOfMonth();
                    double divide = settingSwitch.isChecked() ? 1 : -1;
                    double prevLimit = getDoublePreference("limit");
                    double prevDivide = getDoublePreference("divide");
                    if (divide != prevDivide && prevLimit == limit) {
                        setDoublePreference("divide", divide);
                    }
                    if (prevLimit != -1) {
                        double left = getDoublePreference("left");
                        double diff = prevLimit - left;
                        left = limit - diff;
                        double cur = getDoublePreference("currentWeek");
                        double moneySpent = getDoublePreference("weekly") - cur;
                        cur = weekly - moneySpent;
                        setMultipleDoublePreferences(new String[]{"limit", "weekly", "left", "currentWeek", "divide"}, new double[]{limit, weekly, left, cur, divide});
                    } else {
                        double currentWeek;
                        int day = CalendarHelp.getDayOfWeek();
                        if(day >1){
                            int left = 7-day+2;
                            currentWeek = left * limit/CalendarHelp.getCurrentMaxDayOfMonth();
                        }else{
                            currentWeek = limit/CalendarHelp.getCurrentMaxDayOfMonth();
                        }
                        setMultipleDoublePreferences(new String[]{"limit", "weekly", "left", "currentWeek", "divide"}, new double[]{limit, weekly, limit, currentWeek, divide});
                    }
                    setIntPreference("currentWeekNumber", CalendarHelp.getCurrentWeekNumber());
                    setIntPreference("currentMonth", CalendarHelp.getCurrentMonth());
                    dialog.dismiss();
                    notifyObservers();
                } else {
                    Toast.makeText(MainActivity.this, "Podaj kwote większą od zera", Toast.LENGTH_SHORT).show();
                }
            }
        });

        double limit = getDoublePreference("limit");
        if (limit != -1) {
            input.setText(String.valueOf(limit));
        }
        double divide = getDoublePreference("divide");
        if (divide == -1) {
            settingSwitch.setChecked(true);
            settingSwitch.setChecked(false);
        } else {
            settingSwitch.setChecked(true);
        }

        dialog.show();

    }


    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void setViewLayoutParams(View view, int width, int height, float x, float y) {
        ViewGroup.LayoutParams par = view.getLayoutParams();
        par.width = width;
        par.height = height;
        view.setY(y);
        view.setX(x);
        view.setLayoutParams(par);
    }

    public void requestMemoryPermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
        } else {
            initDatabase();
            double limit = getDoublePreference("limit");
            if (limit == -1) {
                openSettingsDialog();
            } else {
                checkIfDateChanged();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            noPermissionFlag = false;
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    noPermissionFlag = true;
                }
            }
            if (noPermissionFlag) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                    alertBuilder.setMessage("Aplikacja potrzebuje dostępu do pamięci w celu utrwalania danych. Zadne dane nie są przez aplikacje gromadzone poza tymi, ktore wprowadzasz");
                    alertBuilder.setTitle("Dostep do pamięci");
                    AlertDialog dialog = alertBuilder.create();
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "RETRY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            requestMemoryPermission();
                        }
                    });
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "EXIT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dialog.show();
                } else {
                    final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                    alertBuilder.setMessage("Aplikacja potrzebuje dostępu do pamięci w celu utrwalania danych. Aby korzystac z aplikacji zmien pozwolenia w ustawieniach telefonu");
                    alertBuilder.setTitle("Dostep do pamięci");
                    AlertDialog dialog = alertBuilder.create();

                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dialog.show();
                }
            } else {
                initDatabase();
                double limit = getDoublePreference("limit");
                if (limit == -1) {
                    openSettingsDialog();
                } else {
                    checkIfDateChanged();
                }
            }
        }
    }


    public void checkIfDateChanged() {
        int m;
        if ((m = CalendarHelp.getCurrentMonth()) != getIntPreference("currentMonth")) {
            double currentWeek;
            double limit = getDoublePreference("limit");
            double weekly = 7 * limit / CalendarHelp.getCurrentMaxDayOfMonth();
            int day = CalendarHelp.getDayOfWeek();
            if(day >1){
                int left = 7-day+2;
                currentWeek = left * limit/CalendarHelp.getCurrentMaxDayOfMonth();
            }else{
                currentWeek = limit/CalendarHelp.getCurrentMaxDayOfMonth();
            }

            setIntPreference("currentMonth", m);
            setMultipleDoublePreferences(new String[]{"weekly", "left", "currentWeek"}, new double[]{weekly, limit, currentWeek});
        }
        if ((m = CalendarHelp.getCurrentWeekNumber()) != getIntPreference("currentWeekNumber")) {
            int dif = m - getIntPreference("currentWeekNumber");
            if(CalendarHelp.getCurrentMaxDayOfMonth() - CalendarHelp.getCurrentDayOfMonth()+1 >= 7) {
                setIntPreference("currentWeekNumber", m);
                double cur = getDoublePreference("currentWeek");
                double we = getDoublePreference("weekly");
                setDoublePreference("currentWeek", dif * we + cur);
            }else{
                setDoublePreference("currentWeek", getDoublePreference("left"));
            }
        }
    }


    @Override
    public void registerObserver(Observer obs) {
        if (!observers.contains(obs)) {
            observers.add(obs);
        }
    }


    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.fetchNewData();
        }
    }


    @Override
    public void removeObserver(Observer obs) {
        observers.remove(obs);
    }
}

package android.myexpenses;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import androidx.annotation.NonNull;

public class DialogWindow extends Dialog implements Observable {

    private EditText valueEditText;
    private View preselected;
    private ArrayList<Observer> listOfObservers;

    public DialogWindow(@NonNull Context context) {
        super(context);
        preselected = null;
        listOfObservers=new ArrayList<>(5);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(this.getWindow() != null) {
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getWindow().setGravity(Gravity.CENTER);
            getWindow().setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.add_window_main_back_drawable));
        }
        setContentView(R.layout.add_window_layout);
        ListView categoryListView=findViewById(R.id.categoryListView);
        valueEditText = findViewById(R.id.valueEditText);
        Button okBtn = findViewById(R.id.okBtn);
        TextView titleTextView = findViewById(R.id.titleTextView);
        if(this.getWindow() != null) {
            WindowManager.LayoutParams par = this.getWindow().getAttributes();
            par.width = (int) (MainActivity.SCREEN_WIDTH *0.9);
            par.height = (int) (MainActivity.APP_SCREEN_HEIGHT * 0.5);
            int marginH = (int) (par.height* 0.05);
            int marginW = (int)(par.width * 0.05);
            par.y = 0;
            par.x = 0;
            par.verticalMargin=0;
            par.horizontalMargin=0;
            int winWidth = par.width;
            int winHeight = par.height;
            getWindow().setAttributes(par);
            MainActivity.setViewLayoutParams(titleTextView, (int)(0.425 * winWidth), (int)(0.15*winHeight), winWidth/2f - 0.425f * winWidth/2f, 0);
            titleTextView.setGravity(Gravity.CENTER);
            titleTextView.setText("Nowy wydatek");
            MainActivity.setViewLayoutParams(valueEditText, (int)(0.425 * winWidth), (int)(0.35 * winHeight), marginW, titleTextView.getLayoutParams().height);
            MainActivity.setViewLayoutParams(okBtn, (int)(0.425 * winWidth), (int)(0.35 * winHeight), marginW, valueEditText.getLayoutParams().height + titleTextView.getLayoutParams().height + 2 * marginH);
            MainActivity.setViewLayoutParams(categoryListView,(int)(0.425 * winWidth), (int)(0.8* winHeight) , (int)(0.525 * winWidth), titleTextView.getLayoutParams().height);
            categoryListView.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, MainActivity.CATEGORIES()));
            categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                int color;
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if(view != preselected) {
                        color = view.getSolidColor();
                        if(preselected != null){
                            preselected.setBackgroundColor(color);
                        }
                        view.setBackgroundResource(R.drawable.border_line);
                        preselected = view;
                    }
                }
            });
            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    okClicked();
                }
            });
        }
    }



    private void okClicked(){
        if(valueEditText.getText().toString().length() > 0 && preselected != null ) {
                String date = CalendarHelp.format.format(new Date());
                String cat = ((TextView) preselected).getText().toString();
                double val = Double.parseDouble(valueEditText.getText().toString());
                MainActivity.getDatabase().insertToExpenseTable(date, val, cat);
                MainActivity.setDoublePreference("left", MainActivity.getDoublePreference("left") - val);
                MainActivity.setDoublePreference("currentWeek", MainActivity.getDoublePreference("currentWeek") - val);
                notifyObservers();
                dismiss();
        }else{
            if(valueEditText.getText().toString().length() == 0){
                Toast.makeText(getContext(),"Podaj kwote większą od 0", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getContext(),"Wybierz kategorie", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void registerObserver(Observer obs) {
        if(!listOfObservers.contains(obs)){
            listOfObservers.add(obs);
        }
    }

    @Override
    public void notifyObservers() {
        for(Observer o : listOfObservers){
            o.fetchNewData();
        }
    }

    @Override
    public void removeObserver(Observer obs) {
        listOfObservers.remove(obs);
    }
}

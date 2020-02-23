package android.myexpenses;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ListViewAdapter extends ArrayAdapter<Expense> {

    private ArrayList<Expense> expenses;
    private int resource;

    public ListViewAdapter(@NonNull Context context, int resource, ArrayList<Expense> expenses) {
        super(context, resource, expenses);
        this.expenses = expenses;
        this.resource=resource;
    }

    @Nullable
    @Override
    public Expense getItem(int position) {
       return expenses.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Expense exp = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(resource,parent, false);
        }
        TextView textView = convertView.findViewById(R.id.txtViewListView);
        textView.setText(exp.toString());
        textView.setGravity(Gravity.CENTER);
        textView.setTypeface(MainActivity.consolas);
        MainActivity.setViewLayoutParams(textView, -1,(int)(0.08*MainActivity.viewPagerHeight),0,0);

        return convertView;
    }
}

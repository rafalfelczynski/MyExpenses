package android.myexpenses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class DatabaseConnection {

    private SQLiteDatabase database;

    public DatabaseConnection(AppCompatActivity activity) {
        database = activity.openOrCreateDatabase("MyExpenses", Context.MODE_PRIVATE, null);
        String str = "create table if not exists expenses (id Integer primary key autoincrement, date_ varchar(50), value double, category varchar(50) )";
        SQLiteStatement stat = database.compileStatement(str);
        stat.execute();
    }

    private Cursor execSelectQuery(String table, String[] columns, String whereClause, String[] args, String groupBy, String having, String orderBy, String numOfRec) {
        if (database != null) {
            return database.query(table, columns, whereClause, args, groupBy, having, orderBy, numOfRec);
        } else {
            return null;
        }
    }

    public ArrayList<Expense> getRecordsFromExpensesTable(ArrayList<Expense> list, String numOfRec, String whereClause, String[] args, String groupBy, String having, String orderBy) {

        Cursor cursor = execSelectQuery("expenses", new String[]{"date_", "value", "category"}, whereClause, args, groupBy, having, orderBy, numOfRec);
        list.clear();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = 0;
                do {
                    String date = cursor.getString(cursor.getColumnIndex("date_"));
                    double val = cursor.getDouble(cursor.getColumnIndex("value"));
                    String cat = cursor.getString(cursor.getColumnIndex("category"));
                    list.add(new Expense(cat, val, date));
                    index++;
                }
                while (cursor.moveToNext());// && numOfRec (index < Integer.parseInt(numOfRec) || Integer.parseInt(numOfRec) == -1));
            }
        }
        return list;
    }

    public long insertToExpenseTable(String date, double value, String category) {
        ContentValues content = new ContentValues();
        content.put("category", category);
        content.put("value",value);
        content.put("date_",date);
        return database.insert("expenses", null, content);
    }

    public boolean deleteFromExpenseTable(String data, double value, String category){
        return database.delete("expenses","date_ = ? and value = ? and category = ?",new String[]{data, String.valueOf(value), category}) >0;
    }

}

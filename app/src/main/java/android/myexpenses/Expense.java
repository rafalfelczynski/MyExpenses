package android.myexpenses;

public class Expense {

    private String category;
    private double value;
    private String date;

    public Expense(String category, double value, String date){
        this.category=category;
        this.value=value;
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public double getValue() {
        return value;
    }

    public String getDate() {
        return date;
    }

    public String toString(){
        return String.format("%7.2f zł| %-12s|%19s",value, category, date);
    }
}

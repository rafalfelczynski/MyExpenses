package android.myexpenses;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public  class CalendarHelp {

    public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public static String[] calendarWholeSpecificDay(int day, int month, int year){
        return calBetweenDates(new int[]{month, month},new int[]{year, year},new int[]{day, day}, new int[]{-1,-1}, new int[]{0,23},new int[]{0,59}, new int[]{0,59});
    }

    public static String[] calendarCurrentWeek(){
        return calBetweenDates(new int[]{-1,-1},new int[]{-1,-1},new int[]{-1,-1}, new int[]{Calendar.MONDAY, Calendar.SUNDAY}, new int[]{0,23},new int[]{0,59}, new int[]{0,59});
    }

    public static String[] calendarCurrentMonth(){
        return calBetweenDates(new int[]{-1,-1},new int[]{-1,-1},new int[]{1,31}, new int[]{-1,-1}, new int[]{0,23},new int[]{0,59}, new int[]{0,59});
    }

    public static String[] calendarCurrentDay(){
        return calBetweenDates(new int[]{-1,-1},new int[]{-1,-1},new int[]{-1,-1}, new int[]{-1,-1}, new int[]{0,23},new int[]{0,59}, new int[]{0,59});
    }

    private static String[] calBetweenDates(int[] month, int[] year, int[] dayOfMonth, int[] dayOfWeek, int[] hour, int[] minute, int[] second){
        Calendar cal = Calendar.getInstance();
        String startDate = setCalendar(cal,month[0], year[0],dayOfMonth[0], dayOfWeek[0], hour[0], minute[0], second[0]);
        String endDate = setCalendar(cal,month[1],year[1],dayOfMonth[1], dayOfWeek[1], hour[1], minute[1], second[1]);
        return new String[]{startDate, endDate};
    }

    private static String setCalendar(Calendar cal, int month, int year, int dayOfMonth, int dayOfWeek, int hour, int minute, int second){
        if(month >0){
            cal.set(Calendar.MONTH, month);
        }
        if(year >0){
            cal.set(Calendar.YEAR, year);
        }
        if(dayOfWeek >0) {
            cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        }
        if(hour >=0) {
            cal.set(Calendar.HOUR_OF_DAY, hour);
        }
        if(minute>=0) {
            cal.set(Calendar.MINUTE, minute);
        }
        if(second>=0) {
            cal.set(Calendar.SECOND, second);
        }
        if(dayOfMonth > 0) {
            if(dayOfMonth <= cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }else{
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
        }
        return format.format(cal.getTime());
    }

    public static String calendarMinusDays(Calendar cal, int days){
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH)-days);
        return format.format(cal.getTime());
    }

    public static int getCurrentDayOfMonth(){
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static int getCurrentMonth(){
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.MONTH);
    }

    public static int getDayOfWeek(){
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    public static int getCurrentWeekNumber(){
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.WEEK_OF_YEAR);
    }

    public static double weeksLeft(){
        Calendar cal = Calendar.getInstance();
        double days = cal.getActualMaximum(Calendar.DAY_OF_MONTH) - cal.get(Calendar.DAY_OF_MONTH);
        return days/7;
    }
    public static int getCurrentMaxDayOfMonth(){
        Calendar cal = Calendar.getInstance();
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

}

package com.example.adpractica1atr;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.example.adpractica1atr.CallSaver.DELIMITER;

public class Call implements Comparable<Call>{

    private Calendar date;
    private String number;
    private String contactName="unknown";


    public Call(String number, Calendar dat){
        this.number=number;
        if(dat==null) dat=new GregorianCalendar();
        this.date=dat;
    }

    public Call(String number, String name, Calendar date){
        this(number,date);
        this.contactName=name;
    }

    public Call(String name, String year, String month, String day, String hour, String minutes, String seconds, String number){
        this.contactName=name;
        this.number=number;
        Calendar cal=new GregorianCalendar();
        cal.set(Integer.valueOf(year),Integer.valueOf(month),Integer.valueOf(day),Integer.valueOf(hour),Integer.valueOf(minutes),Integer.valueOf(seconds));
        this.date=cal;
    }

    private String addZeroIfMissing(int num){
        String numString=String.valueOf(num);
        if(num<10) numString="0"+numString;
        return numString;
    }

    public String getNumber() {return this.number;}

    public Calendar getDate() { return this.date; }

    public String getContactName() { return contactName; }

    public String getDay() { return addZeroIfMissing(this.date.get(Calendar.DAY_OF_MONTH)); }

    public String getMonth() { return addZeroIfMissing(this.date.get(Calendar.MONTH)); }

    public String getYear() { return String.valueOf(this.date.get(Calendar.YEAR)); }

    public String getHour() { return addZeroIfMissing(this.date.get(Calendar.HOUR_OF_DAY)); }

    public String getMinutes() { return addZeroIfMissing(this.date.get(Calendar.MINUTE)); }

    public String getSeconds() { return addZeroIfMissing(this.date.get(Calendar.SECOND)); }

    public void setContactName(String name) {this.contactName=name;}



    //----- CSV Formatting -----//

    public static Call CSVExternalToCall(String line){
        try {
            String[] split = line.split(DELIMITER);
            Call call=new Call(split[0],split[1],split[2],split[3],split[4],split[5],split[6],split[7]);
            return call;
        }catch (Exception ex){
            return null;
        }
    }

    public String toStringCSVInternal(){
        return getYear() + DELIMITER + getMonth() + DELIMITER + getDay() + DELIMITER + getHour() + DELIMITER + getMinutes() + DELIMITER + getSeconds() + DELIMITER + number + DELIMITER + contactName;
    }

    public String toStringCSVExternal(){
        return contactName + DELIMITER + getYear() + DELIMITER + getMonth() + DELIMITER + getDay() + DELIMITER + getHour() + DELIMITER + getMinutes() + DELIMITER + getSeconds() + DELIMITER + number;
    }

    @Override
    public int compareTo(Call o) {
        if(!this.contactName.equalsIgnoreCase(o.getContactName())) return this.contactName.compareTo(o.getContactName());
        return this.date.compareTo(o.getDate());
    }
}

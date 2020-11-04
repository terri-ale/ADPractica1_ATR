package com.example.adpractica1atr;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;


public class CallSaver extends Thread {

    protected final static String DELIMITER="; ";
    protected final static String externalFileName="historial.csv";
    protected final static String internalFileName="llamadas.csv";
    private Call call;
    private Context context;

    public CallSaver(Context context, Call call){
        this.context=context;
        this.call=call;
    }

    @Override
    public void run() {
        super.run();
        String contactName=getContactName(call.getNumber());
        call.setContactName(contactName);
        saveCallCsvInternal();
        saveCallCsvExternal();
    }


    private String getContactName(String number){
        ContentResolver cr = context.getContentResolver();

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        //this uri returns something like: "content://com.android.contacts/phone_lookup/0034666666666"
        //it is the easiest way to get the details of the contact with that phone number without the need to
        //iterate between all contacts and comparing the phones, or formatting the number to adjust
        //country prefixes, for example: 0034 or +34 or no prefix

        Log.v("xyzyx tel:", uri.toString());

        String name = context.getString(R.string.string_unknown); //name initially is defined as the string "unknown" in the system language

        try {
            Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    name = cursor.getString(0);
                }
            }

            cursor.close();

            return name;

        }catch (SecurityException ex){ return name; }
        //If contact permission is not granted, SecurityException is thrown and I return variable name containing "unknown"
    }



    private void saveCallCsvInternal(){
        try {
            File f=new File(context.getFilesDir(),internalFileName);

            BufferedWriter bw=new BufferedWriter(new FileWriter(f,true)); //2nd parameter true means append and not overwrite

            bw.write(call.toStringCSVInternal());
            bw.newLine();

            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveCallCsvExternal(){
        List<Call> savedCalls=new ArrayList<>();
        savedCalls.add(call);
        try{
            File f=new File(context.getExternalFilesDir(null),externalFileName);

            //I check that the file exists. If not, means that it's the first time saving a call
            //this avoids FileNotFoundException
            if(f.isFile()) {

                //Read file and parse old calls into arrayList savedCalls
                BufferedReader br = new BufferedReader(new FileReader(f));

                String line = br.readLine();
                Call c;

                while (line != null) {
                    c = Call.CSVExternalToCall(line);
                    if (c != null) savedCalls.add(c);
                    line = br.readLine();
                }

                br.close();

                Collections.sort(savedCalls); //order the array with default order criteria (compareTo in Call)
            }

            //re-write file with ordered calls
            BufferedWriter bw=new BufferedWriter(new FileWriter(f));

            for(Call cal : savedCalls){
                bw.write(cal.toStringCSVExternal());
                bw.newLine();
            }

            bw.close();

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
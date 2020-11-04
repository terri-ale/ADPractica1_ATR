package com.example.adpractica1atr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.adpractica1atr.settings.SettingsActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static com.example.adpractica1atr.CallSaver.externalFileName;
import static com.example.adpractica1atr.CallSaver.internalFileName;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private String[] arrayPermissions={
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG
    };

    private static final int PERMISSIONS_CODE=1;

    private TextView tvText, tvWarning;

    private Button btWarning;

    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState==null) init();
    }


    private void init() {
        tvText=findViewById(R.id.tvText);
        tvWarning=findViewById(R.id.tvWarning);
        btWarning=findViewById(R.id.btWarning);

        btWarning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMissingPermissions();
            }
        });

        preferences= PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);

        if(!allPermissionsGranted()) setWarning(true);
        
        setText();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(!allPermissionsGranted()) setWarning(true);
    }



    //----- PERMISSIONS -----//

    public boolean allPermissionsGranted(){
        //this method can be used at other java classes to check whether the permissions
        //are already granted before taking advantage of them. This avoids an error if
        // permissions have been revoked from android settings.
        //Resource efficiency: if version is previous than M, it will not make any further check.
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M) return true;
        for(String permission : arrayPermissions){
            //as soon as it finds any permission not granted, returns false
            if(checkSelfPermission(permission)!=PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }


    private void requestMissingPermissions(){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.M) return;

        int result;
        List<String> missingPermissions=new ArrayList<>();
        for(String permission : arrayPermissions){
            result=PackageManager.PERMISSION_GRANTED;
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
                result=checkSelfPermission(permission); //if the current version is previous than M, result value will be granted
            if(result!=PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (missingPermissions.size()>0) {
            String[] missingPermissions2=new String[missingPermissions.size()];
            for(int i=0;i<missingPermissions.size();i++)
                missingPermissions2[i]=missingPermissions.get(i);

            requestPermissions(missingPermissions2, PERMISSIONS_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int grantedCounter=0;
        switch (requestCode){
            case PERMISSIONS_CODE:
                for(int result : grantResults){
                    if(result==PackageManager.PERMISSION_GRANTED) grantedCounter++;
                }
                break;
        }
        if(grantedCounter==permissions.length) setWarning(false);
    }



    //----- UI -----//

    private void setText() {
        File f;

        switch (preferences.getString("source","internal")){
            //I do not know why getString() from sharedPreferences returns its index and not the string
            //I guess there is a better way rather than filtering by constants numbers
            case "1":
                f=new File(getFilesDir(),internalFileName);
                break;
            case "2":
                f=new File(getExternalFilesDir(null),externalFileName);
                break;
            default:
                f=new File(getFilesDir(),internalFileName);
                break;
        }

        if(!f.isFile()){
            //I prefer .isFile() rather than .exists(), since .exists() does not discriminate if it is a file or directory.
            //if the file does not exist, means that there are no records yet.
            tvText.setText(getString(R.string.string_norecords));
            return;
        }

        try {

            BufferedReader br=new BufferedReader(new FileReader(f));

            tvText.setText("");

            String line=br.readLine();

            while(line!=null){
                tvText.append("\n"+line);
                line=br.readLine();
            }

            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void setWarning(boolean shouldShowWarning){
        if(shouldShowWarning){
            tvWarning.setVisibility(View.VISIBLE);
            btWarning.setVisibility(View.VISIBLE);
        }else{
            tvWarning.setVisibility(View.GONE);
            btWarning.setVisibility(View.GONE);
        }
    }



    //----- MENU & SHAREDPREFERENCES -----//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.mnSettings:
                return viewSettingsActivity();
            case R.id.mnUpdate:
                setText();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //I could have set the listener in an anonymous class at init(), but at this way is more scalable
        switch (key){
            case "source":
                setText();
                break;
        }
    }

    private boolean viewSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        return true;
    }
    //----------------//
}
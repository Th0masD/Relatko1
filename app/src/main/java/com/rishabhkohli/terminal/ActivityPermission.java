package com.rishabhkohli.terminal;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

// Import permission  files.

import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission.WRITE_SETTINGS;
import static android.Manifest.permission.SEND_SMS;



public class ActivityPermission extends AppCompatActivity {

    Button button1,button2,button3;
    public static final int RequestPermissionCode = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        // Assigning ID to button.
        button1 = (Button)findViewById(R.id.button1);
        button2 = (Button)findViewById(R.id.button2);
        button3 = (Button)findViewById(R.id.button3);

        // Adding Click listener to Button.
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Adding if condition inside button.

                // If All permission is enabled successfully then this block will execute.
                if(CheckingPermissionIsEnabledOrNot())
                {
                    Toast.makeText(ActivityPermission.this, "All Permissions Granted Successfully", Toast.LENGTH_LONG).show();
                }

                // If, If permission is not enabled then else condition will execute.
                else {

                    //Calling method to enable permission.
                    RequestMultiplePermission();

                }

            }
        });


        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityPermission.this, ActivityLaunch.class);
                startActivity(intent);

            }
        });




        // Get the button and add onClickListener.
 //       final Button writeSettingsPermissionButton = (Button)findViewById(R.id.write_settings_permission_button);
 //       writeSettingsPermissionButton.setOnClickListener(new View.OnClickListener() {


        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Context context = getApplicationContext();

                // Check whether has the write settings permission or not.
                boolean settingsCanWrite = Settings.System.canWrite(context);

                if(!settingsCanWrite) {
                    // If do not have write settings permission then open the Can modify system settings panel.
                    //    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:com.rishabhkohli.terminal"));
                    startActivity(intent);
                    //      startActivity(intent);
                }else {
                    // If has permission then show an alert dialog with message.
                    AlertDialog alertDialog = new AlertDialog.Builder(ActivityPermission.this).create();
                    alertDialog.setMessage("You have system write settings permission now.");
                    alertDialog.show();
                }
            }
        });


    }

    //Permission function starts from here
    private void RequestMultiplePermission() {

        // Creating String Array with Permissions.
        ActivityCompat.requestPermissions(ActivityPermission.this, new String[]
                {
                        WRITE_SETTINGS,
                        SEND_SMS,
                        GET_ACCOUNTS
                }, RequestPermissionCode);

    }

    // Calling override method.
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:

                if (grantResults.length > 0) {

                    boolean RecordAudioPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean SendSMSPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean GetAccountsPermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (RecordAudioPermission && SendSMSPermission && GetAccountsPermission) {

                        Toast.makeText(ActivityPermission.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(ActivityPermission.this,"Permission Denied",Toast.LENGTH_LONG).show();

                    }
                }

                break;
        }
    }

    // Checking permission is enabled or not using function starts from here.
    public boolean CheckingPermissionIsEnabledOrNot() {


        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_SETTINGS);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), SEND_SMS);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), GET_ACCOUNTS);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED;

    }


}

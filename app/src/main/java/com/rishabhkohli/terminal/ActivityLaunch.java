package com.rishabhkohli.terminal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ActivityLaunch extends AppCompatActivity {

    private ArrayList<String> savedDeviceMacAddres;
    private ArrayList<String> connectedDevicesList;
    private ArrayAdapter<String> stringArrayAdapter1;
    private ArrayAdapter<String> stringArrayAdapter2;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        sharedPreferences = getSharedPreferences("Devices", MODE_PRIVATE);
        savedDeviceMacAddres = new ArrayList<>();
        connectedDevicesList = new ArrayList<>();

        String line;

        for (int i = 0; i < sharedPreferences.getInt("count", 0); i++) {

         //   line = sharedPreferences.getString("details_"+i, "");
         //   String[] splitted = line.split(">");
         //   savedDeviceMacAddres.add(splitted[1]);
             savedDeviceMacAddres.add(sharedPreferences.getString("details_"+i, ""));
        }




        ListView savedDevicesListView = (ListView)findViewById(R.id.saved_devices_listView);

        stringArrayAdapter1 = new ArrayAdapter<String>(this, R.layout.devices_list_item, R.id.item_text, savedDeviceMacAddres) {
            @NonNull
            @Override
            public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View viewToReturn = super.getView(position, convertView, parent);
                viewToReturn.findViewById(R.id.del_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteEntry(position);
                    }
                });
                return viewToReturn;
            }
        };


        savedDevicesListView.setAdapter(stringArrayAdapter1);

        savedDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String connectionDetail = ((TextView)(view.findViewById(R.id.item_text))).getText().toString();
           //     String ip = connectionDetail.substring(0, connectionDetail.indexOf(">"));
                String mac = connectionDetail.substring(0, connectionDetail.indexOf(">"));

                String ip = assignIpAddress(mac);
                if(ip == null) Toast.makeText(getApplicationContext(), "Device not connected", Toast.LENGTH_LONG).show();
                else  connect(ip, 80);


                // int port = Integer.parseInt(connectionDetail.substring(connectionDetail.indexOf(":") + 1, connectionDetail.length()));

            }
        });


        ListView connectedDevicesListView = (ListView)findViewById(R.id.connect_devices_listView);
        stringArrayAdapter2 = new ArrayAdapter<String>(this, R.layout.devices_list_item, R.id.item_text, connectedDevicesList);
        connectedDevicesListView.setAdapter(stringArrayAdapter2);

        readAddresses();

/*
        findViewById(R.id.connect_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = ((EditText) findViewById(R.id.ip_edit_text)).getText().toString();
                int port = Integer.parseInt(((EditText) findViewById(R.id.port_edit_text)).getText().toString());
                int port = 80;
                savedDeviceMacAddres.add(ip + ":" + Integer.toString(port));
                saveList();
                connect(ip, port);
            }
        });

*/

        findViewById(R.id.add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityLaunch.this, ActivityAddDevice.class);
                startActivityForResult(intent, 0);

            }
        });


        findViewById(R.id.refresh_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectedDevicesList.clear();
                stringArrayAdapter2.notifyDataSetChanged();
                readAddresses();
            }
        });

        findViewById(R.id.buttonOn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.rishabhkohli.terminal.WifiApControl.turnOnOffHotspot(getApplicationContext(),true);

            }
        });

        findViewById(R.id.buttonOff).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.rishabhkohli.terminal.WifiApControl.turnOnOffHotspot(getApplicationContext(),false);

            }
        });




    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check that it is the SecondActivity with an OK result
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) { // Activity.RESULT_OK

                // get String data from Intent
                String returnString = data.getStringExtra("MacValue");
                savedDeviceMacAddres.add(returnString + ":80");
                saveList();
            }
        }
    }


    private String assignIpAddress(String mac) {


        for (int i = 0; i < connectedDevicesList.size() ; i++) {

            String line = connectedDevicesList.get(i);
            String[] splitted = line.split(">");
            if(mac.equals(splitted[1])) {
                Toast.makeText(getApplicationContext(), "IP:" + splitted[0], Toast.LENGTH_LONG).show();
                return (splitted[0]);
            }
       }
            //   line = sharedPreferences.getString("details_"+i, "");
            //   String[] splitted = line.split(">");
            //   savedDeviceMacAddres.add(splitted[1]);
     //   Toast.makeText(getApplicationContext(), "IP:"+ splitted[1], Toast.LENGTH_LONG).show();
        return (null);
        }






    private void readAddresses() {
        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        connectedDevicesList.add(ip + ">" + mac);
                    }
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private void connect(String ip, int port) {
        Intent intent = new Intent(ActivityLaunch.this, ActivityTerminal.class);
        intent.putExtra("port", port);
        intent.putExtra("ip", ip);
        startActivity(intent);
    }

    private void saveList() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (int i = 0; i < savedDeviceMacAddres.size(); i++) {
            editor.putString("details_"+i, savedDeviceMacAddres.get(i));
        }
        editor.putInt("count", savedDeviceMacAddres.size());
        editor.apply();
        stringArrayAdapter1.notifyDataSetChanged();
    }

    private void deleteEntry(int position) {
        savedDeviceMacAddres.remove(position);
        stringArrayAdapter1.notifyDataSetChanged();
        saveList();
    }
}
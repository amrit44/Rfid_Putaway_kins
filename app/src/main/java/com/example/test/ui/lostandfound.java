package com.example.test.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.test.R;
import com.example.test.RFIDActivity;
import com.example.test.RFIDHandlerlost;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;
import com.zebra.rfid.api3.TagData;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import adapter.lostadapter;
import model.lostlist;


public class lostandfound extends AppCompatActivity implements RFIDHandlerlost.ResponseHandlerInterface{
    private static String ip = "43.230.201.20" ;// this is the host ip that your data base exists on you can use 10.0.2.2 for local host                                                    found on your pc. use if config for windows to find the ip if the database exists on                                                    your pc
    private static String port = "1232";// the port sql server runs on
    private static String Classes = "net.sourceforge.jtds.jdbc.Driver";// the driver that is required for this connection use                                                                           "org.postgresql.Driver" for connecting to postgresql
    private static String database = "KinsBackup";// the data base name
    private static String username = "sa";// the user name
    private static String password = "Yu6SBA5s4u#zcT6%e";// the password
    private static String url = "jdbc:jtds:sqlserver://"+ip+":"+port+"/"+database; // the connection url string
    public TextView statustvRFID_lf = null;
    Statement stmt;
    StringBuilder sb;
    List<String> myListT;
    private Connection connection = null;
    private String searchser;
    int totaltagCount = 0;
    private int MAX_POWER = 270;
    ArrayList<String> rangeList;
    private int AVERAGE_POWER = 50;
    private int MIN_POWER = 10;
    private String  serialnum,Racks;
    private String SerialnumCheck;
    private String searchcarton;
    private EditText Carton_num;
    private EditText textrfid;
    ArrayList<lostlist> myList;
    SearchableSpinner spinArticlenum, spincolornum,spinnerSelectRange;
    ArrayList<String> articlenumlist;
    ArrayList<String> colorList;
    private String articlenum,articlecolor;
    private TextView testlfStatus, tvcoutnTv_lf,tvCountSeriallf;
    RFIDHandlerlost rfidHandlerLf;
    final static String TAG = "RFID_SAMPLE";
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lostandfound);

        onSelectSpinner();
        //function
        initView();
        // assign list
        lists();
        //connection status
        connecstatus();
        enterclick();
        rangeSpin();

        // Create an executor that executes tasks in the main thread.
        Executor mainExecutor = ContextCompat.getMainExecutor(this);
        // Execute a task in the main thread
        mainExecutor.execute(new Runnable() {
            @Override
            public void run() {

                querySpinarticle();
            }
        });


        spinnerSelectRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String itemName = spinnerSelectRange.getSelectedItem().toString();


                String result = "";
                if (itemName.equalsIgnoreCase("Max")) {
                    result = rfidHandlerLf.Defaults(MAX_POWER);
                } else if (itemName.equalsIgnoreCase("Average")) {
                    result = rfidHandlerLf.Defaults(AVERAGE_POWER);
                } else if (itemName.equalsIgnoreCase("Low")) {
                    result = rfidHandlerLf.Defaults(MIN_POWER);
                }
                Toast.makeText(lostandfound.this, "" + itemName + " range set successfully", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });




    }



    private void enterclick() {
        textrfid.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN) {



                    return false;
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    SerialnumCheck= textrfid.getText().toString();


                    return true;
                }

                return false;
            }
        });

        Carton_num.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    return false;
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER) {

                    searchcarton=  Carton_num.getText().toString();


                    return true;
                }

                return false;
            }
        });


    }
    private void rangeSpin() {

        try {
            rangeList.add("Max");
            rangeList.add("Average");
            rangeList.add("Low");

            ArrayAdapter arrayAdapter = new ArrayAdapter(lostandfound.this, android.R.layout.simple_spinner_dropdown_item, rangeList);
            spinnerSelectRange.setAdapter(arrayAdapter);
            spinnerSelectRange.setTitle("Select Item");
            spinnerSelectRange.setPositiveButton("OK");


        } catch (Exception ex) {

        }
    }



    private void checkdata(String articlenum, String articlecolor) {
        ArrayList<lostlist> myList = new ArrayList<>();
        try {
            CallableStatement stmt = connection.prepareCall("{call get_SerialAndRackByItemColor(?,?)}");
            stmt.setString(1, articlenum);
            stmt.setString(2, articlecolor);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                serialnum = rs.getString("SerialNumber");
                String Racks = rs.getString("RackNo");
                if (Objects.equals(serialnum, SerialnumCheck)) {
                    Toast.makeText(lostandfound.this, "FOUND!!!!", Toast.LENGTH_SHORT).show();



                    myList.add(new lostlist(SerialnumCheck, Racks));
                    Toast.makeText(lostandfound.this, "" + myList.size() + " range set successfully", Toast.LENGTH_SHORT).show();


                }
            }

            lostadapter adapter = new lostadapter(myList);
            RecyclerView recyclerView = findViewById(R.id.lostandfoundgrid);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(adapter);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void connecstatus() {


            // UI
            statustvRFID_lf = findViewById(R.id.tvStatuslf);
        testlfStatus = findViewById(R.id.testStatus);

            // RFID Handler
            rfidHandlerLf = new RFIDHandlerlost();
            rfidHandlerLf.onCreate(this);

            // set up button click listener
            Button test = findViewById(R.id.button);
            test.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String result = rfidHandlerLf.Test1();
                    testlfStatus.setText(result);
                }
            });

            Button test2 = findViewById(R.id.button2);
            test2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String result = rfidHandlerLf.Test2();
                    testlfStatus.setText(result);
                }
            });

            Button defaultButton = findViewById(R.id.button3);
            defaultButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String result = rfidHandlerLf.Defaults(MAX_POWER);
                    testlfStatus.setText(result);
                }
            });
        }





    private void onSelectSpinner() {
        spinArticlenum = (SearchableSpinner) findViewById(R.id.spinnerArticlenum);
        spinArticlenum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object selectedItem = spinArticlenum.getSelectedItem();
                if (selectedItem != null) {
                    articlenum = selectedItem.toString();
                }
              //  articlenum = spinArticlenum.getSelectedItem().toString();
                Toast.makeText(lostandfound.this, "" + articlenum, Toast.LENGTH_SHORT).show();
                if (textrfid != null) {
                    textrfid.setText("");
                }


               querySpincolor(articlenum);

;


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spincolornum = (SearchableSpinner) findViewById(R.id.spinnercolornum);
        spincolornum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object selectedItem = spincolornum.getSelectedItem();
                if (selectedItem != null) {
                 articlecolor = selectedItem.toString();
                    // Your code here
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void querySpincolor(String articlenum) {
        colorList.clear();
        try {
            CallableStatement stmt = connection.prepareCall("{call getcolor(?)}");
            stmt.setString(1, articlenum);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                articlecolor = rs.getString("Desc1");
                colorList.add(articlecolor);
            }
            if (colorList != null && !colorList.isEmpty()) {
                ArrayAdapter arrayAdapter = new ArrayAdapter(lostandfound.this, android.R.layout.simple_spinner_dropdown_item, colorList);
                spincolornum.setAdapter(arrayAdapter);
                spincolornum.setTitle("Select Item");
                spincolornum.setPositiveButton("OK");
            } else {
                // handle the case where colorList is empty or null
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    //for fetching article number in article spinner
    private void querySpinarticle() {
        articlenumlist.clear();
        try {
            String query = "SELECT DISTINCT itemName FROM wrh_putaway";
            System.out.println("The SQL statement is: " + query + "");
            ResultSet resultSet = stmt.executeQuery(query);
            Set<String> uniqueItems = new HashSet<>();
            while (resultSet.next()) {
                articlenum = resultSet.getString("itemName");
                //uniqueItems.add("hiii");
                articlenumlist.add(articlenum);
            }
            //articlenumlist.addAll(articlenum);
            if (!articlenumlist.isEmpty()) {
                ArrayAdapter arrayAdapter = new ArrayAdapter(lostandfound.this, android.R.layout.simple_spinner_dropdown_item, articlenumlist);
                spinArticlenum.setAdapter(arrayAdapter);
                spinArticlenum.setTitle("Select Item");
                spinArticlenum.setPositiveButton("OK");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void lists() {
        articlenumlist = new ArrayList<>();
        colorList = new ArrayList<>();
    }

    private void initView() {
        //find all view id
        textrfid = findViewById(R.id.edtsearchsacn);
        Carton_num= (EditText) findViewById(R.id.edtcartonno);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.lostandfoundgrid);


        testlfStatus=(TextView) findViewById(R.id.tvStatuslf);
        tvCountSeriallf=(TextView) findViewById(R.id.tvCountSerial_lf);
        tvcoutnTv_lf=(TextView) findViewById(R.id.tvcount_lf);

        //ALL THE SPINNERS
        spinArticlenum = (SearchableSpinner) findViewById(R.id.spinnerArticlenum);
        spincolornum = (SearchableSpinner) findViewById(R.id.spinnercolornum);
        spinnerSelectRange = (SearchableSpinner) findViewById(R.id.spinnerSelectRange);

    }

    @Override
    public void handleTagdata(TagData[] tagData) {
        sb = new StringBuilder();

        myListT = new ArrayList<>();

        for (int index = 0; index < tagData.length; index++) {
            sb.append(tagData[index].getTagID() + "\n");
            myListT.add(tagData[index].getTagID());

            try {
                checkISerialNumbertest(tagData[index].getTagID());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {


            }
        });
    }

    private void checkISerialNumbertest(String serialNumber) {
        runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                RecyclerView recyclerView = findViewById(R.id.putawaygrid);
                totaltagCount++;
                boolean isTagPresent = false;
                if (myList != null) {
                    for (int i = 0; i < myList.size(); i++) {
                        if (serialNumber.equalsIgnoreCase(myList.get(i).getSerialnumber())) {
                            // tag already present
                            isTagPresent = true;
                            break;
                        }
                    }
                } else {
                    // handle the case where myList is null
                }
                if (!isTagPresent) {
                    checkdata( articlenum, articlecolor);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseConnection();
    }
    @Override
    protected void onPause() {
        super.onPause();
        rfidHandlerLf.onPause();
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        String status = rfidHandlerLf.onResume();
        statustvRFID_lf.setText(status);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        rfidHandlerLf.onDestroy();
    }

    private void databaseConnection() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Class.forName(Classes);
            connection = DriverManager.getConnection(url, username,password);
            stmt = connection.createStatement();
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Class fail", Toast.LENGTH_SHORT).show();
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Connected no", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    testlfStatus.setText("");
                }
            });
          rfidHandlerLf.performInventory();
        } else
            rfidHandlerLf.stopInventory();

    }


}
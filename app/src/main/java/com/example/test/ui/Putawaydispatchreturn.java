package com.example.test.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.test.R;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;
import com.zebra.rfid.api3.TagData;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import adapter.MyListAdapter;
import model.MyListData;

public class Putawaydispatchreturn extends AppCompatActivity implements RFIDHandlerPutawayreturn.ResponseHandlerInterface {


    private static String ip = "43.230.201.20" ;// this is the host ip that your data base exists on you can use 10.0.2.2 for local host                                                    found on your pc. use if config for windows to find the ip if the database exists on                                                    your pc
    private static String port = "1232";// the port sql server runs on
    private static String Classes = "net.sourceforge.jtds.jdbc.Driver";// the driver that is required for this connection use                                                                           "org.postgresql.Driver" for connecting to postgresql
    private static String database = "kinsBackup";// the data base name
    private static String username = "sa";// the user name
    private static String password = "Yu6SBA5s4u#zcT6%e";// the password
    private static String url = "jdbc:jtds:sqlserver://"+ip+":"+port+"/"+database; // the connection url string
    private int MAX_POWER = 270;
    private int AVERAGE_POWER = 50;
    private int MIN_POWER = 10;
    public TextView statusTextViewRFID = null;
    private EditText textrfid;
    private EditText rackNumber;
    private String rackNo;
    String query;
    private TextView testStatus, tvtotaltagCount, tvtotaluniquetagCount, tvcoutnTv,tvCountSerial;
    int totaltagCount = 0;
    int totaluniquetagCount = 0;
    RFIDHandlerPutawayreturn rfidHandler;
    final static String TAG = "RFID_SAMPLE";
    StringBuilder sb;
    List<String> myListT;
    Statement stmt;
    private Connection connection = null;
    Dialog dialog;
    SearchableSpinner spinnerSearchPurchaseNumber, spinnerSearchItemName, spinnerSelectRange;
    ArrayList<MyListData> myListData;


    ArrayList<String> puchaseNumberList;
    ArrayList<String> rangeList;
    ArrayList<String> listitemName;
    ArrayList<String> itemNameList;
    //use in insert
    String purchaseNumber;
    String Ordernumber;
    String itemName;
    String partyName;
    String qtySerialNumber;
    int DispatchreturnId;
    String purchaseqty;
    String totalqty;

    int itemIdSave2;

    String itemCodesave2;
    int itemIdSave;
    String itemCodesave;
    String SerialNumberdb;
    int checklock;
    String itemNameSave;
    String itemQtySave;
    String currentdata;
    Button btncustom;
    String color;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_putawaydispatchreturn);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        currentdata = formatter.format(date);
        Log.i("currentdata", "currentdata : " + currentdata);
        // assign variable
        initView();
        // assign list
        lists();

        rangeSpin();

        // Create an executor that executes tasks in the main thread.
        Executor mainExecutor = ContextCompat.getMainExecutor(this);
        // Execute a task in the main thread
        mainExecutor.execute(new Runnable() {
            @Override
            public void run() {

                querySpinDispatchNumber();
            }
        });
        //onselected Spinners
        onSelectSpinner();
        //validate rack
        validateRack();


        useLess();



    }

    private void validateRack() {
        rackNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    return false;
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER) {

                    rackNo = rackNumber.getText().toString();
                    checkRackNumber(rackNo);

                    return true;
                }

                return false;
            }
        });
    }

    private void validateSerialNumber(String serialNumber) {
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try (Connection conn = DriverManager.getConnection("jdbc:jtds:sqlserver://"+ip+":"+port+"/"+database, "sa", "Yu6SBA5s4u#zcT6%e");
                     Statement stmt2 = conn.createStatement()) {
                    if (itemName==null && color==null)
                    {


                     query=  "select SerialNumber from INV_ShoeDispatchReturnItemSerialNumber ipis  \n" +
                               "inner join Inv_ShoeDispatchReturnItem ipi on ipis.ShoeDispatchReturnItemId=ipi.Id\n" +
                               "where ipis.OrderNumber='"+purchaseNumber+"' and\n" +
                               "ipis.SerialNumber ='" + serialNumber + "' and ipis.isdeleted is null";
                    }
                    else{

                        query="select SerialNumber from INV_ShoeDispatchReturnItemSerialNumber ipis  \n" +
                                "inner join Inv_ShoeDispatchReturnItem ipi on ipis.ShoeDispatchReturnItemId=ipi.Id\n" +
                                "where ipi.Particular='"+itemNameSave+"' and  \n" +
                                "ipis.OrderNumber='"+purchaseNumber+"' and \n" +
                                "ipi.Desc1 ='"+color+"' and\n" +
                                "ipis.SerialNumber ='" + serialNumber + "' and ipis.isdeleted is null";

                    }
                    System.out.println("The SQL statement is: " + query + "");
                    ResultSet resultSet = stmt2.executeQuery(query);

                    if (resultSet.next()) {
                        SerialNumberdb = resultSet.getString("SerialNumber");
                        if (Objects.equals(serialNumber, SerialNumberdb)) {
                            getdataPurchaseNo(DispatchreturnId, serialNumber,stmt2);
                           checkSerialExicting(serialNumber,purchaseNumber);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                 //   Toast.makeText(getApplicationContext(), "Invalid Rack Number", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        // handle no results
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }



    private void getdataPurchaseNo(int dispatchreturnId, String serialNumber, Statement stmt2) {
        try {
            String query="SELECT IPT.Particular AS ItemName,IPT.ItemId,IPT.Desc1 as Color,I.Code FROM inv_purchaseitem IPT\n" +
                    "LEFT JOIN inv_purchaseitemSERIALNUMBER IPTS ON IPT.Id = IPTS.PurchaseItemId \n" +
                    "left join Item I on I.Id=IPT.ItemId where IPTS.SerialNumber='" + serialNumber + "'";

            System.out.println("The SQL statement is: " + query + "");

            ResultSet resultSet = stmt2.executeQuery(query);

            if (resultSet.next()) {
                itemIdSave = resultSet.getInt("ItemId");
                itemCodesave = resultSet.getString("Code");
                itemNameSave = resultSet.getString("ItemName");
                runOnUiThread(() -> {
                });
            }


        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }


    }

    private void checkRackNumber(String rackNo) {
        try {
            String query = "select Name from [Wrh_RackMaster]  where IsDeleted is null and Name='" + rackNo + "'  ";
            System.out.println("The SQL statement is: " + query + "");
            ResultSet resultSet = stmt.executeQuery(query);

            if (resultSet.next())
            {

                textrfid.requestFocus();

            }
            else
            {
                Toast.makeText(this, "Invalid Rack Number", Toast.LENGTH_SHORT).show();
                rackNumber.setText("");

            }

        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void useLess() {
        // UI
        statusTextViewRFID = findViewById(R.id.textStatus);
        testStatus = findViewById(R.id.testStatus);

        // RFID Handler
        rfidHandler = new RFIDHandlerPutawayreturn();
        rfidHandler.onCreate(this);

        // set up button click listener
        Button test = findViewById(R.id.button);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = rfidHandler.Test1();
                testStatus.setText(result);
            }
        });

        Button test2 = findViewById(R.id.button2);
        test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = rfidHandler.Test2();
                testStatus.setText(result);
            }
        });

        Button defaultButton = findViewById(R.id.button3);
        defaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = rfidHandler.Defaults(MAX_POWER);
                testStatus.setText(result);
            }
        });
    }

    private void querySpinDispatchNumber() {
        puchaseNumberList.clear();
        try {
            // String query = "select Id,PurchaseNumber,TotalQty,AccountName,Email from viewPurchaseMaster";
            String query = "select distinct OrderNumber from INV_ShoeDispatchReturnItem where IsDeleted is null";
            System.out.println("The SQL statement is: " + query + "");
            ResultSet resultSet = stmt.executeQuery(query);

            while (resultSet.next()) {
                String OrderNumber = resultSet.getString("OrderNumber");


                puchaseNumberList.add(OrderNumber);

                ArrayAdapter arrayAdapter = new ArrayAdapter(Putawaydispatchreturn.this, android.R.layout.simple_spinner_dropdown_item, puchaseNumberList);
                spinnerSearchPurchaseNumber.setAdapter(arrayAdapter);
                spinnerSearchPurchaseNumber.setTitle("Select Item");
                spinnerSearchPurchaseNumber.setPositiveButton("OK");
            }


        } catch (Exception ex) {

        }

    }
    private void onSelectSpinner() {
        spinnerSearchPurchaseNumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                purchaseNumber = spinnerSearchPurchaseNumber.getSelectedItem().toString();
                rackNumber.setText("");
                textrfid.setText("");
                itemNameList.clear();
                Toast.makeText(Putawaydispatchreturn.this, "" + purchaseNumber, Toast.LENGTH_SHORT).show();
                querySpinItemNumber(purchaseNumber);
                valueBypurchaseNumber(purchaseNumber);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerSearchItemName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String itemNameColor = spinnerSearchItemName.getSelectedItem().toString();
                String[] splitString = itemNameColor.split("/");
                itemNameSave=splitString[0];
                color=splitString[1];
                getpuchaseItemId(itemNameSave,color);
                getdetails(purchaseNumber,itemNameSave,color);
                //  Toast.makeText(RFIDActivity.this, "" + itemNameSave +color , Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerSelectRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String itemName = spinnerSelectRange.getSelectedItem().toString();


                String result = "";
                if (itemName.equalsIgnoreCase("Max")) {
                    result = rfidHandler.Defaults(MAX_POWER);
                } else if (itemName.equalsIgnoreCase("Average")) {
                    result = rfidHandler.Defaults(AVERAGE_POWER);
                } else if (itemName.equalsIgnoreCase("Low")) {
                    result = rfidHandler.Defaults(MIN_POWER);
                }
                Toast.makeText(Putawaydispatchreturn.this, "" + itemName + " range set successfully", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void getdetails(String purchaseNumber, String itemNameSave, String color) {
        try {

            String query=  "select sum(Box) as qty from inv_shoedispatchreturnitem where IsDeleted is null AND OrderNumber='" + purchaseNumber + "' and Particular='" + itemNameSave + "' AND Desc1='" + color + "'";
            System.out.println("The SQL statement is: " + query + "");
            ResultSet resultSet = stmt.executeQuery(query);

            if (resultSet.next())
            {
                //partyName= resultSet.getString("PartyName");
                purchaseqty=resultSet.getString("qty");

                tvCountSerial.setText(purchaseqty);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void getpuchaseItemId(String itemNameSave, String color) {
        try {
            String query ="select  Id,Box,Code from INV_ShoeDispatchReturnItem  where IsDeleted is null and Particular='"+itemNameSave+"' and Desc1='"+color+"' and OrderNumber='" + purchaseNumber + "'";
            System.out.println("The SQL statement is: " + query + "");
            ResultSet resultSet = stmt.executeQuery(query);

            if (resultSet.next())
            {
//               itemIdSave2 = resultSet.getInt("Id");
//                itemCodesave2 = resultSet.getString("Code");
                qtySerialNumber = resultSet.getString("Box");
                tvCountSerial.setText(qtySerialNumber);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void valueBypurchaseNumber(String purchaseNumber) {
        try {
           // String query = "select Id,PurchaseNumber,TotalQty,AccountName,Email from viewPurchaseMaster where PurchaseNumber='" + purchaseNumber + "' ";
            String query ="select a.Name as partyname, sdr.OrderNumber,sdr.Id,sum(Qty) as Qty from \n" +
                    " INV_ShoeDispatchReturn sdr left join \n" +
                    " INV_ShoeDispatchreturnItem sdri \n" +
                    "on sdri.ShoeDispatchReturnId=sdr.id left join  \n" +
                    "Account a on a.id=sdr.AccountId where sdr.IsDeleted is null and sdri.isDeleted is null and \n" +
                    "sdr.OrderNumber='" + purchaseNumber + "'  group by a.name ,sdr.ordernumber, Sdr.id ";
            System.out.println("The SQL statement is: " + query + "");
            ResultSet resultSet = stmt.executeQuery(query);

            if (resultSet.next()) {

                DispatchreturnId = resultSet.getInt("Id");
                Ordernumber = resultSet.getString("OrderNumber");
                totalqty = resultSet.getString("Qty");
                partyName = resultSet.getString("PartyName");

                //  Toast.makeText(this, "accountName : " + partyName, Toast.LENGTH_SHORT).show();
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void querySpinItemNumber(String purchaseNumber) {
        itemNameList.clear();
        try {

            String query =  "select distinct Particular,Desc1 from INV_ShoeDispatchReturnItem where IsDeleted is null and OrderNumber ='" + purchaseNumber + "' ";
            System.out.println("The SQL statement is: " + query + "");
            ResultSet resultSet = stmt.executeQuery(query);

            while (resultSet.next()) {

                String itemName = resultSet.getString("Particular");
                String itemcolor = resultSet.getString("Desc1");
                itemNameList.add(itemName+"/"+itemcolor);
                ArrayAdapter arrayAdapter = new ArrayAdapter(Putawaydispatchreturn.this, android.R.layout.simple_spinner_dropdown_item, itemNameList);
                spinnerSearchItemName.setAdapter(arrayAdapter);
                spinnerSearchItemName.setTitle("Select Item");
                spinnerSearchItemName.setPositiveButton("OK");
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void lists() {
        // assign List
        myListData = new ArrayList<>();
        puchaseNumberList = new ArrayList<>();
        itemNameList = new ArrayList<>();
        rangeList = new ArrayList<>();
    }

    private void initView() {
        textrfid = findViewById(R.id.textViewdata);
        rackNumber = findViewById(R.id.edtRackNumber);
        tvtotaltagCount = findViewById(R.id.tvtotaltagCount);
        tvtotaluniquetagCount = findViewById(R.id.tvTotalUniquetagCount);
        tvcoutnTv = findViewById(R.id.tvcoutnTv);
        tvCountSerial = findViewById(R.id.tvCountSerial);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.putawaygrid);
        //ALL THE SPINNERS
        spinnerSearchPurchaseNumber = (SearchableSpinner) findViewById(R.id.spinnerSearchPurchaseNumberm);
        spinnerSearchItemName = (SearchableSpinner) findViewById(R.id.spinnerSearchItemNamem);
        spinnerSelectRange = (SearchableSpinner) findViewById(R.id.spinnerSelectRange);

    }
    private void rangeSpin() {

        try {
            rangeList.add("Max");
            rangeList.add("Average");
            rangeList.add("Low");

            ArrayAdapter arrayAdapter = new ArrayAdapter(Putawaydispatchreturn.this, android.R.layout.simple_spinner_dropdown_item, rangeList);
            spinnerSelectRange.setAdapter(arrayAdapter);
            spinnerSelectRange.setTitle("Select Item");
            spinnerSelectRange.setPositiveButton("OK");


        } catch (Exception ex) {

        }
    }



    private void checkSerialExicting(String serialNumber, String purchaseNumber) {

        try (Connection conn = DriverManager.getConnection("jdbc:jtds:sqlserver://"+ip+":"+port+"/"+database, "sa", "Yu6SBA5s4u#zcT6%e");
        Statement stmt2 = conn.createStatement()) {
            String query = " select SerialNumber from Wrh_PutAway where  SerialNumber = '" + serialNumber + "' and PurchaseNo='"+purchaseNumber+"' ";
            System.out.println("The SQL statement is: " + query + "");
            ResultSet resultSet = stmt2.executeQuery(query);

            if (resultSet.next()) {
                String SerialNumberdb = resultSet.getString("SerialNumber");
                if (Objects.equals(SerialNumberdb, serialNumber)) {
                    //Toast.makeText(this, "Serial Number Already Exist", Toast.LENGTH_SHORT).show();

                } else {



                }

            }
            else {

                insertRfidPutAwayData(null,1,"1","Serial",DispatchreturnId,purchaseNumber,partyName,itemCodesave,itemIdSave,itemNameSave,purchaseqty,1,rackNo,2,"127.0.0.1",1,currentdata,null,null,1,null,serialNumber);
                updaterack(serialNumber,rackNo);

            }

        } catch (Exception ex) {

            ex.printStackTrace();
            Log.d("exception1", "insertRfidPutAwayData: "+ex.getMessage().toString());
        }

    }

    private void updaterack(String serialNumber, String rackNo) {
        try {


            // Build the SQL query string
            String query = "UPDATE inv_purchaseitemserialnumber SET RackNumber = ? WHERE Serialnumber = ?";

            // Create a prepared statement to execute the query
            PreparedStatement statement = connection.prepareStatement(query);

            // Set the parameter values for the prepared statement
            statement.setString(1, rackNo);
            statement.setString(2, serialNumber);

            // Execute the query
            int rowsUpdated = statement.executeUpdate();

            // Check the number of rows updated to confirm the update was successful
            if (rowsUpdated > 0) {
                System.out.println("Serial number " + serialNumber + " updated with new Rack Number: " + rackNo);
            } else {
                System.out.println("No rows updated for Serial number " + serialNumber);
            }
        } catch (SQLException e) {
            System.out.println("Error updating Rack Number for Serial number " + serialNumber + ": " + e.getMessage());
        }
    }



    private void setRecyclerView(final String serialNumber) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                // Check for duplicate serial number
                boolean isDuplicate = false;
                for (MyListData data : myListData) {
                    if (data.getDescription().equals(serialNumber)) {
                        isDuplicate = true;
                        break;
                    }
                }

                if (!isDuplicate) {
                    // Add new data to list
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //checkSerialNumber
                            //totaluniquetagCount++;


                            RecyclerView recyclerView = findViewById(R.id.putawaygrid);
                            totaluniquetagCount++;
                            myListData.add(new MyListData(serialNumber, rackNo, "1"));
                            MyListAdapter adapter = new MyListAdapter(myListData);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                            recyclerView.setAdapter(adapter);
                            textrfid.setText(serialNumber);
                            textrfid.requestFocus();


                        }
                    });
                }
            }
        });
    }

    private void insertRfidPutAwayData(Object o, int i, String s, String serial, int DispatchreturnId, String purchaseNumber, String partyName, String itemCodesave, int itemIdSave, String itemNameSave, String purchaseqty, int i1, String rackNo, int i2, String s1, int i3, String currentdata, Object o1, Object o2, int i4, Object o3, String serialNumber) {
        try {
            int n = 0;
            String sql = "{call INSERTRFIDPUTAWAY (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)}";
            CallableStatement statement = connection.prepareCall(sql);
            statement.setDate(1, null);//IsDeleted
            statement.setInt(2, 1);//CompanyId
            statement.setInt(3, 1);//StoreId
            statement.setString(4, "DR");//Type
            statement.setInt(5, DispatchreturnId);//PurchaseId
            statement.setString(6, Ordernumber);//PurchaseNo
            statement.setString(7, partyName);//PartyName
            statement.setString(8, itemCodesave);//ItemCode
            statement.setInt(9, itemIdSave);//ItemId
            statement.setString(10, itemNameSave);//ItemName
            BigDecimal d = new BigDecimal(String.valueOf(qtySerialNumber == null ? 0 : qtySerialNumber));
            statement.setBigDecimal(11, d);
            statement.setInt(12, 1);//Qty
            statement.setString(13, rackNo);//RackNo
            statement.setInt(14, 2);//FinancialYearId
            statement.setString(15, "127.0.0.1");//WorkStation
            statement.setInt(16, 1);//CreatedBy
            statement.setString(17, currentdata);//CreatedOn
            statement.setString(18, null);//UpdatedBy
            statement.setDate(19, null);//UpdatedOn
            statement.setInt(20, 1);//ItemQty
            statement.setString(21, null);//BatchNo
            statement.setString(22, serialNumber);//SerialNumber

            n = statement.executeUpdate();
            Log.d("TAG", "Trying to insert data...");

            if (n > 0) {
                setRecyclerView(serialNumber);
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (SQLException e) {
            Log.e("TAG", "Error inserting data: " + e.getMessage());
            Log.d("exception", "insertRfidPutAwayData: "+e.getMessage().toString());
            e.printStackTrace();
        }
    }










    @Override
    protected void onStart() {
        super.onStart();

        databaseConnection();



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
    protected void onPause() {
        super.onPause();
        rfidHandler.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        String status = rfidHandler.onResume();
        statusTextViewRFID.setText(status);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rfidHandler.onDestroy();
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
        runOnUiThread(new Runnable()
        {

            @SuppressLint("SetTextI18n")
            @Override
            public void run()
            {
                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.putawaygrid);
                totaltagCount++;
                boolean isTagPresent = false;
                for (int i = 0; i < myListData.size(); i++) {
                    if (serialNumber.equalsIgnoreCase(myListData.get(i).getDescription()))
                    {
                        // tag already present
                        isTagPresent = true;
                        break;
                    }
                }

                if (!isTagPresent)
                {
                    validateSerialNumber(serialNumber);
                    //checkSerialExicting(serialNumber,stmt);
                }
                tvtotaltagCount.setText("Total tags scanned : " + totaltagCount);
                tvtotaluniquetagCount.setText("Unique tags scanned : " + totaluniquetagCount);
                tvcoutnTv.setText("" + totaluniquetagCount);
            }
        });

    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textrfid.setText("");
                }
            });
            rfidHandler.performInventory();
        } else
            rfidHandler.stopInventory();
    }
}
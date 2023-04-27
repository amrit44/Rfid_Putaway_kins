package com.example.test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import adapter.MyListAdapter;
import model.MyListData;


public class RFIDActivity extends AppCompatActivity implements RFIDHandler.ResponseHandlerInterface {


    private static String ip = "43.230.201.20" ;// this is the host ip that your data base exists on you can use 10.0.2.2 for local host                                                    found on your pc. use if config for windows to find the ip if the database exists on                                                    your pc
    private static String port = "1232";// the port sql server runs on
    private static String Classes = "net.sourceforge.jtds.jdbc.Driver";// the driver that is required for this connection use                                                                           "org.postgresql.Driver" for connecting to postgresql
    private static String database = "KinsBackup";// the data base name
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
    private TextView testStatus, tvtotaltagCount, tvtotaluniquetagCount, tvcoutnTv,tvCountSerial;
    int totaltagCount = 0;
    int totaluniquetagCount = 0;
    RFIDHandler rfidHandler;
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
    String purchaseNumberTwo;
    String itemName;
    String partyName;
    String qtySerialNumber;
    int purchaseId;
    String purchaseqty;
    int itemIdSave;
    int itemIdSave2;
    String itemCodesave2;
    String itemCodesave;
    int checklock;
    String itemNameSave;
    String color;
    String itemQtySave;
    String currentdata;
    String itemcolor;
    Button btncustom;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfid);
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

                querySpinPurchaseNumber();
            }
        });


        //onselected Spinners
        onSelectSpinner();


        //validate rack
        validateRack();


        useLess();


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
                        }
                tvtotaltagCount.setText("Total tags scanned : " + totaltagCount);
                tvtotaluniquetagCount.setText("Unique tags scanned : " + totaluniquetagCount);
                tvcoutnTv.setText("" + totaluniquetagCount);
            }
        });


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

    private void useLess() {
        // UI
        statusTextViewRFID = findViewById(R.id.textStatus);
        testStatus = findViewById(R.id.testStatus);

        // RFID Handler
        rfidHandler = new RFIDHandler();
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

    private void onSelectSpinner() {
        spinnerSearchPurchaseNumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                purchaseNumber = spinnerSearchPurchaseNumber.getSelectedItem().toString();
                rackNumber.setText("");
                textrfid.setText("");
                itemNameList.clear();
                Toast.makeText(RFIDActivity.this, "" + purchaseNumber, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(RFIDActivity.this, "" + itemName + " range set successfully", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void getpuchaseItemId(String itemName, String color) {

        try {
          //  String query =  "select Particular,Desc1 from inv_purchaseitem where   IsDeleted is null and PurchaseNumber ='" + purchaseNumber + "' ";;
            String query =  "select Id,Box,Code from inv_purchaseitem where IsDeleted is null and PurchaseNumber ='" + purchaseNumber + "' and   Particular='"+itemName+"' and Desc1='"+color+"' ";
            System.out.println("The SQL statement is: " + query + "");
            ResultSet resultSet = stmt.executeQuery(query);

            if (resultSet.next())
            {
                itemIdSave2 = resultSet.getInt("Id");
                itemCodesave2 = resultSet.getString("Code");
                 qtySerialNumber = resultSet.getString("Box");
                tvCountSerial.setText(qtySerialNumber);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void querySpinItemNumber(String purchaseNumber) {
        itemNameList.clear();
        try {

            String query =  "select Particular,Desc1 from inv_purchaseitem where   IsDeleted is null and PurchaseNumber ='" + purchaseNumber + "' ";
            System.out.println("The SQL statement is: " + query + "");
            ResultSet resultSet = stmt.executeQuery(query);

            while (resultSet.next()) {

                String itemName = resultSet.getString("Particular");
                String itemcolor = resultSet.getString("Desc1");
                itemNameList.add(itemName+"/"+itemcolor);
                ArrayAdapter arrayAdapter = new ArrayAdapter(RFIDActivity.this, android.R.layout.simple_spinner_dropdown_item, itemNameList);
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

    private void querySpinPurchaseNumber() {
        puchaseNumberList.clear();
        try {
           // String query = "select Id,PurchaseNumber,TotalQty,AccountName,Email from viewPurchaseMaster";
            String query = "select Id,PurchaseNumber,AccountName,Email,TotalQty from viewPurchaseMaster";
            System.out.println("The SQL statement is: " + query + "");
            ResultSet resultSet = stmt.executeQuery(query);

            while (resultSet.next()) {
                int  purchaseid = resultSet.getInt("Id");
                String purchaseNumber = resultSet.getString("PurchaseNumber");
                String  accountName = resultSet.getString("AccountName");
                String email = resultSet.getString("Email");
                String totalQty = resultSet.getString("TotalQty");


                puchaseNumberList.add(purchaseNumber);

                ArrayAdapter arrayAdapter = new ArrayAdapter(RFIDActivity.this, android.R.layout.simple_spinner_dropdown_item, puchaseNumberList);
                spinnerSearchPurchaseNumber.setAdapter(arrayAdapter);
                spinnerSearchPurchaseNumber.setTitle("Select Item");
                spinnerSearchPurchaseNumber.setPositiveButton("OK");
            }


        } catch (Exception ex) {

        }
    }

    private void rangeSpin() {

        try {
            rangeList.add("Max");
            rangeList.add("Average");
            rangeList.add("Low");

            ArrayAdapter arrayAdapter = new ArrayAdapter(RFIDActivity.this, android.R.layout.simple_spinner_dropdown_item, rangeList);
            spinnerSelectRange.setAdapter(arrayAdapter);
            spinnerSelectRange.setTitle("Select Item");
            spinnerSelectRange.setPositiveButton("OK");


        } catch (Exception ex) {

        }
    }

//to fetch data
    private void valueBypurchaseNumber(String purchaseNumber) {
//        String query ="select Name from [Wrh_RackMaster]  where IsDeleted is null and Name='"+rackNo+"' ";

        try {
            String query = "select Id,PurchaseNumber,TotalQty,AccountName,Email from viewPurchaseMaster where PurchaseNumber='" + purchaseNumber + "' ";
            System.out.println("The SQL statement is: " + query + "");
            ResultSet resultSet = stmt.executeQuery(query);

            if (resultSet.next()) {

                purchaseId = resultSet.getInt("Id");
                purchaseNumberTwo = resultSet.getString("PurchaseNumber");
                purchaseqty = resultSet.getString("TotalQty");
                partyName = resultSet.getString("AccountName");
                String email = resultSet.getString("Email");
              //  Toast.makeText(this, "accountName : " + partyName, Toast.LENGTH_SHORT).show();
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void checkRackNumber(String rackNo) {
        try {
            String query = "select Name from [Wrh_RackMaster]  where IsDeleted is null and Name='" + rackNo + "'  ";
            System.out.println("The SQL statement is: " + query + "");
            ResultSet resultSet = stmt.executeQuery(query);

            if (resultSet.next()) {
                String rackNumberdb = resultSet.getString("Name");
                if (Objects.equals(rackNumberdb, rackNo)) {
                    textrfid.requestFocus();
                    Toast.makeText(this, "valid Rack Number", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Invalid Rack Number", Toast.LENGTH_SHORT).show();
                    textrfid.setText("");

                }

            } else {
                Toast.makeText(this, "Invalid Rack Number", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception ex) {

        }

    }



 private void validateSerialNumber(String serialNumber) {
    threadPool.execute(new Runnable() {
        @Override
        public void run() {
            try (Connection conn = DriverManager.getConnection("jdbc:jtds:sqlserver://"+ip+":"+port+"/"+database, "sa", "Yu6SBA5s4u#zcT6%e");
                 Statement stmt2 = conn.createStatement()) {
//                String query = "select SerialNumber from inv_purchaseitemserialnumber where IsDeleted is null and purchaseitemid='" + itemIdSave +"' and SerialNumber='" + serialNumber + "' and PurchaseNumber ='"+purchaseNumber+"' ";
          String query="Select SerialNumber from INV_PurchaseItemSerialNumber ipis\n" +
                  "inner join INV_PurchaseItem ipi on ipis.PurchaseItemId = ipi.Id\n" +
                  "where   ipi.Particular='"+itemNameSave+"' and ipis. PurchaseNumber='"+purchaseNumber+"' and ipi.Desc1='"+ color+"' and ipis.SerialNumber='" + serialNumber + "' ";
                System.out.println("The SQL statement is: " + query + "");
                ResultSet resultSet = stmt2.executeQuery(query);

                if (resultSet.next()) {
                    String SerialNumberdb = resultSet.getString("SerialNumber");
                    if (Objects.equals(serialNumber, SerialNumberdb)) {
                        //Toast.makeText(this, "valid Serial  Number", Toast.LENGTH_SHORT).show();
                        getdataPurchaseNo(purchaseId, serialNumber,stmt2);
                        checkSerialExicting(serialNumber);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Invalid Rack Number", Toast.LENGTH_SHORT).show();
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






    private void getdataPurchaseNo(int purchaseId, String serialNumber, Statement stmt2) {
        try {

            String query=" Select IPISN.Id,IPISN.SerialNumber,I.Code As ItemCode,I.Name As ItemName,IPISN.Qty as ItemQty,IPISN.PurchaseId, Null as Qty,Null as BatchNo,\n" +
                    "                    IPISN.ItemId,IPISN.RackNumber as RackNo\n" +
                    "                    from INV_PurchaseItemSerialNumber IPISN\n" +
                    "                     inner join item i On i.Id = IPISN.ItemId\n" +
                    "                     Where IPISN.PurchaseId='" + purchaseId + "' and IPISN.SerialNumber='" + serialNumber + "'  and IPISN.IsDeleted is null";

            System.out.println("The SQL statement is: " + query + "");

            ResultSet resultSet = stmt2.executeQuery(query);

            if (resultSet.next()) {
                itemIdSave = resultSet.getInt("ItemId");
                itemCodesave = resultSet.getString("ItemCode");
                itemNameSave = resultSet.getString("ItemName");
                itemQtySave = resultSet.getString("ItemQty");



                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText (getApplicationContext(), "accountName : " + partyName, Toast.LENGTH_SHORT).show();
                    }
                });
            }


        } catch (Exception ex) {

        }


    }
//    public void checkSerialExicting(String serialNumber, Statement stmt2) {
//        try {
//            String query = "select SerialNumber from Wrh_PutAway where SerialNumber = '" + serialNumber + "'";
//            System.out.println("The SQL statement is: " + query + "");
//
//            // create a thread pool with 4 threads
//            ExecutorService threadPool = Executors.newFixedThreadPool(10);
//
//            // use async and await to run the query on a background thread
//            CompletableFuture<ResultSet> future = CompletableFuture.supplyAsync(() -> {
//                try {
//                    return stmt2.executeQuery(query);
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                    return null;
//                }
//            }, threadPool);
//
//            // handle the result of the query when it's ready
//            future.thenAccept(resultSet -> {
//                try {
//                    if (resultSet.next()) {
//                        String SerialNumberdb = resultSet.getString("SerialNumber");
//                        if (Objects.equals(SerialNumberdb, serialNumber)) {
//                            //Toast.makeText(this, "Serial Number Already Exist", Toast.LENGTH_SHORT).show();
//                        } else {
//                            // do something else
//                        }
//                    } else {
//                        setRecyclerView(serialNumber);
//                        insertDatatwo(serialNumber, stmt2);
//                        updaterack(serialNumber, rackNo);
//                    }
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            });
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
    private void checkSerialExicting(String serialNumber) {
        try (Connection conn = DriverManager.getConnection("jdbc:jtds:sqlserver://"+ip+":"+port+"/"+database, "sa", "Yu6SBA5s4u#zcT6%e");
             Statement stmt2 = conn.createStatement()) {
            String query = " select SerialNumber from Wrh_PutAway where  SerialNumber = '" + serialNumber + "'  ";
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


              // insertDatatwo(serialNumber,stmt2);
                insertRfidPutAwayData(null,1,"1","Serial",purchaseId,purchaseNumber,partyName,itemCodesave,itemIdSave,itemNameSave,purchaseqty,1,rackNo,2,"127.0.0.1",1,currentdata,null,null,1,null,serialNumber);
                updaterack(serialNumber,rackNo);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

   }

    private void insertRfidPutAwayData(Object o, int i, String s, String serial, int purchaseId, String purchaseNumber, String partyName, String itemCodesave, int itemIdSave, String itemNameSave, String purchaseqty, int i1, String rackNo, int i2, String s1, int i3, String currentdata, Object o1, Object o2, int i4, Object o3, String serialNumber)
            throws SQLException{

        int n = 0;
        String sql = "{call INSERTRFIDPUTAWAY (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)}";
        CallableStatement statement = connection.prepareCall(sql);
        statement.setDate(1, null);//IsDeleted
        statement.setInt(2, 1);//CompanyId
        statement.setString(3, "1");//StoreId
        statement.setString(4, "PU");//Type
        statement.setInt(5, purchaseId);//PurchaseId
        statement.setString(6, purchaseNumber);//PurchaseNo
        statement.setString(7, partyName);//PartyName
        statement.setString(8, itemCodesave);//ItemCode
        statement.setInt(9, itemIdSave);//ItemId
        statement.setString(10, itemNameSave);//ItemName
        BigDecimal d = new BigDecimal(qtySerialNumber);
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

        if (n > 0) {
            //Toast.makeText(this, "successfully Inserted", Toast.LENGTH_SHORT).show();

            setRecyclerView(serialNumber);

        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_SHORT).show();
                }
            });

        }



    }








    private void setRecyclerView(final String serialNumber) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                boolean isDuplicate = false;
                for (MyListData data : myListData) {
                    if (data.getDescription().equals(serialNumber)) {
                        isDuplicate = true;
                        break;
                    }
                }

                if (!isDuplicate) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            totaluniquetagCount++;

                            // Create a new MyListData object with the provided serial number, rack number, and quantity.
                            MyListData newData = new MyListData(serialNumber, rackNo, "1");
                            myListData.add(newData);

                            // Delay updating the RecyclerView by 2 seconds
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Update the RecyclerView with the new data.
                                    RecyclerView recyclerView = findViewById(R.id.putawaygrid);
                                    MyListAdapter adapter = new MyListAdapter(myListData);
                                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                    recyclerView.setAdapter(adapter);

                                    // Set the text of the textrfid TextView to the new serial number and give it focus.
                                    textrfid.setText(serialNumber);
                                    textrfid.requestFocus();
                                }
                            }, 2000); // 2 second delay
                        }
                    });
                }
            }
        });
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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



}

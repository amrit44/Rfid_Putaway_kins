package com.example.test.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import model.MyListData;

import com.example.test.R;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private static String ip = "43.230.201.20" ;// this is the host ip that your data base exists on you can use 10.0.2.2 for local host                                                    found on your pc. use if config for windows to find the ip if the database exists on                                                    your pc
    private static String port = "1232";// the port sql server runs on
    private static String Classes = "net.sourceforge.jtds.jdbc.Driver";// the driver that is required for this connection use                                                                           "org.postgresql.Driver" for connecting to postgresql
    private static String database = "TestingKinsBackup";// the data base name
    private static String username = "sa";// the user name
    private static String password = "Yu6SBA5s4u#zcT6%e";// the password
    private static String url = "jdbc:jtds:sqlserver://"+ip+":"+port+"/"+database; // the connection url string

    Statement stmt;
    private Connection connection = null;
    String[] names;
    EditText Ty;
    ArrayList<MyListData> myListData;
    String value="am";
    SearchableSpinner spinnerSearchPurchaseNumber;
    SearchableSpinner spinnerSearchItemName;

    ArrayList<String> puchaseNumberList;
    ArrayList<String> itemNameList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerSearchPurchaseNumber = (SearchableSpinner) findViewById(R.id.spinnerSearchPurchaseNumber);
        spinnerSearchItemName = (SearchableSpinner) findViewById(R.id.spinnerSearchItemName);
        puchaseNumberList =new ArrayList<>();
        itemNameList=new ArrayList<>();
        // Create an executor that executes tasks in the main thread.
        Executor mainExecutor = ContextCompat.getMainExecutor(this);
        // Execute a task in the main thread
        mainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                querySpinPurchaseNumber();
            }
        });

        spinnerSearchPurchaseNumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String purchaseNumber = spinnerSearchPurchaseNumber.getSelectedItem().toString();
                Toast.makeText(MainActivity.this, ""+purchaseNumber, Toast.LENGTH_SHORT).show();
                querySpinItemNumber(purchaseNumber);
                valueBypurchaseNumber(purchaseNumber);


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });









    }

    private void querySpinItemNumber(String purchaseNumber) {

        try {
            String query ="Select distinct I.Name As ItemName from INV_PurchaseItemSerialNumber IPISN Inner Join  INV_PurchaseItem IPI On IPI.Id = IPISN.PurchaseItemId Inner join Item I on I.Id = IPI.ItemId Inner Join\n" +
                    "INV_Purchase IP On Ip.Id = IPI.purchaseid where IPI.PurchaseNumber ='"+purchaseNumber+"' ";



            System.out.println("The SQL statement is: " + query + "");
            ResultSet resultSet = stmt.executeQuery(query);

            while (resultSet.next())
            {

                String itemName = resultSet.getString("ItemName");
                itemNameList.add(itemName);

                ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this,android.R.layout.simple_spinner_dropdown_item,itemNameList);
                spinnerSearchItemName.setAdapter(arrayAdapter);
                spinnerSearchItemName.setTitle("Select Item");
                spinnerSearchItemName.setPositiveButton("OK");
            }



        }
        catch (Exception ex)
        {

        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        databaseConnection();



    }

    private void querySpinPurchaseNumber() {
        try {
            String query ="select Id,PurchaseNumber,AccountName,Email,TotalQty from viewPurchaseMaster";
            System.out.println("The SQL statement is: " + query + "");
            ResultSet resultSet = stmt.executeQuery(query);

            while (resultSet.next())
            {
                int  purchaseid = resultSet.getInt("Id");
                String purchaseNumber = resultSet.getString("PurchaseNumber");
                String  accountName = resultSet.getString("AccountName");
                String email = resultSet.getString("Email");
                String totalQty = resultSet.getString("TotalQty");


                puchaseNumberList.add(purchaseNumber);

                ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this,android.R.layout.simple_spinner_dropdown_item, puchaseNumberList);
                spinnerSearchPurchaseNumber.setAdapter(arrayAdapter);
                spinnerSearchPurchaseNumber.setTitle("Select Item");
                spinnerSearchPurchaseNumber.setPositiveButton("OK");
            }



        }
        catch (Exception ex)
        {

        }
    }


    private void valueBypurchaseNumber(String purchaseNumber) {
//        String query ="select Name from [Wrh_RackMaster]  where IsDeleted is null and Name='"+rackNo+"' ";

        try {
            String query ="select Id,PurchaseNumber,TotalQty,AccountName,Email from viewPurchaseMaster where PurchaseNumber='"+purchaseNumber+"' ";
            System.out.println("The SQL statement is: " + query + "");
            ResultSet resultSet = stmt.executeQuery(query);

            while (resultSet.next())
            {
                String id = resultSet.getString("Id");
                String totalQty = resultSet.getString("TotalQty");
                String accountName = resultSet.getString("AccountName");
                String email = resultSet.getString("Email");
                Toast.makeText(this, ""+accountName, Toast.LENGTH_SHORT).show();
            }



        }
        catch (Exception ex)
        {

        }
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
}
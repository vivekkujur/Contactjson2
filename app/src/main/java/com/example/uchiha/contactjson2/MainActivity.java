package com.example.uchiha.contactjson2;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main_activity";
    private boolean mContactPermissionGranted = false;
    private static final String READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    private static final String WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS;
    private static final String WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private static final int CONTACTS_PERMISSION_REQUEST_CODE = 1234;
    private Button moreItembtn,getallcontactbtn;

    private ArrayList<String> mEntries=new ArrayList<>();
    private ArrayList<String> AllEntries=new ArrayList<>();

    private Cursor cursor;
    private int page=1;
    private int position=0;

    private RecyclerAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getContactPermission();




    }

    private void declearation(){

        cursor = getApplicationContext().getContentResolver()
                .query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
                        ContactsContract.Contacts.SORT_KEY_PRIMARY + " ASC");
        page=cursor.getCount()/10;

        moreItembtn=findViewById(R.id.moreBtn);
        getallcontactbtn=findViewById(R.id.allcontactbtn);
        recyclerView=findViewById(R.id.main_recycler_view);

        linearLayoutManager= new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerAdapter=new RecyclerAdapter();

        moreItembtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pagination();
            }
        });

        getallcontactbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                writeToFile();

            }
        });
    }

    public void writeToFile() {

        AllDetails();
        String data=AllEntries.toString();

        // Get the directory for the user's public pictures directory.
        String path =
                Environment.getExternalStorageDirectory() + File.separator  + "ContactDetail";

        // Create the folder.
        File folder = new File(path);
        folder.mkdirs();

        // Create the file.
        String filename= randomq();
        File file = new File(folder, filename+".txt");

        try
        {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
            Toast.makeText(MainActivity.this, "contact details write successful at /phone/ContactDetail/"+filename+".txt", Toast.LENGTH_LONG).show();

        }

        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private  static String randomq()
    {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(9);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            if(randomLength == 0) randomLength = 10 ;
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    private void AllDetails() {


        JSONArray resultSet = new JSONArray();

        if (mContactPermissionGranted) {

            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {
                JSONObject Object = new JSONObject();
                JSONArray jArr = new JSONArray();

                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String displayname = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                try {
                    Object.put("Display_name", displayname);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                int has_number = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (has_number > 0) {

                    Cursor pCursor = getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                            null, null);

                    // continue till this cursor reaches to all phone numbers which are associated with a contact in the contact list

                    while (pCursor.moveToNext()) {

                        String phoneNo = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                        jArr.put(phoneNo);

                    }

                    pCursor.close();
                }

                try {
                    Object.put("Contact_number", jArr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                resultSet.put(Object);
                cursor.moveToNext();
            }


            //json array to Array list
            for (int i = 0; i < resultSet.length(); i++) {
                try {
                    JSONObject jsonObject = resultSet.getJSONObject(i);
                    AllEntries.add(jsonObject.toString());
                } catch (JSONException e) {
                    AllEntries.add("Error: " + e.getLocalizedMessage());
                }
            }
        }

    }

    private void getContactPermission() {


        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),READ_CONTACTS)== PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),WRITE_CONTACTS)==PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(this.getApplicationContext(),WRITE_STORAGE)==PackageManager.PERMISSION_GRANTED) {

                    mContactPermissionGranted = true;
                    declearation();
                    ShowContacts();
                }else{
                    ActivityCompat.requestPermissions(this, new String[]{WRITE_STORAGE},
                            CONTACTS_PERMISSION_REQUEST_CODE);

                }
            }else{
                ActivityCompat.requestPermissions(this, new String[]{WRITE_CONTACTS},
                        CONTACTS_PERMISSION_REQUEST_CODE);
            }

        }else{
            ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS},
                    CONTACTS_PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){

            case CONTACTS_PERMISSION_REQUEST_CODE:{

                if (grantResults.length > 0){
                    for (int i=0; i< grantResults.length;i++){

                        if( grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                            mContactPermissionGranted=false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    mContactPermissionGranted=true;
                    declearation();
                    ShowContacts();

                }
            }

        }
    }

    private void ShowContacts(){


        JSONArray resultSet = new JSONArray();

        if(mContactPermissionGranted){

            cursor.moveToFirst();

            int k=10;
            if(page > 0){

                while(  k>0 && cursor.isAfterLast()==false){
                    position ++;
                    JSONObject Object = new JSONObject();
                    JSONArray jArr=new JSONArray();

                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String displayname=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    try {
                        Object.put("Display_name",displayname);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    int has_number= Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                    if(has_number > 0){

                        Cursor pCursor=getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                null, null);

                        // continue till this cursor reaches to all phone numbers which are associated with a contact in the contact list

                        while (pCursor.moveToNext())
                        {

                            String phoneNo= pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                            jArr.put(phoneNo);

                        }

                        pCursor.close();
                    }

                    try {
                        Object.put("Contact_number",jArr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    resultSet.put(Object);
                    cursor.moveToNext();
                    k--;
                }
                page--;
            }

            //json array to Array list

            for(int i = 0; i < resultSet.length(); i++) {
                try {
                    JSONObject jsonObject = resultSet.getJSONObject(i);
                    mEntries.add(jsonObject.toString());
                }
                catch(JSONException e) {
                    mEntries.add("Error: " + e.getLocalizedMessage());
                }
            }

            recyclerAdapter=new RecyclerAdapter(mEntries,getApplicationContext());
            recyclerView.setAdapter(recyclerAdapter);

        }

    }

    private void pagination(){


        JSONArray resultSet = new JSONArray();

        int k=10;
        if(page >0){

            cursor.moveToPosition(position);
            while(  k > 0 && cursor.isAfterLast()==false){
                position++;
                JSONObject Object = new JSONObject();
                JSONArray jArr=new JSONArray();

                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String displayname=cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                try {
                    Object.put("Display_name",displayname);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                int has_number= Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

                if(has_number > 0){

                    Cursor pCursor=getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                            null, null);

                    // continue till this cursor reaches to all phone numbers which are associated with a contact in the contact list

                    while (pCursor.moveToNext())
                    {
                        String phoneNo= pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                        jArr.put(phoneNo);

                    }

                    pCursor.close();
                }

                try {
                    Object.put("Contact_number",jArr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                resultSet.put(Object);
                cursor.moveToNext();
                k--;
            }
            page--;

        }else {
            cursor.close();
        }

        //json array to Array list
        ArrayList<String> data =new ArrayList<>();


        for(int i = 0; i < resultSet.length(); i++) {
            try {
                JSONObject jsonObject = resultSet.getJSONObject(i);
                data.add(jsonObject.toString());
            }
            catch(JSONException e) {
                data.add("Error: " + e.getLocalizedMessage());
            }
        }
        recyclerAdapter.adddetails(data);

    }
}

package com.example.uchiha.contactjson2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main_activity";
    private boolean mContactPermissionGranted = false;
    private static final String READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    private static final String WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS;
    private static final int CONTACTS_PERMISSION_REQUEST_CODE = 1234;
    private Button moreItembtn;

    private ArrayList<String> mEntries=new ArrayList<>();
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
    }

    private void getContactPermission() {


        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),READ_CONTACTS)== PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),WRITE_CONTACTS)==PackageManager.PERMISSION_GRANTED){

                mContactPermissionGranted=true;
                declearation();
                ShowContacts();

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

                        int count=0;
                        while (pCursor.moveToNext())
                        {
                            count++;
                            JSONObject pnObj = new JSONObject();

                            String phoneNo= pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                            try {
                                pnObj.put("num"+count, phoneNo);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            jArr.put(pnObj);

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

                    int count=0;
                    while (pCursor.moveToNext())
                    {
                        count++;
                        JSONObject pnObj = new JSONObject();

                        String phoneNo= pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        try {
                            pnObj.put("num"+count, phoneNo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        jArr.put(pnObj);

                    }

                    pCursor.close();
                }

                try {
                    Object.put("phone_number",jArr);
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

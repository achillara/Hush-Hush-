package com.example.anusha.place;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class Address extends ActionBarActivity  {



         private DatabaseHandler dbHelper;
         private SimpleCursorAdapter dataAdapter;
          private ListView listview;
           Cursor cursor;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_address);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);



            dbHelper = new DatabaseHandler(this);

             //Generate ListView from SQLite Database
             displayListView();
        }

         private void displayListView(){
             cursor= dbHelper.fetchAllAddresses();




           /* String columns[]= new String[]{
                    dbHelper.KEY_NAME

            };


             int[] to = new int[] {
                R.id.rowTextView,
              };

             dataAdapter= new SimpleCursorAdapter(
                    this,R.layout.simplerow, cursor,columns,to,0);
             dataAdapter.swapCursor(cursor);
             dataAdapter.notifyDataSetChanged();


            listview= (ListView)findViewById(R.id.listView);
             listview.setAdapter(dataAdapter);*/


        }
    public void onResume() {

        super.onResume();
        deleteAddress();
        cursor= dbHelper.fetchAllAddresses();
        String columns[]= new String[]{
                dbHelper.KEY_NAME

        };
        int[] to = new int[] {
                R.id.rowTextView,
        };
        dataAdapter= new SimpleCursorAdapter(
                this,R.layout.simplerow, cursor,columns,to,0);
        dataAdapter.notifyDataSetChanged();
        listview= (ListView)findViewById(R.id.listView);
        listview.setAdapter(dataAdapter);
    }



    private void deleteAddress(){
        listview= (ListView)findViewById(R.id.listView);
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> a, View v, int position, long id) {
                final String selectedID = String.valueOf(id);
                AlertDialog.Builder adb=new AlertDialog.Builder(Address.this);
                adb.setTitle("Delete?");
                adb.setMessage("Are you sure you want to delete?");
                final int positionToRemove = position;
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteAddress(selectedID);
                        dataAdapter.notifyDataSetChanged();
                        updateList();

                    }});
                adb.show();
                return true;
            }

        });
    }
    public void updateList(){
        cursor=dbHelper.fetchAllAddresses();
        dataAdapter.swapCursor(cursor);
        String columns[]= new String[]{
                dbHelper.KEY_NAME

        };
        int[] to = new int[] {
                R.id.rowTextView,
        };
        dataAdapter= new SimpleCursorAdapter(
                this,R.layout.simplerow, cursor,columns,to,0);
        dataAdapter.notifyDataSetChanged();
        listview.setAdapter(dataAdapter);
    }

        private void Add() {
            Intent intent = new Intent(Address.this, addPlace.class);
            startActivityForResult(intent, 2);

        }


        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_address, menu);
            return super.onCreateOptionsMenu(menu);

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.button:
                    Add();
                    return true;
                case android.R.id.home:
                   // super.onBackPressed();
                    Intent h = new Intent(Address.this, MapsActivity.class);
                    h.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(h);
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }


    }



package com.example.travelmantics;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DealActivity extends AppCompatActivity {
    //enterypoint foor accessing the database
    private FirebaseDatabase mFirebaseDatabase;
    //reference location for the database
    private DatabaseReference mDatabaseReference;
    //get default database instance
    EditText txtTitle, txtDescription, txtPrice;
    TravelDeal deal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        FirebaseUtil.openFbReference("traveldeals",this);
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtDescription = (EditText) findViewById(R.id.txtDescription);
        txtPrice = (EditText) findViewById(R.id.txtPrice);

        //get the deal sent through the intent from insert activity
        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        //for creating a new deal
        if(deal==null){
            deal = new TravelDeal();
        }
        this.deal = deal;
        txtTitle.setText(deal.getTitle());
        txtPrice.setText(deal.getPrice());
        txtDescription.setText(deal.getDescription());
    }
    //when user clicks one of the menus

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(getApplicationContext(), "Save clicked", Toast.LENGTH_LONG).show();
                clean();//to clear the edit texts
                backToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_LONG).show();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //create menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //create menu from xml menu resources
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        return true;
    }

    private void saveDeal(){
        deal.setTitle(txtTitle.getText().toString());
        deal.setDescription(txtDescription.getText().toString());
        deal.setPrice(txtPrice.getText().toString());
        if(deal.getId()== null){
            //push to database
            //firebase generates a push_id when calling push method
            mDatabaseReference.push().setValue(deal);
        }else{
            //edit an existing data in the database
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }
    }
    private void deleteDeal(){
        if(deal.getId()==null){
            Toast.makeText(this, "Please save toast before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            mDatabaseReference.child(deal.getId()).removeValue();
        }

    }
    private void backToList(){
        startActivity(new Intent(this, ListActivity.class));
    }

    private void clean(){
        txtPrice.setText("");
        txtTitle.setText("");
        txtDescription.setText("");
        txtTitle.requestFocus();
    }


}
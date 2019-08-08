package com.example.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {
    //enterypoint foor accessing the database
    private FirebaseDatabase mFirebaseDatabase;
    //reference location for the database
    private DatabaseReference mDatabaseReference;
    //get default database instance
    private static final int PICTURE_RESULT =42;
    EditText txtTitle, txtDescription, txtPrice;
    TravelDeal deal;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
//        FirebaseUtil.openFbReference("traveldeals",this);
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtDescription = (EditText) findViewById(R.id.txtDescription);
        txtPrice = (EditText) findViewById(R.id.txtPrice);
        Button btnImage = (Button) findViewById(R.id.btnImage);
        imageView = (ImageView) findViewById(R.id.image);


        //get the deal sent through the intent from insert activity
        Intent intent = getIntent();
        TravelDeal deal2 = (TravelDeal) intent.getSerializableExtra("Deal");
        //for creating a new deal
        if(deal2==null){
            deal2 = new TravelDeal();
        }
        this.deal = deal2;
        txtTitle.setText(deal.getTitle());
        txtPrice.setText(deal.getPrice());
        txtDescription.setText(deal.getDescription());
//        showImage(deal.getImageUrl());

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(intent.createChooser(intent,
                        "Insert Picture"), PICTURE_RESULT);

            }
        });


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
        if(FirebaseUtil.isAdmin ==true){
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enableEditTexts(true);
        }else {
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enableEditTexts(false);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            final StorageReference ref = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            String url = task.getResult().toString();
                            Log.d("URL is ", url);
                            deal.setImageUrl(url);
                            deal.setImageName(taskSnapshot.getStorage().getPath());
                            Log.d("path is ", taskSnapshot.getStorage().getPath());
                            showImage(url);
                        }
                    });
                }
            });
        }
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
    private void enableEditTexts(boolean isEnabled){
        txtDescription.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
        txtTitle.setEnabled(isEnabled);
    }

    private void showImage(String url) {
        if (url != null && url.isEmpty() == false) {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(url)
                    .resize(width, width*2/3)
                    .centerCrop()
                    .into(imageView);
        }
    }

}
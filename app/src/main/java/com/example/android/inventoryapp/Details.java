package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryHelper;


public class Details extends ActionBarActivity  implements
    LoaderManager.LoaderCallbacks<Cursor>
    {
        private static final int EXISTING_PRODUCT_LOADER = 0;
        private Uri mCurrentProductUri;
        private boolean mProductHasChanged = false;


        private TextView nameText;
        private TextView quantityText;
        private TextView priceText;
        private TextView sellerNameText;
        private TextView sellerContactText;
        int quantity;
        String sellerContact;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_details);

            Intent intent = getIntent();
            mCurrentProductUri = intent.getData();

                // Initialize a loader to read the pet data from the database
                // and display the current values in the editor
                getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);


            nameText = (TextView)  findViewById(R.id.detailName);
            quantityText = (TextView) findViewById(R.id.detailQuantity);
            priceText = (TextView) findViewById(R.id.detailPrice);
            sellerNameText = (TextView) findViewById(R.id.detailSellerName);
            sellerContactText = (TextView) findViewById(R.id.detailSellerContact);

            Button increase = (Button) findViewById(R.id.incButton);
            increase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int incQuantity = quantity++;
                    quantityText.setText(String.valueOf(incQuantity));


                }
            });
            Button decrease = (Button) findViewById(R.id.decButton);
            decrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(quantity>=0) {
                        int decQuantity = quantity--;
                        quantityText.setText(String.valueOf(decQuantity));
                    }

                }
            });

            Button order = (Button) findViewById(R.id.orderButton);
            order.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(Intent.ACTION_CALL);
                    i.setData(Uri.parse("tel:"+sellerContact));
                    startActivity(i);
                }
            });

            Button delete = (Button) findViewById(R.id.deleteButton);
            delete.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    showDeleteConfirmationDialog();
                }
            });


        }


        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String[] projection = {
                    InventoryContract.InventoryEntry._ID,
                    InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,
                    InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                    InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE,
                    InventoryContract.InventoryEntry.COLUMN_PRODUCT_IMAGE,
                    InventoryContract.InventoryEntry.COLUMN_PRODUCT_SELLER_NAME,
                    InventoryContract.InventoryEntry.COLUMN_PRODUCT_SELLER_CONTACT};

            return new CursorLoader(this,   // Parent activity context
                    mCurrentProductUri,         // Query the content URI for the current pet
                    projection,             // Columns to include in the resulting Cursor
                    null,                   // No selection clause
                    null,                   // No selection arguments
                    null);                  // Default sort order
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if (cursor == null || cursor.getCount() < 1) {
                return;}

            if (cursor.moveToFirst()) {
                // Find the columns of pet attributes that we're interested in
                int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
                int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
                int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
                int sellerNameIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SELLER_NAME);
                int sellerContactIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SELLER_CONTACT);

                // Extract out the value from the Cursor for the given column index
                String name = cursor.getString(nameColumnIndex);
                quantity = cursor.getInt(quantityColumnIndex);
                double price = cursor.getDouble(priceColumnIndex);
                String sellerName = cursor.getString(sellerNameIndex);
                  sellerContact = cursor.getString(sellerContactIndex);

                // Update the views on the screen with the values from the database
                nameText.setText(name);
                quantityText.setText(String.valueOf(quantity));
                priceText.setText(String.valueOf(price));
                sellerNameText.setText(sellerName);
                sellerContactText.setText(sellerContact);


            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            nameText.setText("");
            quantityText.setText("");
            priceText.setText("");
            sellerNameText.setText("");
            sellerContactText.setText("");
        }


        private void showDeleteConfirmationDialog() {
            // Create an AlertDialog.Builder and set the message, and click listeners
            // for the postivie and negative buttons on the dialog.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.delete_dialog_msg);
            builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Delete" button, so delete the pet.
                    deletePet();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Cancel" button, so dismiss the dialog
                    // and continue editing the pet.
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });

            // Create and show the AlertDialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        private void deletePet() {
            // Only perform the delete if this is an existing pet.
            if (mCurrentProductUri != null) {
                // Call the ContentResolver to delete the pet at the given content URI.
                // Pass in null for the selection and selection args because the mCurrentPetUri
                // content URI already identifies the pet that we want.
                int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

                // Show a toast message depending on whether or not the delete was successful.
                if (rowsDeleted == 0) {
                    // If no rows were deleted, then there was an error with the delete.
                    Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the delete was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
            finish();
        }








}
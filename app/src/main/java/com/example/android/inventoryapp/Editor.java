package com.example.android.inventoryapp;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryHelper;
import com.example.android.inventoryapp.data.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLDataException;
import java.sql.SQLException;


public class Editor extends ActionBarActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private static final int EXISTING_PRODUCT_LOADER = 0;
    private static final int SELECT_PICTURE = 100;
    private Uri mCurrentProductUri;
    private boolean mProductHasChanged = false;


    private EditText nameEditText;
    private EditText quantityEditText;
    private EditText priceEditText;
    private EditText sellerNameEditText;
    private EditText sellerContactEditText;
    private FloatingActionButton selectImageButton;
    private AppCompatImageView imgView ;
    Bitmap bitmap;
    private Uri selectedImageUri ;


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

                Intent intent = getIntent();
                mCurrentProductUri = intent.getData();


                if (mCurrentProductUri == null) {
                    // This is a new pet, so change the app bar to say "Add a Pet"
                    setTitle(getString(R.string.editor_activity_title_new_product));

                    // Invalidate the options menu, so the "Delete" menu option can be hidden.
                    // (It doesn't make sense to delete a pet that hasn't been created yet.)
                    invalidateOptionsMenu();
                }

                else {
                    // Otherwise this is an existing pet, so change app bar to say "Edit Pet"
                    setTitle(getString(R.string.editor_activity_title_edit_product));

                    // Initialize a loader to read the pet data from the database
                    // and display the current values in the editor
                    getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
                }

                nameEditText = (EditText) findViewById(R.id.productName);
                quantityEditText = (EditText) findViewById(R.id.productQuantity);
                priceEditText = (EditText) findViewById(R.id.productPrice);
                sellerNameEditText = (EditText) findViewById(R.id.productSellerName);
                sellerContactEditText = (EditText) findViewById(R.id.productSellerContact);
                selectImageButton = (FloatingActionButton) findViewById(R.id.btnSelectImage);
                imgView = (AppCompatImageView) findViewById(R.id.imgView);

                nameEditText.setOnTouchListener(mTouchListener);
                quantityEditText.setOnTouchListener(mTouchListener);
                priceEditText.setOnTouchListener(mTouchListener);
                sellerNameEditText.setOnTouchListener(mTouchListener);
                sellerContactEditText.setOnTouchListener(mTouchListener);
                selectImageButton.setOnTouchListener(mTouchListener);

                selectImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openImageChooser();

                    }
                });
            }




        private void saveProduct() {
            // Read from input fields
            // Use trim to eliminate leading or trailing white space
            String name = nameEditText.getText().toString().trim();
            String quantity = quantityEditText.getText().toString().trim();
            String price = priceEditText.getText().toString().trim();
            String sellerName = sellerNameEditText.getText().toString().trim();
            String sellerContact = sellerContactEditText.getText().toString().trim();

            byte[] data = profileImage(bitmap);
            Toast.makeText(this,data.toString(),Toast.LENGTH_LONG).show();




            // Check if this is supposed to be a new pet
            // and check if all the fields in the editor are blank
            if (mCurrentProductUri == null &&
                    TextUtils.isEmpty(name) && TextUtils.isEmpty(quantity) &&
                    TextUtils.isEmpty(price) && TextUtils.isEmpty(sellerName) && TextUtils.isEmpty(sellerContact) && (imgView == null)) {
                // Since no fields were modified, we can return early without creating a new pet.
                // No need to create ContentValues and no need to do any ContentProvider operations.
                return;
            }
            if ((TextUtils.isEmpty(name)) || (TextUtils.isEmpty(quantity)) || (TextUtils.isEmpty(price))) {
                Toast.makeText(this, "Please fill the empty fields", Toast.LENGTH_LONG).show();
            } else {

                    boolean isInt, isDouble, isNum;
                    try {
                        BigDecimal i = new BigDecimal(quantity);
                        isInt = true;
                    } catch (Exception e) {
                        isInt = false;
                    }
                try {
                    BigDecimal d = new BigDecimal(price);
                    isDouble = true;
                } catch (Exception e) {
                    isDouble = false;
                }
                try {
                    BigDecimal num = new BigDecimal(sellerContact);
                    isNum = true;
                } catch (Exception e) {
                    isNum = false;
                }
                    if (isInt == true && isDouble == true && isNum == true&& Integer.parseInt(quantity)>0) {

                        ContentValues values = new ContentValues();
                        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME, name);
                        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE, price);
                        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SELLER_NAME, sellerName);
                        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SELLER_CONTACT, sellerContact);
                        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_IMAGE, data);
                        //decodeUri(selectedImageUri);
                       // bitmap=  ((BitmapDrawable)imgView.getDrawable()).getBitmap();

                        if (mCurrentProductUri == null) {
                            // This is a NEW pet, so insert a new pet into the provider,
                            // returning the content URI for the new pet.
                            Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);

                            // Show a toast message depending on whether or not the insertion was successful.
                            if (newUri == null) {
                                // If the new content URI is null, then there was an error with insertion.
                                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Otherwise, the insertion was successful and we can display a toast.
                                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
                            // and pass in the new ContentValues. Pass in null for the selection and selection args
                            // because mCurrentPetUri will already identify the correct row in the database that
                            // we want to modify.
                            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

                            // Show a toast message depending on whether or not the update was successful.
                            if (rowsAffected == 0) {
                                // If no rows were affected, then there was an error with the update.
                                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Otherwise, the update was successful and we can display a toast.
                                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    } else
                        Toast.makeText(this, "Quantity,Price and seller contact should be a number and greater than zero", Toast.LENGTH_LONG).show();
                }
            }




        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu options from the res/menu/menu_catalog.xml file.
            // This adds menu items to the app bar.
            getMenuInflater().inflate(R.menu.menu_editor, menu);
            return true;
        }

        @Override
        public boolean onPrepareOptionsMenu(Menu menu) {
            super.onPrepareOptionsMenu(menu);
            // If this is a new pet, hide the "Delete" menu item.
            if (mCurrentProductUri == null) {
                MenuItem menuItem = menu.findItem(R.id.action_delete);
                menuItem.setVisible(false);
            }
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                // Respond to a click on the "Save" menu option
                case R.id.action_save:
                    // Save pet to database
                    saveProduct();
                    // Exit activity
                    //finish();
                    return true;
                // Respond to a click on the "Delete" menu option
                case R.id.action_delete:
                    // Pop up confirmation dialog for deletion
                    showDeleteConfirmationDialog();
                    return true;
                // Respond to a click on the "Up" arrow button in the app bar
                case android.R.id.home:
                    // If the pet hasn't changed, continue with navigating up to parent activity
                    // which is the {@link CatalogActivity}.
                    if (!mProductHasChanged) {
                        NavUtils.navigateUpFromSameTask(Editor.this);
                        return true;
                    }

                    // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                    // Create a click listener to handle the user confirming that
                    // changes should be discarded.
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // User clicked "Discard" button, navigate to parent activity.
                                    NavUtils.navigateUpFromSameTask(Editor.this);
                                }
                            };

                    // Show a dialog that notifies the user they have unsaved changes
                    showUnsavedChangesDialog(discardButtonClickListener);
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onBackPressed() {
            // If the pet hasn't changed, continue with handling back button press
            if (!mProductHasChanged) {
                super.onBackPressed();
                return;
            }

            // Otherwise if there are unsaved changes, setup a dialog to warn the user.
            // Create a click listener to handle the user confirming that changes should be discarded.
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, close the current activity.
                            finish();
                        }
                    };

            // Show dialog that there are unsaved changes
            showUnsavedChangesDialog(discardButtonClickListener);
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
                    int imageIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_IMAGE);

                    // Extract out the value from the Cursor for the given column index
                    String name = cursor.getString(nameColumnIndex);
                    int quantity = cursor.getInt(quantityColumnIndex);
                    double price = cursor.getDouble(priceColumnIndex);
                    String sellerName = cursor.getString(sellerNameIndex);
                    String sellerContact = cursor.getString(sellerContactIndex);
                    byte[] imgeByte= cursor.getBlob(imageIndex);

                    // Update the views on the screen with the values from the database
                    nameEditText.setText(name);
                    quantityEditText.setText(String.valueOf(quantity));
                    priceEditText.setText(String.valueOf(price));
                    sellerNameEditText.setText(sellerName);
                    sellerContactEditText.setText(sellerContact);
                    imgView.setImageBitmap(Utils.getImage(imgeByte));
                }
        }

            @Override
        public void onLoaderReset (Loader<Cursor> loader) {
                nameEditText.setText("");
            quantityEditText.setText("");
            priceEditText.setText("");
            sellerNameEditText.setText("");
            sellerContactEditText.setText("");
        }


        private void showUnsavedChangesDialog(
                DialogInterface.OnClickListener discardButtonClickListener) {
            // Create an AlertDialog.Builder and set the message, and click listeners
            // for the postivie and negative buttons on the dialog.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.unsaved_changes_dialog_msg);
            builder.setPositiveButton(R.string.discard, discardButtonClickListener);
            builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Keep editing" button, so dismiss the dialog
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


    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {

                 selectedImageUri = data.getData();

                if (null != selectedImageUri) {

                    bitmap = decodeUri(selectedImageUri,400);
                    imgView.setImageBitmap(bitmap);
                    }


                }
            }
        }
    private Bitmap decodeUri(Uri uri, int REQUIRED_SIZE){
        try {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, o);

        // The new size we want to scale to
        // final int REQUIRED_SIZE =  size;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, o2);
    }
    catch (Exception e){
        e.printStackTrace();
    }
    return null;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private byte[] profileImage(Bitmap b){

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 0, bos);
        return bos.toByteArray();

    }


    }





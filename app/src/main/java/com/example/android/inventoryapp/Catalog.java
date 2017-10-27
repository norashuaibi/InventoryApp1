package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryCursorAdapter;
import com.example.android.inventoryapp.data.InventoryHelper;


public class Catalog extends ActionBarActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final int PRODUCT_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Catalog.this, Editor.class);
                startActivity(intent);
            }
        });

    ListView productListView = (ListView) findViewById(R.id.list);

    // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
    View emptyView = findViewById(R.id.empty_view);
    productListView.setEmptyView(emptyView);

    // Setup an Adapter to create a list item for each row of pet data in the Cursor.
    // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
    mCursorAdapter=new InventoryCursorAdapter(this,null);

    productListView.setAdapter(mCursorAdapter);

    // Setup the item click listener
    productListView.setOnItemClickListener(new AdapterView.OnItemClickListener()

    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            // Create new intent to go to {@link EditorActivity}
            Intent intent = new Intent(Catalog.this, Details.class);

            // Form the content URI that represents the specific pet that was clicked on,
            // by appending the "id" (passed as input to this method) onto the
            // {@link PetEntry#CONTENT_URI}.
            // For example, the URI would be "content://com.example.android.pets/pets/2"
            // if the pet with ID 2 was clicked on.
            Uri currentProductUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);

            // Set the URI on the data field of the intent
            intent.setData(currentProductUri);

            // Launch the {@link EditorActivity} to display the data for the current pet.
            startActivity(intent);
        }
    });

    // Kick off the loader
    getLoaderManager().initLoader(PRODUCT_LOADER, null, this);




    }
    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */

        private void insertProduct()
        {
            ContentValues values = new ContentValues();
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME, "jjj");
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY,2);
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE,15);
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SELLER_NAME,"ddd");
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SELLER_CONTACT,"ss");




        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all pets in the database.
     */
    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(InventoryContract.InventoryEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from inventory database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE};

        return new CursorLoader(this,   // Parent activity context
                InventoryContract.InventoryEntry.CONTENT_URI,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link PetCursorAdapter} with this new cursor containing updated pet data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
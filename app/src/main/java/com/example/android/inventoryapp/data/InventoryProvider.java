package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by nora on 10/6/2017.
 */
public class InventoryProvider extends ContentProvider {
    /**
     * URI matcher code for the content URI for the pets table
     */
    private static final int PRODUCTS = 100;

    /**
     * URI matcher code for the content URI for a single pet in the pets table
     */
    private static final int PRODUCTS_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, PRODUCTS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY + "/#", PRODUCTS_ID);

    }

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * Initialize the provider and the database helper object.
     */

    InventoryHelper mDBHelper;
    @Override
    public boolean onCreate() {
        mDBHelper = new InventoryHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDBHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor = null;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // For the PETS code, query the pets table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCTS_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = InventoryContract.InventoryEntry._ID + "=?";
                long parsedUri = ContentUris.parseId(uri);
                selectionArgs = new String[]{String.valueOf(parsedUri)};

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[]
            selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCTS_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDBHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                return database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
            case PRODUCTS_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }



    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
              switch (match) {
                        case PRODUCTS:
                                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
                       case PRODUCTS_ID:
                               return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
                       default:
                                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
                        }    }


    private Uri insertProduct(Uri uri, ContentValues values) {

        String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        int quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        double price = values.getAsDouble(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }
        if( quantity == 0)
            throw new IllegalArgumentException("Provide quantity of thw product");
        if(price==0)
            throw new IllegalArgumentException("Provide price of the product");

        long id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(uri, id);

    }


    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity == null && quantity < 0) {
                throw new IllegalArgumentException("product requires valid quantity");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE)) {
            // Check that the weight is greater than or equal to 0 kg
            double price = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
            if (price == 0 && price < 0) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase database = mDBHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        return database.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
    }

}



package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by nora on 10/6/2017.
 */
public class InventoryHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    public InventoryHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + InventoryEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER, "
                + InventoryEntry.COLUMN_PRODUCT_PRICE + " REAL, "
                + InventoryEntry.COLUMN_PRODUCT_IMAGE + " BLOB, "
                + InventoryEntry.COLUMN_PRODUCT_SELLER_NAME + " TEXT, "
                + InventoryEntry.COLUMN_PRODUCT_SELLER_CONTACT + " TEXT );";

        sqLiteDatabase.execSQL(SQL_CREATE_PRODUCTS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ InventoryEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}

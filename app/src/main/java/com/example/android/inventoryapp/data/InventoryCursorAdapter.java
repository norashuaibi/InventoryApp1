package com.example.android.inventoryapp.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.R;

/**
 * Created by nora on 10/6/2017.
 */
public class InventoryCursorAdapter extends CursorAdapter {
    private LayoutInflater cursorInflater;
    Context cxt;
    View viewItem;
    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
        cxt = context;
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantityTextView);
        TextView priceTextView = (TextView) view.findViewById(R.id.priceTextView);
        AppCompatImageView imgView = (AppCompatImageView) view.findViewById(R.id.listImgView);
        // Find the columns of pet attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        final int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
        int id = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);
        int imgColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_IMAGE);
        Toast.makeText(cxt,String.valueOf(imgColumnIndex), Toast.LENGTH_LONG).show();


        // Read the pet attributes from the Cursor for the current pet
        String productName = cursor.getString(nameColumnIndex);
        int productQuantity = cursor.getInt(quantityColumnIndex);
        double productPrice = cursor.getDouble(priceColumnIndex);
        if(imgColumnIndex==1)
        {
            byte[] bytes =cursor.getBlob(imgColumnIndex);
            imgView.setImageBitmap(convertToBitmap(bytes));
            Toast.makeText(cxt, bytes.toString(), Toast.LENGTH_LONG).show();

        }
                // Show Image from DB in ImageView




            // Update the TextViews with the attributes for the current pet
        nameTextView.setText(productName);
        quantityTextView.setText(String.valueOf(productQuantity));
        priceTextView.setText(String.valueOf(productPrice));



        Button saleB = (Button) view.findViewById(R.id.saleButton);
        saleB.setTag(R.id.quantity,productQuantity);
        saleB.setTag(R.id.rowId,cursor.getInt(id));
        saleB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = (Integer) view.getTag(R.id.quantity);
                int rowId = (Integer) view.getTag(R.id.rowId);
                ContentValues values = new ContentValues();
                // Uri currentUri = Uri.parse(InventoryContract.InventoryEntry.CONTENT_URI + "/" + view.getId());
                Uri currentProductUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, rowId);
                if (quantity > 0) {
                    quantity = quantity - 1;
                }
                values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                int rowsAffected = cxt.getContentResolver().update(currentProductUri, values, null, null);
                if (rowsAffected == 0) {
                    //If no rows were affected, then there was an error with the update.
                    Toast.makeText(cxt, "Error", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(cxt, "success", Toast.LENGTH_LONG).show();
                }


            }

        });
        notifyDataSetChanged();
    }

    private Bitmap convertToBitmap(byte[] b){

        return BitmapFactory.decodeByteArray(b, 0, b.length);

    }

}
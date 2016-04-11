package com.example.khalid.savingdata;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    FeedReaderDbHelper mDbHelper;
    TableLayout entryTable;

    private void addEntryRow(String title, String subTitle) {
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(
                new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
        ActionBar.LayoutParams textViewLayoutParams = new ActionBar.LayoutParams(
                android.app.ActionBar.LayoutParams.WRAP_CONTENT,
                android.app.ActionBar.LayoutParams.WRAP_CONTENT);
        TextView titleView = new TextView(this);
        titleView.setLayoutParams(textViewLayoutParams);
        titleView.setText(title);
        tableRow.addView(titleView);
        TextView subTitleView = new TextView(this);
        subTitleView.setLayoutParams(textViewLayoutParams);
        subTitleView.setText(subTitle);
        tableRow.addView(subTitleView);
        entryTable.addView(tableRow);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDbHelper = new FeedReaderDbHelper(this);

        TextView score = (TextView)findViewById(R.id.score);
        entryTable = (TableLayout)findViewById(R.id.entryTable);
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        int defaultValue = getResources().getInteger(R.integer.default_score);
        int highScore = sharedPreferences.getInt(getString(R.string.saved_high_score), defaultValue);
        score.setText(Integer.toString(highScore));
        //score.setText(highScore);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                FeedReaderContract.FeedEntry._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE,
                FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE
        };

        String [] whereValues = {};

        // How you want the results sorted in the resulting Cursor
        String sortOrder = FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + " DESC";

        Cursor c = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME, //Table to query
                projection, //columns to return
                null, //columns for the where clause
                null, // the values for the where clause
                null, // don't group the rows
                null, // don't filter by row groups
                sortOrder // sort Order
        );

        try {
            while (c.moveToNext()) {
                int index = c.getColumnIndex(FeedReaderContract.FeedEntry._ID);
                String title = c.getString(c.getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE));
                String subTitle = c.getString(c.getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE));
                addEntryRow(title, subTitle);
            }
        } finally {
            c.close();
        }
    }

    public void saveHighScore(View view) {
        EditText newScore = (EditText)findViewById(R.id.newscore);
        TextView score = (TextView)findViewById(R.id.score);
        String newScoreValue = newScore.getText().toString();
        if (newScoreValue.length() <= 0) return;
        int value = Integer.parseInt(newScoreValue);
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.saved_high_score), value);
        editor.commit();
        score.setText(Integer.toString(value));
    }

    public void createInternalFile(View view) {
        FileOutputStream outputStream;
        String filename = "myfile";
        String string = "Hello world!";
        //File[] fileList = getFilesDir().listFiles();

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveEntry(View view){
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        EditText title = (EditText)findViewById(R.id.title);
        EditText subTitle =(EditText)findViewById(R.id.subtitle);

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, title.getText().toString());
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE, subTitle.getText().toString());
        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                FeedReaderContract.FeedEntry.TABLE_NAME, "", values);
    }
}

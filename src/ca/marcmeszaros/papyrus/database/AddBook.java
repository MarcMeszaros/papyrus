/*******************************************************************************
 * Copyright (c) 2011 - Marc Meszaros
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

package ca.marcmeszaros.papyrus.database;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import ca.marcmeszaros.papyrus.R;
import ca.marcmeszaros.papyrushunter.PapyrusHunter;
import ca.marcmeszaros.papyrushunter.PapyrusHunterHandler;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;


public class AddBook extends Activity implements OnClickListener, OnItemSelectedListener {
	
	private PapyrusHunterHandler handler;
	private int libraryId;
	private Cursor result;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_book);
				
		this.handler = new PapyrusHunterHandler(this);
		
		// Set up click listeners for all the buttons
		findViewById(R.id.AddBook_button_scan).setOnClickListener(this);
		findViewById(R.id.AddBook_button_addBook).setOnClickListener(this);
		//findViewById(R.id.AddBook_field_isbn).setOnClickListener(this);
		
		// create the spinner and db connection
		Spinner spinner = (Spinner)findViewById(R.id.AddBook_spinner_library);
		SQLiteDatabase db = new DBHelper(getApplicationContext()).getWritableDatabase();
		
		// get all the libraries
		result = db.query(DBHelper.LIBRARY_TABLE_NAME, null, null, null, null, null, DBHelper.LIBRARY_FIELD_NAME);
		startManagingCursor(result);
		
		// specify what fields to map to what views
		String[] from = {DBHelper.LIBRARY_FIELD_NAME};
		int[] to = {android.R.id.text1};
		
		// create a cursor adapter and set it to the list
		SimpleCursorAdapter adp = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, result, from, to);
		adp.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
		spinner.setAdapter(adp);
		
		// get the preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		// try and get the id
		String libraryId = prefs.getString("defaultLibrary", "");
		
		// if the id is not empty
		if(libraryId != ""){
			// parse it
			int defLibrary = Integer.parseInt(libraryId);
			
			// set the spinner selection to the matching default library id
			for (int i = 0; i < spinner.getCount(); i++) {
			    Cursor value = (Cursor) spinner.getItemAtPosition(i);
			    long id = value.getLong(value.getColumnIndex(DBHelper.LIBRARY_FIELD_ID));
			    if (id == defLibrary) {
			        spinner.setSelection(i);
			    }
			}
		}
		
		// when and item is selected
		spinner.setOnItemSelectedListener(this);
		
		
		// focus stuff
		findViewById(R.id.AddBook_field_isbn_label).requestFocus();
	}
	
		
	@Override
	public void onClick(View v) {
		final EditText isbnField = (EditText)findViewById(R.id.AddBook_field_isbn);
		switch (v.getId()) {
		case R.id.AddBook_button_addBook:
				
				// create a progress dialog
				ProgressDialog progress = ProgressDialog.show(this, "", getString(R.string.AddBook_toast_searchingForBook), true);
				handler.setDialog(progress);
			
				// get the data from the activity to start the request
				int copies = Integer.valueOf((((EditText)findViewById(R.id.AddBook_field_quantity)).getText()).toString());
				PapyrusHunter result = new PapyrusHunter(getApplicationContext(), handler, isbnField.getText().toString(), libraryId, copies);
				result.start();
				
				// hide the soft keyboard
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(isbnField.getWindowToken(), 0);
				
			break;
		case R.id.AddBook_button_scan:
				if(isbnField.getText().toString().equals("")){
					IntentIntegrator.initiateScan(this);
				}
			break;
		}
	}
	
	/**
	 * This function handles the return of an activity that returns a result.
	 * This particular instance handles the result from a scan. 
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// parse the result from the scan
		
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		EditText isbnField = (EditText)findViewById(R.id.AddBook_field_isbn);
		
		// make sure the result is not null
		if (scanResult != null) {
			// handle scan result
			if (scanResult.getContents() != null && scanResult.getFormatName() != null) {
				// get result object
				isbnField.setText(scanResult.getContents());
			} 
			else {
				// scan canceled/error
			}
		}
		// else continue with any other code you need in the method
		
	}

	
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,long row) {
		// TODO Auto-generated method stub
		result.moveToPosition(pos);
		libraryId = result.getInt(result.getColumnIndex(DBHelper.LIBRARY_FIELD_ID));
	}


	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

}

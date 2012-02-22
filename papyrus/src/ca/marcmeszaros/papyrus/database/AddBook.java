/**
 * Copyright 2011 Marc Meszaros
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.marcmeszaros.papyrus.database;

import ca.marcmeszaros.papyrus.R;
import ca.marcmeszaros.papyrus.provider.PapyrusContentProvider;
import ca.marcmeszaros.papyrus.remote.PapyrusHunter;
import ca.marcmeszaros.papyrus.remote.PapyrusHunterHandler;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
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

		// create the spinner and db connection
		Spinner spinner = (Spinner) findViewById(R.id.AddBook_spinner_library);

		// get all the libraries
		result = getContentResolver().query(PapyrusContentProvider.Libraries.CONTENT_URI, null, null, null, PapyrusContentProvider.Libraries.FIELD_NAME);

		// specify what fields to map to what views
		String[] from = { PapyrusContentProvider.Libraries.FIELD_NAME };
		int[] to = { android.R.id.text1 };

		// create a cursor adapter and set it to the list
		SimpleCursorAdapter adp = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, result, from, to);
		adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adp);

		// get the preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		// try and get the id
		String libraryId = prefs.getString("defaultLibrary", "");

		// if the id is not empty
		if (!TextUtils.isEmpty(libraryId)) {
			// parse it
			int defLibrary = Integer.parseInt(libraryId);

			// set the spinner selection to the matching default library id
			for (int i = 0; i < spinner.getCount(); i++) {
			    Cursor value = (Cursor) spinner.getItemAtPosition(i);
			    long id = value.getLong(value.getColumnIndex(PapyrusContentProvider.Libraries.FIELD_ID));
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
		final EditText isbnField = (EditText) findViewById(R.id.AddBook_field_isbn);
		switch (v.getId()) {
		case R.id.AddBook_button_addBook:

				// create a progress dialog
				ProgressDialog progress = new ProgressDialog(this);
				progress.setTitle("");
				progress.setMessage(getString(R.string.AddBook_toast_searchingForBook));
				progress.setIndeterminate(true);

				// dirty hack: set an annonymous inner dismiss listener to
				// clear the isbn input
				progress.setOnDismissListener(new DialogInterface.OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
						((EditText) findViewById(R.id.AddBook_field_isbn)).setText("");

					}
				});

				// start the dialog
				progress.show();
				handler.setDialog(progress);

				// get the data from the activity to start the request
				int copies = Integer.valueOf((((EditText) findViewById(R.id.AddBook_field_quantity)).getText()).toString());
				PapyrusHunter result = new PapyrusHunter(this, handler, isbnField.getText().toString(), libraryId, copies);
				result.start();

				// hide the soft keyboard
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(isbnField.getWindowToken(), 0);

			break;
		case R.id.AddBook_button_scan:
				if (isbnField.getText().toString().equals("")) {
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
		EditText isbnField = (EditText) findViewById(R.id.AddBook_field_isbn);

		// make sure the result is not null
		if (scanResult != null) {
			// handle scan result
			if (scanResult.getContents() != null && scanResult.getFormatName() != null) {
				// get result object
				isbnField.setText(scanResult.getContents());
			} else {
				// scan canceled/error
			}
		}
		// else continue with any other code you need in the method
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long row) {
		// TODO Auto-generated method stub
		result.moveToPosition(pos);
		libraryId = result.getInt(result.getColumnIndex(PapyrusContentProvider.Libraries.FIELD_ID));
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
	}

}

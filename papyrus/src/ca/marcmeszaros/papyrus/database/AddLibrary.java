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
import ca.marcmeszaros.papyrus.Settings;
import ca.marcmeszaros.papyrus.database.sqlite.DBHelper;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class AddLibrary extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_library);

		// set on click listeners
		findViewById(R.id.AddLibrary_button_addLibrary).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.AddLibrary_button_addLibrary:
				addLibrary();
				Toast.makeText(this, getString(R.string.AddLibrary_toast_libraryAdded), Toast.LENGTH_SHORT).show();
				finish();
			break;
		}
	}

	/**
	 * Adds the library to the database.
	 */
	private void addLibrary() {

		boolean isFirstLibrary = false;

		// get the text field that has the name
		EditText libraryName = (EditText) findViewById(R.id.AddLibrary_field_name);

		// get the connection to the database
		SQLiteDatabase db = new DBHelper(getApplicationContext()).getWritableDatabase();

		// get all the libraries
		Cursor result = db.query(DBHelper.LIBRARY_TABLE_NAME, null, null, null, null, null, null, null);

		// check if it's the first one
		if (result.getCount() == 0) {
			isFirstLibrary = true;
		}

		// create the query
		ContentValues values = new ContentValues();
		values.put(DBHelper.LIBRARY_FIELD_NAME, libraryName.getText().toString());

		// insert the values
		db.insert(DBHelper.LIBRARY_TABLE_NAME, "unknown", values);

		// requery
		if (isFirstLibrary) {
			result = db.query(DBHelper.LIBRARY_TABLE_NAME, null, null, null, null, null, null, null);
			result.moveToFirst();

			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); 
			SharedPreferences.Editor prefEditor = pref.edit();
			prefEditor.putString(Settings.KEY_DEFAULT_LIBRARY, Long.toString(result.getLong(result.getColumnIndex(DBHelper.LIBRARY_FIELD_ID))));
			prefEditor.commit();
		}

		// we don't need the cursor anymore
		result.close();

		// close the connection
		db.close();

	}

}
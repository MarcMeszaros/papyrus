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
	private void addLibrary(){
		
		boolean isFirstLibrary = false;
		
		// get the text field that has the name
		EditText libraryName = (EditText)findViewById(R.id.AddLibrary_field_name);
		
		// get the connection to the database
		SQLiteDatabase db = new DBHelper(getApplicationContext()).getWritableDatabase();
		
		// get all the libraries
		Cursor result = db.query(DBHelper.LIBRARY_TABLE_NAME, null, null, null, null, null, null, null);
		
		// check if it's the first one
		if(result.getCount() == 0) {
			isFirstLibrary = true;
		}
		
		// create the query
		ContentValues values = new ContentValues();
		values.put(DBHelper.LIBRARY_FIELD_NAME, libraryName.getText().toString());
		
		// insert the values
		db.insert(DBHelper.LIBRARY_TABLE_NAME, "unknown", values);
		
		// requery
		if(isFirstLibrary) {
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

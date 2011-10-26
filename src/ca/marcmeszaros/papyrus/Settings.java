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

package ca.marcmeszaros.papyrus;

import ca.marcmeszaros.papyrus.database.sqlite.DBHelper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity {

	public static String KEY_DEFAULT_LIBRARY = "defaultLibrary";
	
	private SQLiteDatabase db;
	
	/** 
	 * Called when the activity is first created. 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		
		// setup the db connection
		DBHelper helper = new DBHelper(getApplicationContext());
		this.db = helper.getWritableDatabase();

		String[] columns = {DBHelper.LIBRARY_FIELD_ID, DBHelper.LIBRARY_FIELD_NAME};
		
		// get all libraries
		Cursor result = db.query(DBHelper.LIBRARY_TABLE_NAME, columns, null, null, null, null, DBHelper.LIBRARY_FIELD_NAME);
		
		// get the default library preference
		ListPreference defaultLibrary = (ListPreference)findPreference(KEY_DEFAULT_LIBRARY);
		
		// create the list arrays of the right size
		CharSequence[] entries = new CharSequence[result.getCount()];
		CharSequence[] entryValues = new CharSequence[result.getCount()];
		
		// populate the 
		for(int i=0; i<result.getCount(); i++){
			result.moveToNext();
			entries[i] = result.getString(result.getColumnIndex(DBHelper.LIBRARY_FIELD_NAME));
			entryValues[i] = result.getString(result.getColumnIndex(DBHelper.LIBRARY_FIELD_ID));
		}		
		
		// set the list and associated values
		defaultLibrary.setEntries(entries);
		defaultLibrary.setEntryValues(entryValues);		
	}
}

/*******************************************************************************
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
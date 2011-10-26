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

package ca.marcmeszaros.papyrus.browser;

import ca.marcmeszaros.papyrus.R;
import android.app.TabActivity;
import android.content.Intent;
//import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class TabBrowser extends TabActivity {

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_tab_browser);
	    
	    // get the extras
	    Bundle bundle = getIntent().getExtras();
	    // if the tab browser was started from a notification, set the
	    // default tab to the one specified by the intent
	    int tab = (bundle == null) ? 0 : bundle.getInt("tab", 0);
	    
	    //Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent(this, BooksBrowser.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("books").setIndicator(getString(R.string.BooksBrowser_label)).setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent(this, LoansBrowser.class);
	    spec = tabHost.newTabSpec("loans").setIndicator(getString(R.string.LoansBrowser_label)).setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent(this, LibrariesBrowser.class);
	    spec = tabHost.newTabSpec("libraries").setIndicator(getString(R.string.LibrariesBrowser_label)).setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(tab);
	}
	
}

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
package ca.marcmeszaros.papyrus;

import android.app.Application;

import org.acra.*;
import org.acra.annotation.*;

// Uncomment the following line and replace "YOUR_KEY", with your bugsense API key
//@ReportsCrashes(formUri = "http://www.bugsense.com/api/acra?api_key=YOUR_KEY", formKey="")
public class Papyrus extends Application {

	/**
	 * Called when the application is first created.
	 */
	@Override
	public void onCreate() {
		// uncomment the following line to enable ACRA crash reports
		//ACRA.init(this);
		super.onCreate();
	}
}

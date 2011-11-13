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

package ca.marcmeszaros.papyrus.remote.google;

import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;

import java.io.IOException;

public class Feed {
	
	private static final String TAG = "Feed";
	
	@Key("openSearch:startIndex")
	public int startIndex;
  
	@Key("openSearch:totalResults")
	public int totalResults;
   
	static Feed executeGet(HttpTransport transport, BookUrl url, Class<? extends Feed> feedClass, HttpRequest request) throws IOException {
		Log.i(TAG, "in Feed.class");

		return request.execute().parseAs(feedClass);
	}
}

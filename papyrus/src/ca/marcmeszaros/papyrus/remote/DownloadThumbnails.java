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
package ca.marcmeszaros.papyrus.remote;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class DownloadThumbnails extends AsyncTask<URL, Void, LinkedList<Bitmap>> {

	private static final String TAG = "DownloadThumbnails";

	@Override
	protected LinkedList<Bitmap> doInBackground(URL... urls) {

		// some class variables
		HttpGet httpRequest;
		Bitmap bm;
		LinkedList<Bitmap> bitmaps = new LinkedList<Bitmap>();

		try {

			// loop through all urls
			for (URL url : urls) {
				// create the HTTP request
				httpRequest = new HttpGet(url.toURI());

				// create an HTTP client and get the response
				HttpClient httpclient = new DefaultHttpClient();
				HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);

				// get a handle on the http entity
				HttpEntity entity = response.getEntity();
				BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
				InputStream instream = bufHttpEntity.getContent();

				// read the entity from the stream into a bitmap object
				bm = BitmapFactory.decodeStream(instream);

				// add the bitmap to the list
				bitmaps.add(bm);

			}

		} catch (URISyntaxException e) {
			Log.e(TAG, "URISyntaxException", e);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException", e);
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		}

		return bitmaps;
	}

}

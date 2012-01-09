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
package ca.marcmeszaros.papyrus.tools;

import ca.marcmeszaros.papyrus.database.sqlite.DBHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

public class TNManager {

	private static final String TAG = "TNManager";

	/**
	 * Downloads and saves the thumbnail of a book to the SD card.
	 *
	 * @param thumbnailURL the URL to the thumbnail
	 * @param isbn the ISBN number of the book
	 * @return {@code true} on success or {@code false} on failure
	 */
	public static boolean saveThumbnail(URL thumbnailURL, String isbn) {
		// make sure we have access to the SD card
		if (Environment.MEDIA_MOUNTED.equals(DBHelper.PAPYRUS_SDCARD_STATE)) {
			try {
				// if the folder on the SD carld doesn't exist, create it
				if (!DBHelper.PAPYRUS_SDCARD_ROOT.exists()) {
					DBHelper.PAPYRUS_SDCARD_ROOT.mkdir();
					// creates the ".nomedia" file to hide content from "Gallery"
					new File(DBHelper.PAPYRUS_SDCARD_ROOT, ".nomedia").createNewFile();
				}

				// create the thumbnail
				File thumbnail = new File(DBHelper.PAPYRUS_SDCARD_ROOT, isbn + ".jpg");

				// if the file doesn't exist, create it and get the data
				if (!thumbnail.exists()) {
					thumbnail.createNewFile();

					Log.i(TAG, "Can write to sdcard: " + thumbnail.canWrite());

					HttpGet httpRequest;
					httpRequest = new HttpGet(thumbnailURL.toURI());

					HttpClient httpclient = new DefaultHttpClient();
					HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);

					HttpEntity entity = response.getEntity();
					BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
					InputStream instream = bufHttpEntity.getContent();
					Bitmap bm = BitmapFactory.decodeStream(instream);

					FileOutputStream out = new FileOutputStream(thumbnail);

					bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
				}

			} catch (URISyntaxException e) {
				Log.e(TAG, "URISyntaxException", e);
			} catch (IOException e) {
				Log.e(TAG, "IOException", e);
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * Return a File handler to the book thumbnail.
	 *
	 * @param isbn the ISBN number to the book to get
	 * @return a {@code File} handle to the thumbnail image
	 */
	public static File getThumbnail(String isbn) {
		return new File(DBHelper.PAPYRUS_SDCARD_ROOT, isbn + ".jpg");
	}

	/**
	 * Delete a book thumbnail using it's ISBN number.
	 *
	 * @param isbn the ISBN number to delete
	 * @return {@code true} on operation success or {@code false} on failure
	 */
	public static boolean deleteThumbnail(String isbn) {
		return new File(DBHelper.PAPYRUS_SDCARD_ROOT, isbn + ".jpg").delete();
	}

}

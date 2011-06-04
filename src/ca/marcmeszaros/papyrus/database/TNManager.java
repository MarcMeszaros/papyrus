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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class TNManager {
	/**
	 * method: saveThumbnail
	 * 
	 * description: stores the thumbnail to the SD card
	 * 
	 * @param thumbnailURL
	 * @param isbn
	 * @return true/false
	 */
	public static boolean saveThumbnail(URL thumbnailURL, String isbn) {
		// make sure we have access to the SD card
		if (Environment.MEDIA_MOUNTED.equals(DBHelper.PAPYRUS_SDCARD_STATE)) {
			try {
				// if the folder on the SD carld doesn't exist, create it
				if (!DBHelper.PAPYRUS_SDCARD_ROOT.exists()) {
					DBHelper.PAPYRUS_SDCARD_ROOT.mkdir();
					// creates the ".nomedia" file to hide content from
					// "Gallery"
					new File(DBHelper.PAPYRUS_SDCARD_ROOT, ".nomedia")
							.createNewFile();
				}

				// create the thumbnail
				File thumbnail = new File(DBHelper.PAPYRUS_SDCARD_ROOT, isbn
						+ ".jpg");

				// if the file doesn't exist, create it and get the data
				if (!thumbnail.exists()) {
					thumbnail.createNewFile();

					Log.i("sdcard",
							"can we write to sdcard: " + thumbnail.canWrite());

					HttpGet httpRequest;
					try {
						httpRequest = new HttpGet(thumbnailURL.toURI());

						HttpClient httpclient = new DefaultHttpClient();
						HttpResponse response = (HttpResponse) httpclient
								.execute(httpRequest);

						HttpEntity entity = response.getEntity();
						BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(
								entity);
						InputStream instream = bufHttpEntity.getContent();
						Bitmap bm = BitmapFactory.decodeStream(instream);

						FileOutputStream out = new FileOutputStream(thumbnail);

						bm.compress(Bitmap.CompressFormat.JPEG, 100, out);

					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				// error
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * method: getThumbnail
	 * 
	 * @param isbn
	 * @return
	 */
	public static File getThumbnail(String isbn) {
		return new File(DBHelper.PAPYRUS_SDCARD_ROOT, isbn + ".jpg");
	}

	/**
	 * method: deleteThumbnail
	 * 
	 * @param isbn
	 * @return
	 */
	public static boolean deleteThumbnail(String isbn) {
		return new File(DBHelper.PAPYRUS_SDCARD_ROOT, isbn + ".jpg").delete();
	}
}

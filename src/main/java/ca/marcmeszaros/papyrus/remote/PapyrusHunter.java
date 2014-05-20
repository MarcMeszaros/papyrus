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

import ca.marcmeszaros.papyrus.Papyrus;
import ca.marcmeszaros.papyrus.provider.PapyrusContentProvider;
import ca.marcmeszaros.papyrus.tools.TNManager;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.Books.Volumes.List;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class PapyrusHunter implements Runnable {

	private static final String TAG = "PapyrusHunter";

	// class variables
	private Context context;
	private int libraryID;
	private int quantity;
	private Handler messageHandler;
	private String bar_code;

	public PapyrusHunter(Context applicationContext, Handler messageHandler, String bar_code, int libraryID, int quantity) {
		this.context = applicationContext;
		this.messageHandler = messageHandler;
		this.bar_code = bar_code;
		this.libraryID = libraryID;
		this.quantity = quantity;
	}

	@Override
	public void run() {

		Message msg = new Message();

		try {

			// create the JsonFactory and the booksrequest builder
			JsonFactory jsonFactory = new JacksonFactory();
			Books.Builder booksBuilder = new Books.Builder(new NetHttpTransport(), jsonFactory, null);

			// set some properties on the builder
			String applicationName = "Papyrus/" + Papyrus.getVersionName();
			booksBuilder.setApplicationName(applicationName);
            booksBuilder.setGoogleClientRequestInitializer(new BooksRequestInitializer());

			// get the final builder
			final Books books = booksBuilder.build();

			// prepare the query
			List volumesList = books.volumes().list("isbn:" + bar_code);

		    // execute the query.
		    Volumes volumes = volumesList.execute();

			// parse some results... if we have some of course...
			if (volumes.getTotalItems() > 0 && volumes.getItems() != null) {
				for (Volume volume : volumes.getItems()) {
					Log.i("PapyrusHunter", "Got book: " + volume.getVolumeInfo().getTitle());
				}

				// get the first entry
				Volume volume = volumes.getItems().get(0);
				Volume.VolumeInfo volInfo = volume.getVolumeInfo();
				java.util.List<Volume.VolumeInfo.IndustryIdentifiers> volIdents = volInfo.getIndustryIdentifiers();

				// get the isbn numbers
				String isbn10 = "";
				String isbn13 = "";
				// iterate through the identifiers
				for (Volume.VolumeInfo.IndustryIdentifiers identifier : volIdents) {
					if (identifier.getType().equals("ISBN_10")) {
						isbn10 = identifier.getIdentifier();
					} else if (identifier.getType().equals("ISBN_13")) {
						isbn13 = identifier.getIdentifier();
					}
				}
				Log.d(TAG, "isbn10: " + isbn10);
				Log.d(TAG, "isbn13: " + isbn13);

				// get the title
				String title = volInfo.getTitle();

				// get the authors
				String authors = "";
				for (String author : volInfo.getAuthors()) {
					authors += author + ", ";
				}
				// fix the last ', '
				if (authors.length() > 0) {
					authors = authors.substring(0, authors.length() - 2);
				}

				// get other data
				String publishers = volInfo.getPublisher();
				String date = volInfo.getPublishedDate();

				// the thumbnail url
				String rawThumbnailUrl = null;
				if (volInfo.getImageLinks() != null) {
					rawThumbnailUrl = volInfo.getImageLinks().getSmallThumbnail();
				}
				URL thumbnail = null;
				if (rawThumbnailUrl != null) {
					Log.d(TAG, "thumbnail url: " + rawThumbnailUrl);
					thumbnail = new URL(rawThumbnailUrl);
				}

				Log.i(TAG, "Start saving book");

				// create the query
				ContentValues values = new ContentValues();
				values.put(PapyrusContentProvider.Books.FIELD_TITLE, title);
				values.put(PapyrusContentProvider.Books.FIELD_AUTHOR, authors);
				values.put(PapyrusContentProvider.Books.FIELD_ISBN10, isbn10);
				values.put(PapyrusContentProvider.Books.FIELD_ISBN13, isbn13);
				values.put(PapyrusContentProvider.Books.FIELD_PUBLISHER, publishers);
				values.put(PapyrusContentProvider.Books.FIELD_PUBLICATION_DATE, date);
				values.put(PapyrusContentProvider.Books.FIELD_LIBRARY_ID, libraryID);
				values.put(PapyrusContentProvider.Books.FIELD_QUANTITY, quantity);

				// insert the book
				context.getContentResolver().insert(PapyrusContentProvider.Books.CONTENT_URI, values);
				Log.d(TAG, "Saving book complete");

				// get the thumbnail and save it
				// check if we got an isbn10 number from query and file exists
				if (isbn10 != "" && thumbnail != null) {
					DownloadThumbnails download = new DownloadThumbnails();

					download.execute(thumbnail);
					LinkedList<Bitmap> images = download.get();

					TNManager.saveThumbnail(images.getFirst(), isbn10);
					Log.d(TAG, "Got thumbnail asynctask");
				} else if (isbn13 != "" && thumbnail != null) {
					// check if we got an isbn13 number from query and file exists
					DownloadThumbnails download = new DownloadThumbnails();

					download.execute(thumbnail);
					LinkedList<Bitmap> images = download.get();

					TNManager.saveThumbnail(images.getFirst(), isbn13);
					Log.d(TAG, "Got thumbnail");
				}

				// send message that we saved the book
				msg.what = -1;
				msg.obj = title;
				messageHandler.sendMessage(msg);

			} else {
				msg.what = 1; // no book info
				messageHandler.sendMessage(msg); // send message to handler
			}

		} catch (MalformedURLException e) {
			messageHandler.sendEmptyMessage(0);
			Log.e(TAG, "Malformed URL Exception.", e);
		} catch (IOException e) {
			messageHandler.sendEmptyMessage(0);
			Log.e(TAG, "Couldn't connect to the server.", e);
		} catch (InterruptedException e) {
			Log.e(TAG, "The task was interuppted.", e);
		} catch (ExecutionException e) {
			Log.e(TAG, "The thread crashed.", e);
		}
	}
}

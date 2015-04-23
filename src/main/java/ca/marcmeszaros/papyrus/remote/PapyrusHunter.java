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

import ca.marcmeszaros.papyrus.BuildConfig;
import ca.marcmeszaros.papyrus.provider.PapyrusContentProvider;
import ca.marcmeszaros.papyrus.util.TNManager;
import timber.log.Timber;

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
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class PapyrusHunter implements Runnable {

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
            ContentValues values = new ContentValues();

			// create the JsonFactory and the books request builder
			JsonFactory jsonFactory = new JacksonFactory();
			Books.Builder booksBuilder = new Books.Builder(new NetHttpTransport(), jsonFactory, null);

			// set some properties on the builder
			String applicationName = "Papyrus/" + BuildConfig.VERSION_NAME;
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
					Timber.i("Got book: %s", volume.getVolumeInfo().getTitle());
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
				Timber.d("isbn10: %s", isbn10);
				Timber.d("isbn13: %s", isbn13);

				// get the title
                values.put(PapyrusContentProvider.Books.FIELD_TITLE, volInfo.getTitle());

				// get the authors
				if (volInfo.getAuthors() != null) {
                    values.put(PapyrusContentProvider.Books.FIELD_AUTHOR, TextUtils.join(", ", volInfo.getAuthors()));
                }

				// get other data
                values.put(PapyrusContentProvider.Books.FIELD_PUBLISHER, volInfo.getPublisher());
                values.put(PapyrusContentProvider.Books.FIELD_PUBLICATION_DATE, volInfo.getPublishedDate());

                // the thumbnail url
                URL thumbnail = null;
                if (volInfo.getImageLinks() != null) {
                    thumbnail = new URL(volInfo.getImageLinks().getSmallThumbnail());
                }

                // create the query
                values.put(PapyrusContentProvider.Books.FIELD_ISBN10, isbn10);
                values.put(PapyrusContentProvider.Books.FIELD_ISBN13, isbn13);
				values.put(PapyrusContentProvider.Books.FIELD_LIBRARY_ID, libraryID);
				values.put(PapyrusContentProvider.Books.FIELD_QUANTITY, quantity);

				// insert the book
				context.getContentResolver().insert(PapyrusContentProvider.Books.CONTENT_URI, values);

				// get the thumbnail and save it
				// check if we got an isbn10 number from query and file exists
				if (!TextUtils.isEmpty(isbn10) && thumbnail != null) {
					DownloadThumbnails download = new DownloadThumbnails();

					download.execute(thumbnail);
					LinkedList<Bitmap> images = download.get();

					TNManager.saveThumbnail(images.getFirst(), isbn10);
					Timber.d("Got thumbnail asynctask");
				} else if (!TextUtils.isEmpty(isbn13) && thumbnail != null) {
					// check if we got an isbn13 number from query and file exists
					DownloadThumbnails download = new DownloadThumbnails();

					download.execute(thumbnail);
					LinkedList<Bitmap> images = download.get();

					TNManager.saveThumbnail(images.getFirst(), isbn13);
					Timber.d("Got thumbnail");
				}

				// send message that we saved the book
				msg.what = -1;
				msg.obj = volInfo.getTitle();
				messageHandler.sendMessage(msg);

			} else {
				msg.what = 1; // no book info
				messageHandler.sendMessage(msg); // send message to handler
			}

		} catch (MalformedURLException e) {
			messageHandler.sendEmptyMessage(0);
			Timber.e(e, "Malformed URL Exception.");
		} catch (IOException e) {
			messageHandler.sendEmptyMessage(0);
			Timber.e(e, "Couldn't connect to the server.");
		} catch (InterruptedException e) {
			Timber.e(e, "The task was interrupted.");
		} catch (ExecutionException e) {
			Timber.e(e, "The thread crashed.");
		}
	}
}

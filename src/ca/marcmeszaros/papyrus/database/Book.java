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

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {
	
	private String isbn10;
	private String isbn13;
	private String title;
	private String author;
	private String edition;
	private int quantity;
	private String publisher;
	private int bookID;
	private int libraryID;
		
	public Book() {}
	
	public Book(String isbn10, String title, String author) {
		this.isbn10 = isbn10;
		this.title = title;
		this.author = author;
	}
	
	public Book(String isbn10, String isbn13, String title, String author) {
		this.isbn10 = isbn10;
		this.isbn13 = isbn13;
		this.title = title;
		this.author = author;
	}
	
	// Getters
	public String getISBN10() {
		return isbn10;
	}
	
	public String getISBN13() {
		return isbn13;
	}

	public String getTitle() {
		return title;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getEdition() {
		return edition;
	}
	
	public int getQuantity() {
		return quantity;
	}
	
	public String getPublisher() {
		return publisher;
	}
	
	public int getBookID() {
		return bookID;
	}
	
	public int getLibraryID() {
		return libraryID;
	}
	
	// Setters
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	
	public void setBookID(int bookID) {
		this.bookID = bookID;
	}
	
	public void setLibraryID(int libraryID) {
		this.libraryID = libraryID;
	}
	
	// Parcelable 
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(isbn10);
		dest.writeString(isbn13);
		dest.writeString(title);
		dest.writeString(author);
		dest.writeString(publisher);
		dest.writeInt(quantity);
		dest.writeInt(bookID);
		dest.writeInt(libraryID);
	}
	
	public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
		public Book createFromParcel(Parcel in) {
			return new Book(in);
		}

		public Book[] newArray(int size) {
			return new Book[size];
		}
	};

	private Book(Parcel in) {
		this.isbn10 = in.readString();
		this.isbn13 = in.readString();
		this.title = in.readString();
		this.author = in.readString();
		this.publisher = in.readString();
		this.quantity = in.readInt();
		this.bookID = in.readInt();
		this.libraryID = in.readInt();
	}
}

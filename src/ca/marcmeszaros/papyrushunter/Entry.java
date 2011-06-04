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

package ca.marcmeszaros.papyrushunter;

import java.util.List;

import com.google.api.client.util.Key;

public class Entry {
  @Key("title")
  public String title;
  
  @Key("dc:identifier")
  public List<String> identifiers;
  
  @Key("dc:title")
  public List<String> dcTitle;
  
  @Key("dc:creator")
  public List<String> dcCreator;
  
  @Key("dc:description")
  public String dcDescription;
  
  @Key("dc:publisher")
  public String dcPublisher;
  
  @Key("dc:date")
  public String dcDate;
  
  @Key("link")
  public List<Link> links;
  
  public String getThumbnailUrl(){
	  return Link.find(links, "http://schemas.google.com/books/2008/thumbnail");
  }
  
}
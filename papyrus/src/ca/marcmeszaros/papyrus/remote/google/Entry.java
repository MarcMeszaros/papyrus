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
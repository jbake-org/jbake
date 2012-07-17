package com.jbake.main;

import java.util.Date;

// Copyright 2012 Jonathan Bullock
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

public class Content {
	private String uri;
	private String title;
	private Date date;
	private String body;
	private String[] tags;
	private Status status;
	private Type type;
	
	public Content() {
		super();
	}

	public Content(String uri, String title, Date date, String body, String[] tags, Status status, Type type) {
		super();
		this.uri = uri;
		this.title = title;
		this.date = date;
		this.body = body;
		this.tags = tags;
		this.status = status;
		this.type = type;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}

	public String getTagsAsString() {
		if (tags != null) {
			StringBuffer output = new StringBuffer();
			for (String tag : tags) {
				output.append(tag + ",");
			}
			return output.toString().substring(0, output.length()-1);
		} else {
			return "";
		}
	}
	
	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
}

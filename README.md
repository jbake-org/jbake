# JBake

by **[Jonathan Bullock](http://jonathanbullock.com/)**

JBake is a Java based static site/blog generator for developers, bake your own web site!

## Features

- Supports both HTML & Markdown content
- Allows you to structure your content any way you see fit
- Blog aware (RSS feed, tags, archive)
- Customisable templates
- Scripting support in templates via Freemarker
- Supports custom meta data that is exposed to templates
- Features are configurable
- Store your site in Dropbox, CVS, SVN, Git whatever you want

## Getting Started

- Get the code and run `mvn package` from the root folder to build an executable JAR file
- Run `java -jar <jarfile> <source_folder> <destination_folder>` to do some baking

## Usage

Example source structure:

<pre>
.
|-- assets
|   |-- favicon.gif
|   |-- robots.txt
|   |-- css
|       |-- style.css
|-- content
|   |-- about.html
|   |-- 2013
|       |-- 02 
|           |-- weekly-links-1.html
|           |-- weekly-links-2.md
|-- templates
|   |-- index.ftl
|   |-- page.ftl
|   |-- post.ftl
|   |-- feed.ftl
|-- custom.properties
</pre>

Provide link to exmaple site with templates (maybe my site?).

### assets

Place your static files in this folder and they will be copied to the root of the destination folder. Any folder structure you create will be maintained.

### content

Place your dynamic content in this folder, the content in the files in this folder will be "mixed" with the templates to generate your site. Again any folder structure you create will be maintained in the destination folder.

The extension of the file determines what content it contains:

- .html = raw html
- .md = Markdown

Each content file needs to have a meta-data header in it:

<pre>
title=Weekly Links #2
date=2013-02-01
type=post
tags=weekly links, java
status=published
~~~~~~
</pre>

The header MUST have at least the **status** & **type** fields, the rest are optional.

You can also add extra meta data to the header:

<pre>
summary=This is a summary of the larger post
</pre>

And access it from the template like so:

`<p>${content.summary}</p>`

Drafts ........

### templates

This is where your [Freemarker](http://freemarker.sourceforge.net) templates go.

Here's whats available to you in what template files:

**Common**

`${version}` outputs the version of JBake

The contents of the file is always available via `${content.body}`

**index.ftl / feed.ftl**

`${posts}` is a collection of content which can be iterated through (this collection only has files which doesn't have type=page, i.e. posts)

**post.ftl / page.ftl**

`${content}` is an object that contains the the header & body of the file

You can choose what template your content file will be "mixed" with by changing the *post* header field (i.e. type=post will use post.ftl, type=page will use page.ftl).

Some example templates are included in the `/misc/templates/` folder.

See the 
[Freemarker Manual](http://freemarker.sourceforge.net/docs/index.html) for more information on what you can do in Freemarker templates.

Why Freemarker? I've used them as part of the Hibernate reverse engineering system.

### custom.properties

The custom.properties file allows you to override the default configuration of JBake. You can change the name of the folder that stores your content or templates, decide whether to generate a an RSS feed or not. See default.properties for what options are available.

## License

Licensed under the MIT License, see the LICENSE file.

## Dependencies

- Java v1.6 or above
- Apache Maven v2.2 or above

## Alternatives

- Jekyll
- Awestruct
- Usermesh

## Contact

- [http://jonathanbullock.com/](http://jonathanbullock.com/)
- [jonbullock@gmail.com](jonbullock@gmail.com)
- [@jonbullock](http://twitter.com/jonbullock)

# JBake

[![Build Status](https://travis-ci.org/jonbullock/JBake.png?branch=master)](https://travis-ci.org/jonbullock/JBake)

by **[Jonathan Bullock](http://jonathanbullock.com/)**

JBake is a Java based open source static site/blog generator for developers, bake your own web site!

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

- Get the source code
- To start developing JBake run `mvn eclipse:eclipse` to build required Eclipse project files
- To start using JBake run `mvn package` from the root folder to build a release distribution

## Usage

- Run `jbake` or `jbake.bat` (if you are on Windows) to do some baking, this will assume the current folder as the source and place any output into an `output` folder in the current folder
- Alternatively run `jbake <source_folder> <destination_folder>` if you want full control over the source and destination folders

## Run from maven

You can run directly from sources using the maven exec plugin:

```
mvn exec:java -Dexec.mainClass=org.jbake.launcher.Main -Dexec.args="/fromFolder /tmp/toFolder/"
```

## Source structure

Here is an example source folder structure:

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

A full example source folder is provided with the [source code](https://github.com/jonbullock/JBake/tree/master/src/test/resources).

Both [http://jonathanbullock.com](http://jonathanbullock.com) and [http://jbake.org](http://jbake.org) are built using JBake.

### Assets

Place your static files in this folder and they will be copied to the root of the destination folder. Any folder structure you create will be maintained.

### Content

Place your dynamic content in this folder, the content in the files in this folder will be "mixed" with the templates to generate your site. Again any folder structure you create will be maintained in the destination folder.

The extension of the file determines what content it contains:

- .html = HTML content
- .md = Markdown content

#### Header

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

##### Status

You have 2 options for the status field: `draft` and `published`, drafts are rendered along with published posts however they are given a "-draft" suffix, for example `first-post-draft.html`.

##### Type

You can choose what template your content file will be "mixed" with by changing the type field (i.e. type=post will use post.ftl, type=page will use page.ftl).

#### Extra Header

You can also add extra meta data in the header that is also exposed to the templates:

<pre>
summary=This is a summary of the larger post
</pre>

And access it from the template like so:

`<p>${content.summary}</p>`

### Templates

This is the folder where your [Freemarker](http://freemarker.sourceforge.net) templates go. For more information on what you can do in Freemarker templates see the [Manual](http://freemarker.sourceforge.net/docs/index.html).

Here is the data that is available to you in your template files:

#### Global

This data is available to all templates regardless.

- `${version}` = version of JBake
- `${config.[options]}` = map of configuration data

All the configuration options in `default.properties` are available with any `.` in the property being replaced with `_`.
For example `template.index.file=index.ftl` is available via `${config.template_index_file}`.

- `${posts}` = collection of all posts (files that don't have `type=page`)
- `${pages}` = collection of all pages (files that have `type=page`)

You can loop through the above collections using:

<pre>
&lt;#list posts as post&gt;
	..
&lt;/#list&gt;
</pre>

Within the loop you can then access the options for each post or page: `${post.[options]}` or `${page.[options]}`

All of the header fields are available such as `${post.title}` and the body of the file is available via `${post.body}`.

#### Page / Post

These templates (page.ftl & post.ftl) as well as any custom templates you create yourself have the following data available to them:

- `${content.[options]}` = map of file contents

All of the header fields are available such as `${content.title}` and the body of the file is available via `${content.body}`.

#### Index / Feed / Archive

These templates (index.ftl, feed.ftl & archive.ftl) have the following extra data available to them:

- `${published_posts}` = collection of published posts
- `${published_date}` = date when file is generated (only available to Feed)

#### Tags

This template (tags.ftl) has the following extra data available to it:

- `${tag}` = tag being rendered
- `${tag_posts}` = collection posts for tag

## Configuration

The `custom.properties` file allows you to override the default configuration of JBake. You can change the name of the folder that stores your content or templates, decide whether to generate a an RSS feed or not. See [default.properties](https://github.com/jonbullock/JBake/blob/master/src/main/resources/default.properties) for what options are available.

## License

Licensed under the MIT License, see the LICENSE file.

## Development Tools

- Java v1.6 SDK
- Eclipse Juno (Java EE Package)
- Apache Maven v3.0.3

## Alternatives

- [Jekyll - Ruby](http://jekyllrb.com/)
- [Awestruct - Ruby](http://awestruct.org/)
- [Usermesh - Perl](http://usermesh.org/)

## Contact

- [http://jonathanbullock.com/](http://jonathanbullock.com/)
- [jonbullock@gmail.com](mailto:jonbullock@gmail.com)
- [@jonbullock](http://twitter.com/jonbullock)

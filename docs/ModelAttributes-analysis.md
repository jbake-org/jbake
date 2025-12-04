# Model Attributes Data Flow Matrix

| Key                              | Type                          | Assigned By | Accessed By | Notes |
|----------------------------------|-------------------------------| --- | --- | --- |
| `DOC_BODY_RENDERED`              | `String`                      | ParserContext / MarkupEngine | DocumentModel, HsqldbContentRepository, Neo4jContentRepository | Rendered HTML body captured from markup parsing |
| `DOC_DATE`                       | `Date?`                       | AsciidoctorEngine, ParserContext | DocumentModel, Crawler (status transition logic), tests | Null means no explicit date provided |
| `DOC_NAME`                       | `String`                      | BaseModel / TypedBaseModel (DB hydration) | DocumentModel | Derived from filename or DB row |
| `DOC_STATUS`                     | `String?`                     | AsciidoctorEngine, Crawler.addAdditionalDocumentAttributes | DocumentModel, Renderer, Crawler, OrientDBContentRepository | Defaults to empty string; converted from published-date to published |
| `DOC_TAGS`                       | `List<String>`                | AsciidoctorEngine, Parser.setTags | DocumentModel, Renderer | Empty when no tags declared |
| `DOC_TITLE`                      | `String?`                     | AsciidoctorEngine / ParserContext | DocumentModel | Null when no title header present |
| `DOC_TYPE`                       | `String`                      | AsciidoctorEngine, Crawler (data files), Renderer.buildSimpleModel | DocumentModel, OrientDBContentRepository | Describes logical doc type (post/page/index/etc.) |
| `FS_DOC_SHA1`                    | `String?`                     | Crawler.addAdditionalDocumentAttributes | DocumentModel, OrientDBContentRepository | Change detector hash |
| `FS_DOC_SOURCE_PATH_ABS`         | `String?`                     | Crawler.addAdditionalDocumentAttributes | DocumentModel | Absolute file path |
| `FS_DOC_OUTPUT_URI_NOEXT`        | `String?`                     | Crawler.addAdditionalDocumentAttributes | DocumentModel | URI variant without extension |
| `FS_DOC_IS_CACHED_IN_DB`         | `Boolean`                     | Crawler.addAdditionalDocumentAttributes | DocumentModel, OrientDBContentRepository | Marks cached rows |
| `FS_DOC_WAS_RENDERED`            | `Boolean`                     | Crawler.addAdditionalDocumentAttributes, DB hydration | DocumentModel, OrientDBContentRepository | Tracks rendering status |
| `FS_DOC_OUTPUT_URI`              | `String?`                     | Crawler.addAdditionalDocumentAttributes, DefaultRenderingConfig.model | DocumentModel | Final output URI |
| `FS_DOC_SOURCE_REL_URI`          | `String?`                     | Crawler.addAdditionalDocumentAttributes, DB hydration | DocumentModel, OrientDBContentRepository | Source path relative to content dir |
| `FS_REL_FROM_DOC_TO_SITEROOT`    | `String`                      | Crawler.addAdditionalDocumentAttributes, Renderer.buildSimpleModel | DocumentModel, tests | Root path helper |
| `TMPL_CONTENT_MODEL`             | `DocumentModel`               | Renderer.render / renderIndexPaging / renderTags, DefaultRenderingConfig.model, TemplateModel.fromContext | TemplateModel | Primary per-render content |
| `TMPL_DB_ACCESS`                 | `ContentStore`                | DelegatingTemplateEngine eager model, FreemarkerTemplateEngine wrappers | FreemarkerTemplateEngine adapters | Enables db access from templates |
| `TMPL_OUT_WRITER`                | `Writer`                      | DelegatingTemplateEngine.renderDocument | TemplateModel | Underlying output writer |
| `TMPL_JBAKE_CONFIG`              | `Map<String, Any>`            | Renderer.renderTags, Renderer.tags index builder, TemplateModel.fromContext, FreemarkerTemplateEngine merged config | TemplateModel, FreemarkerTemplateEngine | Merged underscore + dotted config |
| `TMPL_ENGINE`                    | `DelegatingTemplateEngine?`   | Renderer render flows, DefaultRenderingConfig.model | TemplateModel | Gives templates access to render delegates |
| `PAGI_CUR_PAGE_NUMBER`           | `Int`                         | Renderer.renderIndexPaging, DefaultRenderingConfig.model, TemplateModel.fromContext | TemplateModel | Current pagination index |
| `PAGI_NEXT_CONTENT`              | `DocumentModel?`              | DocumentsRenderingTool.getContentForNav | DocumentModel, Renderer | Next doc link |
| `PAGI_NEXT_FILENAME`             | `String?`                     | Renderer.renderIndexPaging, DefaultRenderingConfig.model, TemplateModel.fromContext | TemplateModel | Next page file |
| `PAGI_PREV_CONTENT`              | `DocumentModel?`              | DocumentsRenderingTool.getContentForNav | DocumentModel, Renderer | Prev doc link |
| `PAGI_PREV_FILENAME`             | `String?`                     | Renderer.renderIndexPaging, DefaultRenderingConfig.model, TemplateModel.fromContext | TemplateModel | Prev page file |
| `PAGI_TOTAL_PAGES_COUNT`         | `Int`                         | Renderer.renderIndexPaging, DefaultRenderingConfig.model, TemplateModel.fromContext | TemplateModel | Total pagination pages |
| `TAGS_ALL`                       | `List<String>`                | Renderer.renderTagsIndex, TagsExtractor.collectTags | FreemarkerTemplateEngine, tag templates | Distinct tag list |
| `TAGS_CURRENT_TAG`               | `String?`                     | Renderer.renderTags, TemplateModel.fromContext | TemplateModel, Renderer | Currently processed tag |
| `TAGS_DOCS_TAGGED_CUR`           | `DocumentList<DocumentModel>` | TagsExtractor.populateTagContext, TemplateModel.fromContext | TemplateModel | Documents filtered by tag |
| `TAGS_POSTS_TAGGED_CUR`          | `DocumentList<DocumentModel>` | TagsExtractor.populateTagContext, TemplateModel.fromContext | TemplateModel | Posts filtered by tag |
| `DATA_FILES`                     | `Map<String, Any>`            | FreemarkerTemplateEngine (DataFileUtil adapter), TemplateModel.fromContext | FreemarkerTemplateEngine | Exposes data dir records |
| `GLOB_PUBLISHING_DATE_FORMATTED` | `String`                      | FreemarkerTemplateEngine | FreemarkerTemplateEngine/templates | Formatted publish date |
| `GLOB_JBAKE_VERSION`             | `String`                      | Renderer.render, DefaultRenderingConfig.model, TemplateModel.fromContext | TemplateModel | JBake version string |


package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.app.configuration.PropertyList.DATA_FILE_DOCTYPE
import org.jbake.template.ModelExtractor
import org.jbake.util.DataFileUtil

class DataExtractor : ModelExtractor<DataFileUtil?> {

    override fun get(db: ContentStore, model: MutableMap<String, Any>, key: String): DataFileUtil {
        val config = model["config"] as MutableMap<String, Any>
        val defaultDocType: String = config[DATA_FILE_DOCTYPE.key.replace(".", "_")].toString()
        return DataFileUtil(db, defaultDocType)
    }
}

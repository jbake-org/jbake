package org.jbake.template.model

import org.jbake.app.ContentStore
import org.jbake.template.ModelExtractor
import org.jbake.util.DataFileUtil

class DataExtractor : ModelExtractor<DataFileUtil?> {
    override fun get(db: ContentStore?, model: MutableMap<*, *>, key: String?): DataFileUtil {
        val config = model.get("config") as MutableMap<String?, Any?>
        val defaultDocType: String? = config.get(DATA_FILE_DOCTYPE.key.replace(".", "_")).toString()
        val dataUtil = DataFileUtil(db, defaultDocType)
        return dataUtil
    }
}

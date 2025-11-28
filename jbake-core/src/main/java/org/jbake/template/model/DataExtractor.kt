package org.jbake.template.model

import org.jbake.app.configuration.PropertyList.DATA_FILE_DOCTYPE
import org.jbake.template.TypedModelExtractor
import org.jbake.util.DataFileUtil

class DataExtractor : TypedModelExtractor<DataFileUtil> {

    @Suppress("DEPRECATION", "UNCHECKED_CAST")
    override fun extract(context: RenderContext, key: String): DataFileUtil {
        val model = context.toLegacyMap()
        val config = model["config"] as? Map<String, Any>
        val defaultDocType: String = config?.get(DATA_FILE_DOCTYPE.key.replace(".", "_"))?.toString() ?: ""
        return DataFileUtil(context.db, defaultDocType)
    }
}

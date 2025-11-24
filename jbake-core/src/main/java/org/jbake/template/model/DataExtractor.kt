package org.jbake.template.model

import org.jbake.template.TypedModelExtractor
import org.jbake.util.DataFileUtil
import org.jbake.app.configuration.PropertyList.DATA_FILE_DOCTYPE

class DataExtractor : TypedModelExtractor<DataFileUtil> {

    override fun extract(context: RenderContext, key: String): DataFileUtil {
        val model = context.toLegacyMap()
        val config = model["config"] as? Map<String, Any>
        val defaultDocType: String = config?.get(DATA_FILE_DOCTYPE.key.replace(".", "_"))?.toString() ?: ""
        return DataFileUtil(context.db, defaultDocType)
    }
}

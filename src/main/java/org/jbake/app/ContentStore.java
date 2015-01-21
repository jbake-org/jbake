/*
 * The MIT License
 *
 * Copyright 2015 jdlee.
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
 */
package org.jbake.app;

import com.orientechnologies.orient.core.command.OCommandRequest;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.query.OQuery;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import java.util.List;
import static org.jbake.app.DBUtil.updateSchema;
import org.jbake.model.DocumentTypes;

/**
 *
 * @author jdlee
 */
public class ContentStore {
    private ODatabaseDocumentTx db;
    
    public ContentStore(final String type, String name) {
        db = new ODatabaseDocumentTx(type + ":" + name);
        boolean exists = db.exists();
        if (!exists) {
            db.create();
        }
        db = ODatabaseDocumentPool.global().acquire(type + ":" + name, "admin", "admin");
        ODatabaseRecordThreadLocal.INSTANCE.set(db);
        if (!exists) {
            updateSchema();
        }
    }
    
    public  void updateSchema() {
        OSchema schema = db.getMetadata().getSchema();
        for (String docType : DocumentTypes.getDocumentTypes()) {
            if (schema.getClass(docType)==null) {
                createDocType(schema, docType);
            }
        }
        if (schema.getClass("Signatures")==null) {
            // create the sha1 signatures class
            OClass signatures = schema.createClass("Signatures");
            signatures.createProperty("key", OType.STRING).setNotNull(true);
            signatures.createProperty("sha1", OType.STRING).setNotNull(true);
        }
    }



    public void close() {
        db.close();
    }

    public void drop() {
        db.drop();
    }

    public <RET extends List<?>> RET query(OQuery<? extends Object> iCommand, Object... iArgs) {
        return db.query(iCommand, iArgs);
    }

    public long countClass(String iClassName) {
        return db.countClass(iClassName);
    }

    public <RET extends OCommandRequest> RET command(OCommandRequest iCommand) {
        return db.command(iCommand);
    }

    public <RET extends OCommandRequest> RET  command(OCommandSQL oCommandSQL) {
        return db.command(oCommandSQL);
    }
    
    private static void createDocType(final OSchema schema, final String doctype) {
        OClass page = schema.createClass(doctype);
        page.createProperty("sha1", OType.STRING).setNotNull(true);
        page.createProperty("sourceuri", OType.STRING).setNotNull(true);
        page.createProperty("rendered", OType.BOOLEAN).setNotNull(true);
        page.createProperty("cached", OType.BOOLEAN).setNotNull(true);

        // commented out because for some reason index seems to be written
        // after the database is closed to this triggers an exception
        //page.createIndex("uriIdx", OClass.INDEX_TYPE.UNIQUE, "uri");
        //page.createIndex("renderedIdx", OClass.INDEX_TYPE.NOTUNIQUE, "rendered");
    }    
}

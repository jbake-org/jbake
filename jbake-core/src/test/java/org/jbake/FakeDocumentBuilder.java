package org.jbake;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.jbake.app.Crawler;
import org.jbake.model.DocumentAttributes;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FakeDocumentBuilder {

    Map<String, Object> fileModel = new HashMap<String, Object>();
    String type;
    private boolean hasSourceUri = false;
    private boolean hasSha1 = false;
    private boolean hasDate = false;

    public FakeDocumentBuilder(String type) {
        this.type = type;
    }

    public FakeDocumentBuilder withName(String name) {
        fileModel.put(DocumentAttributes.NAME.toString(), name);
        return this;
    }

    public FakeDocumentBuilder withStatus(String status) {
        fileModel.put(DocumentAttributes.STATUS.toString(), status);
        return this;
    }

    public FakeDocumentBuilder withRandomSha1() throws NoSuchAlgorithmException {
        fileModel.put(DocumentAttributes.SHA1.toString(), getRandomSha1());
        hasSha1 = true;
        return this;
    }

    public FakeDocumentBuilder withDate(Date date) {
    	fileModel.put(Crawler.Attributes.DATE, date);
    	hasDate = true;
    	return this;
    }
    
    private FakeDocumentBuilder withCurrentDate() {
        fileModel.put(Crawler.Attributes.DATE, new Date() );
        return this;
    }

    private FakeDocumentBuilder withRandomSourceUri() throws NoSuchAlgorithmException {
        String path = "/tmp/" + getRandomSha1() + ".txt";
        fileModel.put(DocumentAttributes.SOURCE_URI.toString(), path);
        return this;
    }

    public FakeDocumentBuilder withCached(boolean cached) {
        fileModel.put(DocumentAttributes.CACHED.toString(), cached);
        return this;
    }

    public void build() {

        try {
            if ( ! hasSourceUri() ) {
                this.withRandomSourceUri();
            }
            if ( ! hasSha1() ) {
                this.withRandomSha1();
            }
            if ( ! hasDate() ) {
                this.withCurrentDate();
            }
            ODocument document = new ODocument(type).fromMap(fileModel);
            document.save();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private String getRandomSha1() throws NoSuchAlgorithmException {
        MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");
        Random random = new Random();
        int size = random.nextInt(1000) + 1000;
        byte[] content = new byte[size];

        random.nextBytes(content);
        return DatatypeConverter.printHexBinary(sha1Digest.digest(content));
    }

    private boolean hasDate() {
        return hasDate;
    }

    private boolean hasSha1() {
        return hasSha1;
    }

    private boolean hasSourceUri() {
        return hasSourceUri;
    }
}

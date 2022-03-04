package org.jbake;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.jbake.model.DocumentModel;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Random;

public class FakeDocumentBuilder {

    private DocumentModel fileModel = new DocumentModel();
    private String type;
    private boolean hasSourceUri = false;
    private boolean hasSha1 = false;
    private boolean hasDate = false;

    public FakeDocumentBuilder(String type) {
        this.type = type;
        fileModel.setType(type);
    }

    public FakeDocumentBuilder withStatus(String status) {
        fileModel.setStatus(status);
        return this;
    }

    public FakeDocumentBuilder withRandomSha1() throws NoSuchAlgorithmException {
        fileModel.setSha1(getRandomSha1());
        hasSha1 = true;
        return this;
    }

    public FakeDocumentBuilder withDate(Instant date) {
        fileModel.setDate(date);
        hasDate = true;
        return this;
    }

    private FakeDocumentBuilder withCurrentDate() {
        fileModel.setDate(Instant.now());
        return this;
    }

    private FakeDocumentBuilder withRandomSourceUri() throws NoSuchAlgorithmException {
        String path = "/tmp/" + getRandomSha1() + ".txt";
        fileModel.setSourceUri(path);
        return this;
    }

    public FakeDocumentBuilder withCached(boolean cached) {
        fileModel.setCached(cached);
        return this;
    }

    public void build() {

        try {
            if (!hasSourceUri()) {
                this.withRandomSourceUri();
            }
            if (!hasSha1()) {
                this.withRandomSha1();
            }
            if (!hasDate()) {
                this.withCurrentDate();
            }
            ODocument document = new ODocument("Documents").fromMap(fileModel);
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
        return new BigInteger(sha1Digest.digest(content)).toString(16);
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

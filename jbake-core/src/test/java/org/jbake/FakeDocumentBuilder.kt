package org.jbake

import com.orientechnologies.orient.core.record.impl.ODocument
import org.jbake.model.DocumentModel
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class FakeDocumentBuilder(private val type: String) {
    private val fileModel = DocumentModel()
    private val hasSourceUri = false
    private var hasSha1 = false
    private var hasDate = false

    init {
        fileModel.type = type
    }

    fun withStatus(status: String?): FakeDocumentBuilder {
        fileModel.status = status
        return this
    }

    @Throws(NoSuchAlgorithmException::class)
    fun withRandomSha1(): FakeDocumentBuilder {
        fileModel.sha1 = this.randomSha1
        hasSha1 = true
        return this
    }

    fun withDate(date: Date?): FakeDocumentBuilder {
        fileModel.date = date
        hasDate = true
        return this
    }

    private fun withCurrentDate(): FakeDocumentBuilder {
        fileModel.date = Date()
        return this
    }

    @Throws(NoSuchAlgorithmException::class)
    private fun withRandomSourceUri(): FakeDocumentBuilder {
        val path = "/tmp/" + this.randomSha1 + ".txt"
        fileModel.sourceUri = path
        return this
    }

    fun withCached(cached: Boolean): FakeDocumentBuilder {
        fileModel.cached = cached
        return this
    }

    fun build() {
        try {
            if (!hasSourceUri()) {
                this.withRandomSourceUri()
            }
            if (!hasSha1()) {
                this.withRandomSha1()
            }
            if (!hasDate()) {
                this.withCurrentDate()
            }
            val document = ODocument("Documents").fromMap(fileModel)
            document.save()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

    @get:Throws(NoSuchAlgorithmException::class)
    private val randomSha1: String
        get() {
            val sha1Digest = MessageDigest.getInstance("SHA-1")
            val random = Random()
            val size = random.nextInt(1000) + 1000
            val content = ByteArray(size)

            random.nextBytes(content)
            return BigInteger(sha1Digest.digest(content)).toString(16)
        }

    private fun hasDate(): Boolean {
        return hasDate
    }

    private fun hasSha1(): Boolean {
        return hasSha1
    }

    private fun hasSourceUri(): Boolean {
        return hasSourceUri
    }
}

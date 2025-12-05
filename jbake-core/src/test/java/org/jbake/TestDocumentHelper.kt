package org.jbake

import org.jbake.app.ContentStore
import org.jbake.model.DocumentModel
import java.math.BigInteger
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.util.*

fun ContentStore.addTestDocument(
    type: String,
    status: String = "published",
    date: OffsetDateTime? = OffsetDateTime.now(),
    cached: Boolean = true,
    sha1: String? = generateRandomSha1(),
    sourceUri: String? = "/tmp/${generateRandomSha1()}.txt",
    configure: (DocumentModel.() -> Unit)? = null
) {
    val document = DocumentModel().apply {
        this.type = type
        this.status = status
        this.date =  date
        this.cached = cached
        this.sha1 = sha1
        this.sourceUri = sourceUri
        configure?.invoke(this)
    }
    this.addDocument(document)
}

fun generateRandomSha1(): String {
    val sha1Digest = MessageDigest.getInstance("SHA-1")
    val random = Random()
    val size = random.nextInt(1000) + 1000
    val content = ByteArray(size)
    random.nextBytes(content)
    return BigInteger(sha1Digest.digest(content)).toString(16)
}


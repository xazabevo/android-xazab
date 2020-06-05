/**
 * Copyright (c) 2020-present, Dash Core Group
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package org.dashevo.dashpay

import org.bitcoinj.core.ECKey
import org.dashevo.dapiclient.model.DocumentQuery
import org.dashevo.dpp.document.Document
import org.dashevo.dpp.identity.Identity
import org.dashevo.platform.Platform

class Profiles(
    val platform: Platform
) {

    private val typeLocator: String = "dashpay.profile"

    fun create(
        displayName: String,
        publicMessage: String,
        avatarUrl: String?,
        identity: Identity,
        id: Int,
        signingKey: ECKey
    ) {
        val profileDocument = createProfileDocument(displayName, publicMessage, avatarUrl, identity)

        val profileStateTransition =
            platform.dpp.document.createStateTransition(listOf(profileDocument))
        profileStateTransition.sign(identity.getPublicKeyById(id)!!, signingKey.privateKeyAsHex)
        platform.client.applyStateTransition(profileStateTransition)
    }

    fun createProfileDocument(
        displayName: String,
        publicMessage: String,
        avatarUrl: String?,
        identity: Identity
    ) : Document {
        val avatarUrl = avatarUrl ?: "https://api.adorable.io/avatars/120/$displayName"
        return platform.documents.create(
            typeLocator, identity,
            mutableMapOf<String, Any?>(
                "publicMessage" to publicMessage,
                "displayName" to displayName,
                "avatarUrl" to avatarUrl
            )
        )
    }

    fun get(userId: String) : Document? {
        val query = DocumentQuery.Builder()
            .where("\$userId", "==", userId)
            .build()
        try {
            val documents = platform.documents.get(typeLocator, query)
            return if (documents.isNotEmpty()) documents[0] else null
        } catch (e : Exception) {
            throw e
        }
    }

}
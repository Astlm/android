package com.x8bit.bitwarden.ui.vault.feature.vault.util

import com.bitwarden.core.CipherRepromptType
import com.bitwarden.core.CipherType
import com.bitwarden.core.CipherView
import com.bitwarden.core.FieldType
import com.bitwarden.core.FieldView
import com.bitwarden.core.IdentityView
import com.bitwarden.core.LoginUriView
import com.bitwarden.core.LoginView
import com.bitwarden.core.PasswordHistoryView
import com.bitwarden.core.SecureNoteType
import com.bitwarden.core.SecureNoteView
import com.bitwarden.core.UriMatchType
import com.x8bit.bitwarden.ui.platform.base.util.asText
import com.x8bit.bitwarden.ui.vault.feature.additem.VaultAddItemState
import com.x8bit.bitwarden.ui.vault.model.VaultLinkedFieldType
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class VaultAddItemStateExtensionsTest {

    @AfterEach
    fun tearDown() {
        // Some individual tests call mockkStatic so we will make sure this is always undone.
        unmockkStatic(Instant::class)
    }

    @Test
    fun `toCipherView should transform Login ItemType to CipherView`() {
        mockkStatic(Instant::class)
        every { Instant.now() } returns Instant.MIN
        val loginItemType = VaultAddItemState.ViewState.Content(
            common = VaultAddItemState.ViewState.Content.Common(
                name = "mockName-1",
                folderName = "mockFolder-1".asText(),
                favorite = false,
                masterPasswordReprompt = false,
                notes = "mockNotes-1",
                ownership = "mockOwnership-1",
            ),
            type = VaultAddItemState.ViewState.Content.ItemType.Login(
                username = "mockUsername-1",
                password = "mockPassword-1",
                uri = "mockUri-1",
            ),
        )

        val result = loginItemType.toCipherView()

        assertEquals(
            CipherView(
                id = null,
                organizationId = null,
                folderId = null,
                collectionIds = emptyList(),
                key = null,
                name = "mockName-1",
                notes = "mockNotes-1",
                type = CipherType.LOGIN,
                login = LoginView(
                    username = "mockUsername-1",
                    password = "mockPassword-1",
                    passwordRevisionDate = null,
                    uris = listOf(
                        LoginUriView(
                            uri = "mockUri-1",
                            match = UriMatchType.DOMAIN,
                        ),
                    ),
                    totp = null,
                    autofillOnPageLoad = null,
                ),
                identity = null,
                card = null,
                secureNote = null,
                favorite = false,
                reprompt = CipherRepromptType.NONE,
                organizationUseTotp = false,
                edit = true,
                viewPassword = true,
                localData = null,
                attachments = null,
                fields = emptyList(),
                passwordHistory = null,
                creationDate = Instant.MIN,
                deletedDate = null,
                revisionDate = Instant.MIN,
            ),
            result,
        )
    }

    @Test
    fun `toCipherView should transform Login ItemType to CipherView with original cipher`() {
        val cipherView = DEFAULT_LOGIN_CIPHER_VIEW
        val viewState = VaultAddItemState.ViewState.Content(
            common = VaultAddItemState.ViewState.Content.Common(
                originalCipher = cipherView,
                name = "mockName-1",
                folderName = "mockFolder-1".asText(),
                favorite = true,
                masterPasswordReprompt = false,
                customFieldData = listOf(
                    VaultAddItemState.Custom.BooleanField("testId", "TestBoolean", false),
                    VaultAddItemState.Custom.TextField("testId", "TestText", "TestText"),
                    VaultAddItemState.Custom.HiddenField("testId", "TestHidden", "TestHidden"),
                    VaultAddItemState.Custom.LinkedField(
                        "testId",
                        "TestLinked",
                        VaultLinkedFieldType.USERNAME,
                    ),
                ),
                notes = "mockNotes-1",
                ownership = "mockOwnership-1",
            ),
            type = VaultAddItemState.ViewState.Content.ItemType.Login(
                username = "mockUsername-1",
                password = "mockPassword-1",
                uri = "mockUri-1",
            ),
        )

        val result = viewState.toCipherView()

        assertEquals(
            @Suppress("MaxLineLength")
            cipherView.copy(
                name = "mockName-1",
                notes = "mockNotes-1",
                type = CipherType.LOGIN,
                login = LoginView(
                    username = "mockUsername-1",
                    password = "mockPassword-1",
                    passwordRevisionDate = Instant.ofEpochSecond(1_000L),
                    uris = listOf(
                        LoginUriView(
                            uri = "mockUri-1",
                            match = UriMatchType.DOMAIN,
                        ),
                    ),
                    totp = "otpauth://totp/Example:alice@google.com?secret=JBSWY3DPEHPK3PXP&issuer=Example",
                    autofillOnPageLoad = false,
                ),
                favorite = true,
                reprompt = CipherRepromptType.NONE,
                fields = listOf(
                    FieldView(
                        name = "TestBoolean",
                        value = "false",
                        type = FieldType.BOOLEAN,
                        linkedId = null,
                    ),
                    FieldView(
                        name = "TestText",
                        value = "TestText",
                        type = FieldType.TEXT,
                        linkedId = null,
                    ),
                    FieldView(
                        name = "TestHidden",
                        value = "TestHidden",
                        type = FieldType.HIDDEN,
                        linkedId = null,
                    ),
                    FieldView(
                        name = "TestLinked",
                        value = null,
                        type = FieldType.LINKED,
                        linkedId = VaultLinkedFieldType.USERNAME.id,
                    ),
                ),
                passwordHistory = listOf(
                    PasswordHistoryView(
                        password = "old_password",
                        lastUsedDate = Instant.ofEpochSecond(1_000L),
                    ),
                ),
            ),
            result,
        )
    }

    @Test
    fun `toCipherView should transform SecureNotes ItemType to CipherView`() {
        mockkStatic(Instant::class)
        every { Instant.now() } returns Instant.MIN
        val viewState = VaultAddItemState.ViewState.Content(
            common = VaultAddItemState.ViewState.Content.Common(
                name = "mockName-1",
                folderName = "mockFolder-1".asText(),
                favorite = false,
                masterPasswordReprompt = false,
                notes = "mockNotes-1",
                ownership = "mockOwnership-1",
                customFieldData = listOf(
                    VaultAddItemState.Custom.BooleanField("testId", "TestBoolean", false),
                    VaultAddItemState.Custom.TextField("testId", "TestText", "TestText"),
                    VaultAddItemState.Custom.HiddenField("testId", "TestHidden", "TestHidden"),
                ),
            ),
            type = VaultAddItemState.ViewState.Content.ItemType.SecureNotes,
        )

        val result = viewState.toCipherView()

        assertEquals(
            CipherView(
                id = null,
                organizationId = null,
                folderId = null,
                collectionIds = emptyList(),
                key = null,
                name = "mockName-1",
                notes = "mockNotes-1",
                type = CipherType.SECURE_NOTE,
                login = null,
                identity = null,
                card = null,
                secureNote = SecureNoteView(SecureNoteType.GENERIC),
                favorite = false,
                reprompt = CipherRepromptType.NONE,
                organizationUseTotp = false,
                edit = true,
                viewPassword = true,
                localData = null,
                attachments = null,
                fields = listOf(
                    FieldView(
                        name = "TestBoolean",
                        value = "false",
                        type = FieldType.BOOLEAN,
                        linkedId = null,
                    ),
                    FieldView(
                        name = "TestText",
                        value = "TestText",
                        type = FieldType.TEXT,
                        linkedId = null,
                    ),
                    FieldView(
                        name = "TestHidden",
                        value = "TestHidden",
                        type = FieldType.HIDDEN,
                        linkedId = null,
                    ),
                ),
                passwordHistory = null,
                creationDate = Instant.MIN,
                deletedDate = null,
                revisionDate = Instant.MIN,
            ),
            result,
        )
    }

    @Test
    fun `toCipherView should transform SecureNotes ItemType to CipherView with original cipher`() {
        val cipherView = DEFAULT_SECURE_NOTES_CIPHER_VIEW
        val viewState = VaultAddItemState.ViewState.Content(
            common = VaultAddItemState.ViewState.Content.Common(
                originalCipher = cipherView,
                name = "mockName-1",
                folderName = "mockFolder-1".asText(),
                favorite = false,
                masterPasswordReprompt = true,
                notes = "mockNotes-1",
                ownership = "mockOwnership-1",
                customFieldData = emptyList(),
            ),
            type = VaultAddItemState.ViewState.Content.ItemType.SecureNotes,
        )

        val result = viewState.toCipherView()

        assertEquals(
            cipherView.copy(
                name = "mockName-1",
                notes = "mockNotes-1",
                type = CipherType.SECURE_NOTE,
                secureNote = SecureNoteView(SecureNoteType.GENERIC),
                reprompt = CipherRepromptType.PASSWORD,
                fields = emptyList(),
            ),
            result,
        )
    }

    @Test
    fun `toCipherView should transform Identity ItemType to CipherView`() {
        mockkStatic(Instant::class)
        every { Instant.now() } returns Instant.MIN
        val viewState = VaultAddItemState.ViewState.Content(
            common = VaultAddItemState.ViewState.Content.Common(
                name = "mockName-1",
                folderName = "mockFolder-1".asText(),
                favorite = false,
                masterPasswordReprompt = false,
                notes = "mockNotes-1",
                ownership = "mockOwnership-1",
            ),
            type = VaultAddItemState.ViewState.Content.ItemType.Identity(
                selectedTitle = VaultAddItemState.ViewState.Content.ItemType.Identity.Title.MR,
                firstName = "mockFirstName",
                lastName = "mockLastName",
                middleName = "mockMiddleName",
                address1 = "mockAddress1",
                address2 = "mockAddress2",
                address3 = "mockAddress3",
                city = "mockCity",
                state = "mockState",
                zip = "mockPostalCode",
                country = "mockCountry",
                company = "mockCompany",
                email = "mockEmail",
                phone = "mockPhone",
                ssn = "mockSsn",
                username = "MockUsername",
                passportNumber = "mockPassportNumber",
                licenseNumber = "mockLicenseNumber",
            ),
        )

        val result = viewState.toCipherView()

        assertEquals(
            CipherView(
                id = null,
                organizationId = null,
                folderId = null,
                collectionIds = emptyList(),
                key = null,
                name = "mockName-1",
                notes = "mockNotes-1",
                type = CipherType.IDENTITY,
                login = null,
                identity = IdentityView(
                    title = "MR",
                    firstName = "mockFirstName",
                    lastName = "mockLastName",
                    middleName = "mockMiddleName",
                    address1 = "mockAddress1",
                    address2 = "mockAddress2",
                    address3 = "mockAddress3",
                    city = "mockCity",
                    state = "mockState",
                    postalCode = "mockPostalCode",
                    country = "mockCountry",
                    company = "mockCompany",
                    email = "mockEmail",
                    phone = "mockPhone",
                    ssn = "mockSsn",
                    username = "MockUsername",
                    passportNumber = "mockPassportNumber",
                    licenseNumber = "mockLicenseNumber",
                ),
                card = null,
                secureNote = null,
                favorite = false,
                reprompt = CipherRepromptType.NONE,
                organizationUseTotp = false,
                edit = true,
                viewPassword = true,
                localData = null,
                attachments = null,
                fields = emptyList(),
                passwordHistory = null,
                creationDate = Instant.MIN,
                deletedDate = null,
                revisionDate = Instant.MIN,
            ),
            result,
        )
    }

    @Test
    fun `toCipherView should transform Identity ItemType to CipherView with original cipher`() {
        val cipherView = DEFAULT_IDENTITY_CIPHER_VIEW
        val viewState = VaultAddItemState.ViewState.Content(
            common = VaultAddItemState.ViewState.Content.Common(
                originalCipher = cipherView,
                name = "mockName-1",
                folderName = "mockFolder-1".asText(),
                favorite = true,
                masterPasswordReprompt = false,
                customFieldData = listOf(
                    VaultAddItemState.Custom.BooleanField("testId", "TestBoolean", false),
                    VaultAddItemState.Custom.TextField("testId", "TestText", "TestText"),
                    VaultAddItemState.Custom.HiddenField("testId", "TestHidden", "TestHidden"),
                    VaultAddItemState.Custom.LinkedField(
                        "testId",
                        "TestLinked",
                        VaultLinkedFieldType.USERNAME,
                    ),
                ),
                notes = "mockNotes-1",
                ownership = "mockOwnership-1",
            ),
            type = VaultAddItemState.ViewState.Content.ItemType.Identity(
                selectedTitle = VaultAddItemState.ViewState.Content.ItemType.Identity.Title.MR,
                firstName = "mockFirstName",
                lastName = "mockLastName",
                middleName = "mockMiddleName",
                address1 = "mockAddress1",
                address2 = "mockAddress2",
                address3 = "mockAddress3",
                city = "mockCity",
                state = "mockState",
                zip = "mockPostalCode",
                country = "mockCountry",
                company = "mockCompany",
                email = "mockEmail",
                phone = "mockPhone",
                ssn = "mockSsn",
                username = "MockUsername",
                passportNumber = "mockPassportNumber",
                licenseNumber = "mockLicenseNumber",
            ),
        )

        val result = viewState.toCipherView()

        assertEquals(
            @Suppress("MaxLineLength")
            cipherView.copy(
                name = "mockName-1",
                notes = "mockNotes-1",
                type = CipherType.IDENTITY,
                identity = IdentityView(
                    title = "MR",
                    firstName = "mockFirstName",
                    lastName = "mockLastName",
                    middleName = "mockMiddleName",
                    address1 = "mockAddress1",
                    address2 = "mockAddress2",
                    address3 = "mockAddress3",
                    city = "mockCity",
                    state = "mockState",
                    postalCode = "mockPostalCode",
                    country = "mockCountry",
                    company = "mockCompany",
                    email = "mockEmail",
                    phone = "mockPhone",
                    ssn = "mockSsn",
                    username = "MockUsername",
                    passportNumber = "mockPassportNumber",
                    licenseNumber = "mockLicenseNumber",
                ),
                favorite = true,
                reprompt = CipherRepromptType.NONE,
                fields = listOf(
                    FieldView(
                        name = "TestBoolean",
                        value = "false",
                        type = FieldType.BOOLEAN,
                        linkedId = null,
                    ),
                    FieldView(
                        name = "TestText",
                        value = "TestText",
                        type = FieldType.TEXT,
                        linkedId = null,
                    ),
                    FieldView(
                        name = "TestHidden",
                        value = "TestHidden",
                        type = FieldType.HIDDEN,
                        linkedId = null,
                    ),
                    FieldView(
                        name = "TestLinked",
                        value = null,
                        type = FieldType.LINKED,
                        linkedId = VaultLinkedFieldType.USERNAME.id,
                    ),
                ),
                passwordHistory = listOf(
                    PasswordHistoryView(
                        password = "old_password",
                        lastUsedDate = Instant.ofEpochSecond(1_000L),
                    ),
                ),
            ),
            result,
        )
    }
}

private val DEFAULT_BASE_CIPHER_VIEW: CipherView = CipherView(
    id = "id1234",
    organizationId = null,
    folderId = null,
    collectionIds = emptyList(),
    key = null,
    name = "cipher",
    notes = "Lots of notes",
    type = CipherType.LOGIN,
    login = null,
    identity = null,
    card = null,
    secureNote = null,
    favorite = false,
    reprompt = CipherRepromptType.PASSWORD,
    organizationUseTotp = false,
    edit = false,
    viewPassword = false,
    localData = null,
    attachments = null,
    fields = listOf(
        FieldView(
            name = "text",
            value = "value",
            type = FieldType.TEXT,
            linkedId = null,
        ),
        FieldView(
            name = "hidden",
            value = "value",
            type = FieldType.HIDDEN,
            linkedId = null,
        ),
        FieldView(
            name = "boolean",
            value = "true",
            type = FieldType.BOOLEAN,
            linkedId = null,
        ),
        FieldView(
            name = "linked username",
            value = null,
            type = FieldType.LINKED,
            linkedId = 100U,
        ),
        FieldView(
            name = "linked password",
            value = null,
            type = FieldType.LINKED,
            linkedId = 101U,
        ),
    ),
    passwordHistory = listOf(
        PasswordHistoryView(
            password = "old_password",
            lastUsedDate = Instant.ofEpochSecond(1_000L),
        ),
    ),
    creationDate = Instant.ofEpochSecond(1_000L),
    deletedDate = null,
    revisionDate = Instant.ofEpochSecond(1_000L),
)

private val DEFAULT_LOGIN_CIPHER_VIEW: CipherView = DEFAULT_BASE_CIPHER_VIEW.copy(
    type = CipherType.LOGIN,
    login = LoginView(
        username = "username",
        password = "password",
        passwordRevisionDate = Instant.ofEpochSecond(1_000L),
        uris = listOf(
            LoginUriView(
                uri = "www.example.com",
                match = null,
            ),
        ),
        totp = "otpauth://totp/Example:alice@google.com?secret=JBSWY3DPEHPK3PXP&issuer=Example",
        autofillOnPageLoad = false,
    ),
)

private val DEFAULT_SECURE_NOTES_CIPHER_VIEW: CipherView = DEFAULT_BASE_CIPHER_VIEW.copy(
    type = CipherType.SECURE_NOTE,
    fields = listOf(
        FieldView(
            name = "text",
            value = "value",
            type = FieldType.TEXT,
            linkedId = null,
        ),
        FieldView(
            name = "hidden",
            value = "value",
            type = FieldType.HIDDEN,
            linkedId = null,
        ),
        FieldView(
            name = "boolean",
            value = "true",
            type = FieldType.BOOLEAN,
            linkedId = null,
        ),
    ),
    secureNote = SecureNoteView(type = SecureNoteType.GENERIC),
)

private val DEFAULT_IDENTITY_CIPHER_VIEW: CipherView = DEFAULT_BASE_CIPHER_VIEW.copy(
    type = CipherType.IDENTITY,
    identity = IdentityView(
        title = "MR",
        firstName = "mockFirstName",
        lastName = "mockLastName",
        middleName = "mockMiddleName",
        address1 = "mockAddress1",
        address2 = "mockAddress2",
        address3 = "mockAddress3",
        city = "mockCity",
        state = "mockState",
        postalCode = "mockPostalCode",
        country = "mockCountry",
        company = "mockCompany",
        email = "mockEmail",
        phone = "mockPhone",
        ssn = "mockSsn",
        username = "MockUsername",
        passportNumber = "mockPassportNumber",
        licenseNumber = "mockLicenseNumber",
    ),
)
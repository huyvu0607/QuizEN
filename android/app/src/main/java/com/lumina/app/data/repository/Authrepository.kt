package com.lumina.app.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Repository chịu trách nhiệm xác thực qua Firebase Auth.
 * Tách riêng khỏi UserRepository (Room) vì đây là nguồn dữ liệu khác (remote auth),
 * UserRepository chỉ lo việc lưu/đọc profile User ở local (Room).
 */
class AuthRepository(private val context: Context) {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    /** Đăng ký tài khoản mới bằng email/mật khẩu. */
    suspend fun registerWithEmail(email: String, password: String): FirebaseUser {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        return result.user ?: throw IllegalStateException("Đăng ký thất bại, vui lòng thử lại")
    }

    /** Đăng nhập bằng email/mật khẩu. */
    suspend fun loginWithEmail(email: String, password: String): FirebaseUser {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: throw IllegalStateException("Đăng nhập thất bại, vui lòng thử lại")
    }

    /**
     * Đăng nhập/Đăng ký bằng Google thông qua Credential Manager.
     *
     * @param activity Context của Activity để hiển thị trình chọn tài khoản.
     * @param webClientId Web Client ID lấy từ google-services.json.
     */
    suspend fun signInWithGoogle(activity: android.app.Activity, webClientId: String): FirebaseUser {
        val credentialManager = CredentialManager.create(activity)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(activity, request)
        val credential = result.credential

        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw IllegalStateException("Loại thông tin đăng nhập không hợp lệ")
        }

        val googleIdTokenCredential = try {
            GoogleIdTokenCredential.createFrom(credential.data)
        } catch (e: GoogleIdTokenParsingException) {
            throw IllegalStateException("Không thể đọc thông tin tài khoản Google")
        }

        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
        return authResult.user ?: throw IllegalStateException("Đăng nhập Google thất bại")
    }

    fun logout() {
        firebaseAuth.signOut()
    }
}
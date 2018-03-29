package dxmnd.com.firebaseapplication.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GithubAuthProvider
import dxmnd.com.firebaseapplication.R
import dxmnd.com.firebaseapplication.utils.*
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.*
import java.io.IOException
import java.util.*


class LoginActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance()

//        ui()

        btn_github.setOnClickListener { _ -> githubAccess() }
    }

    private fun ui() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                AuthUI.IdpConfig.GoogleBuilder().build()
                        )).build()
                , SIGN_IN
        )
    }

    private fun githubAccess() {

        val httpUrl = HttpUrl.Builder()
                .scheme("https")
                .host("github.com")
                .addPathSegment("login")
                .addPathSegment("oauth")
                .addPathSegment("authorize")
                .addQueryParameter("client_id", GITHUB_CLIENT_ID)
                .addQueryParameter("redirect_uri", GITHUB_REDIRECT_URL)
                .addQueryParameter("state", "1")
                .addQueryParameter("scope", "user:email")
                .build()

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(httpUrl.toString()))
        startActivity(intent)

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            val code = intent.getStringExtra("code")
            val state = intent.getStringExtra("state")

            sendPost(code, state)
        }
    }

    private fun sendPost(code: String?, state: String?) {
        val okHttpClient = OkHttpClient()

        val form: FormBody = FormBody.Builder()
                .add("client_id", GITHUB_CLIENT_ID)
                .add("client_secret", GITHUB_SECRET_KEY)
                .add("code", code)
                .add("state", state)
                .add("redirect_uri", GITHUB_REDIRECT_URL)
                .build()

        val request: Request = Request.Builder()
                .url("https://github.com/login/oauth/access_token")
                .post(form)
                .build()


        okHttpClient.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                log(e)
                // onFailure
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                val responseBody = response.body()!!.string()
                val splitBody = responseBody.split("=")
                if (splitBody[0].equals("access_token", ignoreCase = true)) {
                    val token = splitBody[1].split("&")
                    signInWithToken(token[0])
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            SIGN_IN -> {
                if (data != null) {
                    val code = data.getStringExtra("code")
                    val state = data.getStringExtra("state")

                    sendPost(code, state)
                }
            }
        }
    }


    private fun signInWithToken(token: String) {
        val credential: AuthCredential = GithubAuthProvider.getCredential(token)
        mAuth.let {
            it?.signInWithCredential(credential)
                    ?.addOnCompleteListener(this, { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            finish()
                        }
                    })
                    ?.addOnFailureListener(this, { _ ->
                        log(it)
                    })
        }
    }

    override fun onStart() {
        super.onStart()
        mAuth.let {
            val currentUser = it?.currentUser
            if(currentUser?.uid != null) {
                finish()
            }
        }
    }
}

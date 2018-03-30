package dxmnd.com.firebaseapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import dxmnd.com.firebaseapplication.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException


class MainActivity : BaseActivity() {


    private var mGoogleSignInClient: GoogleSignInClient? = null

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        mAuth?.let {
            it.signOut()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(GOOGLE_CLIENT_TOKEN)
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        btn_sign_in_google.setOnClickListener { _ ->
            val intent = mGoogleSignInClient?.signInIntent
            startActivityForResult(intent, GOOGLE_SIGN_IN)
        }

        btn_sign_in_github.setOnClickListener { _ ->
            githubAccess()
        }

    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        showProgressDialog()
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth?.let {
            it.signInWithCredential(credential)
                    .addOnCompleteListener(this, { task ->
                        if (task.isSuccessful) {
                            startHome()
                        } else {
                            // fail
                        }
                        hideProgressDialog()
                    })
        }
    }

    private fun githubAccess() {
        showProgressDialog()
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

    private fun sendPost(code: String, state: String) {
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

        hideProgressDialog()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            GOOGLE_SIGN_IN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    log("Google sign in failed $e")
                }
            }
        }
    }


    private fun signInWithToken(token: String) {
        val credential: AuthCredential = GithubAuthProvider.getCredential(token)
        mAuth?.let {
            it.signInWithCredential(credential)
                    .addOnCompleteListener(this, { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            startHome()
                        }
                    })
                    .addOnFailureListener(this, { _ ->
                        log("fail")
                    })
        }
    }

    private fun startHome() {
        startActivity(Intent(this, HomeActivity::class.java))
    }

}

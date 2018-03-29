package dxmnd.com.firebaseapplication.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import dxmnd.com.firebaseapplication.utils.GITHUB_REDIRECT_URL

class RedirectView : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent.data

        if (uri.toString().startsWith(GITHUB_REDIRECT_URL)) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("code", uri.getQueryParameter("code"))
            intent.putExtra("state", uri.getQueryParameter("state"))
            startActivity(intent)
            finish()
        }
    }
}
package dxmnd.com.firebaseapplication

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import dxmnd.com.firebaseapplication.login.LoginActivity
import dxmnd.com.firebaseapplication.utils.log

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startActivity(Intent(this, LoginActivity::class.java))

        val auth = FirebaseAuth.getInstance()

        log("" + auth.currentUser?.email)
    }
}

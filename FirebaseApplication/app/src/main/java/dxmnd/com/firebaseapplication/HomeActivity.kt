package dxmnd.com.firebaseapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import dxmnd.com.firebaseapplication.utils.log

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val mAuth = FirebaseAuth.getInstance()
        log("who : " + mAuth.currentUser?.email)
        Toast.makeText(this,"who : " + mAuth.currentUser?.email,Toast.LENGTH_SHORT).show()
    }
}

package dxmnd.com.firebaseapplication.utils

import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

/**
 * Created by HunJin on 2018-03-29.
 */

fun log(any: Any) {
    Logger.addLogAdapter(AndroidLogAdapter())

    when(any) {
        is Throwable -> Logger.e(any.message)
        else -> Logger.d(any.toString())
    }

}
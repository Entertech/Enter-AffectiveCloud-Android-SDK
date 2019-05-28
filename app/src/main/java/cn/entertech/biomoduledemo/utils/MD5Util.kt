package cn.entertech.biomoduledemo.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


fun MD5Encode(text:String):String{
    var re_md5 = String()
    try {
        val md = MessageDigest.getInstance("MD5")
        md.update(text.toByteArray())
        val b = md.digest()

        var i: Int

        val buf = StringBuffer("")
        for (offset in b.indices) {
            i = b[offset].toInt()
            if (i < 0)
                i += 256
            if (i < 16)
                buf.append("0")
            buf.append(Integer.toHexString(i))
        }

        re_md5 = buf.toString()

    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }

    return re_md5
}
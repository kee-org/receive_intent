package com.bhikadia.receive_intent

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest


fun jsonToBundle(json: JSONObject): Bundle {
    val bundle = Bundle()
    json.keys().forEach {
        val k = it
        val v = json.get(k)
        when (v) {
            is Byte -> bundle.putByte(k, v)
            is ByteArray -> bundle.putByteArray(k, v)
            is Char -> bundle.putChar(k, v)
            is CharArray -> bundle.putCharArray(k, v)
            is CharSequence -> bundle.putCharSequence(k, v)
            is Float -> bundle.putFloat(k, v)
            is FloatArray -> bundle.putFloatArray(k, v)
            is Parcelable -> bundle.putParcelable(k, v)
            is Short -> bundle.putShort(k, v)
            is ShortArray -> bundle.putShortArray(k, v)
            else -> throw IllegalArgumentException("$v is of a type that is not currently supported")
        }
    }
    return bundle;
}

fun jsonToIntent(json: JSONObject): Intent = Intent().apply {
    putExtras(jsonToBundle(json))
}


fun bundleToJSON(bundle: Bundle): JSONObject {
    val json = JSONObject()
    val ks = bundle.keySet()
    val iterator: Iterator<String> = ks.iterator()
    while (iterator.hasNext()) {
        val key = iterator.next()
        try {
            json.put(key, wrap(bundle.get(key)))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
    return json
}

fun wrap(o: Any?): Any? {
    if (o == null) {
        return JSONObject.NULL
    }
    if (o is JSONArray || o is JSONObject) {
        return o
    }
    if (o == JSONObject.NULL) {
        return o
    }
    try {
        if (o is Collection<*>) {
            Log.e("ReceiveIntentPlugin", "$o is Collection<*>")
            return JSONArray(o as Collection<*>?)
        } else if (o.javaClass.isArray) {
            Log.e("ReceiveIntentPlugin", "$o is isArray")
            return toJSONArray(o)
        }
        if (o is Map<*, *>) {
            Log.e("ReceiveIntentPlugin", "$o is Map<*, *>")
            return JSONObject(o as Map<*, *>?)
        }
        if (o is Boolean ||
                o is Byte ||
                o is Char ||
                o is Double ||
                o is Float ||
                o is Int ||
                o is Long ||
                o is Short ||
                o is String) {
            return o
        }
        if (o.javaClass.getPackage().name.startsWith("java.")) {
            return o.toString()
        }
    } catch (e: Exception) {
        Log.e("ReceiveIntentPlugin", e.message, e)
        e.printStackTrace()
    }
    return null
}

@Throws(JSONException::class)
fun toJSONArray(array: Any): JSONArray? {
    val result = JSONArray()
    if (!array.javaClass.isArray) {
        throw JSONException("Not a primitive array: " + array.javaClass)
    }

    when (array) {
        is List<*> -> {
            array.forEach { result.put(wrap(it)) }
        }
        is Array<*> -> {
            array.forEach { result.put(wrap(it)) }
        }
    }

    return result
}

fun getApplicationSignature(context: Context, packageName: String): List<String> {
    val signatureList: List<String>
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // New signature
            val sig = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo
            signatureList = if (sig.hasMultipleSigners()) {
                // Send all with apkContentsSigners
                sig.apkContentsSigners.map {
                    val digest = MessageDigest.getInstance("SHA")
                    digest.update(it.toByteArray())
                    bytesToHex(digest.digest())
                }
            } else {
                // Send one with signingCertificateHistory
                sig.signingCertificateHistory.map {
                    val digest = MessageDigest.getInstance("SHA")
                    digest.update(it.toByteArray())
                    bytesToHex(digest.digest())
                }
            }
        } else {
            val sig = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
            signatureList = sig.map {
                val digest = MessageDigest.getInstance("SHA")
                digest.update(it.toByteArray())
                bytesToHex(digest.digest())
            }
        }

        return signatureList
    } catch (e: Exception) {
        // Handle error
    }
    return emptyList()
}

fun bytesToHex(bytes: ByteArray): String {
    val hexArray = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
    val hexChars = CharArray(bytes.size * 2)
    var v: Int
    for (j in bytes.indices) {
        v = bytes[j].toInt() and 0xFF
        hexChars[j * 2] = hexArray[v.ushr(4)]
        hexChars[j * 2 + 1] = hexArray[v and 0x0F]
    }
    return String(hexChars)
}

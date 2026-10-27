package pe.upeu.agrosierra.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AlmacenSeguroAndroid(context: Context) : AlmacenSeguro {
    private val preferencias = context.getSharedPreferences("agrocampo_seguro", Context.MODE_PRIVATE)

    override fun guardar(clave: String, valor: String) {
        val cipher = Cipher.getInstance(TRANSFORMACION)
        cipher.init(Cipher.ENCRYPT_MODE, obtenerClave())
        val cifrado = cipher.doFinal(valor.toByteArray(StandardCharsets.UTF_8))
        preferencias.edit().putString(clave, Base64.encodeToString(cipher.iv + cifrado, Base64.NO_WRAP)).apply()
    }

    override fun leer(clave: String): String? = preferencias.getString(clave, null)?.let { raw ->
        runCatching {
            val datos = Base64.decode(raw, Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMACION)
            cipher.init(Cipher.DECRYPT_MODE, obtenerClave(), GCMParameterSpec(128, datos.copyOfRange(0, 12)))
            String(cipher.doFinal(datos.copyOfRange(12, datos.size)), StandardCharsets.UTF_8)
        }.getOrNull()
    }

    override fun eliminar(clave: String) { preferencias.edit().remove(clave).apply() }

    private fun obtenerClave(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
            generateKey()
        }
    }

    private companion object { const val ALIAS = "agrocampo_sesion"; const val TRANSFORMACION = "AES/GCM/NoPadding" }
}

actual fun tokenSigueVigente(token: String): Boolean = runCatching {
    val partes = token.split('.')
    val payload = String(Base64.decode(partes[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
    JSONObject(payload).getLong("exp") * 1000L > System.currentTimeMillis()
}.getOrDefault(false)

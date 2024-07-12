package com.lucendar.common.serv.utils

import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.{SSLContext, TrustManager, X509TrustManager}

object SslUtils {

  final def trustAllTrustManager: Array[TrustManager] = Array(
    new X509TrustManager {
      override def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}

      override def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}

      override def getAcceptedIssuers: Array[X509Certificate] = null
    }
  )

  final def trustAllSslContext: SSLContext = {
    val r = SSLContext.getInstance("TLS")
    r.init(null, trustAllTrustManager, new SecureRandom())
    r
  }
}

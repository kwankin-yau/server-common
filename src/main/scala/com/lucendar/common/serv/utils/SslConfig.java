package com.lucendar.common.serv.utils;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.netty.util.NettySslUtils;
import nl.altindag.ssl.pem.util.PemUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Spring boot style SSL config
 */
public interface SslConfig {

    String getKeyStoreType();

    String getKeyStore();

    String getKeyStorePassword();

    boolean isEnabled();

    String getCertificate();

    String getCertificatePrivateKey();

    String getTrustedCertificate();

    default boolean isValid() {
        if (!isEnabled())
            return false;

        String certificate = getCertificate();
        if (certificate != null) {
            return !certificate.isEmpty() && getCertificatePrivateKey() != null && !getCertificatePrivateKey().isEmpty();
        } else {
            return getKeyStoreType() != null && !getKeyStoreType().isEmpty()
                    && getKeyStore() != null && !getKeyStore().isEmpty();
        }
    }


    /**
     * Build netty SslContext, return null if SSL is disabled.
     * @return netty SslContext
     */
    @Nullable
    default SslContext buildServerSslContext() {
        if (isEnabled()) {
            try {
                String certificate = getCertificate();
                if (certificate != null) {
                    X509ExtendedKeyManager keyManager = PemUtils.loadIdentityMaterial(
                            Path.of(certificate),
                            Path.of(getCertificate()));
                    SSLFactory.Builder builder = SSLFactory.builder()
                            .withIdentityMaterial(keyManager);

                    if (getTrustedCertificate() != null) {
                        X509ExtendedTrustManager trustManager = PemUtils.loadTrustMaterial(getTrustedCertificate());
                        builder.withTrustMaterial(trustManager);
                    }

                    SSLFactory sslFactory = builder.build();

                    return NettySslUtils.forServer(sslFactory).build();
                } else {
                    KeyStore keyStore1 = KeyStore.getInstance(getKeyStoreType());
                    char[] pwd = getKeyStorePassword().toCharArray();
                    try (FileInputStream res = new FileInputStream(getKeyStore())) {
                        keyStore1.load(res, pwd);
                        KeyManagerFactory factory = KeyManagerFactory.getInstance("SunX509");
                        factory.init(keyStore1, pwd);
                        return SslContextBuilder.forServer(factory).build();
                    }
                }
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException |
                     UnrecoverableKeyException e) {
                throw new RuntimeException(e);
            }
        } else
            return null;
    }


}

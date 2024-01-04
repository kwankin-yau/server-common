package com.lucendar.common.serv.utils;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
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

//    String getKeyAlias();

    boolean isEnabled();

    default boolean isValid() {
        return getKeyStoreType() != null && !getKeyStoreType().isEmpty() && getKeyStore() != null && !getKeyStore().isEmpty() && isEnabled();
    }


    /**
     * Build netty SslContext, return null if SSL is disabled.
     * @return netty SslContext
     */
    default SslContext buildServerSslContext() {
        if (isEnabled()) {
            try {
                KeyStore keyStore1 = KeyStore.getInstance(getKeyStoreType());
                char[] pwd = getKeyStorePassword().toCharArray();
                try (FileInputStream res = new FileInputStream(getKeyStore())) {
                    keyStore1.load(res, pwd);
                    KeyManagerFactory factory = KeyManagerFactory.getInstance("SunX509");
                    factory.init(keyStore1, pwd);
                    return SslContextBuilder.forServer(factory).build();
                } catch (NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableKeyException e) {
                    throw new RuntimeException(e);
                }
            } catch (KeyStoreException e) {
                throw new RuntimeException(e);
            }
        } else
            return null;
    }


}

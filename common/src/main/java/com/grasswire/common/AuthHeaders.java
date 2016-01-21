package com.grasswire.common;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Formatter;

public class AuthHeaders {

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final String TAG = AuthHeaders.class.getSimpleName();

    public String uuid;
    public Long timestamp;
    public String digest;
    public String username;

    public AuthHeaders(String uuid, Long timestamp, String digest, String username) {

        this.uuid = uuid;
        this.timestamp = timestamp;
        this.digest = digest;
        this.username = username;
    }

    @Override
    public String toString() {
        return "timestamp: " + timestamp + " uuid: " + uuid + " username: " + username + " digest: " + digest;
    }

    public static AuthHeaders get(String secret, String username) {

        long timestamp = DateTime.now(DateTimeZone.UTC).getMillis() / 1000;
        String digest = null;
        try {
            digest = calculateRFC2104HMAC(String.valueOf(timestamp) + "_" + secret, secret);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return new AuthHeaders(secret, timestamp, digest, username);

    }

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();

        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return formatter.toString();
    }

    public static String calculateRFC2104HMAC(String data, String key)
            throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        return toHexString(mac.doFinal(data.getBytes()));
    }
}


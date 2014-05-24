/*******************************************************************************
 * Copyright 2011, 2012, 2013 fanfou.com, Xiaoke, Zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/**
 * 
 */
package com.fanfou.app.opensource.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.text.TextUtils;

import com.fanfou.app.opensource.util.support.Base64;

/**
 * @author mcxiaoke
 * @version 1.0 2013.03.16
 * 
 */
public final class CryptoHelper {
    public static class AES {
        static final int ITERATION_COUNT_DEFAULT = 100;
        static final int KEY_SIZE_DEFAULT = 256;
        static final int SALT_SIZE_DEFAULT = 8;
        static final int IV_SIZE_DEFAULT = 16;

        public static byte[] decrypt(final byte[] data) {
            return AES.decrypt(data, AES.getSimplePassword(),
                    AES.getSimpleSalt(), AES.getSimpleIV(),
                    AES.KEY_SIZE_DEFAULT, AES.ITERATION_COUNT_DEFAULT);
        }

        /**
         * AES decrypt function
         * 
         * @param encrypted
         * @param key
         *            16, 24, 32 bytes available
         * @param iv
         *            initial vector (16 bytes) - if null: ECB mode, otherwise:
         *            CBC mode
         * @return
         */
        public static byte[] decrypt(final byte[] encrypted, final byte[] key,
                final byte[] iv) {
            if ((key == null)
                    || ((key.length != 16) && (key.length != 24) && (key.length != 32))) {
                return null;
            }
            if ((iv != null) && (iv.length != 16)) {
                return null;
            }

            try {
                SecretKeySpec keySpec = null;
                Cipher cipher = null;
                if (iv != null) {
                    keySpec = new SecretKeySpec(key, "AES/CBC/PKCS7Padding");// AES/ECB/PKCS5Padding
                    cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                    cipher.init(Cipher.DECRYPT_MODE, keySpec,
                            new IvParameterSpec(iv));
                } else // if(iv == null)
                {
                    keySpec = new SecretKeySpec(key, "AES/ECB/PKCS7Padding");
                    cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
                    cipher.init(Cipher.DECRYPT_MODE, keySpec);
                }

                return cipher.doFinal(encrypted);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public static byte[] decrypt(final byte[] data, final String password) {
            return AES.decrypt(data, password, AES.getSimpleSalt(),
                    AES.getSimpleIV(), AES.KEY_SIZE_DEFAULT,
                    AES.ITERATION_COUNT_DEFAULT);
        }

        public static byte[] decrypt(final byte[] data, final String password,
                final byte[] salt) {
            return AES.decrypt(data, password, salt, AES.getSimpleIV(),
                    AES.KEY_SIZE_DEFAULT, AES.ITERATION_COUNT_DEFAULT);
        }

        public static byte[] decrypt(final byte[] data, final String password,
                final byte[] salt, final byte[] iv) {
            return AES.decrypt(data, password, salt, iv, AES.KEY_SIZE_DEFAULT,
                    AES.ITERATION_COUNT_DEFAULT);
        }

        public static byte[] decrypt(final byte[] data, final String password,
                final byte[] salt, final byte[] iv, final int keySize) {
            return AES.decrypt(data, password, salt, iv, keySize,
                    AES.ITERATION_COUNT_DEFAULT);
        }

        public static byte[] decrypt(final byte[] data, final String password,
                final byte[] salt, final byte[] iv, final int keySize,
                final int iterationCount) {
            return AES.process(data, Cipher.DECRYPT_MODE, password, salt, iv,
                    keySize, iterationCount);
        }

        public static String decrypt(final String text) {
            return AES.decrypt(text, AES.getSimplePassword(),
                    AES.getSimpleSalt(), AES.getSimpleIV());
        }

        public static String decrypt(final String text, final String password) {
            return AES.decrypt(text, password, AES.getSimpleSalt(),
                    AES.getSimpleIV());
        }

        public static String decrypt(final String text, final String password,
                final byte[] salt) {
            return AES.decrypt(text, password, salt, AES.getSimpleIV());
        }

        public static String decrypt(final String text, final String password,
                final byte[] salt, final byte[] iv) {
            final byte[] encryptedData = CryptoHelper.base64Decode(text);
            final byte[] data = AES.decrypt(encryptedData, password, salt, iv,
                    AES.KEY_SIZE_DEFAULT, AES.ITERATION_COUNT_DEFAULT);
            return CryptoHelper.getString(data);
        }

        public static byte[] encrypt(final byte[] data) {
            return AES.encrypt(data, AES.getSimplePassword(),
                    AES.getSimpleSalt(), AES.getSimpleIV(),
                    AES.KEY_SIZE_DEFAULT, AES.ITERATION_COUNT_DEFAULT);
        }

        /**
         * AES encrypt function
         * 
         * @param original
         * @param key
         *            16, 24, 32 bytes available
         * @param iv
         *            initial vector (16 bytes) - if null: ECB mode, otherwise:
         *            CBC mode
         * @return
         */
        public static byte[] encrypt(final byte[] original, final byte[] key,
                final byte[] iv) {
            if ((key == null)
                    || ((key.length != 16) && (key.length != 24) && (key.length != 32))) {
                return null;
            }
            if ((iv != null) && (iv.length != 16)) {
                return null;
            }

            try {
                SecretKeySpec keySpec = null;
                Cipher cipher = null;
                if (iv != null) {
                    keySpec = new SecretKeySpec(key, "AES/CBC/PKCS7Padding");
                    cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, keySpec,
                            new IvParameterSpec(iv));
                } else // if(iv == null)
                {
                    keySpec = new SecretKeySpec(key, "AES/ECB/PKCS7Padding");
                    cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, keySpec);
                }

                return cipher.doFinal(original);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public static byte[] encrypt(final byte[] data, final String password) {
            return AES.encrypt(data, password, AES.getSimpleSalt(),
                    AES.getSimpleIV(), AES.KEY_SIZE_DEFAULT,
                    AES.ITERATION_COUNT_DEFAULT);
        }

        public static byte[] encrypt(final byte[] data, final String password,
                final byte[] salt) {
            return AES.encrypt(data, password, salt, AES.getSimpleIV(),
                    AES.KEY_SIZE_DEFAULT, AES.ITERATION_COUNT_DEFAULT);
        }

        public static byte[] encrypt(final byte[] data, final String password,
                final byte[] salt, final byte[] iv) {
            return AES.encrypt(data, password, salt, iv, AES.KEY_SIZE_DEFAULT,
                    AES.ITERATION_COUNT_DEFAULT);
        }

        public static byte[] encrypt(final byte[] data, final String password,
                final byte[] salt, final byte[] iv, final int keySize) {
            return AES.encrypt(data, password, salt, iv, keySize,
                    AES.ITERATION_COUNT_DEFAULT);
        }

        public static byte[] encrypt(final byte[] data, final String password,
                final byte[] salt, final byte[] iv, final int keySize,
                final int iterationCount) {
            return AES.process(data, Cipher.ENCRYPT_MODE, password, salt, iv,
                    keySize, iterationCount);
        }

        public static String encrypt(final String text) {
            return AES.encrypt(text, AES.getSimplePassword(),
                    AES.getSimpleSalt(), AES.getSimpleIV());
        }

        public static String encrypt(final String text, final String password) {
            return AES.encrypt(text, password, AES.getSimpleSalt(),
                    AES.getSimpleIV());
        }

        public static String encrypt(final String text, final String password,
                final byte[] salt) {
            return AES.encrypt(text, password, salt, AES.getSimpleIV());
        }

        public static String encrypt(final String text, final String password,
                final byte[] salt, final byte[] iv) {
            final byte[] data = CryptoHelper.getRawBytes(text);
            final byte[] encryptedData = AES.encrypt(data, password, salt, iv,
                    AES.KEY_SIZE_DEFAULT, AES.ITERATION_COUNT_DEFAULT);
            return CryptoHelper.base64Encode(encryptedData);
        }

        static byte[] getSimpleIV() {
            final byte[] iv = new byte[AES.IV_SIZE_DEFAULT];
            Arrays.fill(iv, (byte) 5);
            return iv;
        }

        static String getSimplePassword() {
            return "GZ9Gn2U5nhpea8hw";
        }

        static byte[] getSimpleSalt() {
            return "rUiey8D2GNzV69Mp".getBytes();
        }

        static byte[] process(final byte[] data, final int mode,
                final String password, final byte[] salt, final byte[] iv,
                final int keySize, final int iterationCount) {
            final KeySpec keySpec = new PBEKeySpec(password.toCharArray(),
                    salt, iterationCount, keySize);
            try {
                final SecretKeyFactory keyFactory = SecretKeyFactory
                        .getInstance("PBKDF2WithHmacSHA1");
                final byte[] keyBytes = keyFactory.generateSecret(keySpec)
                        .getEncoded();
                final SecretKey key = new SecretKeySpec(keyBytes, "AES");
                final Cipher cipher = Cipher
                        .getInstance("AES/CBC/PKCS5Padding");
                final IvParameterSpec ivParams = new IvParameterSpec(iv);
                cipher.init(mode, key, ivParams);
                return cipher.doFinal(data);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // http://nelenkov.blogspot.jp/2012/04/using-password-based-encryption-on.html
    public static class AESCrypto {
        private static final int ITERATION_COUNT_DEFAULT = 100;
        private static final int ITERATION_COUNT_MIN = 10;
        private static final int ITERATION_COUNT_MAX = 10000;
        private static final int SALT_SIZE_DEFAULT = 8;
        private static final int KEY_SIZE_DEFAULT = 256;
        private static final int KEY_SIZE_MIN = 64;
        private static final int KEY_SIZE_MAX = 1024;
        private static final int IV_SIZE_DEFAULT = 16;
        private String password;
        private byte[] salt;
        private byte[] iv;
        private int keySize;
        private int iterationCount;

        public AESCrypto(final String password) {
            initialize(password, AESCrypto.KEY_SIZE_DEFAULT, null,
                    AESCrypto.ITERATION_COUNT_DEFAULT);
        }

        public AESCrypto(final String password, final int keySize,
                final String salt, final int iterationCount) {
            initialize(password, keySize, salt, iterationCount);
        }

        public AESCrypto(final String password, final String salt) {
            initialize(password, AESCrypto.KEY_SIZE_DEFAULT, salt,
                    AESCrypto.ITERATION_COUNT_DEFAULT);
        }

        private void checkAndSetIterationCount(final int iterationCount) {
            if (iterationCount < AESCrypto.ITERATION_COUNT_MIN) {
                this.iterationCount = AESCrypto.ITERATION_COUNT_MIN;
            } else if (iterationCount > AESCrypto.ITERATION_COUNT_MAX) {
                this.iterationCount = AESCrypto.ITERATION_COUNT_MAX;
            } else {
                this.iterationCount = iterationCount;
            }
        }

        private void checkAndSetIV() {
            this.iv = CryptoHelper.getRandomBytes(AESCrypto.IV_SIZE_DEFAULT);
        }

        private void checkAndSetKeySize(final int keySize) {
            if (keySize < AESCrypto.KEY_SIZE_MIN) {
                this.keySize = AESCrypto.KEY_SIZE_MIN;
            } else if (keySize > AESCrypto.KEY_SIZE_MAX) {
                this.keySize = AESCrypto.KEY_SIZE_MAX;
            } else {
                this.keySize = keySize;
            }
        }

        private void checkAndSetPassword(final String password) {
            if (TextUtils.isEmpty(password)) {
                this.password = CryptoHelper.getRandomString();
            } else {
                this.password = password;
            }
        }

        private void checkAndSetSalt(final String salt) {
            if (TextUtils.isEmpty(salt)) {
                this.salt = CryptoHelper
                        .getRandomBytes(AESCrypto.SALT_SIZE_DEFAULT);
            } else {
                this.salt = CryptoHelper.getRawBytes(salt);
            }
        }

        public byte[] decrypt(final byte[] encryptedData) {
            return process(encryptedData, Cipher.DECRYPT_MODE);
        }

        public String decrypt(final String text) {
            final byte[] encryptedData = CryptoHelper.base64Decode(text);
            final byte[] data = decrypt(encryptedData);
            return CryptoHelper.getString(data);
        }

        public byte[] encrypt(final byte[] data) {
            return process(data, Cipher.ENCRYPT_MODE);
        }

        public String encrypt(final String text) {
            final byte[] data = CryptoHelper.getRawBytes(text);
            final byte[] encryptedData = encrypt(data);
            return CryptoHelper.base64Encode(encryptedData);
        }

        public int getIterationCount() {
            return this.iterationCount;
        }

        public byte[] getIv() {
            return this.iv;
        }

        public int getKeySize() {
            return this.keySize;
        }

        public String getPassword() {
            return this.password;
        }

        public byte[] getSalt() {
            return this.salt;
        }

        private void initialize(final String password, final int keySize,
                final String salt, final int iterationCount) {
            checkAndSetPassword(password);
            checkAndSetKeySize(keySize);
            checkAndSetSalt(salt);
            checkAndSetIterationCount(iterationCount);
            checkAndSetIV();
        }

        private byte[] process(final byte[] data, final int mode) {
            return AES.process(data, mode, this.password, this.salt, this.iv,
                    this.keySize, this.iterationCount);
        }

        public void setIterationCount(final int iterationCount) {
            checkAndSetIterationCount(iterationCount);
        }

        public void setKeySize(final int keySize) {
            checkAndSetKeySize(keySize);
        }

        public void setPassword(final String password) {
            checkAndSetPassword(password);
        }

        public void setSalt(final String salt) {
            checkAndSetSalt(salt);
        }

    }

    public static class HASH {
        private static final String MD5 = "MD5";
        private static final String SHA_1 = "SHA-1";
        private static final String SHA_256 = "SHA-256";
        private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4',
                '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        private static final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4',
                '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

        private static char[] encodeHex(final byte[] data) {
            return HASH.encodeHex(data, true);
        }

        private static char[] encodeHex(final byte[] data,
                final boolean toLowerCase) {
            return HASH.encodeHex(data, toLowerCase ? HASH.DIGITS_LOWER
                    : HASH.DIGITS_UPPER);
        }

        private static char[] encodeHex(final byte[] data, final char[] toDigits) {
            final int l = data.length;
            final char[] out = new char[l << 1];
            for (int i = 0, j = 0; i < l; i++) {
                out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
                out[j++] = toDigits[0x0F & data[i]];
            }
            return out;
        }

        private static MessageDigest getDigest(final String algorithm) {
            try {
                return MessageDigest.getInstance(algorithm);
            } catch (final NoSuchAlgorithmException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public static String md5(final byte[] data) {
            return new String(HASH.encodeHex(HASH.md5Bytes(data)));
        }

        public static String md5(final String text) {
            return new String(HASH.encodeHex(HASH.md5Bytes(CryptoHelper
                    .getRawBytes(text))));
        }

        public static byte[] md5Bytes(final byte[] data) {
            return HASH.getDigest(HASH.MD5).digest(data);
        }

        public static String sha1(final byte[] data) {
            return new String(HASH.encodeHex(HASH.sha1Bytes(data)));
        }

        public static String sha1(final String text) {
            return new String(HASH.encodeHex(HASH.sha1Bytes(CryptoHelper
                    .getRawBytes(text))));
        }

        public static byte[] sha1Bytes(final byte[] data) {
            return HASH.getDigest(HASH.SHA_1).digest(data);
        }

        public static String sha256(final byte[] data) {
            return new String(HASH.encodeHex(HASH.sha256Bytes(data)));
        }

        public static String sha256(final String text) {
            return new String(HASH.encodeHex(HASH.sha256Bytes(CryptoHelper
                    .getRawBytes(text))));
        }

        public static byte[] sha256Bytes(final byte[] data) {
            return HASH.getDigest(HASH.SHA_256).digest(data);
        }

    }

    public static final String TAG = CryptoHelper.class.getSimpleName();

    public static final String ENC_UTF8 = "UTF-8";

    static byte[] base64Decode(final String text) {
        try {
            return Base64.decode(text);
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    static String base64Encode(final byte[] data) {
        return Base64.encodeBytes(data);
    }

    static byte[] getRandomBytes(final int size) {
        final SecureRandom random = new SecureRandom();
        final byte[] bytes = new byte[size];
        random.nextBytes(bytes);
        return bytes;
    }

    static String getRandomString() {
        final SecureRandom random = new SecureRandom();
        return String.valueOf(random.nextLong());
    }

    static byte[] getRawBytes(final String text) {
        try {
            return text.getBytes(CryptoHelper.ENC_UTF8);
        } catch (final UnsupportedEncodingException e) {
            return text.getBytes();
        }
    }

    static String getString(final byte[] data) {
        try {
            return new String(data, CryptoHelper.ENC_UTF8);
        } catch (final UnsupportedEncodingException e) {
            return new String(data);
        }
    }

}

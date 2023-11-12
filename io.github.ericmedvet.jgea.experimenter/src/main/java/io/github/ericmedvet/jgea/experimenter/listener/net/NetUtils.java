/*-
 * ========================LICENSE_START=================================
 * jgea-experimenter
 * %%
 * Copyright (C) 2018 - 2023 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.jgea.experimenter.listener.net;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class NetUtils {

  private static final String CIPHER_ALG = "AES/CBC/PKCS5Padding";
  private static final String SALT = NetUtils.class.getName();
  private static final byte[] IV_BYTES = SALT.getBytes(StandardCharsets.UTF_8);

  private NetUtils() {}

  public static double getCPULoad() {
    return ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getSystemLoadAverage();
  }

  public static String getMachineName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      return "unknown";
    }
  }

  public static int getNumberOfProcessors() {
    return ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getAvailableProcessors();
  }

  public static long getProcessMaxMemory() {
    return Runtime.getRuntime().maxMemory();
  }

  public static String getProcessName() {
    return ManagementFactory.getRuntimeMXBean().getName();
  }

  public static long getProcessUsedMemory() {
    return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
  }

  public static String getUserName() {
    return System.getProperty("user.name");
  }

  public static String decrypt(String ciphredString, String secret)
      throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException,
          NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
    return decrypt(CIPHER_ALG, ciphredString, getKeyFromPassword(secret, SALT), generateIv());
  }

  private static String decrypt(String algorithm, String cipherText, SecretKey key, IvParameterSpec iv)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
          InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(Cipher.DECRYPT_MODE, key, iv);
    byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
    return new String(plainText);
  }

  private static String encrypt(String algorithm, String input, SecretKey key, IvParameterSpec iv)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
          InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    Cipher cipher = Cipher.getInstance(algorithm);
    cipher.init(Cipher.ENCRYPT_MODE, key, iv);
    byte[] cipherText = cipher.doFinal(input.getBytes());
    return Base64.getEncoder().encodeToString(cipherText);
  }

  public static String encrypt(String clearString, String secret)
      throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException,
          NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
    return encrypt(CIPHER_ALG, clearString, getKeyFromPassword(secret, SALT), generateIv());
  }

  private static IvParameterSpec generateIv() {
    byte[] iv = new byte[16];
    System.arraycopy(IV_BYTES, 0, iv, 0, iv.length);
    return new IvParameterSpec(iv);
  }

  private static SecretKey getKeyFromPassword(String password, String salt)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
    return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
  }
}

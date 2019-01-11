/*******************************************************************************
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *    Bosch Software Innovations - initial creation
 ******************************************************************************/
package org.eclipse.californium.scandium.dtls.cipher;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import org.eclipse.californium.elements.util.NotForAndroid;

/**
 * A generic authenticated encryption block cipher mode.
 */
public class AeadBlockCipher {

	public static final String AES_CCM = "AES/CCM";

	/**
	 * Test, if cipher is supported.
	 * 
	 * @param transformation name of cipher
	 * @return {@code true}, if supported
	 */
	public final static boolean isSupported(String transformation) {
		boolean supported = AES_CCM.equals(transformation);
		if (!supported) {
			try {
				CipherManager.getInstance(transformation);
				supported = true;
			} catch (GeneralSecurityException ex) {
			}
		}
		return supported;
	}

	/**
	 * Decrypt with AEAD cipher.
	 * 
	 * @param suite the cipher suite
	 * @param key the encryption key K.
	 * @param nonce the nonce N.
	 * @param a the additional authenticated data a.
	 * @param c the encrypted and authenticated message c.
	 * @return the decrypted message
	 * 
	 * @throws GeneralSecurityException if the message could not be de-crypted,
	 *             e.g. because the ciphertext's block size is not correct
	 * @throws InvalidMacException if the message could not be authenticated
	 */
	public final static byte[] decrypt(CipherSuite suite, SecretKey key, byte[] nonce, byte[] a, byte[] c)
			throws GeneralSecurityException {
		if (AES_CCM.equals(suite.getTransformation())) {
			return CCMBlockCipher.decrypt(key, nonce, a, c, suite.getCiphertextExpansion());
		} else {
			return jreDecrypt(suite, key, nonce, a, c);
		}
	}

	/**
	 * Encrypt with AEAD cipher.
	 * 
	 * @param suite the cipher suite
	 * @param key the encryption key K.
	 * @param nonce the nonce N.
	 * @param a the additional authenticated data a.
	 * @param m the message to authenticate and encrypt.
	 * @return the encrypted and authenticated message.
	 * @throws GeneralSecurityException if the data could not be encrypted, e.g.
	 *             because the JVM does not support the AES cipher algorithm
	 */
	public final static byte[] encrypt(CipherSuite suite, SecretKey key, byte[] nonce, byte[] a, byte[] m)
			throws GeneralSecurityException {
		if (AES_CCM.equals(suite.getTransformation())) {
			return CCMBlockCipher.encrypt(key, nonce, a, m, suite.getCiphertextExpansion());
		} else {
			return jreEncrypt(suite, key, nonce, a, m);
		}
	}

	/**
	 * Decrypt with jre AEAD cipher.
	 * 
	 * @param suite the cipher suite
	 * @param key the encryption key K.
	 * @param nonce the nonce N.
	 * @param a the additional authenticated data a.
	 * @param c the encrypted and authenticated message c.
	 * @return the decrypted message
	 * 
	 * @throws GeneralSecurityException if the message could not be de-crypted,
	 *             e.g. because the ciphertext's block size is not correct
	 * @throws InvalidMacException if the message could not be authenticated
	 */
	@NotForAndroid
	private final static byte[] jreDecrypt(CipherSuite suite, SecretKey key, byte[] nonce, byte[] a, byte[] c)
			throws GeneralSecurityException {

		Cipher cipher = CipherManager.getInstance(suite.getTransformation());
		GCMParameterSpec parameterSpec = new GCMParameterSpec(suite.getEncKeyLength() * 8, nonce);
		cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
		cipher.updateAAD(a);
		return cipher.doFinal(c);
	}

	/**
	 * Encrypt with jre AEAD cipher.
	 * 
	 * @param suite the cipher suite
	 * @param key the encryption key K.
	 * @param nonce the nonce N.
	 * @param a the additional authenticated data a.
	 * @param m the message to authenticate and encrypt.
	 * @return the encrypted and authenticated message.
	 * @throws GeneralSecurityException if the data could not be encrypted, e.g.
	 *             because the JVM does not support the AES cipher algorithm
	 */
	@NotForAndroid
	private final static byte[] jreEncrypt(CipherSuite suite, SecretKey key, byte[] nonce, byte[] a, byte[] m)
			throws GeneralSecurityException {
		Cipher cipher = CipherManager.getInstance(suite.getTransformation());
		GCMParameterSpec parameterSpec = new GCMParameterSpec(suite.getEncKeyLength() * 8, nonce);
		cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
		cipher.updateAAD(a);
		return cipher.doFinal(m);
	}
}

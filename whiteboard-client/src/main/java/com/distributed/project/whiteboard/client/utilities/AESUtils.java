package com.distributed.project.whiteboard.client.utilities;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used for AES encryption and decryption of the chat messages
 * shared between the clients.
 * 
 * @implNote We use the AES (Advanced Encryption Standard) symmetric key
 *           algorithm for encryption and decryption.
 * 
 * @implNote We use the CBC (Cipher Block Chaining) variation of AES encryption
 *           algorithm.
 * 
 *           {@link https://www.baeldung.com/java-aes-encryption-decryption}
 * 
 * @author Abhijeet - 1278218
 *
 */
public final class AESUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(AESUtils.class);

	private static final String ENCRYPTION_KEY_PART1 = "WHITEBOARD";
	private static final String ENCRYPTION_INIT_VECTOR = "encryptionIntVec";

	private AESUtils() {
		throw new IllegalStateException("AESUtils class cannot be instantiated");
	}

	/**
	 * This method is used to encrypt the given string using the client UID as part
	 * 2 of the secret key and part 1 as the pre-defined key to complete 16
	 * characters required for encryption.
	 * 
	 * @param textToBeEncrypted
	 * @param encryptionKeyPart2
	 * @return
	 */
	public static String encryptString(String textToBeEncrypted, String encryptionKeyPart2) {
		try {
			if (StringUtils.isBlank(textToBeEncrypted)) {
				return StringUtils.EMPTY;
			}

			String secretKey = ENCRYPTION_KEY_PART1 + encryptionKeyPart2;

			IvParameterSpec ivSpec = new IvParameterSpec(ENCRYPTION_INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
			SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

			byte[] encryptedBytes = cipher.doFinal(textToBeEncrypted.getBytes());
			return Base64.getEncoder().encodeToString(encryptedBytes);
		} catch (Exception e) {
			LOGGER.error("Exception in encryptString() method", e);
			return StringUtils.EMPTY;
		}
	}

	/**
	 * This method is used to decrypt the given encrypted string using the client
	 * UID as p[art 2 of the secret key and part 1 as the pre-defined key to
	 * complete 16 charcates required for encryption.
	 * 
	 * @param encryptedString
	 * @param encryptionKeyPart2
	 * @return
	 */
	public static String decryptString(String encryptedString, String encryptionKeyPart2) {
		try {
			if (StringUtils.isBlank(encryptedString)) {
				return StringUtils.EMPTY;
			}

			String secretKey = ENCRYPTION_KEY_PART1 + encryptionKeyPart2;

			IvParameterSpec ivSpec = new IvParameterSpec(ENCRYPTION_INIT_VECTOR.getBytes(StandardCharsets.UTF_8));
			SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

			byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedString));
			return new String(decryptedBytes);
		} catch (Exception e) {
			LOGGER.error("Exception in decryptString() method", e);
			return StringUtils.EMPTY;
		}
	}

}

package pt.ulisboa.tecnico.cross.account;

import static android.security.keystore.KeyProperties.DIGEST_SHA256;
import static android.security.keystore.KeyProperties.KEY_ALGORITHM_RSA;
import static android.security.keystore.KeyProperties.PURPOSE_SIGN;
import static android.security.keystore.KeyProperties.PURPOSE_VERIFY;
import static android.security.keystore.KeyProperties.SIGNATURE_PADDING_RSA_PKCS1;

import android.security.keystore.KeyGenParameterSpec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import timber.log.Timber;

public class CryptoManager {

  private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
  private static final String USER_KEYPAIR_ALIAS = "CROSSUserKeyPairAlias";
  private static final String SERVER_KEYPAIR_ALIAS = "CROSSServerKeyPairAlias";
  private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
  private static final String ENCRYPTION_ALGORITHM = "RSA/ECB/PKCS1Padding";
  private static final String CERTIFICATE_ALGORITHM = "X.509";

  private PrivateKey userPrivateKey;
  private PublicKey userPublicKey;
  private PublicKey serverPublicKey;

  public static CryptoManager get() {
    return CryptoManagerHolder.INSTANCE;
  }

  private CryptoManager() {}

  /****************************
   * User key pair management *
   ****************************/

  public synchronized void generateNewUserKeyPair() {
    try {
      KeyPairGenerator keyPairGenerator =
          KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, KEYSTORE_PROVIDER);
      keyPairGenerator.initialize(
          new KeyGenParameterSpec.Builder(USER_KEYPAIR_ALIAS, PURPOSE_SIGN | PURPOSE_VERIFY)
              .setDigests(DIGEST_SHA256)
              .setSignaturePaddings(SIGNATURE_PADDING_RSA_PKCS1)
              .build());

      KeyPair keyPair = keyPairGenerator.generateKeyPair();
      userPublicKey = keyPair.getPublic();
      userPrivateKey = keyPair.getPrivate();
      Timber.i("A new key pair was successfully generated.");
    } catch (InvalidAlgorithmParameterException
        | NoSuchAlgorithmException
        | NoSuchProviderException e) {
      CROSSCityApp.get().showToast("Unable to generate a cryptographic identity.");
      Timber.e(e, "Failed to generate key pair.");
    }
  }

  public synchronized PublicKey getUserPublicKey() {
    loadUserKeyPair();
    if (userPublicKey == null) return null;
    return userPublicKey;
  }

  public synchronized byte[] sign(byte[] data) {
    loadUserKeyPair();
    if (userPrivateKey == null) return null;
    try {
      Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
      signature.initSign(userPrivateKey);
      signature.update(data);

      byte[] signedData = signature.sign();
      Timber.i("Data signed successfully.");
      return signedData;
    } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
      Timber.e(e, "Failed to sign data.");
      return null;
    }
  }

  private synchronized void loadUserKeyPair() {
    if (userPrivateKey != null && userPublicKey != null) return;
    try {
      KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
      keyStore.load(null);

      userPublicKey = keyStore.getCertificate(USER_KEYPAIR_ALIAS).getPublicKey();
      userPrivateKey = (PrivateKey) keyStore.getKey(USER_KEYPAIR_ALIAS, null);
      Timber.i("The existing key pair was successfully loaded.");
    } catch (UnrecoverableEntryException
        | CertificateException
        | KeyStoreException
        | IOException
        | NoSuchAlgorithmException e) {
      Timber.e(e, "Failed to load key pair.");
    }
  }

  public synchronized void deleteUserKeyPair() {
    try {
      KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
      keyStore.load(null);

      keyStore.deleteEntry(USER_KEYPAIR_ALIAS);
      userPrivateKey = null;
      userPublicKey = null;
      Timber.i("The existing key pair was successfully deleted.");
    } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
      Timber.e(e, "Failed to delete key pair.");
    }
  }

  /*************************
   * Server key management *
   *************************/

  public synchronized void storeServerCertificate(byte[] certificateBytes) {
    try {
      KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
      keyStore.load(null);

      Certificate certificate =
          CertificateFactory.getInstance(CERTIFICATE_ALGORITHM)
              .generateCertificate(new ByteArrayInputStream(certificateBytes));
      keyStore.setCertificateEntry(SERVER_KEYPAIR_ALIAS, certificate);
      serverPublicKey = certificate.getPublicKey();
      Timber.i("The server certificate was successfully stored.");
    } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
      Timber.e(e, "The server certificate store failed.");
    }
  }

  public synchronized byte[] encrypt(byte[] data) {
    loadServerPublicKey();
    if (serverPublicKey == null) return null;
    try {
      Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);

      byte[] encryptedData = cipher.doFinal(data);
      Timber.i("Data encrypted successfully.");
      return encryptedData;
    } catch (NoSuchPaddingException
        | IllegalBlockSizeException
        | NoSuchAlgorithmException
        | BadPaddingException
        | InvalidKeyException e) {
      Timber.e(e, "Failed to encrypt data.");
      return null;
    }
  }

  private synchronized void loadServerPublicKey() {
    if (serverPublicKey != null) return;
    try {
      KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
      keyStore.load(null);

      serverPublicKey = keyStore.getCertificate(SERVER_KEYPAIR_ALIAS).getPublicKey();
      Timber.i("The existing server public key was successfully loaded.");
    } catch (CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException e) {
      Timber.e(e, "Failed to load server public key.");
    }
  }

  private static class CryptoManagerHolder {
    private static final CryptoManager INSTANCE = new CryptoManager();
  }
}

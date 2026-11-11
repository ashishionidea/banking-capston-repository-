package com.ashish.banking.customerservice.config;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
/** AES-256-GCM field encryption. Supply a base64-encoded 32-byte key using PII_AES_KEY_B64. */
@Component public class EncryptionService {
 private final byte[] key; private final SecureRandom random=new SecureRandom();
 public EncryptionService(@Value("${PII_AES_KEY_B64:}") String encoded){
  if(encoded==null||encoded.isBlank()){this.key=null;return;}
  this.key=Base64.getDecoder().decode(encoded);
  if(this.key.length!=32)throw new IllegalArgumentException("PII AES key must decode to exactly 32 bytes");
 }
 public String encrypt(String plain){
  if(plain==null)return null;if(key==null)throw new IllegalStateException("PII_AES_KEY_B64 must be configured before storing sensitive fields");
  try{byte[] iv=new byte[12];random.nextBytes(iv);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));byte[] encrypted=c.doFinal(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8));return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length+encrypted.length).put(iv).put(encrypted).array());}
  catch(Exception e){throw new IllegalStateException("Could not encrypt PII",e);}
 }
 public String decrypt(String value){
  if(value==null)return null;if(key==null)throw new IllegalStateException("PII_AES_KEY_B64 is not configured");
  try{byte[] all=Base64.getDecoder().decode(value);ByteBuffer b=ByteBuffer.wrap(all);byte[] iv=new byte[12];b.get(iv);byte[] enc=new byte[b.remaining()];b.get(enc);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new GCMParameterSpec(128,iv));return new String(c.doFinal(enc),java.nio.charset.StandardCharsets.UTF_8);}
  catch(Exception e){throw new IllegalStateException("Could not decrypt PII",e);}
 }
}

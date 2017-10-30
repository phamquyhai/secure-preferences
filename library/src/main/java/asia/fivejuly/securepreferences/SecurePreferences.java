package asia.fivejuly.securepreferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.keychain.KeyChain;
import com.facebook.soloader.SoLoader;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by haipq on 12/27/16.
 */

public class SecurePreferences implements SharedPreferences {

    private static final String TAG = SecurePreferences.class.getName();
    private static boolean isDebug = false;
    private static boolean isInit = false;

    private SharedPreferences sharedPreferences;
    private Entity entity;
    private Crypto crypto;


    public final static class Builder {

        private Context context;
        private String password;
        private String sharedPrefFilename;

        public Builder(final Context context) {
            this.context = context;
        }

        public String password() {
            return password;
        }

        public Builder password(final String password) {
            this.password = password;
            return this;
        }

        public String filename() {
            return sharedPrefFilename;
        }

        public Builder filename(final String sharedPrefFilename) {
            this.sharedPrefFilename = sharedPrefFilename;
            return this;
        }

        public SharedPreferences build() {
            if(!isInit) {
                Log.w(TAG, "You need call 'SecurePreferences.init()' in onCreate() from your application class.");
            }
            KeyChain keyChain = new SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256);
            Entity entity = Entity.create(
                    TextUtils.isEmpty(password) ? getClass().getPackage().getName() : password
            );
            return new SecurePreferences(
                    context,
                    keyChain,
                    entity,
                    sharedPrefFilename
            );
        }
    }

    private SecurePreferences(Context context,
                              final KeyChain keyChain,
                              final Entity entity,
                              final String sharedPrefFilename) {
        this.entity = entity;
        this.sharedPreferences = getSharedPreferenceFile(context, sharedPrefFilename);
        this.crypto = AndroidConceal.get().createCrypto256Bits(keyChain);
    }

    public static void init(Context pContext) {
        SoLoader.init(pContext, false);
        isInit = true;
    }

    private String encrypt(final String plainText) {
        if (TextUtils.isEmpty(plainText)) {
            return plainText;
        }

        byte[] cipherText = null;

        if (!crypto.isAvailable()) {
            log(Log.WARN, "encrypt: crypto not available");
            return null;
        }

        try {
            cipherText = crypto.encrypt(plainText.getBytes(), entity);
        } catch (KeyChainException | CryptoInitializationException | IOException e) {
            log(Log.ERROR, "encrypt: " + e);
        }

        return cipherText != null ? Base64.encodeToString(cipherText, Base64.DEFAULT) : null;
    }

    private String decrypt(final String encryptedText) {
        if (TextUtils.isEmpty(encryptedText)) {
            return encryptedText;
        }

        byte[] plainText = null;

        if (!crypto.isAvailable()) {
            log(Log.WARN, "decrypt: crypto not available");
            return null;
        }

        try {
            plainText = crypto.decrypt(Base64.decode(encryptedText, Base64.DEFAULT), entity);
        } catch (KeyChainException | CryptoInitializationException | IOException e) {
            log(Log.ERROR, "decrypt: " + e);
        }

        return plainText != null
                ? new String(plainText)
                : null;
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static void setIsDebug(boolean isDebug) {
        SecurePreferences.isDebug = isDebug;
    }

    @Override
    public Map<String, ?> getAll() {
        final Map<String, ?> encryptedMap = sharedPreferences.getAll();
        final Map<String, String> decryptedMap = new HashMap<>(
                encryptedMap.size());
        for (Map.Entry<String, ?> entry : encryptedMap.entrySet()) {
            try {
                Object cipherText = entry.getValue();
                if (cipherText != null) {
                    decryptedMap.put(entry.getKey(),
                            decrypt(cipherText.toString()));
                }
            } catch (Exception e) {
                log(Log.ERROR, "error getAll: " + e);
                decryptedMap.put(entry.getKey(),
                        entry.getValue().toString());
            }
        }
        return decryptedMap;
    }

    @Override
    public String getString(String key, String defaultValue) {
        final String encryptedValue = sharedPreferences.getString(
                SecurePreferences.hashKey(key), null);
        return (encryptedValue != null) ? decrypt(encryptedValue) : defaultValue;
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defaultValues) {
        final Set<String> encryptedSet = sharedPreferences.getStringSet(
                SecurePreferences.hashKey(key), null);
        if (encryptedSet == null) {
            return defaultValues;
        }
        final Set<String> decryptedSet = new HashSet<>(
                encryptedSet.size());
        for (String encryptedValue : encryptedSet) {
            decryptedSet.add(decrypt(encryptedValue));
        }
        return decryptedSet;
    }

    @Override
    public int getInt(String key, int defaultValue) {
        final String encryptedValue = sharedPreferences.getString(
                SecurePreferences.hashKey(key), null);
        if (encryptedValue == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(decrypt(encryptedValue));
        } catch (NumberFormatException e) {
            throw new ClassCastException(e.getMessage());
        }
    }

    @Override
    public long getLong(String key, long defaultValue) {
        final String encryptedValue = sharedPreferences.getString(
                SecurePreferences.hashKey(key), null);
        if (encryptedValue == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(decrypt(encryptedValue));
        } catch (NumberFormatException e) {
            throw new ClassCastException(e.getMessage());
        }
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        final String encryptedValue = sharedPreferences.getString(
                SecurePreferences.hashKey(key), null);
        if (encryptedValue == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(decrypt(encryptedValue));
        } catch (NumberFormatException e) {
            throw new ClassCastException(e.getMessage());
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        final String encryptedValue = sharedPreferences.getString(
                SecurePreferences.hashKey(key), null);
        if (encryptedValue == null) {
            return defaultValue;
        }
        try {
            return Boolean.parseBoolean(decrypt(encryptedValue));
        } catch (NumberFormatException e) {
            throw new ClassCastException(e.getMessage());
        }
    }

    @Override
    public boolean contains(String key) {
        return sharedPreferences.contains(SecurePreferences.hashKey(key));
    }

    @Override
    public Editor edit() {
        return new Editor();
    }

    public final class Editor implements SharedPreferences.Editor {

        private SharedPreferences.Editor mEditor;

        @SuppressLint("CommitPrefEdits")
        private Editor() {
            mEditor = sharedPreferences.edit();
        }

        @Override
        public SharedPreferences.Editor putString(String key, String value) {
            mEditor.putString(SecurePreferences.hashKey(key),
                    encrypt(value));
            return this;
        }

        @Override
        public SharedPreferences.Editor putStringSet(String key,
                                                     Set<String> values) {
            final Set<String> encryptedValues = new HashSet<>(
                    values.size());
            for (String value : values) {
                encryptedValues.add(encrypt(value));
            }
            mEditor.putStringSet(SecurePreferences.hashKey(key),
                    encryptedValues);
            return this;
        }

        @Override
        public SharedPreferences.Editor putInt(String key, int value) {
            mEditor.putString(SecurePreferences.hashKey(key),
                    encrypt(Integer.toString(value)));
            return this;
        }

        @Override
        public SharedPreferences.Editor putLong(String key, long value) {
            mEditor.putString(SecurePreferences.hashKey(key),
                    encrypt(Long.toString(value)));
            return this;
        }

        @Override
        public SharedPreferences.Editor putFloat(String key, float value) {
            mEditor.putString(SecurePreferences.hashKey(key),
                    encrypt(Float.toString(value)));
            return this;
        }

        @Override
        public SharedPreferences.Editor putBoolean(String key, boolean value) {
            mEditor.putString(SecurePreferences.hashKey(key),
                    encrypt(Boolean.toString(value)));
            return this;
        }

        @Override
        public SharedPreferences.Editor remove(String key) {
            mEditor.remove(SecurePreferences.hashKey(key));
            return this;
        }

        @Override
        public SharedPreferences.Editor clear() {
            mEditor.clear();
            return this;
        }

        @Override
        public boolean commit() {
            return mEditor.commit();
        }


        @Override
        public void apply() {
            mEditor.apply();
        }
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(
            final OnSharedPreferenceChangeListener listener) {
        sharedPreferences
                .registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(
            final OnSharedPreferenceChangeListener listener) {
        sharedPreferences
                .unregisterOnSharedPreferenceChangeListener(listener);
    }

    private static String hashKey(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(key.getBytes());
            StringBuffer sb = new StringBuffer();
            for (byte anArray : array) {
                sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log(Log.WARN, " SecurePreferences.hashKey error: " + e);
        }

        return key;
    }

    private SharedPreferences getSharedPreferenceFile(Context context, String prefFilename) {

        if (TextUtils.isEmpty(prefFilename)) {
            return PreferenceManager
                    .getDefaultSharedPreferences(context);
        } else {
            return context.getSharedPreferences(prefFilename, Context.MODE_PRIVATE);
        }
    }

    private static void log(int type, String str) {
        if (isDebug) {
            switch (type) {
                case Log.WARN:
                    Log.w(TAG, str);
                    break;
                case Log.ERROR:
                    Log.e(TAG, str);
                    break;
                case Log.DEBUG:
                    Log.d(TAG, str);
                    break;
            }
        }
    }


}

package br.com.cafes.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Hash de senhas com PBKDF2 (nativo do Java, sem biblioteca extra).
 *
 * NUNCA guardamos a senha pura no banco. Guardamos:
 *   pbkdf2$iteracoes$salt(base64)$hash(base64)
 */
public final class SenhaUtil {

    private static final int ITERACOES = 65536;
    private static final int TAMANHO_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private SenhaUtil() {
    }

    public static String gerarHash(String senha) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return gerarHash(senha, salt, ITERACOES);
    }

    public static boolean confere(String senha, String hashGuardado) {
        String[] partes = hashGuardado.split("\\$");
        if (partes.length != 4 || !"pbkdf2".equals(partes[0])) {
            return false;
        }
        byte[] salt = Base64.getDecoder().decode(partes[2]);
        String calculado = gerarHash(senha, salt, Integer.parseInt(partes[1]));
        // Comparacao em tempo constante (evita ataque de tempo).
        return MessageDigest.isEqual(calculado.getBytes(), hashGuardado.getBytes());
    }

    private static String gerarHash(String senha, byte[] salt, int iteracoes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), salt, iteracoes, TAMANHO_BITS);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            Base64.Encoder b64 = Base64.getEncoder();
            return "pbkdf2$" + iteracoes + "$" + b64.encodeToString(salt) + "$" + b64.encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar hash de senha.", e);
        }
    }
}

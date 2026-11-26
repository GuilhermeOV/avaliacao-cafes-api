package br.com.cafes.config;

import br.com.cafes.model.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Geracao e validacao de tokens JWT usando a biblioteca java-jwt (Auth0).
 *
 * Estrutura do token: Header (algoritmo HS256) . Payload (claims) . Signature.
 * O segredo vem da variavel de ambiente JWT_SECRET.
 */
public final class JwtUtil {

    private static final String ISSUER = "api-avaliacao-cafes";
    private static final long HORAS_VALIDADE = 2;

    private static final Algorithm ALGORITMO = Algorithm.HMAC256(segredo());
    private static final JWTVerifier VERIFICADOR = JWT.require(ALGORITMO).withIssuer(ISSUER).build();

    private JwtUtil() {
    }

    /** Gera um token assinado para o usuario autenticado. */
    public static String gerarToken(Usuario usuario) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(String.valueOf(usuario.getId()))
                .withClaim("nome", usuario.getNome())
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(HORAS_VALIDADE, ChronoUnit.HOURS))
                .sign(ALGORITMO);
    }

    /** Valida assinatura, emissor e expiracao. Lanca JWTVerificationException se invalido. */
    public static DecodedJWT validar(String token) throws JWTVerificationException {
        return VERIFICADOR.verify(token);
    }

    public static long horasValidade() {
        return HORAS_VALIDADE;
    }

    private static String segredo() {
        String valor = System.getenv("JWT_SECRET");
        return (valor == null || valor.isBlank()) ? "segredo-local-apenas-para-desenvolvimento" : valor;
    }
}

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Usuario e autenticação")
public class UsuarioTest {

    @BeforeEach
    void setUp() {
        TestHelper.resetarAmbiente();
        Demanda.resetarContador();
    }

    @Test
    @DisplayName("Deve normalizar email ignorando maiúsculas e espaços")
    void deveNormalizarEmail() {
        Usuario u = new Cliente(0, "Lucas", "  LuCaS@Test.COM ", "1234", TipoUsuario.Cliente);

        assertAll("Normalização de e-mail",
            () -> assertEquals("lucas@ufmg.br", Usuario.normalizarEmail("  LUCAS@UFMG.BR  ")),
            () -> assertNull(Usuario.normalizarEmail(null)),
            () -> assertEquals("lucas@test.com", u.getEmail())
        );
    }

    @Test
    @DisplayName("Deve armazenar senha criptografada com BCrypt e não em texto plano")
    void deveCriptografarSenhaComBCrypt() {
        Usuario u = new Cliente(0, "Lucas", "lucas@test.com", "minhaSenhaSecreta", TipoUsuario.Cliente);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        assertAll("Segurança de senha",
            () -> assertNotEquals("minhaSenhaSecreta", u.getSenha()),
            () -> assertTrue(encoder.matches("minhaSenhaSecreta", u.getSenha())),
            () -> assertFalse(encoder.matches("senhaErrada", u.getSenha()))
        );
    }

    @Test
    @DisplayName("Deve considerar iguais duas instâncias distintas com a mesma matrícula")
    void deveCompararUsuariosPorMatricula() {
        Usuario u1 = new Cliente(0, "Lucas", "a@test.com", "123", TipoUsuario.Cliente);
        Usuario u2 = new Cliente(0, "Maria", "b@test.com", "456", TipoUsuario.Cliente);

        // Reinicia o contador para gerar uma segunda instância diferente em memória com a mesma matrícula (0)
        Usuario.restaurarContador(u1.getMatricula());
        Usuario copiaMesmaMatricula = new Cliente(0, "Lucas Clone", "outro@test.com", "789", TipoUsuario.Cliente);

        assertAll("Igualdade e hashCode por matrícula",
            () -> assertNotSame(u1, copiaMesmaMatricula, "Devem ser objetos diferentes na memória"),
            () -> assertEquals(u1, copiaMesmaMatricula, "Objetos distintos com mesma matrícula devem ser iguais"),
            () -> assertEquals(u1.hashCode(), copiaMesmaMatricula.hashCode()),
            () -> assertNotEquals(u1, u2),
            () -> assertNotEquals(u1, null),
            () -> assertNotEquals(u1, "stringQualquer")
        );
    }
}
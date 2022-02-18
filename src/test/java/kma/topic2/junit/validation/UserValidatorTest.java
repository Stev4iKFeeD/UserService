package kma.topic2.junit.validation;

import kma.topic2.junit.exceptions.ConstraintViolationException;
import kma.topic2.junit.exceptions.LoginExistsException;
import kma.topic2.junit.model.NewUser;
import kma.topic2.junit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserValidator userValidator;

    @Test
    void nonExistingUserTest() {
        Mockito.when(userRepository.isLoginExists(ArgumentMatchers.anyString())).thenReturn(false);

        NewUser newUser = NewUser.builder()
                .login("login")
                .password("passwor")
                .fullName("name")
                .build();
        assertThatCode(() -> userValidator.validateNewUser(newUser)).doesNotThrowAnyException();
    }

    @Test
    void existingUserTest() {
        Mockito.when(userRepository.isLoginExists(ArgumentMatchers.anyString())).thenReturn(true);

        NewUser newUser = NewUser.builder()
                .login("login")
                .password("passwor")
                .fullName("name")
                .build();
        assertThatThrownBy(() -> userValidator.validateNewUser(newUser))
                .isInstanceOf(LoginExistsException.class)
                .hasMessage(String.format("Login %s already taken", newUser.getLogin()));
    }

    @Test
    void smallPasswordTest() {
        Mockito.when(userRepository.isLoginExists(ArgumentMatchers.anyString())).thenReturn(false);

        NewUser newUser = NewUser.builder()
                .login("login")
                .password("1")
                .fullName("name")
                .build();
        assertThatThrownBy(() -> userValidator.validateNewUser(newUser))
                .isInstanceOf(ConstraintViolationException.class)
                .extracting("errors")
                .asList()
                .containsExactly("Password has invalid size");
    }

    @Test
    void bigPasswordTest() {
        Mockito.when(userRepository.isLoginExists(ArgumentMatchers.anyString())).thenReturn(false);

        NewUser newUser = NewUser.builder()
                .login("login")
                .password("1234567890")
                .fullName("name")
                .build();
        assertThatThrownBy(() -> userValidator.validateNewUser(newUser))
                .isInstanceOf(ConstraintViolationException.class)
                .extracting("errors")
                .asList()
                .containsExactly("Password has invalid size");
    }

    @Test
    void passwordHasProhibitedSymbolsTest() {
        Mockito.when(userRepository.isLoginExists(ArgumentMatchers.anyString())).thenReturn(false);

        NewUser newUser = NewUser.builder()
                .login("login")
                .password(":^)")
                .fullName("name")
                .build();
        assertThatThrownBy(() -> userValidator.validateNewUser(newUser))
                .isInstanceOf(ConstraintViolationException.class)
                .extracting("errors")
                .asList()
                .containsExactly("Password doesn't match regex");
    }

    @Test
    void smallPasswordHasProhibitedSymbolsTest() {
        Mockito.when(userRepository.isLoginExists(ArgumentMatchers.anyString())).thenReturn(false);

        NewUser newUser = NewUser.builder()
                .login("login")
                .password(":)")
                .fullName("name")
                .build();
        assertThatThrownBy(() -> userValidator.validateNewUser(newUser))
                .isInstanceOf(ConstraintViolationException.class)
                .extracting("errors")
                .asList()
                .containsExactlyInAnyOrder("Password has invalid size",
                        "Password doesn't match regex");
    }

    @Test
    void bigPasswordHasProhibitedSymbolsTest() {
        Mockito.when(userRepository.isLoginExists(ArgumentMatchers.anyString())).thenReturn(false);

        NewUser newUser = NewUser.builder()
                .login("login")
                .password(":))))))))))))")
                .fullName("name")
                .build();
        assertThatThrownBy(() -> userValidator.validateNewUser(newUser))
                .isInstanceOf(ConstraintViolationException.class)
                .extracting("errors")
                .asList()
                .containsExactlyInAnyOrder("Password has invalid size",
                        "Password doesn't match regex");
    }

}
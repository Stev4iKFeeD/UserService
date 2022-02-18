package kma.topic2.junit.service;

import kma.topic2.junit.exceptions.LoginExistsException;
import kma.topic2.junit.exceptions.UserNotFoundException;
import kma.topic2.junit.model.NewUser;
import kma.topic2.junit.model.User;
import kma.topic2.junit.repository.UserRepository;
import kma.topic2.junit.validation.UserValidator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @SpyBean
    private UserValidator userValidator;

    @Test
    void validUserCreationTest() {
        NewUser newUser = NewUser.builder()
                .login("myLogin1")
                .password("passwor")
                .fullName("name")
                .build();

        assertThatCode(() -> userService.createNewUser(newUser)).doesNotThrowAnyException();
        Mockito.verify(userValidator).validateNewUser(ArgumentMatchers.any());
        assertThatCode(() -> userService.getUserByLogin(newUser.getLogin())).doesNotThrowAnyException();
        assertThat(userRepository.isLoginExists(newUser.getLogin())).isTrue();
    }

    @Test
    void twoUsersWithTheSameLoginCreationTest() {
        NewUser newUser = NewUser.builder()
                .login("myLogin2")
                .password("passwor")
                .fullName("name")
                .build();

        assertThatCode(() -> userService.createNewUser(newUser)).doesNotThrowAnyException();
        assertThatThrownBy(() -> userService.createNewUser(newUser))
                .isInstanceOf(LoginExistsException.class)
                .hasMessage(String.format("Login %s already taken", newUser.getLogin()));
    }

    @Test
    void existingUserGettingTest() {
        NewUser newUser = NewUser.builder()
                .login("myLogin3")
                .password("passwor")
                .fullName("name")
                .build();

        userService.createNewUser(newUser);
        assertThatCode(() -> userService.getUserByLogin(newUser.getLogin())).doesNotThrowAnyException();
    }

    @Test
    void nonExistingUserGettingTest() {
        NewUser newUser = NewUser.builder()
                .login("myLogin4")
                .password("passwor")
                .fullName("name")
                .build();

        assertThatThrownBy(() -> userService.getUserByLogin(newUser.getLogin()))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Can't find user by login: " + newUser.getLogin());
    }

    @Test
    void userCreationValidationTest() {
        NewUser newUser = NewUser.builder()
                .login("myLogin5")
                .password("passwor")
                .fullName("name")
                .build();

        userService.createNewUser(newUser);
        User user = userService.getUserByLogin(newUser.getLogin());
        assertThat(user.getLogin()).isEqualTo(newUser.getLogin());
        assertThat(user.getPassword()).isEqualTo(newUser.getPassword());
        assertThat(user.getFullName()).isEqualTo(newUser.getFullName());
    }

}
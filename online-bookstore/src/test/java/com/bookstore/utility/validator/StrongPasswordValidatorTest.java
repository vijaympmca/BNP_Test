package com.bookstore.utility.validator;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class StrongPasswordValidatorTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @ParameterizedTest
    @ValueSource(strings = {"Secure#Pass1", "Abcdef1!", "My$tr0ngPassword", "Zz9@zzzz"})
    void validPasswords(String password) {
        assertThat(validator.isValid(password, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "short1!",            // too short
            "alllowercase1!",     // no uppercase
            "ALLUPPERCASE1!",     // no lowercase
            "NoDigits!!",         // no digit
            "NoSpecial123",       // no special character
            "Has Space1!"         // contains a space
    })
    void invalidPasswords(String password) {
        assertThat(validator.isValid(password, null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {""})
    void emptyIsLeftToNotBlank(String password) {
        assertThat(validator.isValid(password, null)).isTrue();
    }
}

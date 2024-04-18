package com.task.server.util;

import com.task.server.dto.request.SigninRequest;
import com.task.server.dto.request.SignupRequest;
import com.task.server.exception.ApiExceptionType;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

@UtilityClass
public class ValidationUtil {

    public static void validateSignupRequest(SignupRequest request) {
        ValidationUtil.validateLogin(request.login());
        ValidationUtil.validatePassword(request.password());
    }

    public static void validateSigninRequest(SigninRequest request) {
        ValidationUtil.validateLogin(request.login());
        ValidationUtil.validatePassword(request.password());
    }

    private static void validateLogin(String login) {
        boolean isValid = StringUtils.hasText(login);
        if (!isValid) {
            throw ApiExceptionType.VALIDATION_FAILURE.toException("login should not be empty");
        }
    }

    private static void validatePassword(String password) {
        boolean isValid = StringUtils.hasText(password);
        if (!isValid) {
            throw ApiExceptionType.VALIDATION_FAILURE.toException("password should not be empty");
        }
    }
}

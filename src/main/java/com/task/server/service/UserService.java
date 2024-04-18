package com.task.server.service;

import com.task.server.dao.BankAccountDao;
import com.task.server.dao.UserDao;
import com.task.server.dto.request.SendMoneyRequest;
import com.task.server.dto.request.SigninRequest;
import com.task.server.dto.request.SignupRequest;
import com.task.server.dto.response.BalanceResponse;
import com.task.server.dto.response.TokenResponse;
import com.task.server.entity.User;
import com.task.server.exception.ApiExceptionType;
import com.task.server.util.JwtUtil;
import com.task.server.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;
    private final UserDao userDao;
    private final BankAccountDao bankAccountDao;

    public void save(SignupRequest request) {
        ValidationUtil.validateSignupRequest(request);
        validateUniqueLogin(request.login());
        User user = User.builder()
            .login(request.login())
            .password(passwordEncoder.encode(request.password()))
            .build();
        userDao.save(user);
        log.info("user registration: \"{}\"", user.getLogin());
    }

    public TokenResponse login(SigninRequest request) {
        ValidationUtil.validateSigninRequest(request);
        User user = getUserByLogin(request.login());
        checkPasswordMatches(request.password(), user.getPassword(), user.getLogin());
        String token = jwtUtil.generateToken(user.getLogin());
        log.info("user authentication: \"{}\"", user.getLogin());
        return new TokenResponse(token);
    }

    public BalanceResponse sendMoney(String fromLogin, SendMoneyRequest request) {
        if (fromLogin.equals(request.toLogin())) {
            throw ApiExceptionType.VALIDATION_FAILURE.toException(
                "the sender and the recipient of the transfer must be different users");
        }
        Double balance = bankAccountDao.sendMoney(fromLogin, request.toLogin(), request.amount());
        log.info("user \"{}\" was sent {} to the user \"{}\"", fromLogin, request.amount(), request.toLogin());
        return new BalanceResponse(balance);
    }

    public BalanceResponse getBalance(String login) {
        Double balance = bankAccountDao.getBalance(login);
        log.info("user \"{}\" balance: {}", login, balance);
        return new BalanceResponse(balance);
    }

    private User getUserByLogin(String login) {
        return userDao.findByLogin(login)
            .orElseThrow(() -> ApiExceptionType.USER_NOT_FOUND.toException(login));
    }

    private void validateUniqueLogin(String login) {
        boolean exists = userDao.existsByLogin(login);
        if (exists) {
            throw ApiExceptionType.USER_ALREADY_EXISTS.toException(login);
        }
    }

    private void checkPasswordMatches(String rawPassword, String encodedPassword, String login) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw ApiExceptionType.WRONG_PASSWORD.toException(login);
        }
    }
}

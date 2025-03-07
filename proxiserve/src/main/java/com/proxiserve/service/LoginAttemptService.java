package com.proxiserve.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proxiserve.model.User;
import com.proxiserve.repository.UserRepository;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_TIME_DURATION = 15; // Minutes

    @Autowired
    private UserRepository userRepository;

    public void loginFailed(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return;

        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        if (user.getFailedLoginAttempts() >= MAX_ATTEMPTS) {
            user.setAccountLocked(true);
            user.setLockTime(LocalDateTime.now());
        }
        userRepository.save(user);
    }

    public void loginSucceeded(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return;

        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        user.setLockTime(null);
        userRepository.save(user);
    }

    public boolean isBlocked(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;

        if (user.isAccountLocked()) {
            long minutesSinceLock = ChronoUnit.MINUTES.between(user.getLockTime(), LocalDateTime.now());
            if (minutesSinceLock >= LOCK_TIME_DURATION) {
                user.setAccountLocked(false);
                user.setFailedLoginAttempts(0);
                user.setLockTime(null);
                userRepository.save(user);
                return false;
            }
            return true;
        }
        return false;
    }
}

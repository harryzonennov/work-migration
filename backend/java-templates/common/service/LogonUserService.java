package com.company.IntelligentPlatform.common.service;

import com.company.IntelligentPlatform.common.model.LogonUser;
import com.company.IntelligentPlatform.common.repository.LogonUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Replaces: ThorsteinPlatform LogonUserManager (ServiceEntityManager subclass)
 */
@Service
@Transactional
public class LogonUserService {

    @Autowired
    private LogonUserRepository logonUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public LogonUser create(LogonUser user) {
        user.setUuid(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(LogonUser.STATUS_INIT);
        return logonUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LogonUser getByUuid(String uuid) {
        return logonUserRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public LogonUser getByName(String name) {
        return logonUserRepository.findByName(name).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<LogonUser> getAll() {
        return logonUserRepository.findAll();
    }

    public LogonUser update(LogonUser user) {
        return logonUserRepository.save(user);
    }

    public void changePassword(String uuid, String newRawPassword) {
        LogonUser user = logonUserRepository.findById(uuid).orElseThrow();
        user.setPassword(passwordEncoder.encode(newRawPassword));
        user.setPasswordInitFlag(0);
        logonUserRepository.save(user);
    }

    public void setStatus(String uuid, int status) {
        LogonUser user = logonUserRepository.findById(uuid).orElseThrow();
        user.setStatus(status);
        logonUserRepository.save(user);
    }
}

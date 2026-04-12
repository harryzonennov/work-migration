package com.company.IntelligentPlatform.finance.service;

import com.company.IntelligentPlatform.finance.model.FinAccount;
import com.company.IntelligentPlatform.finance.model.FinAccountTitle;
import com.company.IntelligentPlatform.finance.repository.FinAccountRepository;
import com.company.IntelligentPlatform.finance.repository.FinAccountTitleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Replaces: ThorsteinFinance FinAccountManager (ServiceEntityManager subclass)
 *
 * FinAccount has a tri-step approval workflow:
 *   audit → record → verify
 * Each step is triggered by an explicit action method.
 */
@Service
@Transactional
public class FinAccountService {

    @Autowired
    private FinAccountRepository finAccountRepository;

    @Autowired
    private FinAccountTitleRepository finAccountTitleRepository;

    public FinAccount create(FinAccount account) {
        account.setUuid(UUID.randomUUID().toString());
        account.setAuditStatus(FinAccount.AUDIT_UNDONE);
        account.setRecordStatus(FinAccount.RECORDED_UNDONE);
        account.setVerifyStatus(FinAccount.VERIFIED_UNDONE);
        account.setFinanceTime(LocalDateTime.now());
        return finAccountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public FinAccount getByUuid(String uuid) {
        return finAccountRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<FinAccount> getByClient(String client) {
        return finAccountRepository.findByClient(client);
    }

    public FinAccount update(FinAccount account) {
        return finAccountRepository.save(account);
    }

    // --- tri-step workflow actions ---

    public void audit(String uuid, String auditBy, String auditNote, boolean approve) {
        FinAccount account = finAccountRepository.findById(uuid).orElseThrow();
        account.setAuditStatus(approve ? FinAccount.AUDIT_DONE : FinAccount.AUDIT_REJECT);
        account.setAuditBy(auditBy);
        account.setAuditTime(LocalDateTime.now());
        account.setAuditNote(auditNote);
        finAccountRepository.save(account);
    }

    public void record(String uuid, String recordBy, String recordNote) {
        FinAccount account = finAccountRepository.findById(uuid).orElseThrow();
        account.setRecordStatus(FinAccount.RECORDED_DONE);
        account.setRecordBy(recordBy);
        account.setRecordTime(LocalDateTime.now());
        account.setRecordNote(recordNote);
        finAccountRepository.save(account);
    }

    public void verify(String uuid, String verifyBy, String verifyNote) {
        FinAccount account = finAccountRepository.findById(uuid).orElseThrow();
        account.setVerifyStatus(FinAccount.VERIFIED_DONE);
        account.setVerifyBy(verifyBy);
        account.setVerifyTime(LocalDateTime.now());
        account.setVerifyNote(verifyNote);
        finAccountRepository.save(account);
    }

    // --- FinAccountTitle ---

    public FinAccountTitle createTitle(FinAccountTitle title) {
        title.setUuid(UUID.randomUUID().toString());
        return finAccountTitleRepository.save(title);
    }

    @Transactional(readOnly = true)
    public FinAccountTitle getTitleByUuid(String uuid) {
        return finAccountTitleRepository.findById(uuid).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<FinAccountTitle> getTitlesByClient(String client) {
        return finAccountTitleRepository.findByClient(client);
    }

    @Transactional(readOnly = true)
    public List<FinAccountTitle> getTitleChildren(String parentUUID) {
        return finAccountTitleRepository.findByParentAccountTitleUUID(parentUUID);
    }
}

package com.avesta.mastercrawler.service.impl;

import com.avesta.mastercrawler.model.SocialAccount;
import com.avesta.mastercrawler.model.Users;
import com.avesta.mastercrawler.repository.SocialAccountRepository;
import com.avesta.mastercrawler.service.ISocialAccountService;
import com.avesta.mastercrawler.service.social.SocialAdapter;
import com.avesta.mastercrawler.utility.CryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocialAccountServiceImpl implements ISocialAccountService {

    private final SocialAccountRepository socialAccountRepository;
    private final CryptoUtil cryptoUtil;
    private final List<SocialAdapter> socialAdapters;

    @Override
    @Transactional
    public SocialAccount save(SocialAccount socialAccount) {
        // Encrypt tokens before saving
        if (socialAccount.getAccessToken() != null) {
            socialAccount.setAccessToken(cryptoUtil.encrypt(socialAccount.getAccessToken()));
        }
        if (socialAccount.getRefreshToken() != null) {
            socialAccount.setRefreshToken(cryptoUtil.encrypt(socialAccount.getRefreshToken()));
        }
        return socialAccountRepository.save(socialAccount);
    }

    @Override
    public Optional<SocialAccount> findById(Long id) {
        return socialAccountRepository.findById(id);
    }

    @Override
    public List<SocialAccount> findByUser(Users user) {
        return socialAccountRepository.findByUserAndIsActiveTrue(user);
    }

    @Override
    public List<SocialAccount> findByUserAndPlatform(Users user, SocialAccount.SocialPlatform platform) {
        return socialAccountRepository.findAvailableAccountsForUserAndPlatform(user, platform);
    }

    @Override
    public List<SocialAccount> findGlobalAccounts() {
        return socialAccountRepository.findByIsGlobalTrueAndIsActiveTrue();
    }

    @Override
    public List<SocialAccount> findByPlatform(SocialAccount.SocialPlatform platform) {
        return socialAccountRepository.findByPlatformAndIsActiveTrue(platform);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        socialAccountRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Optional<SocialAccount> accountOpt = socialAccountRepository.findById(id);
        if (accountOpt.isPresent()) {
            SocialAccount account = accountOpt.get();
            account.setIsActive(false);
            socialAccountRepository.save(account);
        }
    }

    @Override
    @Transactional
    public void activate(Long id) {
        Optional<SocialAccount> accountOpt = socialAccountRepository.findById(id);
        if (accountOpt.isPresent()) {
            SocialAccount account = accountOpt.get();
            account.setIsActive(true);
            socialAccountRepository.save(account);
        }
    }

    @Override
    public boolean validateAccount(Long id) {
        Optional<SocialAccount> accountOpt = socialAccountRepository.findById(id);
        if (accountOpt.isEmpty()) {
            return false;
        }

        SocialAccount account = accountOpt.get();
        SocialAdapter adapter = findAdapter(account.getPlatform());
        
        if (adapter == null) {
            log.warn("No adapter found for platform: {}", account.getPlatform());
            return false;
        }

        return adapter.validateAccount(account);
    }

    @Override
    @Transactional
    public SocialAccount createAccount(Users user, SocialAccount.SocialPlatform platform, 
                                      String accessToken, String refreshToken, String settings) {
        SocialAccount account = SocialAccount.builder()
                .user(user)
                .platform(platform)
                .accessToken(accessToken) // Will be encrypted in save method
                .refreshToken(refreshToken) // Will be encrypted in save method
                .settings(settings)
                .isActive(true)
                .isGlobal(false)
                .build();

        return save(account);
    }

    @Override
    @Transactional
    public SocialAccount createGlobalAccount(SocialAccount.SocialPlatform platform, 
                                           String accessToken, String settings) {
        // Deactivate any existing global account for this platform
        Optional<SocialAccount> existingGlobal = socialAccountRepository
                .findByPlatformAndIsGlobalTrueAndIsActiveTrue(platform);
        
        if (existingGlobal.isPresent()) {
            SocialAccount existing = existingGlobal.get();
            existing.setIsActive(false);
            socialAccountRepository.save(existing);
        }

        SocialAccount account = SocialAccount.builder()
                .user(null)
                .platform(platform)
                .accessToken(accessToken) // Will be encrypted in save method
                .settings(settings)
                .isActive(true)
                .isGlobal(true)
                .build();

        return save(account);
    }

    @Override
    public void deleteByPlatformAndUser(SocialAccount.SocialPlatform platform, Users user) {
        List<SocialAccount> accounts = socialAccountRepository.findByPlatformAndUser(platform, user);
        socialAccountRepository.deleteAll(accounts);
        log.info("Deleted {} accounts for platform {} and user {}", accounts.size(), platform, user.getEmail());
    }

    @Override
    public Optional<SocialAccount> findByPlatformAndUser(SocialAccount.SocialPlatform platform, Users user) {
        List<SocialAccount> accounts = socialAccountRepository.findByPlatformAndUser(platform, user);
        return accounts.isEmpty() ? Optional.empty() : Optional.of(accounts.get(0));
    }

    private SocialAdapter findAdapter(SocialAccount.SocialPlatform platform) {
        return socialAdapters.stream()
                .filter(adapter -> adapter.getSupportedPlatform() == platform)
                .findFirst()
                .orElse(null);
    }
}

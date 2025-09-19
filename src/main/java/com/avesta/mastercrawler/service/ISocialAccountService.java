package com.avesta.mastercrawler.service;

import com.avesta.mastercrawler.model.SocialAccount;
import com.avesta.mastercrawler.model.Users;

import java.util.List;
import java.util.Optional;

public interface ISocialAccountService {
    
    SocialAccount save(SocialAccount socialAccount);
    
    Optional<SocialAccount> findById(Long id);
    
    List<SocialAccount> findByUser(Users user);
    
    List<SocialAccount> findByUserAndPlatform(Users user, SocialAccount.SocialPlatform platform);
    
    List<SocialAccount> findGlobalAccounts();
    
    List<SocialAccount> findByPlatform(SocialAccount.SocialPlatform platform);
    
    void delete(Long id);
    
    void deactivate(Long id);
    
    void activate(Long id);
    
    boolean validateAccount(Long id);
    
    SocialAccount createAccount(Users user, SocialAccount.SocialPlatform platform, 
                               String accessToken, String refreshToken, String settings);
    
    SocialAccount createGlobalAccount(SocialAccount.SocialPlatform platform, 
                                     String accessToken, String settings);
    
    void deleteByPlatformAndUser(SocialAccount.SocialPlatform platform, Users user);
    
    Optional<SocialAccount> findByPlatformAndUser(SocialAccount.SocialPlatform platform, Users user);
}

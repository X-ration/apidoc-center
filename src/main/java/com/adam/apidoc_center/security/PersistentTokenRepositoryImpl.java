package com.adam.apidoc_center.security;

import com.adam.apidoc_center.domain.RememberMeToken;
import com.adam.apidoc_center.repository.RememberMeTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public class PersistentTokenRepositoryImpl implements PersistentTokenRepository {

    @Autowired
    private RememberMeTokenRepository rememberMeTokenRepository;

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        RememberMeToken rememberMeToken = new RememberMeToken();
        rememberMeToken.setUsername(token.getUsername());
        rememberMeToken.setSeries(token.getSeries());
        rememberMeToken.setToken(token.getTokenValue());
        rememberMeToken.setLastUsed(token.getDate());
        rememberMeTokenRepository.save(rememberMeToken);
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        rememberMeTokenRepository.updateTokenAndLastUsedBySeries(tokenValue, lastUsed, series);
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {
        RememberMeToken rememberMeToken = rememberMeTokenRepository.queryBySeries(seriesId);
        PersistentRememberMeToken persistentRememberMeToken = new PersistentRememberMeToken(
                rememberMeToken.getUsername(), rememberMeToken.getSeries(), rememberMeToken.getToken(), rememberMeToken.getLastUsed()
        );
        return persistentRememberMeToken;
    }

    @Override
    public void removeUserTokens(String username) {
        rememberMeTokenRepository.deleteByUsername(username);
    }
}
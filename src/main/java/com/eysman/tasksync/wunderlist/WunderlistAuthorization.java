package com.eysman.tasksync.wunderlist;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;

import java.io.IOException;

/**
 * Created by jeysman on 2/4/17.
 */
public class WunderlistAuthorization extends AuthorizationCodeInstalledApp {
    /**
     * @param flow     authorization code flow
     * @param receiver verification code receiver
     */
    public WunderlistAuthorization(AuthorizationCodeFlow flow, VerificationCodeReceiver receiver) {
        super(flow, receiver);
    }

    public Credential authorize(String userId) throws IOException {
        try {
            Credential credential = getFlow().loadCredential(userId);
            if (credential != null
                    && (credential.getRefreshToken() != null ||
                            credential.getExpiresInSeconds() == null ||
                            credential.getExpiresInSeconds() > 60)) {
                return credential;
            }
            // open in browser
            String redirectUri = getReceiver().getRedirectUri();
            AuthorizationCodeRequestUrl authorizationUrl =
                    getFlow().newAuthorizationUrl().setRedirectUri(redirectUri);
            onAuthorization(authorizationUrl);
            // receive authorization code and exchange it for an access token
            String code = getReceiver().waitForCode();
            TokenResponse response = getFlow().newTokenRequest(code).setRedirectUri(redirectUri).execute();
            // store credential and return it
            return getFlow().createAndStoreCredential(response, userId);
        } finally {
            getReceiver().stop();
        }
    }
}

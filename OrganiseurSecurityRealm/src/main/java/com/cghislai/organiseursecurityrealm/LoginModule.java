/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseursecurityrealm;

import java.util.Arrays;
import static java.util.logging.Level.FINEST;
import javax.security.auth.login.LoginException;
import com.sun.appserv.security.AppservPasswordLoginModule;
import java.util.logging.Logger;

/**
 *
 * @author cghislai
 */
public class LoginModule extends AppservPasswordLoginModule {

    @Override
    protected void authenticateUser() throws LoginException {
        checkRealm();
        checkUser();

        final UserRealm jdbcRealm = (UserRealm) _currentRealm;
        String[] grpList = jdbcRealm.authenticate(_username, getPassword());

        if (grpList == null) {
            throw new LoginException("No groups found for user");
        }
        
        Logger logger = Logger.getLogger(getClass().getName());

        if (logger.isLoggable(FINEST)) {
            logger.finest("JDBC login succeeded for: " + _username
                    + " groups:" + Arrays.toString(grpList));
        }

        commitUserAuthentication(grpList);
    }

    /**
     * @throws LoginException when username is null or empty
     */
    private void checkUser() throws LoginException {
        // A JDBC user must have a name not null and non-empty.
        if (_username == null || _username.isEmpty()) {
            throw new LoginException("Username must have a value");
        }
    }

    /**
     * @throws LoginException when Realm is not the expected Realm
     */
    private void checkRealm() throws LoginException {
        if (!(_currentRealm instanceof UserRealm)) {
            throw new LoginException("Wrong Realm, expected a " + UserRealm.class.getName());
        }
    }
}

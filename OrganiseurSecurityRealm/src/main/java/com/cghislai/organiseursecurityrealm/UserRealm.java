/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseursecurityrealm;

import com.sun.appserv.security.AppservRealm;
import com.sun.enterprise.security.auth.realm.BadRealmException;
import com.sun.enterprise.security.auth.realm.NoSuchRealmException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author cghislai
 */
@Service(name = UserRealm.SERVICE_NAME)
public class UserRealm extends AppservRealm {

    final static String GROUP_NAME_USER = "USER";
    final static String GROUP_NAME_ADMIN = "ADMIN";

    final static String SERVICE_NAME = "ilesDePaixRealm";

    final static String PARAM_DIGEST_ALGORITHM = "digest-algorithm";
    final static String PARAM_PASSWORD_CHARSET = "password-charset";

    final static String PARAM_PRINCIPAL_FROM_MAIL_QUERY = "PARAM_PRINCIPAL_FROM_MAIL_QUERY";
    final static String PARAM_PRINCIPAL_FROM_LOGIN_QUERY = "PARAM_PRINCIPAL_FROM_LOGIN_QUERY";
    final static String PARAM_PASSWORD_QUERY = "PARAM_PASSWORD_QUERY";
    final static String PARAM_ROLES_QUERY = "PARAM_ROLES_QUERY";
    final static String PARAM_JNDI_DATASOURCE = "datasource-jndi";

    private static final String DEFAULT_DIGEST_ALGORITHM = "SHA-256";

    private static final String DEFAULT_JNDI_DATASOURCE = "mysql/ilesdepaix";
    private static final String DEFAULT_PRINCIPAL_ID_FROM_MAIL_QUERY = "select ID from ouser where EMAIL = '?'";
    private static final String DEFAULT_PRINCIPAL_ID_FROM_LOGIN_QUERY = "select ID from ouser where USER_NAME = ?";
    private static final String DEFAULT_PASSWORD_FROM_PRINCIPAL_ID_QUERY = "select PASSWORD_HASH from ouser where ID = ?";
    private static final String DEFAULT_ROLES_FROM_PRINCIPAL_ID_QUERY = "select IS_ADMIN from ouser where ID = ?";

    private static final Map<String, String> OPTIONAL_PROPERTIES = new HashMap<>();

    static {
        OPTIONAL_PROPERTIES.put(PARAM_DIGEST_ALGORITHM, DEFAULT_DIGEST_ALGORITHM);
        OPTIONAL_PROPERTIES.put(PARAM_PASSWORD_CHARSET, Charset.defaultCharset().name());

        OPTIONAL_PROPERTIES.put(PARAM_JNDI_DATASOURCE, DEFAULT_JNDI_DATASOURCE);

        OPTIONAL_PROPERTIES.put(PARAM_PRINCIPAL_FROM_LOGIN_QUERY, DEFAULT_PRINCIPAL_ID_FROM_LOGIN_QUERY);
        OPTIONAL_PROPERTIES.put(PARAM_PRINCIPAL_FROM_MAIL_QUERY, DEFAULT_PRINCIPAL_ID_FROM_MAIL_QUERY);
        OPTIONAL_PROPERTIES.put(PARAM_PASSWORD_QUERY, DEFAULT_PASSWORD_FROM_PRINCIPAL_ID_QUERY);
        OPTIONAL_PROPERTIES.put(PARAM_ROLES_QUERY, DEFAULT_ROLES_FROM_PRINCIPAL_ID_QUERY);

    }
    private static final Logger LOG = Logger.getLogger(UserRealm.class.getName());

    /**
     * @return a descriptive string representing the type of authentication done
     * by this realm
     */
    @Override
    public String getAuthType() {
        return "Custom Jdbc User Realm";
    }

    @Override
    protected void init(final Properties parameters) throws BadRealmException, NoSuchRealmException {
        super.init(parameters);
        LOG.log(Level.FINE, "Initializing {0}", this.getClass().getSimpleName());

        // Among the other custom properties, there is a property jaas-context (which is explained later in this post).
        // This property should be set using the call setProperty method implemented in the parent class.
        /// From: https://blogs.oracle.com/nithya/entry/groups_in_custom_realms
        checkAndSetProperty(JAAS_CONTEXT_PARAM, parameters);

        for (Map.Entry<String, String> entry : OPTIONAL_PROPERTIES.entrySet()) {
            setOptionalProperty(entry.getKey(), parameters, entry.getValue());
        }

    }

    private void setOptionalProperty(final String name, final Properties parameters, final String defaultValue) throws BadRealmException {
        checkAndSetProperty(name, parameters.getProperty(name, defaultValue));
    }

    private void checkAndSetProperty(final String name, final Properties parameters) throws BadRealmException {
        checkAndSetProperty(name, parameters.getProperty(name));
    }

    private void checkAndSetProperty(final String name, final String value) throws BadRealmException {
        if (value == null) {
            String message = sm.getString("realm.missingprop", name, SERVICE_NAME);
            throw new BadRealmException(message);
        }
        LOG.log(FINE, "Setting property {0} to ''{1}''", new Object[]{name, value});

        super.setProperty(name, value);
    }

    @Override
    public Enumeration getGroupNames(final String username) {
        Long userId = getUserId(username);
        if (userId == null) {
            return null;
        }
        return Collections.enumeration(getGroups(userId));
    }

    private Long getUserId(String userName) {
        Long userId = null;
        if (userName.contains("@")) {
            userId = getUserIdFromMail(userName);
        }
        if (userId == null) {
            userId = getUserIdFromLogin(userName);
        }
        return userId;
    }

    private Long getUserIdFromLogin(String userLogin) {
        final String qureyString = getProperty(PARAM_PRINCIPAL_FROM_LOGIN_QUERY);
        LOG.log(FINEST, "Executing query ''{0}'' with username {1}", new Object[]{qureyString, userLogin});

        Long userId = null;
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(qureyString)) {
            statement.setString(1, userLogin);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    userId = resultSet.getLong(1);
                }
                if (resultSet.next()) {
                    return null;
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        LOG.log(FINEST, "User with login {0} has id {1}", new Object[]{userLogin, userId});
        return userId;
    }

    private Long getUserIdFromMail(String userMail) {
        final String qureyString = getProperty(PARAM_PRINCIPAL_FROM_MAIL_QUERY);
        LOG.log(FINEST, "Executing query ''{0}'' with user mail {1}", new Object[]{qureyString, userMail});

        Long userId = null;
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(qureyString)) {
            statement.setString(1, userMail);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    userId = resultSet.getLong(1);
                }
                if (resultSet.next()) {
                    return null;
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        LOG.log(FINEST, "User with mail {0} has id {1}", new Object[]{userMail, userId});
        return userId;
    }

    private List<String> getGroups(final Long userId) {
        List<String> groupNames = new ArrayList<>();
        final String securityRolesQuery = getProperty(PARAM_ROLES_QUERY);
        LOG.log(FINEST, "Executing query ''{0}'' with user id {1}", new Object[]{securityRolesQuery, userId});

        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(securityRolesQuery)) {
            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    boolean isAdmin = resultSet.getBoolean(1);
                    groupNames.add(GROUP_NAME_USER);
                    if (isAdmin) {
                        groupNames.add(GROUP_NAME_ADMIN);
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        LOG.log(FINEST, "User {0} has groups {1}", new Object[]{userId, groupNames});
        return groupNames;
    }

    private Connection getConnection() {
        final String dataSourceJndi = getProperty(PARAM_JNDI_DATASOURCE);
        try {
            InitialContext context = new InitialContext();
            DataSource datasource = (DataSource) context.lookup(dataSourceJndi);
            return datasource.getConnection();
        } catch (NamingException | SQLException e) {
            throw new IllegalStateException("Error retrieving connection", e);
        }
    }

    public String[] authenticate(final String username, final String password) throws LoginException {
        LOG.log(FINEST, "Authenticating user {0}", username);

        Long userId = getUserId(username);
        if (userId == null) {
            LOG.log(WARNING, "No user with name {0}", username);
            throw new LoginException("Invalid username");
        }

        final boolean authenticated = hasValidCredentials(userId, password);
        final String[] groups = authenticated ? convertToArray(getGroups(userId)) : null;

        LOG.log(FINEST, "User {0}, authenticated {1} has groups {2}", new Object[]{username, authenticated, Arrays.deepToString(groups)});
        return groups;
    }

    private String[] convertToArray(final List<String> groups) {
        String[] groupsArray = new String[groups.size()];
        groups.toArray(groupsArray);
        return groupsArray;
    }

    private boolean hasValidCredentials(final Long userId, final String givenPassword) {
        final String principalQuery = getProperty(PARAM_PASSWORD_QUERY);
        LOG.log(FINEST, "Executing query ''{0}'' with user id {1}", new Object[]{principalQuery, userId});

        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(principalQuery)) {

            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return isValidPassword(userId, givenPassword, resultSet);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isValidPassword(final Long userId, final String givenPassword, final ResultSet resultSet) throws SQLException {
        if (!resultSet.next()) {
            LOG.log(INFO, "No user found for id {0}!", userId);
            return false;
        }

        String databasePassword = resultSet.getString(1);
        if (databasePassword == null || databasePassword.trim().isEmpty()) {
            // Password should be required so log with warning
            LOG.log(WARNING, "User {0} has NO Password!", userId);
            return givenPassword == null || givenPassword.trim().isEmpty();
        }
        boolean passwordsEqual = givenPassword.equals(databasePassword);
        if (!passwordsEqual) {
            LOG.log(INFO, "Invalid Password entered for user {0}!", userId);
            return false;
        }

        LOG.log(FINEST, "User {0} has valid Password.", userId);

        return true;
    }
}

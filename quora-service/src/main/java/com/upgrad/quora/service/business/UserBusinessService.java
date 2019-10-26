package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.User;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import com.upgrad.quora.service.util.QuoraUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class UserBusinessService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    /**
     * This method saves the registered user information to the Database
     * Encrypts the user password before storing in the DB
     * Checks if the existing user is trying to signup again by matching username/email
     * If so, throws error message as already username taken or already registered
     *
     * @param user The user information to be saved as part of signup
     * @return The persisted user details with the id value generated
     * @throws SignUpRestrictedException if the user details matches with the existing records
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public User signup(User user) throws SignUpRestrictedException {

        String password = user.getPassword();
        String[] encryptedText = cryptographyProvider.encrypt(user.getPassword());
        user.setSalt(encryptedText[0]);
        user.setPassword(encryptedText[1]);
        if (userDao.getUserByUserName(user.getUserName()) != null) {
            throw new SignUpRestrictedException("SGR-001", "Try any other Username, this Username has already been taken");
        }
        if (userDao.getUserByEmail(user.getEmail()) != null) {
            throw new SignUpRestrictedException("SGR-002", "This user has already been registered, try with any other emailId");
        }
        return userDao.createUser(user);
    }

    /**
     * This method validates the user session by making use of the access token
     * If it is expired or invalid, then throws back the exception asking the user to sign in
     * If the user session is active, then pulls the UUID of the user̥
     *
     * @param authorization holds the Bearer access token for authenticating the user
     * @return uuid of the user
     * @throws SignOutRestrictedException if the access token is expired or user never signed in
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public String getUserUUID(String authorization) throws SignOutRestrictedException {
        String[] bearerToken = authorization.split(QuoraUtil.BEARER_TOKEN);
        if(bearerToken != null && bearerToken.length > 1) {
            UserAuthEntity userAuthEntity = userDao.getUserAuthToken(bearerToken[1]);
            if (isUserSessionValid(userAuthEntity)) {
                userAuthEntity.setLogoutAt(ZonedDateTime.now());
                userDao.updateUserAuthEntity(userAuthEntity);
                return userAuthEntity.getUuid();
            }
        }
        throw new SignOutRestrictedException("SGR-001", "User is not Signed in");
    }

    /**
     * This method checks if the user session is active based on the access token/logout at
     * If the user has ̥already signed out, even though the token isn't expired still he/she didn't login
     * The access token expiry should always be in future, only then it isn't expired
     * If the token expiry falls before the current time, then user should be asked to login
     *
     * @param userAuthEntity The authentication Entity object holding the information about user login and access token
     * @return true if all session conditions satisfy, false otherwise
     */
    public Boolean isUserSessionValid(UserAuthEntity userAuthEntity) {
        if (userAuthEntity != null && userAuthEntity.getLogoutAt() == null
                && userAuthEntity.getExpiresAt() != null) {
            Long timeDifference = ChronoUnit.MILLIS.between(ZonedDateTime.now(), userAuthEntity.getExpiresAt());
            // Negative timeDifference indicates an expired access token,
            // difference should be with in the limit, token will be expired after 8 hours
            return (timeDifference >= 0 && timeDifference <= QuoraUtil.EIGHT_HOURS_IN_MILLIS);
        }
        // Token expired or user already logged out or user never signed in before(may also be the case of invalid token)
        return false;
    }
}

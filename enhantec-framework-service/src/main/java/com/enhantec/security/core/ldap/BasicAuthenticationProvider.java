package com.enhantec.security.core.ldap;
import com.enhantec.security.common.models.EHUser;
import com.enhantec.security.common.services.EHUserService;
import com.enhantec.security.core.enums.AuthType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/*
    20220415 John Customize DaoAuthenticationProvider to support multiple authentication types
 */

@Component
@RequiredArgsConstructor
@Getter
public class BasicAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

        /**
         * The plaintext password used to perform PasswordEncoder#matches(CharSequence,
         * String)} on when the user is not found to avoid SEC-2056.
         */
        private static final String USER_NOT_FOUND_PASSWORD = "userNotFoundPassword";

        private final PasswordEncoder passwordEncoder;

        /**
         * The password used to perform {@link PasswordEncoder#matches(CharSequence, String)}
         * on when the user is not found to avoid SEC-2056. This is necessary, because some
         * {@link PasswordEncoder} implementations will short circuit if the password is not
         * in a valid format.
         */
        private volatile String userNotFoundEncodedPassword;


        private final UserDetailsService userDetailsService;

        private final EHUserService userService;

        private UserDetailsPasswordService userDetailsPasswordService;


        @Override
        @SuppressWarnings("deprecation")
        protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                      UsernamePasswordAuthenticationToken authentication) throws org.springframework.security.core.AuthenticationException {
            if (authentication.getCredentials() == null) {
                this.logger.debug("Failed to authenticate since no credentials provided");
                throw new BadCredentialsException(this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
            }
            String presentedPassword = authentication.getCredentials().toString();
            if (!this.passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
                this.logger.debug("Failed to authenticate since password does not match stored value");
                throw new BadCredentialsException(this.messages
                        .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
            }
        }

        @Override
        protected void doAfterPropertiesSet() {
            Assert.notNull(this.userDetailsService, "A UserDetailsService must be set");
        }

        @Override
        protected final UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
                throws AuthenticationException {

            userService.checkIfUserExists(username);

            prepareTimingAttackProtection();
            try {
                UserDetails loadedUser = this.getUserDetailsService().loadUserByUsername(username);
                if (loadedUser == null) {
                    throw new InternalAuthenticationServiceException(
                            "UserDetailsService returned null, which is an interface contract violation");
                }

                //john add -- start
                EHUser user = (EHUser)loadedUser;
                if(!AuthType.BASIC.equals(user.getAuthType())){
                    logger.info("User auth type is not BASIC, will forward to next auth provider. Current user auth type:  " + user.getAuthType());
                    throw new UsernameNotFoundException("User auth type is not BASIC, will forward to next auth provider. Current user auth type:  " + user.getAuthType());
                }

                //john add -- end

                return loadedUser;
            }
            catch (UsernameNotFoundException ex) {
                mitigateAgainstTimingAttack(authentication);
                throw ex;
            }
            catch (InternalAuthenticationServiceException ex) {
                throw ex;
            }
            catch (Exception ex) {
                throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
            }
        }

        @Override
        protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
                                                             UserDetails user) {
            boolean upgradeEncoding = this.userDetailsPasswordService != null
                    && this.passwordEncoder.upgradeEncoding(user.getPassword());
            if (upgradeEncoding) {
                String presentedPassword = authentication.getCredentials().toString();
                String newPassword = this.passwordEncoder.encode(presentedPassword);
                user = this.userDetailsPasswordService.updatePassword(user, newPassword);
            }
            return super.createSuccessAuthentication(principal, authentication, user);
        }

        private void prepareTimingAttackProtection() {
            if (this.userNotFoundEncodedPassword == null) {
                this.userNotFoundEncodedPassword = this.passwordEncoder.encode(USER_NOT_FOUND_PASSWORD);
            }
        }

        private void mitigateAgainstTimingAttack(UsernamePasswordAuthenticationToken authentication) {
            if (authentication.getCredentials() != null) {
                String presentedPassword = authentication.getCredentials().toString();
                this.passwordEncoder.matches(presentedPassword, this.userNotFoundEncodedPassword);
            }
        }

    public void setUserDetailsPasswordService(UserDetailsPasswordService userDetailsPasswordService) {
        this.userDetailsPasswordService = userDetailsPasswordService;
    }

    }

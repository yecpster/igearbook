package com.igearbook.action;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import net.jforum.ControllerUtils;
import net.jforum.JForumExecutionContext;
import net.jforum.SessionFacade;
import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.UserDAO;
import net.jforum.dao.UserSessionDAO;
import net.jforum.entities.User;
import net.jforum.entities.UserSession;
import net.jforum.repository.SecurityRepository;
import net.jforum.util.I18n;
import net.jforum.util.MD5;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Maps;
import com.igearbook.constant.UserAPISource;
import com.igearbook.dao.UserApiDao;
import com.igearbook.dao.UserDao;
import com.igearbook.entities.UserApi;
import com.opensymphony.xwork2.ActionContext;
import com.qq.connect.api.OpenID;
import com.qq.connect.api.qzone.UserInfo;
import com.qq.connect.javabeans.AccessToken;
import com.qq.connect.javabeans.qzone.UserInfoBean;
import com.qq.connect.javabeans.weibo.TweetInfo;
import com.qq.connect.oauth.Oauth;

@Namespace("/qqapi")
public class QQApiAction extends BaseAction {
    private static final long serialVersionUID = 7587622153127430L;
    private static final long FIVE_DAYS_MILLISECONDS = 1000 * 3600 * 24 * 5;

    @Autowired
    private UserApiDao userApiDao;

    @Autowired
    private UserDao userDao;

    private String password_confirm;
    private String autologin;
    private User user;

    public void setUserApiDao(final UserApiDao userApiDao) {
        this.userApiDao = userApiDao;
    }

    public void setUserDao(final UserDao userDao) {
        this.userDao = userDao;
    }

    public String getPassword_confirm() {
        return password_confirm;
    }

    public void setPassword_confirm(final String password_confirm) {
        this.password_confirm = password_confirm;
    }

    public String getAutologin() {
        return autologin;
    }

    public void setAutologin(final String autologin) {
        this.autologin = autologin;
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    @Action(value = "login", results = { @Result(name = SUCCESS, location = "${authUrl}", type = "redirect") })
    public String login() throws Exception {
        final ActionContext context = ServletActionContext.getContext();
        final HttpServletRequest request = ServletActionContext.getRequest();
        final String authUrl = new Oauth().getAuthorizeURL(request);
        context.put("authUrl", authUrl);

        final String redirectPath = (String) SessionFacade.getAttribute(SessionFacade.REDIRECT_KEY);
        final String referer = request.getHeader("Referer");
        if (StringUtils.isBlank(redirectPath) && StringUtils.isNotBlank(referer)) {
            SessionFacade.setAttribute(SessionFacade.REDIRECT_KEY, referer);
        }

        return SUCCESS;
    }

    @Action(value = "testlogin", results = { @Result(name = SUCCESS, location = "qqApi_testlogin.ftl") })
    public String testlogin() throws Exception {
        return this.login();
    }

    @Action(value = "afterlogin", results = { @Result(name = SUCCESS, location = "qqApi_afterLogin.ftl"),
            @Result(name = SessionFacade.REDIRECT_KEY, location = "${redirectPath}", type = "redirect") })
    public String afterLogin() throws Exception {
        final ActionContext context = ServletActionContext.getContext();
        final HttpServletRequest request = ServletActionContext.getRequest();
        final AccessToken accessTokenObj = (new Oauth()).getAccessTokenByRequest(request);

        if (StringUtils.isBlank(accessTokenObj.getAccessToken())) {
            // 我们的网站被CSRF攻击了或者用户取消了授权
        } else {
            final String accessToken = accessTokenObj.getAccessToken();
            final long tokenExpireIn = accessTokenObj.getExpireIn() * 1000;

            final OpenID openIDObj = new OpenID(accessToken);
            final String openId = openIDObj.getUserOpenID();

            final UserInfo qzoneUserInfo = new UserInfo(accessToken, openId);
            final UserInfoBean userInfoBean = qzoneUserInfo.getUserInfo();
            if (userInfoBean.getRet() != 0) {
                throw new Exception(userInfoBean.getMsg());
            }

            UserApi userApi = userApiDao.getByOpenId(openId);
            final Date now = new Date();
            if (userApi == null) {
                final User user = insertUser(userInfoBean);
                userApi = new UserApi();
                userApi.setAccessToken(accessToken);
                userApi.setLastUpdateDate(now);
                userApi.setOpenId(openId);
                userApi.setSource(UserAPISource.QQ);
                userApi.setTokenExpireIn(tokenExpireIn);
                userApi.setUser(user);
                userApiDao.add(userApi);
            }

            if (shouldUpdateToken(userApi)) {
                userApi.setAccessToken(accessToken);
                userApi.setTokenExpireIn(tokenExpireIn);
                userApi.setLastUpdateDate(now);
                userApiDao.update(userApi);
            }
            final User loginUser = userApi.getUser();
            logUserIn(loginUser);
            loginUser.setAvatar(userInfoBean.getAvatar().getAvatarURL100());
            this.user = loginUser;
            if (loginUser.isApiUserActive()) {
                final String redirectPath = (String) SessionFacade.getAttribute(SessionFacade.REDIRECT_KEY);
                if (StringUtils.isNotBlank(redirectPath)) {
                    context.put(SessionFacade.REDIRECT_KEY, redirectPath);
                    SessionFacade.removeAttribute(SessionFacade.REDIRECT_KEY);
                } else {
                    context.put(SessionFacade.REDIRECT_KEY, "/");
                }
                return SessionFacade.REDIRECT_KEY;
            }

            final com.qq.connect.api.weibo.UserInfo weiboUserInfo = new com.qq.connect.api.weibo.UserInfo(accessToken, openId);
            final com.qq.connect.javabeans.weibo.UserInfoBean weiboUserInfoBean = weiboUserInfo.getUserInfo();
            if (weiboUserInfoBean.getRet() == 0) {
                final TweetInfo qqweibo = weiboUserInfoBean.getTweetInfo();
                if (qqweibo != null) {
                    context.put("qqweibo", qqweibo);
                }

                context.put("qqweiboOrigText", weiboUserInfoBean.getTweetInfo().getOrigText());
            } else {
               // throw new Exception(weiboUserInfoBean.getMsg());
            }
        }

        return SUCCESS;
    }

    private boolean shouldUpdateToken(final UserApi userApi) {
        final Date now = new Date();
        final Date lastUpdateDate = userApi.getLastUpdateDate();
        final long tokenExpireIn = userApi.getTokenExpireIn();
        final long fiveDaysTestTime = lastUpdateDate.getTime() + tokenExpireIn + FIVE_DAYS_MILLISECONDS;

        // If the token will expire in 5 days, should update it.
        return fiveDaysTestTime > now.getTime();
    }

    @Action(value = "bind", results = { @Result(name = SUCCESS, location = "${redirectPath}", type = "redirect"),
            @Result(name = INPUT, location = "qqApi_afterLogin.ftl") })
    public String bind() throws Exception {
        final UserSession userSession = SessionFacade.getUserSession();
        final int anonymousUserId = SystemGlobals.getIntValue(ConfigKeys.ANONYMOUS_USER_ID);
        if (userSession.getUserId() == anonymousUserId) {
            return PERMISSION;
        }

        final ActionContext context = getContext();
        parseBasicAuthentication();
        final UserDAO um = DataAccessDriver.getInstance().newUserDAO();
        final User vUser = um.validateLogin(user.getUsername(), user.getPassword());
        if (vUser != null) {
            final int oldUserId = userSession.getUserId();
            final User oldUser = userDao.get(oldUserId);
            if (oldUser.isApiUser() && !oldUser.isApiUserActive()) {
                final UserApi userApi = userApiDao.getByUserAndUserAPISource(oldUser, UserAPISource.QQ);
                userApi.setUser(vUser);
                userApiDao.update(userApi);
                vUser.setApiUser(true);
                vUser.setApiUserActive(true);
                userDao.update(vUser);
                um.delete(oldUser.getId());
                logUserIn(vUser);
            }
        } else {
            this.user = um.selectById(userSession.getUserId());
            this.addActionError(I18n.getMessage("Login.invalidLogin"));
            return INPUT;
        }

        final String redirectPath = (String) SessionFacade.getAttribute(SessionFacade.REDIRECT_KEY);
        context.put(SessionFacade.REDIRECT_KEY, "/");
        if (StringUtils.isNotBlank(redirectPath)) {
            // context.put(SessionFacade.REDIRECT_KEY, redirectPath);
            SessionFacade.removeAttribute(SessionFacade.REDIRECT_KEY);
        } else {
            context.put(SessionFacade.REDIRECT_KEY, "/");
        }

        return SUCCESS;
    }

    @Action(value = "reg", results = { @Result(name = SUCCESS, location = "${redirectPath}", type = "redirect"),
            @Result(name = INPUT, location = "qqApi_afterLogin.ftl") })
    public String register() throws Exception {
        final ActionContext context = getContext();
        context.put("accountType", "noAccount");
        final UserSession userSession = SessionFacade.getUserSession();
        final int anonymousUserId = SystemGlobals.getIntValue(ConfigKeys.ANONYMOUS_USER_ID);
        if (userSession.getUserId() == anonymousUserId) {
            return PERMISSION;
        }

        final UserDAO um = DataAccessDriver.getInstance().newUserDAO();
        
        final String username = StringUtils.trimToEmpty(user.getUsername());
        final String password = StringUtils.trimToEmpty(user.getPassword());

        final User loginUser = um.selectById(userSession.getUserId());
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            this.addActionError(I18n.getMessage("UsernamePasswordCannotBeNull"));
            this.user = loginUser;
            return INPUT;
        }

        if (StringUtils.isBlank(password_confirm) || !password_confirm.equals(user.getPassword())) {
            this.addActionError(I18n.getMessage("User.passwordNotMatch"));
            this.user = loginUser;
            return INPUT;
        }

        if (username.length() > SystemGlobals.getIntValue(ConfigKeys.USERNAME_MAX_LENGTH)) {
            this.addActionError(I18n.getMessage("User.usernameTooBig"));
            this.user = loginUser;
            return INPUT;
        }

        if (username.indexOf('<') > -1 || username.indexOf('>') > -1) {
            this.addActionError(I18n.getMessage("User.usernameInvalidChars"));
            this.user = loginUser;
            return INPUT;
        }

        final User inSessionUser = userDao.get(userSession.getUserId());
        // Only inactive apiUser permit to process this action.
        if (!inSessionUser.isApiUser() || inSessionUser.isApiUserActive()) {
            this.user = loginUser;
            return PERMISSION;
        }

        final String lgoinUsernane = StringUtils.trimToEmpty(inSessionUser.getUsername());
        if (!StringUtils.equals(username, lgoinUsernane) && um.isUsernameRegistered(username)) {
            this.addActionError(I18n.getMessage("UsernameExists"));
            this.user = loginUser;
            return INPUT;
        }
        inSessionUser.setUsername(username);
        inSessionUser.setPassword(MD5.crypt(password));
        inSessionUser.setApiUser(true);
        inSessionUser.setApiUserActive(true);
        userDao.update(inSessionUser);
        final User updateLoginUser = um.selectById(inSessionUser.getId());
        logUserIn(updateLoginUser);

        final String redirectPath = (String) SessionFacade.getAttribute(SessionFacade.REDIRECT_KEY);
        context.put(SessionFacade.REDIRECT_KEY, "/");
        if (StringUtils.isNotBlank(redirectPath)) {
            // context.put(SessionFacade.REDIRECT_KEY, redirectPath);
            SessionFacade.removeAttribute(SessionFacade.REDIRECT_KEY);
        } else {
            context.put(SessionFacade.REDIRECT_KEY, "/");
        }

        return SUCCESS;
    }

    private static boolean hasBasicAuthentication(final HttpServletRequest request) {
        final String auth = request.getHeader("Authorization");
        return (auth != null && auth.startsWith("Basic "));
    }

    private boolean parseBasicAuthentication() {
        final HttpServletRequest request = ServletActionContext.getRequest();
        if (hasBasicAuthentication(request)) {
            final String auth = request.getHeader("Authorization");
            String decoded;

            try {
                decoded = new String(new sun.misc.BASE64Decoder().decodeBuffer(auth.substring(6)));
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }

            final int p = decoded.indexOf(':');

            if (p != -1) {
                user.setUsername(decoded.substring(0, p));
                user.setPassword(decoded.substring(p + 1));
                return true;
            }
        }
        return false;
    }

    private User insertUser(final UserInfoBean userInfoBean) {
        final UserDAO dao = DataAccessDriver.getInstance().newUserDAO();
        final User u = new User();
        final String userName = StringUtils.trimToEmpty(userInfoBean.getNickname());
        if (dao.isUsernameRegistered(userName)) {
            for (int i = 2; i < 30; i++) {
                if (!dao.isUsernameRegistered(userName + i)) {
                    u.setUsername(userName + i);
                    break;
                }
            }
        } else {
            u.setUsername(userName);
        }

        u.setPassword(MD5.crypt("*IgearbookIgnore53681"));
        final int newUserId = dao.addNew(u);
        dao.writeUserActive(newUserId);
        final User setApiUser = userDao.get(newUserId);
        setApiUser.setApiUser(true);
        userDao.update(setApiUser);
        final User userUpdate = dao.selectById(newUserId);
        userUpdate.setAvatar(userInfoBean.getAvatar().getAvatarURL100());
        userUpdate.setGender(userInfoBean.getGender());
        dao.update(userUpdate);
        return userUpdate;
    }

    private void logUserIn(final User u) {
        SessionFacade.makeLogged();
        final String sessionId = SessionFacade.isUserInSession(u.getId());
        final UserSession userSession = new UserSession(SessionFacade.getUserSession());
        // Remove the "guest" session
        SessionFacade.remove(userSession.getSessionId());
        userSession.dataToUser(u);
        final UserSession currentUs = SessionFacade.getUserSession(sessionId);
        // Check if the user is returning to the system
        // before its last session has expired ( hypothesis )
        UserSession tmpUs;
        if (sessionId != null && currentUs != null) {
            // Write its old session data
            SessionFacade.storeSessionData(sessionId, JForumExecutionContext.getConnection());
            tmpUs = new UserSession(currentUs);
            SessionFacade.remove(sessionId);
        } else {
            final UserSessionDAO sm = DataAccessDriver.getInstance().newUserSessionDAO();
            tmpUs = sm.selectById(userSession, JForumExecutionContext.getConnection());
        }
        // Autologin
        if (StringUtils.isNotBlank(autologin) && SystemGlobals.getBoolValue(ConfigKeys.AUTO_LOGIN_ENABLED)) {
            userSession.setAutoLogin(true);

            // Generate the user-specific hash
            String systemHash = MD5.crypt(SystemGlobals.getValue(ConfigKeys.USER_HASH_SEQUENCE) + u.getId());
            final String userHash = MD5.crypt(System.currentTimeMillis() + systemHash);

            // Persist the user hash
            final UserDAO dao = DataAccessDriver.getInstance().newUserDAO();
            dao.saveUserAuthHash(u.getId(), userHash);

            systemHash = MD5.crypt(userHash);

            ControllerUtils.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_AUTO_LOGIN), "1");
            ControllerUtils.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_USER_HASH), systemHash);
        } else {
            // Remove cookies for safety
            ControllerUtils.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_USER_HASH), null);
            ControllerUtils.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_AUTO_LOGIN), null);
        }

        if (tmpUs == null) {
            userSession.setLastVisit(new Date(System.currentTimeMillis()));
        } else {
            // Update last visit and session start time
            userSession.setLastVisit(new Date(tmpUs.getStartTime().getTime() + tmpUs.getSessionTime()));
        }

        SessionFacade.add(userSession);
        SessionFacade.setAttribute(ConfigKeys.TOPICS_READ_TIME, Maps.newHashMap());
        ControllerUtils.addCookie(SystemGlobals.getValue(ConfigKeys.COOKIE_NAME_DATA), Integer.toString(u.getId()));

        SecurityRepository.load(u.getId(), true);

        this.getContext().put("logged", SessionFacade.isLogged());
        this.getContext().put("session", SessionFacade.getUserSession());
    }

}

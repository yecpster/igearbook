package com.igearbook.action;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import net.jforum.SessionFacade;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.igearbook.constant.UserAPISource;
import com.igearbook.dao.UserApiDao;
import com.igearbook.entities.UserApi;
import com.opensymphony.xwork2.ActionContext;
import com.qq.connect.QQConnectException;
import com.qq.connect.api.OpenID;
import com.qq.connect.api.qzone.UserInfo;
import com.qq.connect.javabeans.AccessToken;
import com.qq.connect.javabeans.qzone.UserInfoBean;
import com.qq.connect.javabeans.weibo.TweetInfo;
import com.qq.connect.oauth.Oauth;
import com.qq.connect.utils.json.JSONObject;

@Namespace("/qqapi")
public class QQApiAction extends BaseAction {
    private static final long serialVersionUID = 7587622153127430L;
    private static final long FIVE_DAYS_MILLISECONDS = 1000 * 3600 * 24 * 5;

    private UserApiDao userApiDao;

    public void setUserApiDao(final UserApiDao userApiDao) {
        this.userApiDao = userApiDao;
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

        return SUCCESS;
    }

    @Action(value = "afterlogin", results = { @Result(name = SUCCESS, location = "qqApi_afterLogin.ftl") })
    public String afterLogin() throws Exception {
        final ActionContext context = ServletActionContext.getContext();
        final HttpServletRequest request = ServletActionContext.getRequest();

        try {
            final AccessToken accessTokenObj = (new Oauth()).getAccessTokenByRequest(request);

            if (accessTokenObj.getAccessToken().equals("")) {
                // Mock user
                final JSONObject json = new JSONObject();
                json.put("nickname", "testQQUser");
                final String url = "http://www.igearbook.com/upload/teamlogo/20131106223450742.jpg";
                final UserInfoBean userInfoBean = new UserInfoBean(json);
                context.put("qzoneUser", userInfoBean);
                context.put("avatar", url);
                // 我们的网站被CSRF攻击了或者用户取消了授权
                // 做一些数据统计工作
                // System.out.println("没有获取到响应参数");
            } else {
                final String accessToken = accessTokenObj.getAccessToken();
                final long tokenExpireIn = accessTokenObj.getExpireIn() * 1000;

                final OpenID openIDObj = new OpenID(accessToken);
                final String openId = openIDObj.getUserOpenID();
                final UserApi userApi = userApiDao.getByOpenId(openId);
                final Date now = new Date();
                if (userApi == null) {
                    final UserApi userApiAdd = new UserApi();
                    userApiAdd.setAccessToken(accessToken);
                    userApiAdd.setLastUpdateDate(now);
                    userApiAdd.setOpenId(openId);
                    userApiAdd.setSource(UserAPISource.QQ);
                    userApiAdd.setTokenExpireIn(tokenExpireIn);
                    userApiDao.add(userApi);
                } else if (shouldUpdateToken(userApi)) {
                    userApi.setAccessToken(accessToken);
                    userApi.setTokenExpireIn(tokenExpireIn);
                    userApi.setLastUpdateDate(now);
                    userApiDao.update(userApi);
                }
                final UserInfo qzoneUserInfo = new UserInfo(accessToken, openId);
                final UserInfoBean userInfoBean = qzoneUserInfo.getUserInfo();
                if (userInfoBean.getRet() == 0) {
                    context.put("qzoneUser", userInfoBean);
                } else {
                    System.out.println("很抱歉，我们没能正确获取到您的信息，原因是： " + userInfoBean.getMsg());
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
                    System.out.println("很抱歉，我们没能正确获取到您的信息，原因是： " + weiboUserInfoBean.getMsg());
                }

            }
        } catch (final QQConnectException e) {
            e.printStackTrace();
        }

        return SUCCESS;
    }

    @Action(value = "bind", results = { @Result(name = SUCCESS, location = "${redirectPath}", type = "redirect") })
    public String bind() throws Exception {
        final ActionContext context = ServletActionContext.getContext();
        final String redirectPath = (String) SessionFacade.getAttribute(SessionFacade.REDIRECT_KEY);
        context.put(SessionFacade.REDIRECT_KEY, "/");
        if (StringUtils.isNotBlank(redirectPath)) {
            //context.put(SessionFacade.REDIRECT_KEY, redirectPath);
            SessionFacade.removeAttribute(SessionFacade.REDIRECT_KEY);
        } else {
            context.put(SessionFacade.REDIRECT_KEY, "/");
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
}

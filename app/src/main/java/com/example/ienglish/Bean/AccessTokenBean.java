package com.example.ienglish.Bean;

/**
 * @author chaochaowu
 * @Description : AccessTokenBean
 * @class : AccessTokenBean
 * @time Create at 14/5/2020 4:27 PM
 */


public class AccessTokenBean {

    /**
     * 我的access_token:
     * refresh_token : 25.3afe9c22ad7101cb06a5a52e029ccb52.315360000.1904537847.282335-19824169
     * expires_in : 2592000
     * scope : public wise_adapt
     * session_key : 9mzdDcKrnPt1+E0+utWvTcP6sdhjA579UdCptJfb1lnjU88pnhL5F1AbLyRFxi4z6dXdlQCgjG5GbxcVqj6by/AuogBVjQ==
     * access_token : 24.64e4d3fdb06aca05d2662490e190c09b.2592000.1591769847.282335-19824169
     * session_secret : cff2bbaf1392a22046b56d1416da466d
     */

    private String refresh_token;
    private int expires_in;
    private String scope;
    private String session_key;
    private String access_token;
    private String session_secret;

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getSession_key() {
        return session_key;
    }

    public void setSession_key(String session_key) {
        this.session_key = session_key;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getSession_secret() {
        return session_secret;
    }

    public void setSession_secret(String session_secret) {
        this.session_secret = session_secret;
    }
}

/**
  * Copyright 2020 bejson.com 
  */
package com.example.ienglish.Bean;
import java.util.List;

/**
 * Auto-generated: 2020-05-08 15:5:7
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 *搜索成功单词的java bean
 */
public class Words {

    private List<String> translation;
    private Basic basic;
    private String query;
    private int errorCode;
    private List<Web> web;
    public void setTranslation(List<String> translation) {
         this.translation = translation;
     }
     public List<String> getTranslation() {
         return translation;
     }

    public void setBasic(Basic basic) {
         this.basic = basic;
     }
     public Basic getBasic() {
         return basic;
     }

    public void setQuery(String query) {
         this.query = query;
     }
     public String getQuery() {
         return query;
     }

    public void setErrorCode(int errorCode) {
         this.errorCode = errorCode;
     }
     public int getErrorCode() {
         return errorCode;
     }

    public void setWeb(List<Web> web) {
         this.web = web;
     }
     public List<Web> getWeb() {
         return web;
     }

}
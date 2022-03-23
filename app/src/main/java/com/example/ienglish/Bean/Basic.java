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
 * 主要内容javabean,具体查看jason源码
 */
public class Basic {

    private String us_phonetic;
    private String phonetic;
    private String uk_phonetic;
    private List<String> explains;
    public void setUs_phonetic(String us_phonetic) {
         this.us_phonetic = us_phonetic;
     }
     public String getUs_phonetic() {
         return us_phonetic;
     }

    public void setPhonetic(String phonetic) {
         this.phonetic = phonetic;
     }
     public String getPhonetic() {
         return phonetic;
     }

    public void setUk_phonetic(String uk_phonetic) {
         this.uk_phonetic = uk_phonetic;
     }
     public String getUk_phonetic() {
         return uk_phonetic;
     }

    public void setExplains(List<String> explains) {
         this.explains = explains;
     }
     public List<String> getExplains() {
         return explains;
     }

}
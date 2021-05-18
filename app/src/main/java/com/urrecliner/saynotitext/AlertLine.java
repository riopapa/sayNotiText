package com.urrecliner.saynotitext;

class AlertLine {
    private String group, who, key1, key2, talk, memo;

    AlertLine( String group, String who, String key1, String key2, String talk, String memo) {
        this.group = group;this.who = who;
        this.key1 = key1;this.key2 = key2;this.talk = talk;this.memo = memo;
    }
    public String getGroup() { return group; }
    public String getWho() { return who; }
    public String getKey1() { return key1; }
    public String getKey2() { return key2; }
    public String getTalk() { return talk; }
    public String getMemo() { return memo; }
}

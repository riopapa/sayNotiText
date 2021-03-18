package com.urrecliner.saynotitext;

class OneLine {
    private boolean select;
    private String group, who, key1, key2, talk, comment;

    OneLine(boolean select, String group, String who, String key1, String key2, String talk, String comment) {
        this.select = select; this.group = group;this.who = who;
        this.key1 = key1;this.key2 = key2;this.talk = talk;this.comment = comment;
    }

    public boolean isSelect() { return select; }
    public String getGroup() { return group; }
    public String getWho() { return who; }
    public String getKey1() { return key1; }
    public String getKey2() { return key2; }
    public String getTalk() { return talk; }
    public String getComment() { return comment; }

    public void setSelect(boolean select) { this.select = select; }
    public void setGroup(String group) { this.group = group; }
    public void setWho(String who) { this.who = who; }
    public void setKey1(String key1) { this.key1 = key1; }
    public void setKey2(String key2) { this.key2 = key2; }
    public void setTalk(String talk) { this.talk = talk; }
    public void setComment(String comment) { this.comment = comment; }
}

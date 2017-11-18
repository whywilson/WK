package cc.yuyeye.wk.DB;

public class chatListBean {
    private int id;
    private String cSend;
    private String cReceive;
    private String msgContent;
    private String time;
    private boolean isMeSend;
    private String iconUrl;

    public chatListBean() {
        super();
    }

    public chatListBean(int cId, String cSend, String cReceive, String cMsg, String cTime, boolean isMeSend) {
        super();
        this.id = cId;
        this.time = cTime;
        this.cSend = cSend;
        this.cReceive = cReceive;
        this.msgContent = cMsg;
        this.isMeSend = isMeSend;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getcSend() {
        return cSend;
    }

    public String getTime() {
        return time;
    }

    public void setcSend(String cSend) {
        this.cSend = cSend;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public boolean isMeSend() {
        return isMeSend;
    }

    public void setMeSend(boolean isMeSend) {
        this.isMeSend = isMeSend;
    }

    public String getcReceive() {
        return cReceive;
    }

    public void setcReceive(String cReceive) {
        this.cReceive = cReceive;
    }
}

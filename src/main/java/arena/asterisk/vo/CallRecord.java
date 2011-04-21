package arena.asterisk.vo;

import java.util.Date;

import arena.utils.SimpleValueObject;

public class CallRecord extends SimpleValueObject {

    private Long id;
    private String fromPhoneNumber;
    private String toPhoneNumber;
    private String callId;
    private Integer callDurationSeconds;
    private Date callStartTimestamp;
    private Date callFinishTimestamp;
    private boolean callSucceeded;
    private String savedFilename;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Date getCallStartTimestamp() {
        return callStartTimestamp;
    }
    public void setCallStartTimestamp(Date callStartTimestamp) {
        this.callStartTimestamp = callStartTimestamp;
    }
    public Date getCallFinishTimestamp() {
        return callFinishTimestamp;
    }
    public void setCallFinishTimestamp(Date callFinishTimestamp) {
        this.callFinishTimestamp = callFinishTimestamp;
    }
    public String getCallId() {
        return callId;
    }
    public void setCallId(String channelId) {
        this.callId = channelId;
    }
    public Integer getCallDurationSeconds() {
        return callDurationSeconds;
    }
    public void setCallDurationSeconds(Integer callDurationSeconds) {
        this.callDurationSeconds = callDurationSeconds;
    }
    public String getFromPhoneNumber() {
        return fromPhoneNumber;
    }
    public void setFromPhoneNumber(String fromPhoneNumber) {
        this.fromPhoneNumber = fromPhoneNumber;
    }
    public String getToPhoneNumber() {
        return toPhoneNumber;
    }
    public void setToPhoneNumber(String toPhoneNumber) {
        this.toPhoneNumber = toPhoneNumber;
    }
    public boolean isCallSucceeded() {
        return callSucceeded;
    }
    public void setCallSucceeded(boolean callSucceeded) {
        this.callSucceeded = callSucceeded;
    }
    public String getSavedFilename() {
        return savedFilename;
    }
    public void setSavedFilename(String savedFilename) {
        this.savedFilename = savedFilename;
    }
}

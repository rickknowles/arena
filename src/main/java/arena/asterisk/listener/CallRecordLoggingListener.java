package arena.asterisk.listener;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.NewChannelEvent;
import org.asteriskjava.manager.event.NewStateEvent;
import org.asteriskjava.util.AstState;

import arena.asterisk.vo.CallRecord;
import arena.dao.DAO;

public class CallRecordLoggingListener implements ManagerEventListener {
    private final Log log = LogFactory.getLog(CallRecordLoggingListener.class);

    private DAO<CallRecord> callRecordDAO;
    private CallStatusListener[] callStatusListeners;
    
    @Override
    public void onManagerEvent(ManagerEvent event) {
//        log.info("Event: " + event);
        if (event instanceof NewChannelEvent) {
            NewChannelEvent nce = (NewChannelEvent) event;
            log.info("Call started: " + nce.getExten() + " callId=" + nce.getUniqueId());

            // only record channels that have to-phone-number
            if (nce.getExten() != null && !nce.getExten().equals("")) {
                CallRecord callRecord = new CallRecord();
                callRecord.setCallId(nce.getUniqueId());
                callRecord.setCallStartTimestamp(new Date());
                callRecord.setFromPhoneNumber(nce.getCallerIdNum());
                callRecord.setToPhoneNumber(nce.getExten());
                
                this.callRecordDAO.insert(callRecord);
                if (this.callStatusListeners != null) {
                    for (CallStatusListener listener : this.callStatusListeners) {
                        listener.onCallAttempt(nce, callRecord);
                    }
                }
            } else {
                log.debug("No extension no supplied for callId " + nce.getUniqueId() + ", ignoring");
            }
        } else if (event instanceof HangupEvent) {
            HangupEvent he = (HangupEvent) event;
            log.info("Call finished: " + he.getUniqueId());
            
            CallRecord callRecord = this.callRecordDAO.select().where("callId", he.getUniqueId()).unique();
            if (callRecord != null) {
                callRecord.setCallFinishTimestamp(new Date());
                long time = callRecord.getCallFinishTimestamp().getTime() - callRecord.getCallStartTimestamp().getTime();
                callRecord.setCallDurationSeconds((int) (time / 1000L));
                this.callRecordDAO.update(callRecord);
                if (this.callStatusListeners != null) {
                    for (CallStatusListener listener : this.callStatusListeners) {
                        listener.onHangup(he, callRecord);
                    }
                }
            }
        } else if (event instanceof NewStateEvent) {
            NewStateEvent nse = (NewStateEvent) event;
            if (nse.getChannelState().equals(AstState.AST_STATE_UP)) {
                log.info("Call succeeded: " + nse.getChannel());
                
                CallRecord callRecord = this.callRecordDAO.select().where("callId", nse.getUniqueId()).unique();
                if (callRecord != null) {
                    callRecord.setCallSucceeded(true);
                    this.callRecordDAO.update(callRecord);
                    if (this.callStatusListeners != null) {
                        for (CallStatusListener listener : this.callStatusListeners) {
                            listener.onCallConnected(nse, callRecord);
                        }
                    }
                }
            }
        }
    }

    public void setCallRecordDAO(DAO<CallRecord> callRecordDAO) {
        this.callRecordDAO = callRecordDAO;
    }

    public void setCallStatusListeners(CallStatusListener[] callStatusListeners) {
        this.callStatusListeners = callStatusListeners;
    }
}

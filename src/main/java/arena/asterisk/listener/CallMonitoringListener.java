package arena.asterisk.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.action.MonitorAction;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.MonitorStartEvent;
import org.asteriskjava.manager.event.MonitorStopEvent;
import org.asteriskjava.manager.event.NewChannelEvent;
import org.asteriskjava.manager.event.NewStateEvent;

import arena.asterisk.ManagerConnectionAware;
import arena.asterisk.vo.CallRecord;
import arena.dao.DAO;

public class CallMonitoringListener implements CallStatusListener, ManagerEventListener, ManagerConnectionAware {
    private final Log log = LogFactory.getLog(CallMonitoringListener.class);

    private ManagerConnection managerConnection;
    private DAO<CallRecord> callRecordDAO;
    
    private boolean enabled = false;

    @Override
    public void onCallAttempt(NewChannelEvent nce, CallRecord callRecord) {
        if (this.enabled) {
            MonitorAction ma = new MonitorAction();
            ma.setChannel(nce.getChannel());
            ma.setMix(Boolean.TRUE);
            ma.setFormat("wav");
            ma.setFile("call" + callRecord.getId());
            ma.setActionId("Record-" + callRecord.getCallId());
            try {
                this.managerConnection.sendAction(ma);
            } catch (Throwable err) {
                throw new RuntimeException("Error sending monitor start event", err);
            }
        }
    }
    
    @Override
    public void onManagerEvent(ManagerEvent event) {
        if (event instanceof MonitorStartEvent) {
            MonitorStartEvent mse = (MonitorStartEvent) event;
            log.info("Monitoring started on call " + mse.getUniqueId());
        } else if (event instanceof MonitorStopEvent) {
            MonitorStopEvent mse = (MonitorStopEvent) event;
            log.info("Monitoring stopped on call " + mse.getUniqueId());
        }
    }

    @Override
    public void onCallConnected(NewStateEvent nse, CallRecord callRecord) {
    }

    @Override
    public void onHangup(HangupEvent he, CallRecord callRecord) {
        if (this.enabled) {
            callRecord.setSavedFilename("call" + callRecord.getId() + ".wav");
            this.callRecordDAO.update(callRecord);
        }
    }

    public void setManagerConnection(ManagerConnection managerConnection) {
        this.managerConnection = managerConnection;
    }

    public void setCallRecordDAO(DAO<CallRecord> callRecordDAO) {
        this.callRecordDAO = callRecordDAO;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}

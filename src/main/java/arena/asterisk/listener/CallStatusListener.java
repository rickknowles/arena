package arena.asterisk.listener;

import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.NewChannelEvent;
import org.asteriskjava.manager.event.NewStateEvent;

import arena.asterisk.vo.CallRecord;

public interface CallStatusListener {
    public void onCallAttempt(NewChannelEvent nce, CallRecord callRecord);
    public void onCallConnected(NewStateEvent nse, CallRecord callRecord);
    public void onHangup(HangupEvent he, CallRecord callRecord);
}

package arena.asterisk.action;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import arena.action.PagedRowSet;
import arena.action.RequestState;
import arena.asterisk.vo.CallRecord;
import arena.dao.ReadOnlyDAO;
import arena.dao.SelectSQL;

public class ShowCallHistoryAction {
    private final Log log = LogFactory.getLog(ShowCallHistoryAction.class);

    private ReadOnlyDAO<CallRecord> callRecordDAO;
    private String baseAudioDir = "/var/spool/asterisk/monitor";

    public String initial(RequestState state) throws Exception {        
        return "OK";
    }
    
    public String deleteCallRecord(RequestState state) throws Exception {  
        String callId = state.getArg("id", "");
        if (callId.equals("")) {
            throw new IllegalArgumentException("Call id not supplied");
        }
        this.callRecordDAO.select().where("id", Long.valueOf(callId)).delete();
        return "OK";
    }

    public String playCallRecording(RequestState state) throws Exception {        
        String callId = state.getArg("id", "");
        if (callId.equals("")) {
            throw new IllegalArgumentException("Call id not supplied");
        }
        CallRecord cr = this.callRecordDAO.select().where("id", Long.valueOf(callId)).unique();
        if (cr == null) {
            throw new IllegalArgumentException("Call not found: " + callId);
        } else if (cr.getSavedFilename() == null) {
            throw new IllegalArgumentException("No audio saved for call " + callId);
        }
        File file = new File(this.baseAudioDir, cr.getSavedFilename());
        log.info("Trying " + file);
        if (!file.isFile()) {
            throw new IllegalArgumentException("No audio found for call " + callId);
        }
        
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        InputStream in = new FileInputStream(file);
        byte buf[] = new byte[16384];
        int read;
        
        while ((read = in.read(buf)) >= 0) {
            data.write(buf, 0, read);
        }
        
        state.setArg("callRecord", cr);
        state.setArg("filename", cr.getSavedFilename());
        state.setArg("recording", data.toByteArray());
        return "OK";
    }

    public String getCallRows(RequestState state) throws Exception {
        SelectSQL<CallRecord> sql = PagedRowSet.wrapInJQGridSort(
                this.callRecordDAO.select(), 
                state, "fromPhoneNumber", "toPhoneNumber", "callStartTimestamp", "callDurationSeconds");
        int rowCount = sql.rowCount();
        List<CallRecord> out = PagedRowSet.wrapInJQGridLimit(sql, state, rowCount).list();
        state.setArg("pagedCallRows", PagedRowSet.wrapInJQGridPaging(out, state, rowCount));
        return "OK";
    }
    
    public void setCallRecordDAO(ReadOnlyDAO<CallRecord> callRecordDAO) {
        this.callRecordDAO = callRecordDAO;
    }

    public void setBaseAudioDir(String baseAudioDir) {
        this.baseAudioDir = baseAudioDir;
    }
    
    
}

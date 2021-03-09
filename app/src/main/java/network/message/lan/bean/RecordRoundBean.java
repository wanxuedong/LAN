package network.message.lan.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mac
 */
public class RecordRoundBean {

    private String round = "";
    private String recordTime = "";
    private List<RecordBean> recordBeans = new ArrayList<>();

    public void setRound(String round) {
        this.round = round;
    }

    public String getRound() {
        return round;
    }

    public void setRecordTime(String recordTime) {
        this.recordTime = recordTime;
    }

    public String getRecordTime() {
        return recordTime;
    }

    public void setRecordBeans(List<RecordBean> recordBeans) {
        this.recordBeans = recordBeans;
    }

    public List<RecordBean> getRecordBeans() {
        return recordBeans;
    }

    public static class RecordBean{

        private String recordName;
        private String recordNumber;

        public void setRecordName(String recordName) {
            this.recordName = recordName;
        }

        public void setRecordNumber(String recordNumber) {
            this.recordNumber = recordNumber;
        }

        public String getRecordName() {
            return recordName;
        }

        public String getRecordNumber() {
            return recordNumber;
        }
    }

}

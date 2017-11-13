package kr.ac.sch.se.Data_Transmission;

/**
 * Created by sun on 2017-01-10.
 */
public class Network_Http {
    public static final int DATA_RECORD_GET = 20;
    public static final int USERINFO_GET = 21;

    private final String memberinfo_url = "http://welltec.blue-core.com:10000/openapi/member/memberInfo";
    private final String weight_url = "http://welltec.blue-core.com:10000/openapi/p2/weight";

    private HttpDataSend httpSend;
    private RecordData_Parsing recordData_parsing;
    private UserInfo_Parsing userInfo_parsing;

    private String sessionId;

    public Network_Http(String sessionId){
        this.sessionId = sessionId;
        this.httpSend = new HttpDataSend(weight_url, sessionId);
    }

    //java exception
    public void HttpPost_send(String wDate,String weight,String bmi,String fatMass,String fatPer,String arrhythmia){
        httpSend.dataPostSend(wDate, weight, bmi, fatMass, fatPer, arrhythmia);
    }

    public void HttpGet_send(int sendFlag, String firstTime, String lastTime){
        if(sendFlag == DATA_RECORD_GET) {
            this.recordData_parsing = new RecordData_Parsing(weight_url, sessionId);
            //network work
            recordData_parsing.getRecordData(firstTime, lastTime);
        }else if(sendFlag == USERINFO_GET){
//            this.userInfo_parsing = new UserInfo_Parsing(memberinfo_url, sessionId);
//            userInfo_parsing.dataGetParsing();
        }
    }
}

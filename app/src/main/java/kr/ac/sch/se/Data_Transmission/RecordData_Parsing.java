package kr.ac.sch.se.Data_Transmission;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.ac.sch.se.Common.RecordDatas_Info;

/**
 * Created by sun on 2017-01-11.
 */
public class RecordData_Parsing extends HttpDataParsing{
    private String TAG = "RECORDDATA_PARSING";
    private RecordDatas_Info recordDates;

    public RecordData_Parsing(String url, String sessionID){
        super(url, sessionID);

        recordDates = new RecordDatas_Info();
    }

    //
    public RecordDatas_Info getRecordData(String firstTime, String lastTime){
        //상위 클래스의 파싱메소드
        String dataStr = dataGetParsing();

        try {
            JSONObject object = new JSONObject(dataStr);

            JSONObject result = object.getJSONObject("result");
            String str = result.getString("code");

            Log.e(TAG, "code:" + str);
            if(str.equals("200")){
                result = object.getJSONObject("body");
                str = result.getString("totalCount");

                JSONArray jarray = result.getJSONArray("wList");

                //wList 에서 데이터 파싱
                ArrayList<String> dateArrayList = new ArrayList<>();

                for(int n = 0; n <  jarray.length(); n++){
                    JSONObject tmpJson = jarray.getJSONObject(n);
                    str = tmpJson.getString("createDate");

                    dateArrayList.add(str);
                    Log.e(TAG, "RecordData_parsing: date:" + str);
                }

                int createDate = Integer.parseInt(str);
                int fTime = Integer.parseInt(firstTime);
                int lTime = Integer.parseInt(lastTime);

                //원하는 기간 넣기
                for(int n = 0; n < dateArrayList.size(); n++) {
                    //이상적인 설정
                    if (fTime < createDate && lTime > createDate) {
                        //전부 넣으면 됨
                        for(int i = 0; i <  jarray.length(); i++){
                            JSONObject tmpJson = jarray.getJSONObject(i);
                            String date = tmpJson.getString("wDate");
                            String date2 = tmpJson.getString("createDate");
                            String weight = tmpJson.getString("weight");
                            String bmi = tmpJson.getString("bmi");
                            String fatMass = tmpJson.getString("fatMass");
                            String fatPer = tmpJson.getString("fatPer");
                            String arrhythmia = tmpJson.getString("arrhythmia");

                            Log.e(TAG, "date:" + str);
                        }
                        break;
                    }

                    //0번째 라스번째의 시간데이터를 받아와서 크기 비교
                    //start가 0번째보다 작거으면 0번째부터들어가고
                    //start가 라스보다 클수도있어 데이터없다고 표시하고
                    //start가 0번째보다 크다 하지만 라스보다 작다면 문자열찾아서 거기 인덱스부터 시작하면 됨

                    //secondTime이 0번째보다 작다. 데이터 없다고 표시
                    //secondTime이 라스트보다 크다고 하면 secondTime은 라스트가 됨
                    //secondTime이 0번째보다 크지만 라스트보다 작다 그러면 검색해서 하면되고
                    //secondTime이 start보다 작다 그러면 표시안하고 startTime과 last간의 크기 비교해서 secondTime이 더  크게만해서 들어가도록 설정해됨 1.
                }
                this.recordDates.setTotalCont(Integer.parseInt(str));


            }else{
                Log.e(TAG, "result code:" + str);
            }

            Log.e(TAG, "totalCount:" + str);

            JSONArray jarray = result.getJSONArray("wList");

                //wList 에서 데이터 파싱
            for(int n = 0; n <  jarray.length(); n++){
                JSONObject tmpJson = jarray.getJSONObject(n);
                str = tmpJson.getString("createDate");

                 Log.e(TAG, "date:" + str);
            }

//                for(int i=0; i < jarray.length(); i++) {
//                    JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
//                    String address = jObject.getString("bmi");
//
//                    Log.e(TAG, "extracture:"+address);
//                }
        }catch(JSONException e){
            e.printStackTrace();
        }

        return null;
    }
}

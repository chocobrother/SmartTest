package kr.ac.sch.se.Data_Transmission;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by UCLAB on 2016-08-25.
 */
public class FTPConnector {
    private String SERVER = ""; // FTP 호스트 주소
    private int port; //포트번호
    private String ID = ""; // 유저 아이디
    private String PASS = ""; // 유저 패스워드
    private String mEncodingSet = ""; // 케릭터 셋
    private FTPClient ftp = null;
    private FileInputStream fis = null;

    public FTPConnector(String server,int port, String id, String pass, String encodingSet) {
        // TODO Auto-generated constructor stub
        this.SERVER = server;
        this.port = port;
        this.ID = id;
        this.PASS = pass;
        this.mEncodingSet = encodingSet;
        ftp = new FTPClient(); // 객체 생성
    }

    public boolean login() {
        boolean loginResult = false;
        try {
            ftp.setControlEncoding(mEncodingSet);
            Log.e(SERVER, String.valueOf(port));
            ftp.connect(SERVER, port);
            loginResult = ftp.login(ID, PASS);
            ftp.enterLocalPassiveMode(); // PassiveMode 접속
            //ftp.makeDirectory("/home/ecg/");
            ftp.changeWorkingDirectory("/home/ecg/ecg_data/");
            //ftp.makeDirectory(mDefaultWorkDirectory);
            //ftp.changeWorkingDirectory(mDefaultWorkDirectory);
        } catch (IOException e) {
            Log.e("FTP_LOGIN_ERR", e.toString());
        }
        if (!loginResult) {
            Log.e("FTP_LOGIN_ERR", "로그인 실패");
            return false;
        } else {
            Log.e("FPT_LOGIN_OK", "로그인 성공");
            return true;
        }
    }
    /**
     * FTP서버로 파일을 전송합니<br/>
     *
     * @param file
     *            전송할 파일의 객체를 필요로 합니다.
     * @return 파일전송 성공 실패 여부를 리턴합니다.
     */
    public boolean uploadFile(File file) {
        if (!ftp.isConnected()) {
            Log.e("UPLOAD_ERR", "현재 FTP 서버에 접속되어 잇지 않습니다.");
            return false;
        }

        boolean uploadResult = false;
        try {
            int rep;

            ftp.setFileType(FTP.BINARY_FILE_TYPE); // 스트림으로 보낼 파일의 유형
            fis = new FileInputStream(file);
            ftp.enterLocalPassiveMode();
            uploadResult = ftp.storeFile(file.getName(), fis);
            rep = ftp.getReplyCode();
            Log.e("Reply", rep + "");
            if (!uploadResult) {
                Log.e("FTP_SEND_ERR", "파일 전송을 실패하였습니다.");
                uploadResult = false;
            }
        } catch (Exception e) {
            Log.e("FTP_SEND_ERR", "파일전송에 문제가 생겼습니다. " + e.toString());
            uploadResult = false;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                    ftp.logout();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e("FTP", "ftp 접속 스트림 닫고 로그아웃중 오류 발생");
                    e.printStackTrace();
                    uploadResult =  false;
                }
            }
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e("FTP", "ftp 접속 종료중 문제 발생");
                    uploadResult =  false;
                }
            }
        }

        return uploadResult;
    }
}

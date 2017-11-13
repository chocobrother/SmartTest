package kr.ac.sch.se.Data_Transmission;

import java.util.ArrayList;

/**
 * Created by sun on 2016-11-06.
 */
public interface PacketAnalysis {
    public byte[] make(int id, int body);
    public boolean parsing(ArrayList<Byte> packet);
}

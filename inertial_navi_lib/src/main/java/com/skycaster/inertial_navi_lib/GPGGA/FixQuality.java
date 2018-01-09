package com.skycaster.inertial_navi_lib.GPGGA;

/**
 * Created by 廖华凯 on 2017/6/6.
 */

public enum  FixQuality {
    //Fix quality: 0 = invalid
//            1 = GPS fix (SPS)
//            2 = DGPS fix
//            3 = PPS fix
//            4 = Real Time Kinematic
//            5 = Float RTK
//            6 = estimated (dead reckoning) (2.3 feature)
//            7 = Manual input mode
//            8 = Simulation mode
    QUALITY_INVALID,
    QUALITY_GPS_FIX,
    QUALITY_DGPS_FIX,
    QUALITY_PPS_FIX,
    QUALITY_REAL_TIME_KINEMATIC,
    QUALITY_FLOAT_RTK,
    QUALITY_ESTIMATED,
    QUALITY_MANUAL_INPUT_MODE,
    QUALITY_SIMULATION_MODE,
    QUALITY_ERROR
}

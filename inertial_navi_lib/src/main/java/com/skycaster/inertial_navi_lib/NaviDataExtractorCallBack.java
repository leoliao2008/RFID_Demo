package com.skycaster.inertial_navi_lib;

import com.skycaster.inertial_navi_lib.GPGGA.GPGGABean;
import com.skycaster.inertial_navi_lib.GPGGA.TbGNGGABean;
import com.skycaster.inertial_navi_lib.GPGSA.GPGSABean;
import com.skycaster.inertial_navi_lib.GPGSV.GPGSVBean;

/**
 * Created by 廖华凯 on 2017/10/27.
 */

public class NaviDataExtractorCallBack {
    public void onGetGPGGABean(GPGGABean bean){}

    public void onGetTBGNGGABean(TbGNGGABean bean){}

    public void onGetGPGSVBean(GPGSVBean bean) {}

    public void onGetGPGSABean(GPGSABean bean) {

    }
}

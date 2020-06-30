package com.main;

import com.dh.DpsdkCore.*;
import lombok.extern.slf4j.Slf4j;

import javax.sound.midi.Soundbank;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * 大华SDK工具集成类
 *
 * @author Symer
 * @date 2019/11/22
 */
public class DPSDKUtil {
    //句柄
    private static int m_nDLLHandle = -1;
    //组织机构xml长度
    private static Return_Value_Info_t nGroupLen = new Return_Value_Info_t();

    /**
     * 创建DPSDK
     */
    public static void OnCreate() {
        System.out.println("创建DPSDK开始");
        int nRet = -1;
        Return_Value_Info_t res = new Return_Value_Info_t();
        nRet = IDpsdkCore.DPSDK_Create(dpsdk_sdk_type_e.DPSDK_CORE_SDK_SERVER, res);
        m_nDLLHandle = res.nReturnValue;
        System.out.println("创建DPSDK结束，返回 m_nDLLHandle = " + m_nDLLHandle);
        if (m_nDLLHandle > 0) {
            //设置设备状态上报监听函数
//            nRet = IDpsdkCore.DPSDK_SetDPSDKDeviceStatusCallback(m_nDLLHandle, new DPSDKDevStatusCallback());
            //设置NVR通道状态上报监听函数
            nRet = IDpsdkCore.DPSDK_SetDPSDKNVRChnlStatusCallback(m_nDLLHandle, new DPSDKNVRChnlStatusCallback());
            //设置通用JSON回调
            nRet = IDpsdkCore.DPSDK_SetGeneralJsonTransportCallback(m_nDLLHandle, new DPSDKGeneralJsonTransportCallback());
            //卡口过车数据回调
//            nRet = IDpsdkCore.DPSDK_SetDPSDKGetBayCarInfoCallbackEx(m_nDLLHandle, new DPSDKGetBayCarInfoCallbackEx());
            //违章报警回调
//            nRet = IDpsdkCore.DPSDK_SetDPSDKTrafficAlarmCallback(m_nDLLHandle, new DPSDKTrafficAlarmCallback());
            //区间测速回调
//            nRet = IDpsdkCore.DPSDK_SetDPSDKGetAreaSpeedDetectCallback(m_nDLLHandle, new DPSDKGetAreaSpeedDetectCallback());
        }
    }

    /**
     * 登录
     */
    public static void OnLogin() {
        Login_Info_t loginInfo = new Login_Info_t();
        loginInfo.szIp = FileConfig.dahuaIp.getBytes();
        loginInfo.nPort = FileConfig.dahuaPort;
        loginInfo.szUsername = FileConfig.dahuaUsername.getBytes();
        loginInfo.szPassword = FileConfig.dahuaPassword.getBytes();
        loginInfo.nProtocol = dpsdk_protocol_version_e.DPSDK_PROTOCOL_VERSION_II;
        loginInfo.iType = 1;
        int nRet = IDpsdkCore.DPSDK_Login(m_nDLLHandle, loginInfo, 10000);
        if (nRet == dpsdk_retval_e.DPSDK_RET_SUCCESS) {
            System.out.printf("登录成功，nRet = {}", nRet);
        } else {
            System.out.printf("登录失败，nRet = {}", nRet);
        }
    }

    /**
     * 加载所有组织机构树
     */
    public static void LoadAllGroup() {
        int nRet = IDpsdkCore.DPSDK_LoadDGroupInfo(m_nDLLHandle, nGroupLen, 180000);
        if (nRet == dpsdk_retval_e.DPSDK_RET_SUCCESS) {
            System.out.printf("加载所有组织树成功，nRet = {}， nDepCount = {}", nRet, nGroupLen.nReturnValue);
        } else {
            System.out.printf("加载所有组织树失败，nRet = {}", nRet);
        }
    }

    /**
     * 获取所有组织树串
     */
    public static String GetGroupStr() {
        byte[] szGroupBuf = new byte[nGroupLen.nReturnValue];
        int nRet = IDpsdkCore.DPSDK_GetDGroupStr(m_nDLLHandle, szGroupBuf, nGroupLen.nReturnValue, 10000);
        String GroupBuf="";
        if (nRet == dpsdk_retval_e.DPSDK_RET_SUCCESS) {
            try {
                GroupBuf = new String(szGroupBuf, "UTF-8");
            } catch (IOException e) {
                System.out.println("解析所有组织树串失败");
            }
        } else {
            System.out.println("获取所有组织树串失败");
        }
        return GroupBuf;
    }

    /**
     * 云台方向控制.
     *
     * @param szCameraId 通道id
     * @param nDirect    控制命令 参考 dpsdk_ptz_direct_e 类
     * @param nStep      移动步数
     */
    public static int DPSDK_PtzDirection(String szCameraId, int nDirect, int nStep) {
        Ptz_Direct_Info_t directInfo = new Ptz_Direct_Info_t();
        //通道ID
        directInfo.szCameraId = szCameraId.getBytes();
        directInfo.nDirect = nDirect;
        //移动步长
        directInfo.nStep = nStep;
        //是否停止，0表示不停止，1表示停止
        directInfo.bStop = false;

        //转向调用
        return IDpsdkCore.DPSDK_PtzDirection(m_nDLLHandle, directInfo, 10000);

    }

    /**
     * 镜头控制
     *
     * @param szCameraId 通道id
     * @param nOperation 控制命令 参考 dpsdk_camera_operation_e 类
     * @param nStep      移动步数
     */
    public static int DPSDK_PtzCameraOperation(String szCameraId, int nOperation, int nStep) {
        Ptz_Operation_Info_t operation_info_t = new Ptz_Operation_Info_t();
        operation_info_t.szCameraId = szCameraId.getBytes();
        operation_info_t.nOperation = nOperation;
//        operation_info_t.nStep = 10;
        operation_info_t.nStep = nStep;
        operation_info_t.bStop = false;
        return IDpsdkCore.DPSDK_PtzCameraOperation(m_nDLLHandle, operation_info_t, 10000);
    }

    /**
     * 登出
     */
    public static void OnLogout() {
        int nRet = IDpsdkCore.DPSDK_Logout(m_nDLLHandle, 10000);
        if (nRet == dpsdk_retval_e.DPSDK_RET_SUCCESS) {
            System.out.printf("登出成功，nRet = %d", nRet);
        } else {
            System.out.printf("登出失败，nRet = %d", nRet);
        }
        System.out.println();
    }

    public static void getUrl() throws UnsupportedEncodingException {
        String deviid = "1000047";

        int i7 = IDpsdkCore.DPSDK_QueryNVRChnlStatus(m_nDLLHandle, deviid.getBytes(), 9000);
        System.out.println("NVR状态："+i7);

        Device_Info_Ex_t device_info_ex_t = new Device_Info_Ex_t();
        int i6 = IDpsdkCore.DPSDK_GetDeviceInfoExById(m_nDLLHandle, deviid.getBytes(), device_info_ex_t);
        System.out.println(i6);
        System.out.println(device_info_ex_t.toString());

        Get_Dev_StreamType_Info_t dev_streamType_info_t = new Get_Dev_StreamType_Info_t();
        dev_streamType_info_t.szDeviceId = deviid.getBytes();
//        获取设备支持的码流类型.
        int i2 = IDpsdkCore.DPSDK_GetDevStreamType(m_nDLLHandle, dev_streamType_info_t);
        System.out.println(i2);
        // 摄像头无效
        Get_RealStreamUrl_Info_t t = new Get_RealStreamUrl_Info_t();
        t.szCameraId = "1000047$1$0$0".getBytes();
        int i = IDpsdkCore.DPSDK_GetRealStreamUrl(m_nDLLHandle, t, 9000);
        System.out.println(new String(t.szUrl,"UTF-8"));
        System.out.println(i);


        Return_Value_Info_t t1 = new Return_Value_Info_t();
        Get_RealStream_Info_t t2 = new Get_RealStream_Info_t();
        t2.szCameraId = "1000047$1$0$0".getBytes();
        t2.nStreamType = 0; // 主码
        t2.nMediaType = 1; // 视频
        t2.nTransType = 1; // udp

        fMediaDataCallback fMediaDataCallback = new fMediaDataCallback() {
            public void invoke(int nPDLLHandle, int nSeq, int nMediaType, byte[] szNodeId, int nParamVal, byte[] szData, int nDataLen) throws UnsupportedEncodingException {
                System.out.println("SDK句柄:" + nPDLLHandle);
                System.out.println("对应请求时返回的Seq:" + nPDLLHandle);
                System.out.println("媒体类型:" + nMediaType);
                System.out.println("数据对应的通道/设备ID:" + new String(szNodeId, "UTF-8"));
                System.out.println("扩展值;mediaType为real时:" + nParamVal);
                System.out.println("数据对应的通道/设备ID:" + new String(szData, "UTF-8"));
                System.out.println("数据长度:" + nDataLen);
            }
        };


        int i1 = IDpsdkCore.DPSDK_GetRealStream(
                m_nDLLHandle,
                t1,
                t2,
                fMediaDataCallback,
                90000
        );
        System.out.println(t1.nReturnValue);
        System.out.println(i1);

        Get_ExternalRealStreamUrl_Info_t s = new Get_ExternalRealStreamUrl_Info_t();
        s.szCameraId = "1000047$1$0$0".getBytes();
        s.nStreamType = 0;
        s.nMediaType = 1;
        s.nTransType = 0;
        s.bUsedVCS = 0;
        s.szUrl = t.szUrl;
        int i3 = IDpsdkCore.DPSDK_GetExternalRealStreamUrl(m_nDLLHandle, s, 9000);
        System.out.println(new String(s.szUrl));
        System.out.println(i3);

        Get_RealStreamUrl_Info_t get_realStreamUrl_info_t = new Get_RealStreamUrl_Info_t();
        get_realStreamUrl_info_t.szCameraId = "1000047$1$0$0".getBytes();
        get_realStreamUrl_info_t.nStreamType = 0;
        get_realStreamUrl_info_t.nMediaType = 1;
        get_realStreamUrl_info_t.nTransType = 0;
        get_realStreamUrl_info_t.szUrl = t.szUrl;
        int i4 = IDpsdkCore.DPSDK_GetRealStreamUrl(m_nDLLHandle, get_realStreamUrl_info_t, 9000);
        System.out.println(new String(get_realStreamUrl_info_t.szUrl,"UTF-8"));
        System.out.println(i4);

        int i5 = IDpsdkCore.DPSDK_CloseRealStreamByCameraId(m_nDLLHandle, "1000047$1$0$0".getBytes(), 9000);
        System.out.println("关闭码流："+i5);
    }

    /**
     * 释放内存
     */
    public static void OnDestroy() {
        int nRet = IDpsdkCore.DPSDK_Destroy(m_nDLLHandle);
        if (nRet == dpsdk_retval_e.DPSDK_RET_SUCCESS) {
            System.out.printf("释放内存成功，nRet = %d", nRet);
        } else {
            System.out.printf("释放内存失败，nRet = %d", nRet);
        }
        System.out.println();
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        DPSDKUtil.OnCreate();//初始化
        DPSDKUtil.OnLogin();//登陆
        DPSDKUtil.LoadAllGroup();//加载组织结构
        String s = DPSDKUtil.GetGroupStr();//获取组织结构串
        System.out.println(s);
//加载组织结构之后，要延时5秒钟左右，等待与各服务模块取得联系
        getUrl();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String deviid = "1000047";
        Device_Info_Ex_t t = new Device_Info_Ex_t();
        IDpsdkCore.DPSDK_GetDeviceInfoExById(m_nDLLHandle,deviid.getBytes(),t);
        System.out.println("设备名称："+new String(t.szName));

        // 成功了
        int i = IDpsdkCore.DPSDK_QueryNVRChnlStatus(m_nDLLHandle, deviid.getBytes(), 3000);
        System.out.println("nvr 状态：" + i);


        DPSDKUtil.OnLogout();//登出
        DPSDKUtil.OnDestroy();//释放
    }
}

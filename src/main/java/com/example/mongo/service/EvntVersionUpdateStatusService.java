package com.example.mongo.service;


import com.example.mongo.vo.EvntVersionUpdateStatusVO;

import java.util.List;
import java.util.Map;

public interface EvntVersionUpdateStatusService {
     
     /**
      * Statements
      *
      * @param vo
      * @return
      * @throws Exception
      */
     List<Map<String, String>> pckgNmUpdateStateNew(EvntVersionUpdateStatusVO vo) throws Exception;
     
     /**
      * Statements
      *
      * @param vo
      * @return
      * @throws Exception
      */
     List<Map<String, String>> partNoUpdateStateNew(EvntVersionUpdateStatusVO vo) throws Exception;


    List<Map<String, String>> selectEvntDownloadSuccessDetailList(EvntVersionUpdateStatusVO vo);

    List<Map<String, String>> selectEvntDownloadingOrUpdatingDetailList(EvntVersionUpdateStatusVO vo);

}

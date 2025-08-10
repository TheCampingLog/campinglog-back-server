package com.campinglog.campinglogbackserver.campinfo.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResponseGetCampDetail {
  private String facltNm;// 야영장명
  private String lineIntro;// 한줄소개
  private String intro;// 소개
  private String hvofBgnde;// 휴장기간 휴무기간 시작일
  private String hvofEnddle;// 휴장기간 휴무기간 종료일
  private String featureNm;// 특징
  private String induty;// 업종
  private String lctCl;// 입지구분
  private String addr1;// 주소
  private String addr2;// 주소상세
  private String tel;// 전화
  private String homepage;// 홈페이지
  private String resveUrl;// 예약 페이지
  private String siteBottomCl1;// 잔디
  private String siteBottomCl2;// 파쇄석
  private String siteBottomCl3;// 테크
  private String siteBottomCl4;// 자갈
  private String siteBottomCl5;// 맨흙
  private String operPdCl;// 운영기간
  private String operDeCl;// 운영일
  private String toiletCo;// 화장실 개수
  private String swrmCo;// 샤워실 개수
  private String wtrplCo;// 개수대 개수
  private String sbrsCl;// 부대시설
  private String firstImageUrl;// 대표이미지
  private String animalCmgCl;// 애완동물 출입
  private String eqpmnLendCl;// 캠핑장비 대여
  private String posblFcltyCl;// 주변 이용가능시설

}

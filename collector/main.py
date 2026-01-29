#!/usr/bin/env python
# coding: utf-8

# In[2]:


import requests
import pandas as pd
import time
import json
import urllib3
import os
from sqlalchemy import create_engine, text

# SSL 경고 무시 설정
urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

# =============================================================================
# [설정] 본인의 환경에 맞게 해주세요
# =============================================================================
API_KEY = os.environ.get("API_KEY")

# DB 접속 정보
DB_USER = 'sc_25K_LI6_p3_2'          # DB 아이디
DB_PASS = os.environ.get("DB_PASS")  # DB 비밀번호 (여기를 꼭 수정하세요!)
DB_HOST = 'project-db-campus.smhrd.com'     # DB 주소
DB_PORT = '3312'          # 포트
DB_NAME = 'sc_25K_LI6_p3_2'     # 스키마(데이터베이스) 이름

# DB 테이블 컬럼
TARGET_DB_COLUMNS = [
    'plcyNo', 'plcyNm', 'plcyKywdNm', 'plcyExplnCn',
    'lclsfNm', 'mclsfNm',
    'plcySprtCn', 'aplyPrdSeCd', 'aplyYmd',
    'bizPrdSeCd', 'bizPrdBgngYmd', 'bizPrdEndYmd', 
    'zipCd', 'plcyMajorCd', 'jobCd', 'schoolCd', 'sbizCd',
    'mrgSttsCd', 'sprtTrgtMinAge', 'sprtTrgtMaxAge', 
    'earnCndSeCd', 'earnMinAmt', 'earnMaxAmt', 'earnEtcCn',
    'aplyUrlAddr', 'refUrlAddr1', 'refUrlAddr2', 'sbmsnDcmntCn', 'etcMttrCn',
    'inqCnt'
]

# =============================================================================
# 1. API 데이터 수집 (끊김 방지 기능 포함)
# =============================================================================
def get_all_policies(api_key):
    base_url = "https://www.youthcenter.go.kr/go/ythip/getPlcy"
    all_data = []
    page_num = 1
    page_size = 50
    max_retries = 5  # 재시도 횟수
    
    print("▶ 데이터 수집을 시작합니다...")

    while True:
        params = {
            'apiKeyNm': api_key, 'pageNum': page_num, 'pageSize': page_size,
            'pageType': '1', 'rtnType': 'json'
        }
        success = False 

        for attempt in range(max_retries):
            try:
                response = requests.get(base_url, params=params, verify=False, timeout=30)
                if response.status_code == 200:
                    try:
                        data = response.json()
                        success = True
                        break 
                    except:
                        pass
                time.sleep(2 * (attempt + 1)) # 실패 시 대기 시간 늘리기
            except:
                time.sleep(2 * (attempt + 1))
        
        if not success:
            print(f"❌ {page_num}페이지 수집 실패 (서버 응답 없음)")
            break

        policies = []
        if 'youthPolicyList' in data: policies = data['youthPolicyList']
        elif 'result' in data:
             inner = data['result']
             if isinstance(inner, list): policies = inner
             elif isinstance(inner, dict) and 'youthPolicyList' in inner:
                 policies = inner['youthPolicyList']

        if not policies:
            print(f"▶ {page_num-1}페이지까지 수집 완료.")
            break

        all_data.append(pd.DataFrame(policies))
        print(f"  - {page_num}페이지 완료 ({len(policies)}건)")
        page_num += 1
        time.sleep(0.1)

    if all_data: return pd.concat(all_data, ignore_index=True)
    else: return pd.DataFrame()

# =============================================================================
# 2. 데이터 전처리 (숫자 오류 수정 추가됨!)
# =============================================================================
def preprocess_data(df):
    if df.empty: return df
    print("▶ 데이터 전처리 중...")

    # [1] 컬럼 필터링
    available_cols = list(set(TARGET_DB_COLUMNS).intersection(set(df.columns)))
    df = df[available_cols].copy()

    # [2] JSON 변환
    json_cols = ['zipCd', 'plcyMajorCd', 'jobCd', 'schoolCd', 'sbizCd']
    for col in json_cols:
        if col in df.columns:
            df[col] = df[col].apply(lambda x: json.dumps(str(x).split(',')) if x and str(x).strip() else json.dumps([]))

    # [3] 날짜 변환
    date_cols = ['bizPrdBgngYmd', 'bizPrdEndYmd']
    for col in date_cols:
        if col in df.columns:
            df[col] = pd.to_datetime(df[col], format='%Y%m%d', errors='coerce').dt.date

    # [4] ⭐️ 숫자 변환 (이 부분이 에러를 해결해 줍니다!)
    # 빈 문자열 '' -> 숫자 0 으로 강제 변환
    numeric_cols = ['sprtTrgtMinAge', 'sprtTrgtMaxAge', 'earnMinAmt', 'earnMaxAmt', 'inqCnt']
    for col in numeric_cols:
        if col in df.columns:
            # 1. 숫자가 아닌 것들을 NaN으로 바꿈
            df[col] = pd.to_numeric(df[col], errors='coerce')
            # 2. NaN을 0으로 채움
            df[col] = df[col].fillna(0)

    return df

# =============================================================================
# 3. DB 저장 (사라진 데이터 삭제 포함 - 전체 덮어쓰기 방식)
# =============================================================================
def save_to_mysql(df):
    if df.empty:
        print("저장할 데이터가 없습니다.")
        return

    db_url = f"mysql+pymysql://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}"
    
    try:
        engine = create_engine(db_url)
        with engine.connect() as conn:
            print(f"▶ DB({DB_NAME}) 연결 성공. 데이터 동기화 시작...")
            
            # [수정된 부분] 
            # 기존: 수집된 ID만 골라서 삭제 (사라진 데이터가 남음)
            # 변경: 테이블의 모든 데이터를 삭제 (사라진 데이터도 제거됨)
            
            # 방법 1: TRUNCATE (가장 빠름, 단 외래키 제약조건이 있으면 에러 가능성 있음)
            try:
                conn.execute(text("TRUNCATE TABLE policy"))
            except:
                # TRUNCATE 실패 시 (권한 부족이나 FK 문제 등) DELETE 사용
                conn.execute(text("DELETE FROM policy"))
            
            conn.commit() # 삭제 확정

            # 새 데이터 입력
            # chunksize를 설정하면 데이터가 많을 때 메모리 에러를 방지합니다 (선택사항)
            df.to_sql(name='policy', con=conn, if_exists='append', index=False, chunksize=1000)
            
            conn.commit()
            print(f"🎉 성공! 기존 데이터를 모두 비우고, 총 {len(df)}건을 새로 저장했습니다.")
        
    except Exception as e:
        print(f"❌ DB 저장 실패: {e}")
        
# =============================================================================
# 메인 실행부
# =============================================================================
if __name__ == "__main__":
    df_result = get_all_policies(API_KEY)
    df_clean = preprocess_data(df_result)
    save_to_mysql(df_clean)


# In[ ]:





# 1. 파이썬 3.9 버전이 설치된 아주 가벼운 리눅스 환경을 가져옵니다.
FROM python:3.9-slim

# 2. 가상 컴퓨터 안에서 작업을 진행할 폴더(/app)를 만듭니다.
WORKDIR /app

# 3. 라이브러리 목록 파일을 먼저 복사합니다.
COPY requirements.txt .

# 4. 목록에 적힌 파이썬 라이브러리들을 설치합니다.
RUN pip install --no-cache-dir -r requirements.txt

# 5. 내 컴퓨터에 있는 파이썬 코드들(main.py 등)을 가상 컴퓨터로 복사합니다.
COPY . .

# 6. 마지막으로 파이썬 프로그램을 실행합니다.
CMD ["python", "main.py"]
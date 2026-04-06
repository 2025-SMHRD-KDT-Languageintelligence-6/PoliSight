## 1. 프로젝트 개요
- 프로젝트 이름 : PoliSight
- 프로젝트 설명 : LLM+RAG를 활용한 청년 정책 수혜 자격 진단 및 시뮬레이션 서비스

## 2. 팀원 및 소개

| 팀원 | 역할 |
| :---: | --- |
| **최태림** | <ul><li>프로젝트 총괄 기획 및 일정 관리</li><li>서비스 아키텍처 및 DB 스키마 설계</li><li>API & 핵심 로직 개발</li><li>GCP 인프라 & CI/CD 환경 구축</li><li>RAG 검색 로직 및 프롬프트 최적화</li></ul> |
| **방성진** | <ul><li>프로젝트 전반의 UI/UX 설계 및 개발</li><li>Spring Boot 기반의 페이지 기능 구현</li><li>Live2D 연동 및 제어 기능 구현</li> |
| **서준원** | <ul><li>LLM 기반 시뮬레이션 모델 연동</li><li>RAG 기반 AI 로직 및 프롬프트 적용</li><li>프로젝트 산출물 관리</li> |
| **조예빈** | <ul><li>프로젝트 캐릭터 디자인</li><li>Live 2D 모션, 애니메이션  제작</li><li>PPT 디자인</li> |

## 3. 주요 기능

- **4단계 개인 맞춤 정책 매칭 시스템**
- **LLM 시뮬레이션 & 솔루션**
- **시뮬레이션 결과 관리 및 알림 기능**
- **Live2D 챗봇 , AI 정책 전문가 '리아'**

## 4. 화면 구성

## 5. 기술 스택

### 5.1 Backend
<table>
  <tr>
    <td align="center" width="110"><img src="https://cdn.simpleicons.org/springboot/6DB33F" width="45" height="45"><br>Spring Boot</td>
    <td align="center" width="110"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/fastapi/fastapi-original.svg" width="45" height="45"><br>FastAPI</td>
    <td align="center" width="110"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg" width="45" height="45"><br>Java</td>
    <td align="center" width="110"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/python/python-original.svg" width="45" height="45"><br>Python</td>
    <td align="center" width="110"><img src="https://raw.githubusercontent.com/mybatis/logo/master/logo-bird-ninja.svg" width="45" height="45"><br>MyBatis</td>
    <td align="center" width="110"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/gradle/gradle-original.svg" width="45" height="45"><br>Gradle</td>
    <td align="center" width="110"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/tomcat/tomcat-original.svg" width="45" height="45"><br>Tomcat</td>
  </tr>
</table>

### 5.2 AI & Database
<table>
  <tr>
    <td align="center" width="110"><img src="https://cdn.simpleicons.org/openai/412991" width="45" height="45"><br>OpenAI</td>
    <td align="center" width="110"><img src="https://cdn.simpleicons.org/chroma/FF4F00" width="45" height="45"><br>ChromaDB</td>
    <td align="center" width="110"><img src="https://cdn.simpleicons.org/langchain/1C3C3C" width="45" height="45"><br>LangChain</td>
    <td align="center" width="110"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mysql/mysql-original.svg" width="45" height="45"><br>MySQL</td>
  </tr>
</table>

### 5.3 Web & Character
<table>
  <tr>
    <td align="center" width="110"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/javascript/javascript-original.svg" width="45" height="45"><br>JS</td>
    <td align="center" width="110"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/html5/html5-original.svg" width="45" height="45"><br>HTML5</td>
    <td align="center" width="110"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/css3/css3-original.svg" width="45" height="45"><br>CSS3</td>
    <td align="center" width="110"><br><br>Live2D SDK</td>
    <td align="center" width="110"><img src="https://cdn.simpleicons.org/thymeleaf/005F0F" width="45" height="45"><br>Thymeleaf</td>
  </tr>
</table>

### 5.4 Infra & Cloud
<table>
  <tr>
    <td align="center" width="110"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/googlecloud/googlecloud-original.svg" width="45" height="45"><br>GCP</td>
    <td align="center" width="110"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/nginx/nginx-original.svg" width="45" height="45"><br>Nginx</td>
    <td align="center" width="110"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/github/github-original.svg" width="45" height="45"><br>GitHub</td>
    <td align="center" width="110"><img src="https://cdn.simpleicons.org/ngrok/1F1E37" width="45" height="45"><br>Ngrok</td>
    <td align="center" width="110"><br><br>Gabia</td>
  </tr>
</table>

### 5.5 Security & API
<table>
  <tr>
    <td align="center" width="110"><img src="https://cdn.simpleicons.org/springsecurity/6DB33F" width="45" height="45"><br>Spring Security</td>
    <td align="center" width="110"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/google/google-original.svg" width="45" height="45"><br>OAuth 2.0</td>
    <td align="center" width="110"><img src="https://cdn.simpleicons.org/kakaotalk/FFCD00" width="45" height="45"><br>Kakao API</td>
    <td align="center" width="110"><img src="https://cdn.simpleicons.org/gmail/EA4335" width="45" height="45"><br>SMTP</td>
    <td align="center" width="110"><img src="https://cdn.simpleicons.org/letsencrypt/003A70" width="45" height="45"><br>SSL/TLS</td>
  </tr>
</table>

### 5.6 Tools & DevOps
<table>
  <tr>
    <td align="center" width="110"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/intellij/intellij-original.svg" width="45" height="45"><br>IntelliJ</td>
    <td align="center" width="110"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/vscode/vscode-original.svg" width="45" height="45"><br>VS Code</td>
    <td align="center" width="110"><img src="https://cdn.simpleicons.org/googlecolab/F9AB00" width="45" height="45"><br>Colab</td>
    <td align="center" width="110"><img src="https://cdn.simpleicons.org/googlecloud/4285F4" width="45" height="45"><br>Cloud Log</td>
    <td align="center" width="110"><br><br>Cubism</td>
    <td align="center" width="110"><img src="https://cdn.simpleicons.org/githubactions/2088FF" width="45" height="45"><br>GitHub Actions</td>
  </tr>
</table>

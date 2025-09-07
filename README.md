# LandCapturePlugin

Paper 1.16.5용 마인크래프트 땅따먹기 게임 플러그인입니다.

## 🎯 플러그인 개요

**LandCapturePlugin**은 팀 기반 영토 점령 게임을 위한 종합적인 마인크래프트 플러그인입니다. 플레이어들은 팀을 구성하여 다양한 점령지를 두고 전략적인 경쟁을 펼칩니다.

## 🎮 게임 소개

땅따먹기 게임은 3-4팀이 5개의 점령지를 두고 경쟁하는 전략 게임입니다.

### 🗺️ 게임 맵

-  **크기**: 3600×3600 (-1800~1800)
-  **점령지 5곳**: 물, 불, 얼음, 바람, 중앙
-  **점령 구역**: 금블록 안쪽

### 👥 팀 시스템

-  **3-4팀**, 각 팀당 **3명**
-  팀원 간 `/tpa` 사용 가능

### ⚔️ 점령 시스템

-  **기본 점령지** (물, 불, 얼음, 바람): 점령 5분, 탈환 10분
-  **중앙 점령지**: 점령 10분, 탈환 15분
-  **특수 규칙**: 기본 3곳 점령 시 중앙 점령 불가

### 🎯 승리 조건

-  **기본 루트**: 4곳 점령 → 세트 보너스 1점 = 5점 승리
-  **중앙 루트**: 중앙(2점) + 기본 2곳(2점) + 추가 1곳(1점) = 5점 승리

## 🛠️ 개발 환경

-  **Java**: JDK 16 이상
-  **Maven**: 3.6 이상
-  **Minecraft**: 1.16.5
-  **Paper API**: 1.16.5-R0.1-SNAPSHOT

## 📦 빌드 방법

```bash
# 플러그인 빌드
mvn clean package

# 빌드된 JAR 파일은 target/ 폴더에 생성됩니다
```

## 🚀 설치 방법

1. 빌드된 JAR 파일을 서버의 `plugins` 폴더에 복사
2. 서버 재시작
3. 플러그인이 자동으로 로드됩니다

## 🎮 사용법

### 기본 명령어

-  `/join <팀이름>` - 팀에 가입
-  `/leave` - 팀에서 탈퇴
-  `/team` - 팀 정보 확인
-  `/info` - 게임 정보 확인

### 게임 관리

-  `/game start` - 게임 시작 (관리자)
-  `/game stop` - 게임 중단 (관리자)
-  `/game status` - 게임 상태 확인
-  `/game map` - 테스트 맵 표시
-  `/game help` - 게임 명령어 도움말
-  `/capture` - 점령지 정보 확인

### 교환 시스템

-  `/exchange` - 교환 가능한 아이템 목록
-  `/exchange info` - 내가 교환할 수 있는 아이템 확인
-  왼손에 아이템을 들고 우클릭하여 교환

### 교환 가능한 아이템

-  **청금석 64개** → 경험치 병 64개
-  **철 32개** → 빵 64개

### TPA 시스템 (팀원 전용)

-  `/tpa <플레이어>` - 팀원에게 TPA 요청 보내기
-  `/tpaccept` - TPA 요청 수락
-  `/tpdeny` - TPA 요청 거부
-  `/tpcancel` - TPA 요청 취소
-  `/tpastatus` - TPA 상태 확인

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── java/
│   │   └── com/example/plugin/
│   │       ├── MinecraftPlugin.java          # 메인 플러그인 클래스
│   │       ├── team/                         # 팀 시스템
│   │       │   ├── Team.java
│   │       │   └── TeamManager.java
│   │       ├── capture/                      # 점령 시스템
│   │       │   ├── CaptureZone.java
│   │       │   └── CaptureManager.java
│   │       ├── effects/                      # 점령지 효과
│   │       │   └── ZoneEffectManager.java
│   │       ├── exchange/                     # 교환 시스템
│   │       │   └── ExchangeManager.java
│   │       ├── commands/                     # 명령어들
│   │       │   ├── TeamCommand.java
│   │       │   ├── JoinCommand.java
│   │       │   ├── LeaveCommand.java
│   │       │   ├── GameCommand.java
│   │       │   ├── CaptureCommand.java
│   │       │   ├── ExchangeCommand.java
│   │       │   └── InfoCommand.java
│   │       └── listeners/                    # 이벤트 리스너
│   │           └── PlayerListener.java
│   └── resources/
│       ├── plugin.yml                       # 플러그인 메타데이터
│       └── config.yml                       # 설정 파일
├── pom.xml                                  # Maven 설정
└── README.md
```

## ⚙️ 설정

### config.yml

```yaml
settings:
   enabled: true
   debug: false
   messages:
      prefix: "&8[&6플러그인&8] &r"
      no-permission: "&c권한이 없습니다!"
      player-only: "&c이 명령어는 플레이어만 사용할 수 있습니다!"
```

## 🔧 개발 가이드

### 새로운 명령어 추가

1. `src/main/java/com/example/plugin/commands/` 폴더에 명령어 클래스 생성
2. `MinecraftPlugin.java`의 `registerCommands()` 메서드에서 등록
3. `plugin.yml`에 명령어 정보 추가

### 새로운 이벤트 리스너 추가

1. `src/main/java/com/example/plugin/listeners/` 폴더에 리스너 클래스 생성
2. `MinecraftPlugin.java`의 `registerEventListeners()` 메서드에서 등록

### 설정 파일 사용

```java
// config.yml에서 값 읽기
String message = getConfig().getString("settings.messages.prefix");
boolean debug = getConfig().getBoolean("settings.debug");
```

## 🎯 주요 기능

### 팀 시스템

-  4개 팀 (빨강, 파랑, 초록, 노랑)
-  각 팀당 최대 3명
-  팀원 간 TPA 사용 가능

### 점령 시스템

-  5개 점령지 (물, 불, 얼음, 바람, 중앙)
-  실시간 점령 진행률 표시
-  점령 완료 시 효과 적용

### 효과 시스템

-  **불**: 용암 지대 + 화염 저항
-  **물**: 물 지대 + 수중 호흡 + 돌고래의 가호
-  **얼음**: 얼음 지대 + 적에게 구속 효과
-  **바람**: 점프 맵 + 신속 효과
-  **중앙**: 전장 환경

### UI 시스템

-  액션바로 실시간 점령 현황 표시
-  타이틀로 점령/탈환 알림
-  보스바로 팀 점수 표시

### 교환 시스템

-  왼손 아이템으로 교환
-  철/청금석을 경험치/식량으로 교환
-  인벤토리 공간 자동 확인

## 🗺️ 테스트 맵 설정

### 맵 구성 (0,0 중심 20x20 정사각형)

-  **중앙**: (0, 0) - 중심 점령지 (5x5)
-  **북쪽**: (0, -8) - 물 점령지 (5x5)
-  **남쪽**: (0, 8) - 불 점령지 (5x5)
-  **동쪽**: (8, 0) - 땅 점령지 (5x5)
-  **서쪽**: (-8, 0) - 바람 점령지 (5x5)
-  **대각선**: (6, 6) - 얼음 점령지 (5x5)

### 맵 확인 명령어

-  `/game map` - 점령지 위치와 상태 확인

### 맵 레이아웃

```
    북쪽(물)
서쪽(바람)  중앙(중심)  동쪽(땅)
    남쪽(불)
        대각선(얼음)
```

## 📝 라이선스

이 프로젝트는 **Creative Commons Attribution-NonCommercial 4.0 International License** 하에 배포됩니다.

### 라이선스 요약

-  ✅ **자유로운 사용**: 누구나 이 플러그인을 사용할 수 있습니다
-  ✅ **수정 및 배포**: 코드를 수정하고 배포할 수 있습니다
-  ✅ **저작자 표시**: 원작자에게 적절한 크레딧을 제공해야 합니다
-  ❌ **상업적 사용 금지**: 상업적 목적으로 사용할 수 없습니다

### 상세 정보

-  **전체 라이선스 (영어)**: [LICENSE](LICENSE) 파일 참조
-  **전체 라이선스 (한국어)**: [LICENSE_KO.txt](LICENSE_KO.txt) 파일 참조
-  **라이선스 웹사이트**: https://creativecommons.org/licenses/by-nc/4.0/
-  **문의사항**: 상업적 사용이나 라이선스 관련 문의가 있으시면 연락주세요

### 사용 시 주의사항

1. **저작자 표시**: 플러그인 사용 시 원작자를 명시해주세요
2. **상업적 사용 금지**: 수익을 목적으로 한 사용은 금지됩니다
3. **수정 배포**: 수정된 버전을 배포할 때는 원본과의 차이점을 명시해주세요

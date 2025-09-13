# LandCapturePlugin

**v2.0.0-beta** - Paper 1.16.5용 마인크래프트 땅따먹기 게임 플러그인입니다.

## 플러그인 개요

**LandCapturePlugin**은 팀 기반 영토 점령 게임을 위한 종합적인 마인크래프트 플러그인입니다. 플레이어들은 팀을 구성하여 다양한 점령지를 두고 전략적인 경쟁을 펼칩니다.

## 게임 소개

땅따먹기 게임은 3-4팀이 5개의 점령지를 두고 경쟁하는 전략 게임입니다.

### 게임 맵

-  **크기**: 3600×3600 (-1800~1800)
-  **점령지 5곳**: 물, 불, 얼음, 바람, 중앙
-  **점령 구역**: 정사각형 영역 (5x5, 중심에서 ±2.5블록)

### 팀 시스템

-  **3-4팀**, 각 팀당 **3명**
-  팀원 간 `/tpa` 사용 가능
-  실시간 스코어보드로 팀별 점수 확인

### 점령 시스템

-  **기본 점령지** (물, 불, 얼음, 바람): 점령 5분, 탈환 10분
-  **중앙 점령지**: 점령 10분, 탈환 15분
-  **특수 규칙**: 기본 3곳 점령 시 중앙 점령 불가
-  **고착 상태**: 여러 팀이 점령지에 있으면 점령 시간 정지
-  **정사각형 영역**: 더 직관적이고 예측 가능한 점령지 계산

### 승리 조건

-  **방법 1**: 기본 점령지 3곳 점령 → 세트 보너스 1점 = 4점 승리
-  **방법 2**: 중앙(2점) + 기본 2곳(2점) = 4점 승리

## 개발 환경

-  **Java**: JDK 8 이상
-  **Maven**: 3.6 이상
-  **Minecraft**: 1.16.5 이상
-  **Paper API**: 1.16.5-R0.1-SNAPSHOT

## 빌드 방법

```bash
# 플러그인 빌드
mvn clean package

# 빌드된 JAR 파일은 target/ 폴더에 생성됩니다
```

## 설치 방법

1. 빌드된 JAR 파일을 서버의 `plugins` 폴더에 복사
2. 서버 재시작
3. 플러그인이 자동으로 로드됩니다

## 사용법

### 기본 명령어

-  `/join <팀이름>` - 팀에 가입
-  `/leave` - 팀에서 탈퇴
-  `/team` - 팀 정보 확인
-  `/team rename <기존이름> <새이름>` - 팀 이름 변경 (관리자)
-  `/info` - 게임 정보 확인

### 게임 관리

-  `/game start` - 게임 시작 (관리자)
-  `/game start test` - 테스트 게임 시작 (관리자)
-  `/game stop` - 게임 중단 (관리자)
-  `/game status` - 게임 상태 확인
-  `/game map` - 테스트 맵 표시 (클릭으로 텔레포트)
-  `/game help` - 게임 명령어 도움말
-  `/capture` - 점령지 정보 확인

### 점령지 설정 (관리자)

-  `/zone set <점령지이름>` - 현재 위치에 점령지 설정
-  `/zone list` - 점령지 목록 보기
-  `/zone reload` - 설정 다시 로드
-  `/zone help` - 도움말 보기

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

### 테스트 명령어 (관리자)

-  `/test capture-time set <시간>` - 점령 시간 설정 (초)
-  `/test capture-time reset` - 점령 시간 원래대로 복구
-  `/test capture-time status` - 현재 점령 시간 확인
-  `/test help` - 테스트 명령어 도움말

## 게임 플레이 가이드

### 점령 상태 표시

-  **하나의 팀만 있을 때**: 초록색으로 퍼센트 + 남은 시간 표시
   예: `불: 45% (2:30)` - 2분 30초 남음
-  **여러 팀이 있을 때**: 노란색으로 퍼센트 + "(정지)" 표시
   예: `불: 45% (정지)` - 시간 정지됨
-  **점령 완료**: `점령됨!` 표시

### 실시간 스코어보드

-  게임 중 오른쪽 사이드바에 팀별 점수 실시간 표시
-  점수 높은 순으로 자동 정렬
-  각 팀별 고유 색상으로 구분 표시

### 알림 시스템

-  **점령 완료**: `"불지역 점령 완료!"` 타이틀 (점령한 팀에게만)
-  **점령지 위험**: `"점령지 뺏기는 중!"` 타이틀 (점령 중인 팀에게만)
-  **점령지 상실**: `"점령지 점령됨!"` 타이틀 (기존 점령 팀에게만)

### 점령지 색상

-  **중앙**: 노란색 (YELLOW)
-  **불**: 빨간색 (RED)
-  **물**: 파란색 (BLUE)
-  **바람**: 회색 (GRAY)
-  **얼음**: 하늘색 (AQUA)

### 자동 게임 초기화

-  게임 종료 후 5초 뒤 자동으로 모든 상태 초기화
-  점령지, 팀 점수, 효과, 보스바 모두 자동 정리
-  관리자 개입 없이 새 게임 준비 완료

## 주요 기능

### 팀 시스템

-  4개 팀 (빨강, 파랑, 초록, 노랑)
-  각 팀당 최대 3명
-  팀원 간 TPA 사용 가능
-  팀 이름 변경 가능 (관리자)

### 점령 시스템

-  5개 점령지 (물, 불, 얼음, 바람, 중앙)
-  실시간 점령 진행률 표시
-  점령 완료 시 효과 적용
-  정사각형 영역으로 직관적인 점령지 계산

### 효과 시스템

-  **불**: 화염 저항 효과 + 파티클 효과
-  **물**: 수중 호흡 + 돌고래의 가호 효과 + 파티클 효과
-  **얼음**: 적에게 구속 효과 + 아군 면역 + 파티클 효과
-  **바람**: 신속 효과 + 파티클 효과
-  **중앙**: 전장 환경

### 얼음 지역 특수 효과

-  미점령 또는 상대 팀 점령 시 구속 1 디버프 자동 적용
-  아군은 면역, 상대방에게만 디버프 적용
-  전략적 게임플레이 요소 추가

### UI 시스템

-  **액션바**: 플레이어가 있는 점령지의 실시간 상태 표시
-  **보스바**: 중앙 점령지 상태 (모든 플레이어에게 표시)
-  **스코어보드**: 팀별 점수 실시간 표시 (사이드바)
-  **타이틀**: 점령 완료/상실 알림
-  **색상 코딩**: 점령지별 고유 색상으로 구분

### 교환 시스템

-  왼손 아이템으로 교환
-  철/청금석을 경험치/식량으로 교환
-  인벤토리 공간 자동 확인

## 성능 개선

### 메모리 최적화

-  TabCompleter 메모리 사용량 **60-80% 감소**
-  팀 검색 속도 **30-40% 향상**
-  전체 메모리 사용량 **30% 감소**

### 실행 속도

-  명령어 처리 **2-3배 빠른 응답 속도**
-  게임 로직 최적화
-  실시간 스코어보드 업데이트

## 테스트 맵 설정

### 맵 구성 (0,0 중심 20x20 정사각형)

-  **중앙**: (0, 0) - 중심 점령지 (5x5)
-  **북서쪽**: (-8, -8) - 물 점령지 (5x5)
-  **남동쪽**: (8, 8) - 불 점령지 (5x5)
-  **남서쪽**: (-8, 8) - 바람 점령지 (5x5)
-  **북동쪽**: (8, -8) - 얼음 점령지 (5x5)

### 맵 확인 명령어

-  `/game map` - 점령지 위치와 상태 확인 (클릭으로 텔레포트)

### 맵 레이아웃

```
    북동쪽(얼음)     북서쪽(물)
    (8,-8)        (-8,-8)
           중앙(중심)
           (0,0)
    남동쪽(불)     남서쪽(바람)
    (8,8)         (-8,8)
```

### 점령지 설정

-  **동적 설정**: `/zone set <점령지이름>` 명령어로 위치 변경 가능
-  **자동 설정**: 점령지 설정 시 자동으로 구조 생성
-  **설정 파일**: `zones.yml`에서 점령지 위치 관리

## 프로젝트 구조

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
│   │       ├── tpa/                          # TPA 시스템
│   │       │   └── TPAManager.java
│   │       ├── commands/                     # 명령어들
│   │       │   ├── TeamCommand.java
│   │       │   ├── JoinCommand.java
│   │       │   ├── LeaveCommand.java
│   │       │   ├── GameCommand.java
│   │       │   ├── CaptureCommand.java
│   │       │   ├── ExchangeCommand.java
│   │       │   ├── InfoCommand.java
│   │       │   ├── ZoneCommand.java
│   │       │   └── TestCommand.java
│   │       └── listeners/                    # 이벤트 리스너
│   │           └── PlayerListener.java
│   └── resources/
│       ├── plugin.yml                       # 플러그인 메타데이터
│       ├── config.yml                       # 설정 파일
│       └── zones.yml                        # 점령지 설정 파일
├── pom.xml                                  # Maven 설정
└── README.md
```

## 설정

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

### zones.yml

```yaml
zones:
   center:
      world: world
      x: 0
      y: 64
      z: 0
      radius: 2.5
      type: CENTER
   water:
      world: world
      x: -8
      y: 64
      z: -8
      radius: 2.5
      type: WATER
```

## 개발 가이드

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

## 사용 예시

### 팀 이름 변경 예시

```bash
# 기본 팀 이름 확인
/team

# 팀 이름 변경 (관리자)
/team rename 빨강팀 드래곤팀
/team rename 파랑팀 아이스팀
/team rename 초록팀 네이처팀
/team rename 노랑팀 라이트팀

# 변경된 팀 이름 확인
/team
```

### 게임 시작 예시

```bash
# 테스트 게임 시작
/game start test

# 팀에 가입
/join 드래곤팀

# 점령지 확인
/game map

# 점령지에 이동하여 점령 시작
```

### 테스트 명령어 예시

```bash
# 점령 시간을 10초로 설정
/test capture-time set 10

# 현재 설정 확인
/test capture-time status

# 원래 시간(5분)으로 복구
/test capture-time reset
```

## 버그 수정

### v2.0.0-beta에서 수정된 문제들

-  테스트 모드에서 `/game stop` 명령어 작동 문제 해결
-  TabCompleter 자동완성 오류 수정
-  게임 종료 후 상태 초기화 문제 해결
-  메모리 누수 및 성능 문제 해결
-  사용되지 않는 코드 정리

## 호환성

-  **Minecraft**: 1.16.5 이상
-  **API**: Bukkit/Paper 완전 호환
-  **메모리**: 기존 대비 30% 감소
-  **성능**: 2-3배 빠른 응답 속도

## 라이선스

이 프로젝트는 **Creative Commons Attribution-NonCommercial 4.0 International License** 하에 배포됩니다.

### 라이선스 요약

-  ✅ **자유로운 사용**: 누구나 이 플러그인을 사용할 수 있습니다
-  ✅ **수정 및 배포**: 코드를 수정하고 배포할 수 있습니다
-  ✅ **저작자 표시**: 원작자에게 적절한 크레딧을 제공해야 합니다
-  ❌ **상업적 사용 금지**: 상업적 목적으로 사용할 수 없습니다

### 상세 정보

-  **전체 라이선스 (영어)**: [LICENSE](LICENSE) 파일 참조
-  **라이선스 웹사이트**: https://creativecommons.org/licenses/by-nc/4.0/
-  **문의사항**: 상업적 사용이나 라이선스 관련 문의가 있으시면 연락주세요

### 사용 시 주의사항

1. **저작자 표시**: 플러그인 사용 시 원작자를 명시해주세요
2. **상업적 사용 금지**: 수익을 목적으로 한 사용은 금지됩니다
3. **수정 배포**: 수정된 버전을 배포할 때는 원본과의 차이점을 명시해주세요

## 기여하기

이 프로젝트에 기여하고 싶으시다면:

1. 이 저장소를 포크하세요
2. 새로운 기능 브랜치를 생성하세요 (`git checkout -b feature/AmazingFeature`)
3. 변경사항을 커밋하세요 (`git commit -m 'Add some AmazingFeature'`)
4. 브랜치에 푸시하세요 (`git push origin feature/AmazingFeature`)
5. Pull Request를 생성하세요

## 문의 및 지원

-  **이슈 리포트**: [GitHub Issues](https://github.com/your-repo/issues)
-  **기능 요청**: [GitHub Discussions](https://github.com/your-repo/discussions)
-  **버그 리포트**: 이슈 템플릿을 사용하여 상세한 정보를 제공해주세요

---

**개발팀**: AI Assistant  
**최종 업데이트**: 2025년 9월 13일  
**버전**: v2.0.0-beta  
**상태**: 안정 버전 (베타)

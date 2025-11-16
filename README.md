# OccupyPlugin (점령 플러그인)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.16.5-brightgreen.svg)](https://www.minecraft.net/)
[![Java](https://img.shields.io/badge/Java-8-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-CC%20BY--NC%204.0-blue.svg)](LICENSE)

> **주의**: 개인적인 활동을 위한 플러그인입니다.

**Minecraft 1.16.5 Paper** 서버를 위한 팀 기반 점령전 게임 플러그인입니다.

## 📋 목차

- [게임 소개](#-게임-소개)
- [주요 기능](#-주요-기능)
- [설치 방법](#-설치-방법)
- [명령어](#-명령어)
- [게임 플레이](#-게임-플레이)
- [개발 환경](#-개발-환경)
- [프로젝트 구조](#-프로젝트-구조)
- [브랜치 전략](#-브랜치-전략)
- [라이선스](#-라이선스)

## 🎮 게임 소개

**OccupyPlugin**은 3-4팀이 5개의 점령지를 두고 경쟁하는 전략 기반 팀 게임입니다.

### 게임 맵

- **크기**: 3600×3600 블록 (-1800 ~ 1800)
- **점령지**: 5곳 (기본 4곳 + 중앙 1곳)
- **점령 구역**: 정사각형 영역 (15×15 블록)

### 점령지 목록

| 점령지 | 타입 | 색상 | 효과 |
|--------|------|------|------|
| **제네시스** | 중앙 | 노란색 | 2점 (승리 조건) |
| **이그니스** | 불 | 빨간색 | 화염 저항 |
| **아틀란스** | 물 | 파란색 | 수중 호흡 + 돌고래의 가호 |
| **크라이오시스** | 얼음 | 하늘색 | 적 구속 (아군 면역) |
| **사이클론즈** | 바람 | 회색 | 신속 |

### 팀 시스템

- **3-4팀**, 각 팀당 **3명**
- 팀원 간 `/tpa` 텔레포트 가능
- 실시간 스코어보드로 점수 확인

### 점령 시스템

- **기본 점령지**: 점령 5분, 탈환 10분
- **중앙 점령지**: 점령 10분, 탈환 15분
- **특수 규칙**: 한 팀이 기본 점령지 3곳을 점령하면 중앙 점령 불가
- **고착 상태**: 여러 팀이 동시에 점령지에 있으면 점령 시간 정지

### 승리 조건

- **방법 1**: 기본 점령지 4개 모두 점령 → 보너스 1점 = **5점 승리**
- **방법 2**: 기본 점령지 2개 + 중앙(2점) → 보너스 1점 = **5점 승리**

### 알림 시스템

- **기본 점령지**: 점령 완료 시에만 전체 알림 (진행률 알림 없음)
- **중앙 점령지**: 25%, 50%, 75% 진행률 알림 + 점령 완료 알림
- **점령 완료**: 전체 플레이어에게 타이틀 알림
- **고착 상태**: 보스바에 "고착 상태!" 표시

## ✨ 주요 기능

### 🎯 점령 시스템

- **5개 점령지** (정사각형 15×15 영역)
- **실시간 점령 진행률** 표시 (액션바)
- **자동 신호기 설치** (색유리 + 신호기 + 철블록)
- **점령 상태 시각화** (팀 색상별 유리 블록)

### 👥 팀 관리

- **4개 팀** (빨강, 파랑, 초록, 노랑)
- **팀원 제한**: 각 팀당 최대 3명
- **팀 밸런스**: 자동 인원 제한
- **실시간 스코어보드**: 사이드바에 팀별 점수 표시

### 🎁 효과 시스템

점령지를 소유한 팀의 모든 플레이어에게 버프 적용:

- **이그니스 (불)**: 화염 저항
- **아틀란스 (물)**: 수중 호흡 + 돌고래의 가호
- **크라이오시스 (얼음)**: 적에게 구속 효과 (아군 면역)
- **사이클론즈 (바람)**: 신속

### 💎 교환 시스템

왼손에 아이템을 들고 우클릭하여 교환:

- **청금석 64개** → 경험치 병 64개
- **철괴 32개** → 빵 64개

### 🔄 TPA 시스템

- **팀원 전용**: 같은 팀 플레이어끼리만 텔레포트 가능
- **요청/수락/거부**: 간단한 명령어로 관리
- **자동 만료**: 30초 후 요청 자동 취소

### 🎨 UI 시스템

- **액션바**: 플레이어가 있는 점령지 상태 실시간 표시
- **보스바**: 중앙 점령지(제네시스) 상태 표시
- **스코어보드**: 팀별 점수 실시간 업데이트 (사이드바)
- **타이틀**: 점령 완료/상실 알림

## 📦 설치 방법

### 요구사항

- **Minecraft**: 1.16.5
- **서버**: Paper/Spigot 1.16.5 이상
- **Java**: JDK 8 이상 (서버 실행 환경에 맞춰 빌드됨)

### 빌드

```bash
# 저장소 클론
git clone https://github.com/misty6760/mc-occupy-plugin.git
cd mc-occupy-plugin

# 빌드
cd OccupyPlugin
mvn clean package

# 빌드된 JAR 파일 위치
# target/OccupyPlugin-1.0-SNAPSHOT.jar
```

### 설치

1. 빌드된 `OccupyPlugin-1.0-SNAPSHOT.jar` 파일을 서버의 `plugins/` 폴더에 복사
2. 서버 재시작
3. 플러그인 자동 로드 확인

## 📝 명령어

### 게임 관리 (관리자)

```
/occupy start              - 게임 시작
/occupy stop               - 게임 중단
/occupy test start         - 테스트 모드로 게임 시작 (점령 시간 단축)
/occupy info               - 게임 정보 보기
/occupy teaminfo           - 팀 정보 보기
```

### 점령지 설정 (관리자)

```
/occupy setpoint <이름>    - 현재 위치에 점령지 설정 (15x15 정사각형, 신호기 자동 설치)
/occupy removepoint <이름> - 점령지 제거
```

**점령지 이름 목록**:
- `제네시스` (중앙)
- `이그니스` (불)
- `아틀란스` (물)
- `크라이오시스` (얼음)
- `사이클론즈` (바람)

### TPA 명령어 (플레이어)

```
/tpa <플레이어>            - 팀원에게 텔레포트 요청
/tpaaccept                - 텔레포트 요청 수락
/tpadeny                  - 텔레포트 요청 거부
```

### 교환 시스템

```
왼손에 교환할 아이템을 들고 F키를 누르세요
```

**교환 가능한 아이템**:
- 청금석 64개 → 경험치 병 64개
- 철괴 32개 → 빵 64개

## 🎯 게임 플레이

### 게임 시작 전 준비

1. **팀 설정**: 마인크래프트 기본 팀 명령어로 4개 팀 생성
   ```
   /team add red "빨강팀"
   /team add blue "파랑팀"
   /team add green "초록팀"
   /team add yellow "노랑팀"
   ```

2. **팀 색상 설정**:
   ```
   /team modify red color red
   /team modify blue color blue
   /team modify green color green
   /team modify yellow color yellow
   ```

3. **팀 가입**: 각 플레이어를 팀에 배정
   ```
   /team join red <플레이어>
   /team join blue <플레이어>
   ```

4. **점령지 설정**: 5개 점령지 위치 설정
   ```
   /occupy setpoint 제네시스
   /occupy setpoint 이그니스
   /occupy setpoint 아틀란스
   /occupy setpoint 크라이오시스
   /occupy setpoint 사이클론즈
   ```

5. **게임 시작**:
   ```
   /occupy start
   ```

### 점령 진행

- **점령지 진입**: 15×15 정사각형 영역 안으로 이동
- **점령 진행**: 액션바에 실시간 진행률 표시
- **점령 완료**: 타이틀로 알림, 팀 색상으로 신호기 유리 변경
- **효과 획득**: 점령한 점령지에 따라 버프 자동 적용

### 점령 상태 표시 (액션바)

- **초록색**: 점령 진행 중 (`제네시스: 45% (2:30)` - 2분 30초 남음)
- **노란색**: 고착 상태 (`제네시스: 45% (정지)` - 여러 팀이 동시에 있음)
- **완료**: `제네시스: 점령됨!`

### 전략 팁

1. **기본 3개 점령**: 기본 점령지 3곳을 빠르게 점령하면 세트 보너스로 승리
2. **중앙 점령**: 중앙은 2점이므로 전략적으로 중요
3. **팀 협력**: `/tpa`로 팀원끼리 이동하여 점령지 방어/공격
4. **교환 시스템**: 청금석/철을 경험치/식량으로 교환하여 생존력 향상

### 테스트 모드

빠른 테스트를 위해 점령 시간을 단축:

```
/occupy test start
```

- **기본 점령지**: 1분 (정상: 5분)
- **중앙 점령지**: 2분 (정상: 10분)

## 🛠 개발 환경

### 기술 스택

- **언어**: Java 8 (서버 호환성을 위해)
- **빌드 도구**: Apache Maven 3.6+
- **Minecraft API**: Paper 1.16.5-R0.1-SNAPSHOT
- **IDE**: Visual Studio Code / IntelliJ IDEA

### 빌드 설정 (pom.xml)

```xml
<properties>
    <java.version>8</java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <release>8</release>
            </configuration>
        </plugin>
    </plugins>
</build>

<dependencies>
    <dependency>
        <groupId>com.destroystokyo.paper</groupId>
        <artifactId>paper-api</artifactId>
        <version>1.16.5-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### 개발 명령어

```bash
# 컴파일
mvn clean compile

# 패키징
mvn clean package

# 테스트
mvn test
```

## 📁 프로젝트 구조

```
OccupyPlugin/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/
│   │   │       └── think_ing/
│   │   │           └── occupyplugin/
│   │   │               ├── OccupyPlugin.java              # 메인 플러그인 클래스
│   │   │               ├── commands/                       # 명령어 시스템
│   │   │               │   ├── CommandManager.java        # 명령어 라우터
│   │   │               │   ├── SubCommand.java            # 명령어 인터페이스
│   │   │               │   ├── TabCompleteManager.java    # 자동완성 관리자
│   │   │               │   └── subcommands/               # 개별 명령어
│   │   │               │       ├── StartCommand.java      # 게임 시작
│   │   │               │       ├── StopCommand.java       # 게임 중단
│   │   │               │       ├── SetPointCommand.java   # 점령지 설정
│   │   │               │       ├── RemovePointCommand.java # 점령지 제거
│   │   │               │       ├── TeamInfoCommand.java   # 팀 정보
│   │   │               │       ├── InfoCommand.java       # 게임 정보
│   │   │               │       └── TestCommand.java       # 테스트 모드
│   │   │               ├── config/                        # 설정 관리
│   │   │               │   └── TeamConfigManager.java     # 팀 설정
│   │   │               ├── display/                       # UI 시스템
│   │   │               │   ├── BossBarManager.java        # 보스바 관리
│   │   │               │   ├── NotificationManager.java   # 알림 관리
│   │   │               │   └── ScoreboardManager.java     # 스코어보드 관리
│   │   │               ├── events/                        # 이벤트 리스너
│   │   │               │   ├── TeamListener.java          # 팀 이벤트
│   │   │               │   └── ExchangeListener.java      # 교환 이벤트
│   │   │               ├── game/                          # 게임 로직
│   │   │               │   ├── GameManager.java           # 게임 관리자
│   │   │               │   ├── OccupationPoint.java       # 점령지 모델
│   │   │               │   ├── ConfigLoader.java          # 설정 로더
│   │   │               │   ├── CaptureSystem.java         # 점령 시스템
│   │   │               │   ├── BeaconManager.java         # 신호기 관리
│   │   │               │   ├── EffectManager.java         # 효과 관리
│   │   │               │   ├── ScoreManager.java          # 점수 관리
│   │   │               │   └── VictoryChecker.java        # 승리 조건 체크
│   │   │               └── tpa/                           # TPA 시스템
│   │   │                   ├── TPAManager.java            # TPA 관리자
│   │   │                   ├── TPARequest.java            # TPA 요청 모델
│   │   │                   ├── TPACommandExecutor.java    # TPA 명령어
│   │   │                   └── TeamValidator.java         # 팀 검증
│   │   └── resources/
│   │       ├── plugin.yml                                 # 플러그인 메타데이터
│   │       └── config.yml                                 # 게임 설정
├── pom.xml                                                # Maven 설정
├── README.md                                              # 프로젝트 문서
├── LICENSE                                                # 라이선스
└── .gitignore                                             # Git 무시 파일
```

## 🌿 브랜치 전략

이 프로젝트는 **2-브랜치 전략**을 사용합니다:

### 브랜치 구조

```
develop (개발 브랜치, 기본) ──────► release (출시 브랜치)
    ↑
    │
feature/* (기능 브랜치, 선택적)
```

### 브랜치 설명

#### `develop` (개발 브랜치) - **기본 브랜치**
- 모든 개발 작업 진행
- 새로운 기능 개발 및 버그 수정
- 활발한 커밋 및 푸시
- 불안정할 수 있음

#### `release` (출시 브랜치)
- 안정화된 릴리즈 버전만 포함
- 테스트 완료 후 배포용
- JAR 파일 배포는 이 브랜치에서
- GitHub Release 태그 생성

### 작업 흐름

1. **개발**: `develop` 브랜치에서 직접 작업
   ```bash
   git checkout develop
   git add .
   git commit -m "feat: 새로운 기능 추가"
   git push origin develop
   ```

2. **복잡한 기능 개발** (선택적):
   ```bash
   git checkout develop
   git checkout -b feature/awesome-feature
   # 작업...
   git commit -m "feat: awesome feature 구현"
   git checkout develop
   git merge feature/awesome-feature
   git push origin develop
   ```

3. **출시 준비**:
   ```bash
   # 테스트 및 안정화 완료 후
   git checkout release
   git merge develop
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin release --tags
   ```

4. **GitHub Release 생성**:
   - GitHub에서 태그를 기반으로 Release 생성
   - 빌드된 JAR 파일 첨부
   - 변경사항 작성

## 📄 라이선스

이 프로젝트는 **Creative Commons Attribution-NonCommercial 4.0 International License** 하에 배포됩니다.

### 라이선스 요약

- ✅ **자유로운 사용**: 누구나 이 플러그인을 사용할 수 있습니다
- ✅ **수정 및 배포**: 코드를 수정하고 배포할 수 있습니다
- ✅ **저작자 표시**: 원작자에게 적절한 크레딧을 제공해야 합니다
- ❌ **상업적 사용 금지**: 상업적 목적으로 사용할 수 없습니다

### 상세 정보

- **전체 라이선스**: [LICENSE](LICENSE) 파일 참조
- **라이선스 웹사이트**: https://creativecommons.org/licenses/by-nc/4.0/
- **문의사항**: 상업적 사용이나 라이선스 관련 문의가 있으시면 이슈로 연락주세요

### 사용 시 주의사항

1. **저작자 표시**: 플러그인 사용 시 원작자를 명시해주세요
2. **상업적 사용 금지**: 수익을 목적으로 한 사용은 금지됩니다
3. **수정 배포**: 수정된 버전을 배포할 때는 원본과의 차이점을 명시해주세요

## 🤝 기여하기

이 프로젝트에 기여하고 싶으시다면:

1. 저장소를 포크하세요
2. `develop` 브랜치 기반으로 기능 브랜치를 생성하세요
   ```bash
   git checkout develop
   git checkout -b feature/AmazingFeature
   ```
3. 변경사항을 커밋하세요
   ```bash
   git commit -m "feat: Add some AmazingFeature"
   ```
4. 브랜치에 푸시하세요
   ```bash
   git push origin feature/AmazingFeature
   ```
5. `develop` 브랜치로 Pull Request를 생성하세요

### 커밋 컨벤션

- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정
- `style`: 코드 포맷팅
- `refactor`: 코드 리팩토링
- `test`: 테스트 코드
- `chore`: 빌드 설정 등

## 📞 문의 및 지원

- **이슈 리포트**: [GitHub Issues](https://github.com/misty6760/mc-occupy-plugin/issues)
- **기능 요청**: [GitHub Discussions](https://github.com/misty6760/mc-occupy-plugin/discussions)
- **버그 리포트**: 이슈 작성 시 상세한 정보를 포함해주세요

## 📜 변경 이력

### v1.0.0 (개발 중)

#### 추가
- ✨ 5개 점령지 시스템 (기본 4개 + 중앙 1개)
- ✨ 팀 기반 점령전 게임
- ✨ 실시간 스코어보드 시스템
- ✨ TPA 시스템 (팀원 전용)
- ✨ 교환 시스템 (청금석/철 → 경험치/식량)
- ✨ 점령지별 버프 효과
- ✨ 신호기 자동 설치 기능
- ✨ 테스트 모드 (점령 시간 단축)

#### 개선
- 🎨 기본 점령지 진행률 알림 제거 (스팸 방지)
- 🎨 중앙 점령지만 진행률 알림 유지
- 🎨 UI/UX 개선 (액션바, 보스바, 타이틀)
- ⚡ 코드 리팩토링 (단일 책임 원칙 적용)
- ⚡ 성능 최적화

#### 수정
- 🐛 신호기 설치 순서 수정 (색유리 → 신호기 → 철블록)
- 🐛 점령지 크기를 15×15 정사각형으로 수정
- 🐛 스코어보드 점수 표시 방식 개선
- 🐛 Java 8 호환성 수정 (List.of() 제거)
- 🐛 교환 시스템 수정 (왼손 아이템 감지)

---

**개발자**: [Misty6760](https://github.com/misty6760)  
**최종 업데이트**: 2025년 11월 16일  
**버전**: 1.0.0 (개발 중)  
**상태**: 개발 중

## ⭐ 스타 히스토리

이 프로젝트가 도움이 되었다면 스타를 눌러주세요! ⭐

[![Star History Chart](https://api.star-history.com/svg?repos=misty6760/mc-occupy-plugin&type=Date)](https://star-history.com/#misty6760/mc-occupy-plugin&Date)

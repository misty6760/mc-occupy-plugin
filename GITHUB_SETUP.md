# GitHub 브랜치 설정 가이드

이 가이드는 GitHub에서 기본 브랜치를 변경하고 main 브랜치를 삭제하는 방법을 설명합니다.

## 1. 기본 브랜치 변경

### 방법 1: 웹 인터페이스 사용 (권장)

1. **저장소 설정 페이지로 이동**
   ```
   https://github.com/misty6760/mc-occupy-plugin/settings
   ```

2. **왼쪽 사이드바에서 "Branches" 클릭**

3. **"Default branch" 섹션에서:**
   - `main` 옆의 ⇄ 아이콘 클릭
   - 드롭다운에서 `develop` 선택
   - "Update" 버튼 클릭
   - 확인 메시지: "I understand, update the default branch" 클릭

### 방법 2: 직접 URL 접근

브라우저에서 다음 URL을 직접 열기:
```
https://github.com/misty6760/mc-occupy-plugin/settings/branches
```

## 2. main 브랜치 삭제

기본 브랜치를 `develop`으로 변경한 후:

### 웹에서 삭제:

1. **Branches 페이지로 이동**
   ```
   https://github.com/misty6760/mc-occupy-plugin/branches
   ```

2. **main 브랜치 찾기**
   - "All branches" 목록에서 `main` 찾기

3. **삭제**
   - main 브랜치 옆의 🗑️ (휴지통) 아이콘 클릭
   - 확인 대화상자에서 삭제 확인

### 터미널에서 삭제:

기본 브랜치 변경 후 다음 명령어 실행:

```bash
git push origin --delete main
```

## 3. 확인

변경이 완료되면:

```bash
# 원격 브랜치 확인
git fetch --prune
git branch -a
```

**예상 결과:**
```
* develop
  release
  remotes/origin/develop
  remotes/origin/release
```

## 완료! ✅

이제 프로젝트는 2-브랜치 전략을 사용합니다:
- `develop`: 개발 브랜치 (기본)
- `release`: 출시 브랜치

---

**문제가 있나요?**
- GitHub에 로그인되어 있는지 확인
- 저장소에 대한 관리자 권한이 있는지 확인
- 브라우저 캐시를 지우고 다시 시도


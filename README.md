
<h1 align="center">오독오독</h1>

<p align="center">
오독오독은 자신이 읽었던 책, 읽고 있는 책, 앞으로 읽을 책을 자신의 서재에 보관하고 관리하는 모바일 앱 입니다.
</p>

<p align="center">
<img src="/previews/intro.png"/>
</p>


<img src="/previews/preview.gif" align="right" width="240"/>


<h3>Tech stack</h3>

- [Kotlin](https://kotlinlang.org/) 
- [Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [Flow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/) 
- Jetpack
  - Lifecycle
  - ViewModel
  - DataBinding
  - Room
  - [Hilt](https://dagger.dev/hilt/)
- Architecture
  - MVVM Architecture (View - DataBinding - ViewModel - Model)
  - Repository Pattern

<h3>Open API</h3>

**오독오독** 은 도서 정보 검색 기능을 위해 [알라딘 OpenAPI](https://blog.aladin.co.kr/openapi)를 사용하고 있습니다.

## Architecture
**오독오독** 은 [Google의 공식 아키텍처 가이드](https://developer.android.com/topic/architecture) 를 기반으로 한 MVVM 아키텍처와 Repository 패턴을 사용하여 설계되었습니다.

![architecture](/figure/figure0.png)

**오독오독** 의 전체 아키텍처는 UI 레이어와 데이터 레이어의 두 계층으로 구성되어 있으며, 각 계층은 고유한 컴포넌트와 역할을 가지고 있습니다.

### Architecture Overview

![architecture](/figure/figure1.png)

- 각 계층은 단방향 이벤트 및 데이터 흐름을 따릅니다. UI 레이어는 사용자 이벤트를 데이터 레이어로 전달하고, 데이터 레이어는 데이터를 스트림 형태로 UI에 제공합니다.
- 데이터 레이어는 다른 계층에 의존하지 않고 독립적으로 작동하도록 설계되었으며, 순수한 계층(Pure Layer)으로 구현되어 다른 레이어에 의존성이 없습니다.

이와 같이 느슨하게 결합된 아키텍처를 통해 컴포넌트의 재사용성과 앱의 확장성을 높일 수 있습니다.

### UI Layer

![architecture](/figure/figure2.png)

UI 레이어는 사용자와 상호작용할 수 있는 화면을 구성하는 UI 요소들과, 앱 상태를 유지하고 구성 변경 시 데이터를 복원하는 [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) 로 구성됩니다.
- UI 요소는 [DataBinding](https://developer.android.com/topic/libraries/data-binding)을 통해 데이터 흐름을 관찰하며, 이는 MVVM 아키텍처에서 핵심적인 역할을 합니다.

### Data Layer

![architecture](/figure/figure3.png)

데이터 레이어는 로컬 데이터베이스에서 데이터를 조회하거나 네트워크로부터 원격 데이터를 요청하는 등 비즈니스 로직을 처리하는 리포지토리로 구성됩니다.
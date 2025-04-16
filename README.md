
<h1 align="center">오독오독</h1>

<p align="center">
오독오독은 자신이 읽었던 책, 읽고 있는 책, 앞으로 읽을 책을 자신의 서재에 보관하고 관리하는 모바일 앱 입니다.
</p>

<p align="center">
<img src="/previews/intro.png"/>
</p>


<img src="/previews/preview.gif" align="right" width="320"/>


## Tech stack

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

## Architecture
**OdokOdok** is based on the MVVM architecture and the Repository pattern, which follows the [Google's official architecture guidance](https://developer.android.com/topic/architecture).

![architecture](../bookchibakchi/figure/figure0.png)

The overall architecture of **OdokOdok** is composed of two layers; the UI layer and the data layer. Each layer has dedicated components and they have each different responsibilities, as defined below:

**OdokOdok** was built with [Guide to app architecture](https://developer.android.com/topic/architecture), so it would be a great sample to show how the architecture works in real-world projects.


### Architecture Overview

![architecture](../bookchibakchi/figure/figure1.png)

- Each layer follows [unidirectional event/data flow](https://developer.android.com/topic/architecture/ui-layer#udf); the UI layer emits user events to the data layer, and the data layer exposes data as a stream to other layers.
- The data layer is designed to work independently from other layers and must be pure, which means it doesn't have any dependencies on the other layers.

With this loosely coupled architecture, you can increase the reusability of components and scalability of your app.

### UI Layer

![architecture](../bookchibakchi/figure/figure2.png)

The UI layer consists of UI elements to configure screens that could interact with users and [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) that holds app states and restores data when configuration changes.
- UI elements observe the data flow via [DataBinding](https://developer.android.com/topic/libraries/data-binding), which is the most essential part of the MVVM architecture.

### Data Layer

![architecture](../bookchibakchi/figure/figure3.png)

The data Layer consists of repositories, which include business logic, such as querying data from the local database and requesting remote data from the network. It is implemented as an offline-first source of business logic and follows the [single source of truth](https://en.wikipedia.org/wiki/Single_source_of_truth) principle.<br>

**OdokOdok** is an offline-first app is an app that is able to perform all, or a critical subset of its core functionality without access to the internet.
So users don't need to be up-to-date on the network resources every time and it will decrease users' data consumption. For further information, you can check out [Build an offline-first app](https://developer.android.com/topic/architecture/data-layer/offline-first).


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

<h3>Open-source libraries</h3>

- [Material-Components](https://github.com/material-components/material-components-android): 구글의 공식 디자인 시스템을 기반으로 모던하고 일관된 UI 컴포넌트를 구현하기 위해 사용했습니다.
- [Retrofit2 & OkHttp3](https://github.com/square/retrofit): REST API 통신을 효율적으로 처리하고, 네트워크 요청의 로깅 및 커스터마이징을 위해 사용했습니다.
- [Glide](https://github.com/bumptech/glide): 이미지의 비동기 로딩, 캐싱, 리사이징 등을 최적화하여 UI 성능을 향상시키기 위해 활용했습니다.
- [Dot indicator](https://github.com/tommybuonomo/dotsindicator): ViewPager2와 함께 사용하여 페이지 위치를 직관적으로 보여주는 점 기반 인디케이터를 제공했습니다.
- [Paging 3](https://developer.android.com/topic/libraries/architecture/paging/v3-overview?hl=ko): 도서 검색 시 출력할 데이터를 효율적으로 로드하고 UI에서 매끄럽게 표시하기 위해 Paging 아키텍처를 적용했습니다.
- [Shimmer](https://github.com/facebookarchive/shimmer-android): 로딩 상태를 시각적으로 표현하기 위해 Shimmer 효과를 적용하여 사용자 경험을 개선하고, 최신 UI 트렌드를 반영한 모던한 디자인을 구현했습니다.
- [Google ML Kit](https://developers.google.com/ml-kit/vision/text-recognition/v2/android?hl=ko): 책에서 읽었던 텍스트를 보다 쉽게 추출하기 위하여 OCR 기능을 구현할 때 사용했습니다.
- [Lottie](https://github.com/airbnb/lottie-android): JSON 기반의 벡터 애니메이션을 간편하게 재생하여 인터랙티브한 UI를 구성했습니다.
- [sdp](https://github.com/intuit/sdp): 다양한 해상도에서 일관된 UI 크기를 유지하기 위해 dp 단위의 자동 스케일링을 적용했습니다.
- [ImageCropper](https://github.com/Yalantis/uCrop): 사용자가 선택한 책의 페이지 이미지에서 텍스트 부분을 자를 수 있는 이미지 편집 기능을 제공했습니다.

<h3>Open API</h3>

**오독오독** 은 도서 정보 검색 기능을 위해 [알라딘 OpenAPI](https://blog.aladin.co.kr/openapi)를 사용하고 있습니다.

## Architecture
**오독오독** 은 [Google의 공식 아키텍처 가이드](https://developer.android.com/topic/architecture) 를 기반으로 한 MVVM 아키텍처와 Repository 패턴을 사용하여 설계되었습니다.

![architecture](/figure/figure0.png)

**오독오독** 의 전체 아키텍처는 UI 레이어와 데이터 레이어의 두 계층으로 구성되어 있으며, 각 계층은 고유한 컴포넌트와 역할을 가지고 있습니다.

## 주요 기능
<h3>도서 검색 기능</h3>
<table>
  <tr>
   <td valign="top" width="600">
       - 검색어를 입력하여 책을 검색할 수 있습니다.<br>
       - 도서 정보 출력을 위해  알라딘 OPEN API 를 사용하였습니다.<br>
       - 대량의 도서 데이터를 효율적으로 불러오기 위해 Paging 라이브러리를 도입, 사용자의 스크롤에 따라 데이터를 점진적으로 로드합니다.<br>
       - 효율적인 리스트 UI 구현을 위해 ListAdapter를 적용하였으며, DiffUtil을 활용한 데이터 변경 감지로 불필요한 View 바인딩을 줄여 성능을 개선하였습니다.<br>
       - 검색된 책의 상세 정보를 확인하고 저장할 수 있습니다.<br>
       <br><br><br>
      <img src="previews/search_book_architecture.png"/>
    </td>
    <td>
      <img src="previews/search2.gif" width="240"/>
    </td>
  </tr>
</table>

<h3>서재 관리</h3>
<table>
  <tr>
   <td valign="top" width="600">
      나의 서재에 저장된 책을 조회, 수정, 삭제할 수 있습니다.
    </td>
    <td>
      <img src="previews/manage.gif" width="240"/>
    </td>
  </tr>
</table>

<h3>독서 진행도 기록</h3>
<table>
  <tr>
   <td valign="top" width="600">
      독서 진행도를 측정하고 기록할 수 있습니다.
    </td>
    <td>
      <img src="previews/time3.gif" width="240"/>
    </td>
  </tr>
</table>

<h3>메모 기록</h3>
<table>
  <tr>
   <td valign="top" width="600">
      독서 중 기억에 남는 문구를 메모할 수 있습니다.
    </td>
    <td>
      <img src="previews/memoList.gif" width="240"/>
      <img src="previews/addMemo2.gif" width="240"/>
    </td>
  </tr>
</table>

[//]: # (## Architecture Overview)
[//]: # ()
[//]: # (![architecture]&#40;/figure/figure1.png&#41;)

[//]: # ()
[//]: # (- 각 계층은 단방향 이벤트 및 데이터 흐름을 따릅니다. UI 레이어는 사용자 이벤트를 데이터 레이어로 전달하고, 데이터 레이어는 데이터를 스트림 형태로 UI에 제공합니다.)

[//]: # (- 데이터 레이어는 다른 계층에 의존하지 않고 독립적으로 작동하도록 설계되었으며, 순수한 계층&#40;Pure Layer&#41;으로 구현되어 다른 레이어에 의존성이 없습니다.)

[//]: # ()
[//]: # (이와 같이 느슨하게 결합된 아키텍처를 통해 컴포넌트의 재사용성과 앱의 확장성을 높일 수 있습니다.)

[//]: # ()
[//]: # (## UI Layer)

[//]: # ()
[//]: # (![architecture]&#40;/figure/figure2.png&#41;)

[//]: # ()
[//]: # (UI 레이어는 사용자와 상호작용할 수 있는 화면을 구성하는 UI 요소들과, 앱 상태를 유지하고 구성 변경 시 데이터를 복원하는 [ViewModel]&#40;https://developer.android.com/topic/libraries/architecture/viewmodel&#41; 로 구성됩니다.)

[//]: # (- UI 요소는 [DataBinding]&#40;https://developer.android.com/topic/libraries/data-binding&#41;을 통해 데이터 흐름을 관찰하며, 이는 MVVM 아키텍처에서 핵심적인 역할을 합니다.)

[//]: # ()
[//]: # (## Data Layer)

[//]: # ()
[//]: # (![architecture]&#40;/figure/figure3.png&#41;)

[//]: # ()
[//]: # (데이터 레이어는 로컬 데이터베이스에서 데이터를 조회하거나 네트워크로부터 원격 데이터를 요청하는 등 비즈니스 로직을 처리하는 리포지토리로 구성됩니다.)

## License
```xml
Designed and developed by 2022 stevey-sy (Seyoung Shin)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

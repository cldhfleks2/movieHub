$(document).ready(function (){
    clickActorSearching();
    likeBtn();
    bookmarkBtn();
    gotoReviewPage();
    reportBtn();

//   카카오 맵
    initializeKakaoMap();
    handleSidebarToggle();
    handleRouteButton();
})

//배우나 감독 이름 클릭시 검색 하러 페이지 이동
function clickActorSearching(){
    //감독명 검색
    $(document).on("click", ".directorBox", function (){
        const peopleNm = $(this).data("peoplenm").trim();

        if (peopleNm) {
            // 검색 페이지로 이동하면서 peopleNm을 쿼리 파라미터로 전달
            window.location.href = `/search?peopleNm=${encodeURIComponent(peopleNm)}`;
        }
    });
    
    //배우명 검색
    $(document).on("click", ".actorBox", function (){
        const peopleNm = $(this).data("peoplenm").trim();

        if (peopleNm) {
            // 검색 페이지로 이동하면서 peopleNm을 쿼리 파라미터로 전달
            window.location.href = `/search?peopleNm=${encodeURIComponent(peopleNm)}`;
        }
    });
}

//페이지 새로고침하는 함수
function movieInfoReload(){
    const movieCd = $("#movieInfo").data("moviecd")
    $.ajax({
        url: "/detail/" + movieCd,
        method: "get",
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#movieInfo").replaceWith(dataHtml.find("#movieInfo"));

            console.log("movieInfo reload ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("movieInfo reload ajax failed")
        }
    })
}

//좋아요 버튼 동작
function likeBtn(){
    $(document).on("click", ".actionBtn.likeBtn", function (){
        const movieCd = $(this).data("moviecd");

        $.ajax({
            url: "/api/movieDetail/like",
            method: "post",
            data: {movieCd: movieCd},
            success: function (response, textStatus, xhr){
                console.log("add like ajax success")
                if (xhr.status === 200) {
                    movieInfoReload(); //좋아요 요청 성공시 페이지를 새로 고침
                }else{
                    alert("알 수 없는 성공")
                }
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("add like ajax failed")
            }
        })
    });
}

//찜하기 버튼 동작
function bookmarkBtn(){
    $(document).on("click", ".actionBtn.bookmarkBtn", function (){
        const movieCd = $(this).data("moviecd");

        $.ajax({
            url: "/api/bookmark/add",
            method: "post",
            data: {movieCd: movieCd},
            success: function (response, textStatus, xhr){
                console.log("add bookmark ajax success")
                if (xhr.status === 200) {
                    movieInfoReload(); //북마크 요청 성공시 페이지를 새로 고침
                }else{
                    alert("알 수 없는 성공")
                }
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("add bookmark ajax failed")
            }
        })
    });
}

//리뷰 작성 버튼 동작 : 리뷰 페이지로 이동
function gotoReviewPage(){
    $(document).on("click", ".linkBtn", function (){
        const movieCd = $(this).data("moviecd")
        window.location.href="/movieReview?isCometoMovieDetailPage=1&movieCd=" + movieCd; //파라미터 담아서 보냄
    })
}

function reportBtn(){
    //신고 모달창 보이기
    $(document).on('click', '.reportBtn', function() {
        const movieCd = $(this).data('moviecd');
        $('#movieCd').val(movieCd); //폼 안에 movieCd값을 담음
        $('#movieReviewReportModal').fadeIn().css('display', 'flex');
    });

    //신고 모달창 숨기기
    $(document).on('click', '.reportCancelBtn', function() {
        $('#movieReviewReportModal').fadeOut();
    });

    //신고 제출 버튼 클릭시
    $('.reportSubmitBtn').on('click', function(e) {
        e.preventDefault(); // 기본 폼 제출 방지

        // 폼 데이터를 직렬화해서 쿼리스트링으로 만듬
        var formData = $('#movieReviewReportForm').serialize();

        // 서버로 폼 제출
        $.ajax({
            url: '/api/movie/report', // 서버 URL
            type: 'POST',
            data: formData,  // 폼 데이터를 전송
            success: function() {
                alert('신고가 완료되었습니다.');
                $('#movieReviewReportModal').fadeOut();
            },
            error: function() {
                // 서버에서 에러가 발생했을 때 처리
                alert('신고를 처리하는 동안 오류가 발생했습니다. 다시 시도해주세요.');
                $('#movieReviewReportModal').fadeOut();
            }
        });
    });
}



let globalMap = null;
let currentMarker = null;
let currentInfowindow = null;
let theaterMarkers = [];

function initializeKakaoMap() {
    if (typeof kakao === 'undefined') {
        console.error('Kakao Maps API가 로드되지 않았습니다.');
        return;
    }

    // 초기 지도 생성 (서울 중심)
    const mapContainer = $('.id-kakaoMap')[0];
    const mapOption = {
        center: new kakao.maps.LatLng(37.5665, 126.9780),
        level: 5
    };

    globalMap = new kakao.maps.Map(mapContainer, mapOption);

    // 지도 클릭 이벤트 등록
    kakao.maps.event.addListener(globalMap, 'click', function(mouseEvent) {
        const latlng = mouseEvent.latLng;
        updateCurrentLocation(latlng.getLat(), latlng.getLng(), '선택한 위치');
    });

    // 현재 위치 가져오기
    getCurrentLocation();
}

function getCurrentLocation() {
    $('.currentLocation').html('위치 찾는 중... <div class="loader"></div>');

    if (navigator.geolocation) {
        const options = {
            enableHighAccuracy: true,
            timeout: 5000,
            maximumAge: 0
        };

        navigator.geolocation.getCurrentPosition(
            position => {
                const lat = position.coords.latitude;
                const lng = position.coords.longitude;

                // 좌표를 주소로 변환
                convertCoordToAddress(lat, lng, function(result) {
                    if (result) {
                        updateCurrentLocation(lat, lng, result);
                    } else {
                        updateCurrentLocation(lat, lng, '현재 위치');
                    }
                });
            },
            error => {
                console.error('위치 정보 에러:', error);
                $('.currentLocation').text('위치를 찾을 수 없습니다. 지도를 클릭하여 위치를 선택해주세요.');
            },
            options
        );
    } else {
        $('.currentLocation').text('이 브라우저에서는 위치 서비스를 지원하지 않습니다.');
    }
}

function convertCoordToAddress(lat, lng, callback) {
    const geocoder = new kakao.maps.services.Geocoder();

    geocoder.coord2RegionCode(lng, lat, function(result, status) {
        if (status === kakao.maps.services.Status.OK) {
            const addressName = result[0].address_name;
            callback(addressName);
        } else {
            callback(null);
        }
    });
}

function updateCurrentLocation(lat, lng, locationName) {
    // 현재 위치 표시 업데이트
    $('.currentLocation').text('현재 위치: ' + locationName);

    // 기존 현재 위치 마커 제거
    if (currentMarker) {
        currentMarker.setMap(null);
    }

    // 새로운 현재 위치 마커 생성
    const currentPosition = new kakao.maps.LatLng(lat, lng);
    currentMarker = new kakao.maps.Marker({
        position: currentPosition,
        map: globalMap,
        image: new kakao.maps.MarkerImage(
            'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_red.png',
            new kakao.maps.Size(64, 69),
            { offset: new kakao.maps.Point(27, 69) }
        )
    });

    // 현재 위치로 지도 이동
    globalMap.setCenter(currentPosition);

    // 새로운 위치의 주변 CGV 검색
    searchNearbyCGV(lat, lng);
}

function searchNearbyCGV(lat, lng) {
    // 기존 마커들 제거
    theaterMarkers.forEach(marker => {
        if (marker.marker) marker.marker.setMap(null);
        if (marker.infowindow) marker.infowindow.close();
    });
    theaterMarkers = [];

    // 목록 초기화
    $('.id-nearbyTheaters').empty();

    // 로딩 표시
    $('.id-nearbyTheaters').html('<div class="loadingText">주변 영화관 검색 중...</div>');

    const places = new kakao.maps.services.Places();

    const callback = function(result, status) {
        $('.id-nearbyTheaters').empty();

        if (status === kakao.maps.services.Status.OK) {
            if (result.length === 0) {
                $('.id-nearbyTheaters').html('<div class="noTheaters">주변 5km 내에 CGV가 없습니다.</div>');
                return;
            }

            result.forEach(place => {
                // 마커 생성
                const markerPosition = new kakao.maps.LatLng(place.y, place.x);
                const marker = new kakao.maps.Marker({
                    map: globalMap,
                    position: markerPosition
                });

                // 인포윈도우 생성
                const infowindow = new kakao.maps.InfoWindow({
                    content: `
                        <div class="infoWindow">
                            <strong>${place.place_name}</strong><br>
                            ${place.road_address_name || place.address_name}<br>
                            ${(place.distance / 1000).toFixed(1)}km
                        </div>
                    `
                });

                // 마커 클릭 이벤트
                kakao.maps.event.addListener(marker, 'click', function() {
                    if (currentInfowindow) {
                        currentInfowindow.close();
                    }
                    infowindow.open(globalMap, marker);
                    currentInfowindow = infowindow;
                });

                theaterMarkers.push({ marker, infowindow });

                // 목록에 추가
                addTheaterToList(place);
            });
        } else {
            $('.id-nearbyTheaters').html('<div class="errorText">영화관 검색 중 오류가 발생했습니다.</div>');
        }
    };

    // CGV 검색 (반경 5km)
    places.keywordSearch('CGV', callback, {
        location: new kakao.maps.LatLng(lat, lng),
        radius: 5000,
        sort: kakao.maps.services.SortBy.DISTANCE
    });
}

function addTheaterToList(place) {
    const theaterItem = $(`
        <div class="theaterItem">
            <div class="theaterInfo">
                <div class="theaterName">${place.place_name}</div>
                <div class="theaterAddress">${place.road_address_name || place.address_name}</div>
            </div>
            <div class="theaterActions">
                <div class="theaterDistance">${(place.distance / 1000).toFixed(1)}km</div>
                <button class="class-showRouteBtn showRouteBtn" 
                        data-lat="${place.y}" 
                        data-lng="${place.x}"
                        data-name="${place.place_name}">
                    길찾기
                </button>
            </div>
        </div>
    `);

    // 영화관 항목 클릭 시 해당 마커의 인포윈도우 표시
    theaterItem.click(function() {
        const index = $('.theaterItem').index(this);
        if (theaterMarkers[index]) {
            const {marker, infowindow} = theaterMarkers[index];
            if (currentInfowindow) {
                currentInfowindow.close();
            }
            infowindow.open(globalMap, marker);
            currentInfowindow = infowindow;

            // 해당 마커로 지도 중심 이동
            globalMap.panTo(marker.getPosition());
        }
    });

    $('.id-nearbyTheaters').append(theaterItem);
}

function handleSidebarToggle() {
    $(document).on('click', '.id-toggleSidebarBtn', function() {
        $('.theaterMapSidebar').toggleClass('open');

        // 지도 리사이즈
        setTimeout(() => {
            window.dispatchEvent(new Event('resize'));
        }, 300);
    });
}

function handleRouteButton() {
    $(document).on('click', '.class-showRouteBtn', function(e) {
        e.stopPropagation(); // 이벤트 버블링 방지
        const lat = $(this).data('lat');
        const lng = $(this).data('lng');
        const name = $(this).data('name');
        window.open(`https://map.kakao.com/link/to/${name},${lat},${lng}`);
    });
}
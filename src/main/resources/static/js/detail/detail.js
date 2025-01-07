$(document).ready(function (){
    clickActorSearching();
    likeBtn();
    bookmarkBtn();
    gotoReviewPage();
    reportBtn();
    peopleSlide();

//   카카오 맵
    initializeMap();
    initializeEvents();
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

function peopleSlide(){
    const slide = $(".slider")//슬라이드
    const slideItems = $(".slider .actorBox");
    const slideItemWidth = slideItems.eq(0).outerWidth(); //슬라이드 아이템 하나의 너비
    const slideItemCount = slideItems.length //아이템 갯수
    const totalWidth = slideItemWidth * slideItemCount;
    const slideWidth = slideItemWidth * 5; //슬라이드 이동할 너비
    let currentWidth = 0;

    // 이전 버튼 클릭
    $('#slidePrevBtn').click(function() {
        if (currentWidth - slideWidth >= 0) {
            currentWidth -= slideWidth;
            slide.css('transform', `translateX(${-currentWidth}px)`);
        }
    });

    // 다음 버튼 클릭
    $('#slideNextBtn').click(function() {
        if (currentWidth + slideWidth < totalWidth) {
            currentWidth += slideWidth;
            slide.css('transform', `translateX(${-currentWidth}px)`);
        }
    });
}




let globalMap = null;
let currentMarker = null;
let currentInfowindow = null;
let theaterMarkers = [];

function initializeMap() {
    if (typeof kakao === 'undefined') {
        console.error('Kakao Maps API가 로드되지 않았습니다.');
        return;
    }

    const mapContainer = $('.id-kakaoMap')[0];
    const mapOption = {
        center: new kakao.maps.LatLng(37.5665, 126.9780),
        level: 5
    };

    globalMap = new kakao.maps.Map(mapContainer, mapOption);
    getCurrentLocation();
}

function initializeEvents() {
    // 지도 클릭 이벤트
    kakao.maps.event.addListener(globalMap, 'click', function(mouseEvent) {
        const latlng = mouseEvent.latLng;
        updateCurrentLocation(latlng.getLat(), latlng.getLng(), '선택한 위치');
    });

    // 사이드바 토글
    $(document).on('click', '.id-toggleSidebarBtn', function() {
        $('.theaterMapSidebar').toggleClass('open');
        setTimeout(() => window.dispatchEvent(new Event('resize')), 300);
    });

    // 길찾기 버튼
    $(document).on('click', '.showRouteBtn', function(e) {
        e.stopPropagation();
        const theaterData = $(this).data();
        window.open(`https://map.kakao.com/link/to/${theaterData.name},${theaterData.lat},${theaterData.lng}`);
    });

    // 영화관 항목 클릭
    $(document).on('click', '.theater-item', function(e) {
        const theaterId = $(this).data('theaterId');
        const markerInfo = theaterMarkers.find(marker => marker.id === theaterId);

        if (markerInfo) {
            if (currentInfowindow) {
                currentInfowindow.close();
            }

            markerInfo.infowindow.open(globalMap, markerInfo.marker);
            currentInfowindow = markerInfo.infowindow;

            globalMap.panTo(markerInfo.marker.getPosition());
        }
    });
}

function getCurrentLocation() {
    $('.currentLocation').html('위치 찾는 중... <div class="loader"></div>');

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            position => {
                const lat = position.coords.latitude;
                const lng = position.coords.longitude;

                convertCoordToAddress(lat, lng, location => {
                    updateCurrentLocation(lat, lng, location || '현재 위치');
                });
            },
            error => {
                console.error('위치 정보 오류:', error);
                $('.currentLocation').text('위치를 찾을 수 없습니다. 지도를 클릭하여 위치를 선택해주세요.');
            },
            { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 }
        );
    } else {
        $('.currentLocation').text('이 브라우저에서는 위치 서비스를 지원하지 않습니다.');
    }
}

function convertCoordToAddress(lat, lng, callback) {
    const geocoder = new kakao.maps.services.Geocoder();

    geocoder.coord2RegionCode(lng, lat, (result, status) => {
        if (status === kakao.maps.services.Status.OK) {
            callback(result[0].address_name);
        } else {
            callback(null);
        }
    });
}

function updateCurrentLocation(lat, lng, locationName) {
    $('.currentLocation').text('현재 위치: ' + locationName);

    if (currentMarker) {
        currentMarker.setMap(null);
    }

    const position = new kakao.maps.LatLng(lat, lng);
    currentMarker = new kakao.maps.Marker({
        position: position,
        map: globalMap,
        image: new kakao.maps.MarkerImage(
            'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_red.png',
            new kakao.maps.Size(64, 69),
            { offset: new kakao.maps.Point(27, 69) }
        )
    });

    globalMap.setCenter(position);
    searchNearbyCGV(lat, lng);
}

function searchNearbyCGV(lat, lng) {
    clearTheaterMarkers();
    $('.id-nearbyTheaters').html('<div class="loadingText">주변 영화관 검색 중...</div>');

    const places = new kakao.maps.services.Places();

    places.keywordSearch('CGV 영화관',
        (result, status) => {
            $('.id-nearbyTheaters').empty();

            if (status === kakao.maps.services.Status.OK) {
                const theaters = result
                    .filter(place => place.place_name.includes('CGV'))
                    .filter(place => place.distance <= 5000);

                if (theaters.length === 0) {
                    $('.id-nearbyTheaters').html('<div class="noTheaters">주변 5km 내에 CGV가 없습니다.</div>');
                    return;
                }

                theaters.forEach((theater, index) => {
                    addTheaterMarker(theater, index);
                    addTheaterToList(theater, index);
                });
            } else {
                $('.id-nearbyTheaters').html('<div class="errorText">영화관 검색 중 오류가 발생했습니다.</div>');
            }
        },
        {
            location: new kakao.maps.LatLng(lat, lng),
            radius: 10000,
            sort: kakao.maps.services.SortBy.DISTANCE,
            category_group_code: 'CT1' // 영화관 카테고리
        }
    );
}

function clearTheaterMarkers() {
    theaterMarkers.forEach(marker => {
        marker.marker.setMap(null);
        if (marker.infowindow) marker.infowindow.close();
    });
    theaterMarkers = [];
}

function addTheaterMarker(theater, index) {
    const position = new kakao.maps.LatLng(theater.y, theater.x);
    const marker = new kakao.maps.Marker({ position, map: globalMap });

    const infowindow = new kakao.maps.InfoWindow({
        content: `
            <div class="infoWindow">
                <strong>${theater.place_name}</strong><br>
                ${theater.road_address_name || theater.address_name}<br>
                ${(theater.distance / 1000).toFixed(1)}km
            </div>
        `
    });

    kakao.maps.event.addListener(marker, 'click', () => {
        if (currentInfowindow) {
            currentInfowindow.close();
        }
        infowindow.open(globalMap, marker);
        currentInfowindow = infowindow;
    });

    theaterMarkers.push({
        id: `theater-${index}`,
        marker,
        infowindow
    });
}

function addTheaterToList(theater, index) {
    const theaterItem = $(`
        <div class="theater-item" data-theater-id="theater-${index}">
            <div class="theaterInfo">
                <div class="theaterName">${theater.place_name}</div>
                <div class="theaterAddress">${theater.road_address_name || theater.address_name}</div>
            </div>
            <div class="theaterActions">
                <div class="theaterDistance">${(theater.distance / 1000).toFixed(1)}km</div>
                <div class="theaterRouteBtns">
                    <button class="showRouteBtn" 
                        data-lat="${theater.y}" 
                        data-lng="${theater.x}"
                        data-name="${theater.place_name}">
                        길찾기
                    </button>
                    <button class="showTimeBtn" data-name="${theater.place_name}">상영표 보기</button>
                </div>
            </div>
        </div>
    `);

    $('.id-nearbyTheaters').append(theaterItem);
}

function showTimeTable(){
    $(document).on("click", ".showTimeBtn", function () {
        const placeName = $(this).data("name");


    });
}
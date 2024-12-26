$(document).ready(function (){
    A();

    clickActorSearching();
})

function A(){
    // 날짜 선택 기능
    $('.date-item').on('click', function () {
        $('.date-item.active').removeClass('active');
        $(this).addClass('active');
        // 여기에 날짜 변경 시 상영시간표 업데이트 로직 추가
    });

// 지역 선택 시 상세 지역 업데이트
    $('#region').on('change', function () {
        const subRegion = $('#subRegion');
        // 여기에 지역에 따른 상세 지역 업데이트 로직 추가
    });

// 상영시간 선택 시 이벤트
    $('.showtime-item').on('click', function () {
        // 여기에 예매 페이지로 이동하는 로직 추가
        alert('예매 페이지로 이동합니다.');
    });
}

//배우 이름 클릭시 배우 검색 하러 페이지 이동
function clickActorSearching(){
    $(document).on("click", ".actorBox", function (){
        const peopleNm = $(this).data("peoplenm").trim();

        if (peopleNm) {
            // 검색 페이지로 이동하면서 peopleNm을 쿼리 파라미터로 전달
            window.location.href = `/search?peopleNm=${encodeURIComponent(peopleNm)}`;
        }
    });
}
$(document).ready(function (){
    A();

    clickActorSearching();

    likeBtn();

    bookmarkBtn();
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

//좋아요 버튼 동작
function likeBtn(){
    $(document).on("click", ".actionBtn.likeBtn", function (){
        const movieCd = $(this).data("moviecd");

        $.ajax({
            url: "/api/movieDetail/like",
            method: "get",
            data: {movieCd: movieCd},
            success: function (data){
                //좋아요 부분 새로고침
                var data = $.parseHTML(data);
                var dataHtml = $("<div>").append(data);
                $("#likeBtn").replaceWith(dataHtml.find("#likeBtn"));

            },
            error: function () {

            }
        })


    });


}



//찜하기 버튼 동작
function bookmarkBtn() {
    $(document).on("click", ".actionBtn.bookmarkBtn", function (){

    });
}
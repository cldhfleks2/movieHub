$(document).ready(function (){
    A();

    clickActorSearching();

    actionBtns()

    gotoReviewPage();
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

//좋아요 버튼 동작
function actionBtns(){
    //좋아요 버튼
    $(document).on("click", ".actionBtn.likeBtn", function (){
        const movieCd = $(this).data("moviecd");

        $.ajax({
            url: "/api/movieDetail/like",
            method: "post",
            data: {movieCd: movieCd},
            success: function (data){
                //좋아요 부분 새로고침
                var data = $.parseHTML(data);
                var dataHtml = $("<div>").append(data);
                $("#likeBtn").replaceWith(dataHtml.find("#likeBtn"));

                console.log("/api/movieDetail/like ajax success")
            },
            error: function () {
                console.log("/api/movieDetail/like ajax failed")
            }
        })
    });

    //찜하기 버튼
    $(document).on("click", ".actionBtn.bookmarkBtn", function (){
        const movieCd = $(this).data("moviecd");

        $.ajax({
            url: "/api/movieDetail/bookmark",
            method: "post",
            data: {movieCd: movieCd},
            success: function (data){
                //좋아요 부분 새로고침
                var data = $.parseHTML(data);
                var dataHtml = $("<div>").append(data);
                $("#bookmarkBtn").replaceWith(dataHtml.find("#bookmarkBtn"));

                console.log("/api/movieDetail/bookmark ajax success")
            },
            error: function () {
                console.log("/api/movieDetail/bookmark ajax failed")
            }
        })
    });
}

function gotoReviewPage(){
    $(document).on("click", ".linkBtn", function (){
        const movieCd = $(this).data("moviecd")
        window.location.href="/movieReview?isCometoMovieDetailPage=1&movieCd=" + movieCd; //파라미터 담아서 보냄
    })
}





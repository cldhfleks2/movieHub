$(document).ready(function (){
    A();
    clickActorSearching();
    likeBtn();
    bookmarkBtn();
    gotoReviewPage();
    reportBtn();
})

//쓸지안쓸지모르는 기능
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

//페이지 새로고침하는 함수
function movieInfoReload(){
    const movieCd = $(this).data("moviecd")
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



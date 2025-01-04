$(document).ready(function (){
    initialize();
    highlightedReview();
    searchSection();
    clickSearchingReview();
    saveReview()
    deleteReview()
    pagination();
})

function initialize(){
    $(document).on("input", "#reviewContentEdit", function () {
        autoResizeTextarea();
    });

    $(".searchInput").focus(); //페이지 로딩되면 검색바에 포커스
}

//이전 페이지에서 reviewId를 파라미터로 가지고 온 경우
function highlightedReview() {
    const urlParams = new URLSearchParams(window.location.search);
    const highlightReviewId = urlParams.get('highlightReviewId');

    if (highlightReviewId) {
        searchingReviewDetail(highlightReviewId);
    }
}

// 텍스트 영역의 크기를 동적으로 조정하는 함수
function autoResizeTextarea() {
    var $textarea = $('#reviewContentEdit');
    $textarea.height('auto');  // 먼저 높이를 자동으로 리셋
    $textarea.height($textarea[0].scrollHeight - 20);  // 내용에 맞게 높이 조정
}

//검색바 작동
function searchSection() {
    $(document).on("click", ".searchBtn", function () {
        searchingKeyword()
    });

    $(document).on("input", ".searchInput", function () {
        searchingKeyword()
    });
}

//검색어 결과 리뷰 목록을 새로고침
function searchingKeyword(pageIdx = 1) {
    $("#reviewDetailSection").hide(); //리뷰 상세내용뷰 숨김
    
    const keyword = $('.searchInput').val();
    $.ajax({
        url: '/manager/movieReview/search',
        method: 'GET',
        data: { keyword: keyword, pageIdx: pageIdx },
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#reviewTableBody").replaceWith(dataHtml.find("#reviewTableBody"));
            $("#pagination").replaceWith(dataHtml.find("#pagination"));

            console.log("search-review ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("search-review ajax failed")
        }
    });
}

function pagination(){
    $(document).on("click", "#prevPage, #nextPage, .pageNum", function () {
        const pageIdx = $(this).data("pageidx")
        searchingKeyword(pageIdx)
    })
}

//리뷰 수정하기 클릭시, 리뷰 상세 뷰를 보여줌
function clickSearchingReview(){
    $(document).on("click", ".editBtn", function () {
        const reviewId = $(this).data("review-id")
        searchingReviewDetail(reviewId)
    });
}

//reviewId로 리뷰 상세정보를 가져옴
function searchingReviewDetail(reviewId) {
    $("#reviewDetailSection").show(); //리뷰 상세내용뷰 숨김

    $.ajax({
        url: `/api/manager/reviews/${reviewId}`,
        method: 'GET',
        success: function(data) {

            //replaceWith

            // 스크롤 이동
            $('html, body').animate({
                scrollTop: $('#reviewDetailSection').offset().top
            }, 500);
        },
        error: function(xhr) {
            alert('리뷰 정보를 불러오는 중 오류가 발생했습니다.');
        }
    });
}

//리뷰 저장
function saveReview() {
    $(document).on("click", ".saveBtn", function () {
        const reviewId = $(this).data("review-id")

        $.ajax({
            url: `/api/manager/reviews/${reviewId}`,
            method: 'PUT',
            data:{ },
            success: function (response, textStatus, xhr){
                if (xhr.status === 200) {
                    alert("")
                }
                $("#reviewDetailSection").hide(); //리뷰 상세내용뷰 숨김
                console.log(" ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log(" ajax failed")
            }
        });
    });
}

//리뷰 삭제
function deleteReview() {
    $(document).on("click", ".deleteBtn", function () {
        const reviewId = $(this).data("review-id")

        $.ajax({
            url: `/api/manager/reviews/${reviewId}`,
            method: 'DELETE',
            data:{ },
            success: function (response, textStatus, xhr){
                if (xhr.status === 200) {
                    alert("")
                }
                $("#reviewDetailSection").hide(); //리뷰 상세내용뷰 숨김
                console.log(" ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log(" ajax failed")
            }
        });
    });
}

//리뷰 상세 뷰 새로고침 : 쓸지 안쓸지 몰라
function reviewDetailReload() {
    $("#reviewDetailSection").show(); //리뷰 상세내용뷰 숨김

    $.ajax({
        url: "",
        method: "",
        data: {},
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#").replaceWith(dataHtml.find("#"));

            console.log(" ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log(" ajax failed")
        }

    })
}


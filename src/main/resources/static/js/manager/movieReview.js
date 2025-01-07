$(document).ready(function (){
    initialize();
    initialSearching()
    highlightedReview();
    searchSection();
    clickSearchingReview();
    saveReview()
    deleteReview()
    pagination();
})

//페이지 초기 설정
function initialize(){
    $(document).on("input", "#reviewContentEdit", function () {
        autoResizeTextarea();
    });

    $(".searchInput").focus(); //페이지 로딩되면 검색바에 포커스

    $("#reviewDetailSection").hide(); //리뷰 상세내용 뷰 숨김

    searchingKeyword(); //페이지 로딩되면 전체요소 1회 검색

    //페이지가 길어지면 그에따라 사이드바 높이도 설정
    $(document).on("transitionend", ".container", function () {
        const currentContainerHeight = $(".container").outerHeight();
        $(".adminSideBar").css("height", `${currentContainerHeight}px`);
    });
}

//최초 검색 동작 : 신고 관리 페이지에서 자세히 보기 버튼 클릭시
function initialSearching(){
    // URL에서 initialKeyword 파라미터 확인
    const urlParams = new URLSearchParams(window.location.search);
    const movieReviewId = urlParams.get('movieReviewId');

    if (movieReviewId) {
        searchingReviewDetail(movieReviewId)
    }
}

//이전 페이지에서 reviewId를 파라미터로 가지고 온 경우
function highlightedReview() {
    const urlParams = new URLSearchParams(window.location.search);
    const highlightReviewId = urlParams.get('highlightReviewId');

    if (highlightReviewId) {
        searchingReviewDetail(highlightReviewId);
    }
}

//텍스트 영역의 크기를 동적으로 조정하는 함수
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
        url: '/api/manager/movieReview/search',
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

//페이지 버튼 클릭시 동작
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
    $("#reviewDetailSection").show(); //리뷰 상세내용뷰 보여줌

    $.ajax({
        url: "/api/manager/movieReview/detail",
        method: 'get',
        data: {reviewId: reviewId},
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#reviewDetailSection").replaceWith(dataHtml.find("#reviewDetailSection"));

            // 스크롤 이동
            $('html, body').animate({
                scrollTop: $('#reviewDetailSection').offset().top
            }, 500);
            console.log("get-review-detail ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("get-review-detail ajax failed")
        }
    });
}

//리뷰 삭제
function deleteReview() {
    $(document).on("click", ".deleteBtn", function () {
        const reviewId = $(this).data("review-id")

        $.ajax({
            url: "/api/manager/movieReview/delete",
            method: 'delete',
            data:{ reviewId: reviewId },
            success: function (response, textStatus, xhr){
                if (xhr.status === 204) {
                    alert("영화 리뷰가 삭제 되었습니다.")
                }
                $("#reviewDetailSection").hide(); //리뷰 상세내용뷰 숨김
                searchingKeyword(); //검색 결과 뷰 새로고침
                console.log("delete-movieReview ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("delete-movieReview ajax failed")
            }
        });
    });
}

//리뷰 저장
function saveReview() {
    $(document).on("click", ".saveBtn", function () {
        const reviewId = $(this).data("review-id")
        const content = $("#reviewContentEdit").val()

        $.ajax({
            url: '/api/manager/movieReview/edit',
            method: 'patch',
            data:{reviewId: reviewId, content: content },
            success: function (response, textStatus, xhr){
                if (xhr.status === 204) {
                    alert("영화 리뷰가 수정 되었습니다.")
                }
                $("#reviewDetailSection").hide(); //리뷰 상세내용뷰 숨김
                searchingKeyword(); //검색 결과 뷰 새로고침
                console.log("edit-movieReview ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("edit-movieReview ajax failed")
            }
        });
    });
}

$(document).ready(function() {
    initialize();
    tabs();
    postLink();
    postPagination();
    movieReviewPagination();
});

//초기 설정
function initialize() {
    $("#movieReviewSection").hide(); //처음 페이지 로딩되면 게시글 목록부터 보여줌
}

//탭 작동
function tabs() {
    $('.tabButton').on('click', function() {
        const tabId = $(this).data('tab');

        // 탭 버튼 활성화 상태 변경
        $('.tabButton').removeClass('active');
        $(this).addClass('active');

        // 컨텐츠 섹션 표시/숨김
        $('.contentSection').removeClass('active');
        $(".contentSection").hide();
        $(`#${tabId}Section`).addClass('active');
        $(`#${tabId}Section`).show();
    });
}

//게시글 뷰 : 클릭시 페이지 이동
function postLink(){
    $(document).on("click", ".postItem", function () {
        const postId = $(this).data("post-id")
        window.location.href = `/postDetail/` + postId; //완료시 수정한 게시글 상세 페이지로
    });
}

//게시글 뷰 페이지네이션
function postPagination(){
    $(document).on("click", "#postPrevPage, #postNextPage, #postPagination .pageNum", function () {
        const pageIdx = $(this).data("pageidx")
        postPageReload(pageIdx);
    })
}

//게시글뷰를 새로고침하는 함수
function postPageReload(pageIdx = 1){
    const memberId = $("#postSection").data("member-id");
    $.ajax({
        url: "/userprofile/" + memberId,
        method: "get",
        data: {pageIdx: pageIdx},
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#postList").replaceWith(dataHtml.find("#postList"));
            $("#postPagination").replaceWith(dataHtml.find("#postPagination"));
            console.log("post reload ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("post reload ajax failed")
        }
    });
}

//영화 리뷰 뷰 페이지네이션
function movieReviewPagination(){
    $(document).on("click", "#movieReviewPrevPage, #movieReviewNextPage, #movieReviewPagination .pageNum", function () {
        const pageIdx = $(this).data("pageidx")
        postPageReload(pageIdx);
    })
}

//영화 리뷰 뷰를 새로고침하는 함수
function movieReviewPageReload(pageIdx = 1){
    const memberId = $("#movieReviewSection").data("member-id");
    $.ajax({
        url: "/userprofile/" + memberId,
        method: "get",
        data: {pageIdx: pageIdx},
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#movieReviewList").replaceWith(dataHtml.find("#movieReviewList"));
            $("#movieReviewPagination").replaceWith(dataHtml.find("#movieReviewPagination"));
            console.log("movieReview reload ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("movieReview reload ajax failed")
        }
    });
}

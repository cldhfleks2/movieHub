$(document).ready(function() {
    initialize();
    tabs();
    postLink();
    postPagination();
});

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

//게시글 뷰 : 페이지 네이션
function postPagination(){

}


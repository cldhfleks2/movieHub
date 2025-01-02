$(document).ready(function() {
    initialize();
    tabs();
    pagination();
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

//페이지 네이션
function pagination(){

}


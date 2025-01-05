$(document).ready(function() {
    initialize();
    autoResizeTextarea();
    pagination();
    searchingBar();
    editBtn();
});

// 페이지 초기화 시 필요한 설정들
function initialize() {
    searching(); //최소 1회 검색

    hidePostDetail();
}
// 텍스트 영역의 크기를 동적으로 조정하는 함수
function autoResizeTextarea() {
    var $textarea = $('#detailContent');
    $textarea.height('auto');  // 먼저 높이를 자동으로 리셋
    $textarea.height($textarea[0].scrollHeight - 20);  // 내용에 맞게 높이 조정
}
//검색바 동작
function searchingBar() {
    // 이벤트 리스너 설정
    $(document).on("input", "#searchInput", function () {
        searching();
    });
}
//게시글 상세 뷰 보여주기
function showPostDetail() {
    $('#postDetailSection').show();
    // AJAX: 게시글 상세 정보 로드
}
//게시글 상세 뷰 숨김
function hidePostDetail() {
    $('#postDetailSection').hide();
}
//게시글 검색해서 결과를 보여주는 함수
function searching(pageIdx = 1) {
    const keyword = $("#searchInput").val();
    $.ajax({
        url: "/api/manager/post/search",
        method: "get",
        data: {pageIdx: pageIdx, keyword: keyword},
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#searchResultBody").replaceWith(dataHtml.find("#searchResultBody"));

            console.log("search-post ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("search-post ajax failed")
        }

    })
}
//검색결과 에서 수정하기 버튼 눌렀을때 게시글 상세를 보여줌
function editBtn(){
    $(document).on("click", ".editBtn", function () {
        const postId = $(this).data("post-id");

        $.ajax({
            url: "/api/manager/post/detail",
            method: "get",
            data: {postId: postId},
            success: function (data){
                var data = $.parseHTML(data);
                var dataHtml = $("<div>").append(data);
                $("#postDetailSection").replaceWith(dataHtml.find("#postDetailSection"));
                showPostDetail();

                // 스크롤 이동
                $('html, body').animate({
                    scrollTop: $('#postDetailSection').offset().top
                }, 500);

                console.log("searching-postDetail ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("searching-postDetail ajax failed")
            }

        })
    });
}
//게시글 검색결과 페이지네이션
function pagination() {
    // 페이지네이션 UI 업데이트
}
//게시글 상세뷰에서 수정내용 저장
function savePostChanges() {
    // AJAX: 게시글 수정 저장
}
//게시글 상세뷰에서 게시글 삭제
function deletePost() {
    if (confirm('정말로 이 게시글을 삭제하시겠습니까?')) {
        // AJAX: 게시글 삭제
    }
}


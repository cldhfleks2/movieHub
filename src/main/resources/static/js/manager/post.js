$(document).ready(function() {
    initialize();
    pagination();
    searchingBar();
});

function initialize() {
    // 페이지 초기화 시 필요한 설정들
    searching(); //최소 1회 검색
}

function searchingBar() {
    // 이벤트 리스너 설정
    $(document).on("input", "#searchInput", function () {
        searching();
    });
}

function showPostDetail() {
    $('#postDetailSection').show();
    // AJAX: 게시글 상세 정보 로드
}

function hidePostDetail() {
    $('#postDetailSection').hide();
}

function savePostChanges() {
    // AJAX: 게시글 수정 저장
}

function deletePost() {
    if (confirm('정말로 이 게시글을 삭제하시겠습니까?')) {
        // AJAX: 게시글 삭제
    }
}

function searching(pageIdx = 1) {
    // AJAX: 검색 결과 로드
}

function pagination() {
    // 페이지네이션 UI 업데이트
}
$(document).ready(function (){
    initialize();
    highlightedReview();
    searchSection();
    clickSearchingReview();
})

let currentReviewId = null;

function initialize(){
    $(document).on("input", "#reviewContentEdit", function () {
        autoResizeTextarea();
    });
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

//검색어로 리뷰 목록을 가져옴
function searchingKeyword() {
    const keyword = $('.searchInput').val();
    $.ajax({
        url: '/api/manager/reviews/search',
        method: 'GET',
        data: { keyword: keyword },
        success: function(response) {

            //replaceWith
        },
        error: function(xhr) {
            alert('리뷰 검색 중 오류가 발생했습니다.');
        }
    });
}

//리뷰를 클릭하면 아래를 호출하게?
function clickSearchingReview(){
    $(document).on("click", "", function () {
        
    });
}

//reviewId로 리뷰 상세정보를 가져옴
function searchingReviewDetail(reviewId) {
    $.ajax({
        url: `/api/manager/reviews/${reviewId}`,
        method: 'GET',
        success: function(data) {
            currentReviewId = reviewId;

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


function saveReview() {
    if (!currentReviewId) return;

    const updatedContent = $('#reviewContentEdit').val();

    $.ajax({
        url: `/api/manager/reviews/${currentReviewId}`,
        method: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify({
            content: updatedContent
        }),
        success: function() {
            alert('리뷰가 수정되었습니다.');
            loadReviews();
            hideReviewDetail();
        },
        error: function(xhr) {
            alert('리뷰 수정 중 오류가 발생했습니다.');
        }
    });
}

function loadReviews() {
    $.ajax({
        url: '/api/manager/reviews',
        method: 'GET',
        success: function(response) {
            updateReviewTable(response);
        },
        error: function(xhr) {
            alert('리뷰 목록을 불러오는 중 오류가 발생했습니다.');
        }
    });
}

function deleteReview() {
    if (!currentReviewId || !confirm('정말 이 리뷰를 삭제하시겠습니까?')) return;

    $.ajax({
        url: `/api/manager/reviews/${currentReviewId}`,
        method: 'DELETE',
        success: function() {
            alert('리뷰가 삭제되었습니다.');
            loadReviews();
            hideReviewDetail();
        },
        error: function(xhr) {
            alert('리뷰 삭제 중 오류가 발생했습니다.');
        }
    });
}

function hideReviewDetail() {
    $('#reviewDetailSection').hide();
    currentReviewId = null;
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

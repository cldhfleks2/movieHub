$(document).ready(function() {
    initialize();
    tabSetting()
    searching();
    pagination();
    showDetailBtn();
});

function initialize(){
    //가장 처음 영화 신고 내역을 보여줌
    tabContentReload("movie")
}

// 탭 초기화 및 탭 전환 처리
function tabSetting() {
    $(document).on('click', '.tabButton', function() {
        // 모든 탭 버튼에서 active 클래스 제거
        $('.tabButton').removeClass('active');
        // 클릭된 탭 버튼에 active 클래스 추가
        $(this).addClass('active');

        // 클릭된 탭
        const tabType = $(this).data('tab');

        // 모든 탭 컨텐츠 숨기기
        $('.tabContent').removeClass('active');

        // 선택된 탭의 컨텐츠만 보이기
        $(`#${tabType}Content`).addClass('active');

        //전환된 탭에서 최초1회 검색해서 전체 내용을 보여줌
        tabContentReload(`${tabType}`)
    });
}

//각 탭별로 검색바 동작
function searching(){
    $(document).on("input", ".searchInput", function () {
        // 클릭된 탭
        const tabType = $(this).data('tab');
        tabContentReload(tabType); //검색 결과로 페이지 새로고침
    });
}

//각 탭별로 페이지 새로고침
function tabContentReload(tabType, pageIdx = 1) {
    const searchType = $(`#${tabType}SearchType`).val();
    const keyword = $(`#${tabType}SearchInput`).val();

    $.ajax({
        url: `/api/manager/report/${tabType}`,
        method: 'GET',
        data: { pageIdx: pageIdx, searchType: searchType, keyword: keyword },
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $(`#${tabType}Table`).replaceWith(dataHtml.find(`#${tabType}Table`));
            $(`#${tabType}Pagination`).replaceWith(dataHtml.find(`#${tabType}Pagination`));

            console.log("searching ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("searching ajax failed")
        }
    });
}

//페이지네이션 버튼 동작
function pagination(){
    $(document).on("click", ".pageBtn.prev, .pageBtn.next, .pageNum", function () {
        const tabType = $(this).data('tab');
        const pageIdx = $(this).data("pageidx")

        tabContentReload(tabType, pageIdx)
    })
}

//자세히 보기 동작
function showDetailBtn(){
    $(document).on("click", ".detailBtn", function () {
        const tabType = $(this).data('tab');
        if(tabType === "movie"){
            const movieId = $(this).data("movie-id")
            window.location.href = `/manager/movie?movieId=${encodeURIComponent(movieId)}`;
        }else if(tabType === "movieReview"){
            const movieReviewId = $(this).data("review-id")
            window.location.href = `/manager/movieReview?movieReviewId=${encodeURIComponent(movieReviewId)}`;
        }else if(tabType === "post"){
            const postId = $(this).data("post-id")
            window.location.href = `/manager/post?postId=${encodeURIComponent(postId)}`;
        }else if(tabType === "postReview"){
            //개발 안함
        }
    });
}
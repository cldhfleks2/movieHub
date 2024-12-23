$(document).ready(function() {
    searchSection();

    filterSection();

    likeSection();

    paginationSection();
});


function paginationSection(){
    // 페이지네이션
    $('.pageNum').on('click', function() {
        $('.pageNum').removeClass('active');
        $(this).addClass('active');
        loadPage(parseInt($(this).text(), 10));
    });


    // 페이지 로드 함수
    function loadPage(pageNumber) {
        console.log('Loading page:', pageNumber);
        // 여기에 페이지 데이터 로드 로직 추가
    }

    $('.pageBtn:first-child').on('click', function() {
        if (!$(this).prop('disabled')) {
            const $currentPage = $('.pageNum.active');
            const $prevPage = $currentPage.prev('.pageNum');
            if ($prevPage.length) {
                $prevPage.trigger('click');
            }
        }
    });

    $('.pageBtn:last-child').on('click', function() {
        if (!$(this).prop('disabled')) {
            const $currentPage = $('.pageNum.active');
            const $nextPage = $currentPage.next('.pageNum');
            if ($nextPage.length) {
                $nextPage.trigger('click');
            }
        }
    });
}

function likeSection(){
    // 좋아요/북마크 버튼
    $('.btnLike').on('click', function(e) {
        e.stopPropagation();
        $(this).toggleClass('active');
        // 여기에 좋아요 API 호출 로직 추가
    });

    $('.btnBookmark').on('click', function(e) {
        e.stopPropagation();
        $(this).toggleClass('active');
        // 여기에 북마크 API 호출 로직 추가
    });
}

function filterSection(){
    // 필터 태그 토글
    $('.filterTag').on('click', function() {
        $('.filterTag').removeClass('active');
        $(this).addClass('active');
        // 여기에 필터링 로직 추가
    });

    // 정렬 변경
    $('.filterSelect').on('change', function() {
        // 여기에 정렬 로직 추가
    });
}

function searchSection(){
    // 검색 기능
    $('.searchButton').on('click', function() {
        performSearch($('.searchInput').val());
    });

    $('.searchInput').on('keypress', function(e) {
        if (e.key === 'Enter') {
            performSearch($(this).val());
        }
    });

    // 검색 수행 함수
    function performSearch(query) {
        console.log('Searching for:', query);
        // 여기에 실제 검색 API 호출 로직 추가
    }
}




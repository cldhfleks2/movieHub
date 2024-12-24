$(document).ready(function() {
    categorySelectSection();
    searchSection();

    filterSection();

    likeSection();

    // paginationSection();

});

//카테고리 선택에따라 프로필뷰를 보여줄지 아닐지
function categorySelectSection(){
    $('.categorySelect').change(function () {
        const selectedValue = $(this).val();
        if (selectedValue === 'title') {
            $('.profileSection').css('display', 'none');
        } else if (selectedValue === 'actor') {
            $('.profileSection').css('display', 'block');
        }
    });
}

// function paginationSection(){
//     // 페이지네이션
//     $('.pageNum').on('click', function() {
//         $('.pageNum').removeClass('active');
//         $(this).addClass('active');
//         loadPage(parseInt($(this).text(), 10));
//     });
//
//
//     // 페이지 로드 함수
//     function loadPage(pageNumber) {
//         console.log('Loading page:', pageNumber);
//         // 여기에 페이지 데이터 로드 로직 추가
//     }
//
//     $('.pageBtn:first-child').on('click', function() {
//         if (!$(this).prop('disabled')) {
//             const $currentPage = $('.pageNum.active');
//             const $prevPage = $currentPage.prev('.pageNum');
//             if ($prevPage.length) {
//                 $prevPage.trigger('click');
//             }
//         }
//     });
//
//     $('.pageBtn:last-child').on('click', function() {
//         if (!$(this).prop('disabled')) {
//             const $currentPage = $('.pageNum.active');
//             const $nextPage = $currentPage.next('.pageNum');
//             if ($nextPage.length) {
//                 $nextPage.trigger('click');
//             }
//         }
//     });
// }

function likeSection(){
    // 버튼 클릭시 문구를 출력하는 코드
    $(document).on('click', '.btnLike', function(e) {
        e.stopPropagation();
        const $buttons = $(this).closest('.movieOverlay').find('button');
        const isActive = $(this).hasClass('active');
        $(this).toggleClass('active');
        $buttons.hide();
        const $overlay = $(this).closest('.movieOverlay');
        const message = isActive ? '좋아요를 취소하였습니다.' : '좋아요를 눌렀습니다.';
        showNotification($overlay, message);
        setTimeout(() => $buttons.show(), 1300);
    });

    $(document).on('click', '.btnBookmark', function(e) {
        e.stopPropagation();
        const $buttons = $(this).closest('.movieOverlay').find('button');
        const isActive = $(this).hasClass('active');
        $(this).toggleClass('active');
        $buttons.hide();
        const $overlay = $(this).closest('.movieOverlay');
        const message = isActive ? '찜하기를 취소하였습니다.' : '찜한 영화에 추가하였습니다.';
        showNotification($overlay, message);
        setTimeout(() => $buttons.show(), 1300);
    });

    $(document).on('mouseenter', '.movieCard', function() {
        $(this).find('.movieOverlay button').show();
    });

    $(document).on('mouseleave', '.movieCard', function() {
        $(this).find('.movieOverlay button').hide();
    });

    //알림 띄우는
    function showNotification($element, message) {
        const $notification = $('<div>', {
            class: 'notification',
            text: message
        });

        $element.append($notification);

        setTimeout(() => $notification.addClass('show'), 100);
        setTimeout(() => {
            $notification.removeClass('show');
            setTimeout(() => $notification.remove(), 300);
        }, 1000);
    }
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





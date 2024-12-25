$(document).ready(function() {
    categorySelectSection();
    searchSection();

    filterSection();

    likeSection();

    // paginationSection();
});

//배우또는 감독명으로 검색시에만 프로필 결과 뷰를 보여줌
function categorySelectSection() {
    updateProfileSectionVisibility(); //초기상태

    // 카테고리 변경 이벤트
    $('.categorySelect').change(function() {
        updateProfileSectionVisibility();
        // 카테고리 변경시 검색 결과 초기화
        $('.searchInput').val('');
        // input 태그로 포커스 이동
        $('.searchInput').focus();
    });
}

//프로필 뷰 보여지게 설정
function updateProfileSectionVisibility() {
    const selectedValue = $('.categorySelect').val();
    const $profileSection = $('.profileSection');

    if (selectedValue === 'moviePeople') {
        $profileSection.addClass('active');
        $(".movieGrid").hide();
        $(".noResults").hide();
    } else if(selectedValue === 'movieName'){
        $profileSection.removeClass('active');
        $(".movieGrid").show();
    }
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
    function performSearch(keyword) {
        function showLoading() {
            $('#loadingOverlay').addClass('active');
        }
        function hideLoading() {
            $('#loadingOverlay').removeClass('active');
        }

        const category = $(".categorySelect").val();

        if(!keyword.trim()){
            alert("검색어를 입력하세요.")
            return;
        }


        // "검색 중" 표시 보여주기
        showLoading();

        $.ajax({
            url: "/search",
            method: "get",
            data: {keyword: keyword, category: category},
            success: function (data){
                hideLoading(); // 로딩 화면 숨김

                if(category === "movieName"){
                    //결과로 영화 리스트를 갱신
                    var data = $.parseHTML(data);
                    var dataHtml = $("<div>").append(data);
                    $("#movieGrid").replaceWith(dataHtml.find("#movieGrid"));

                    console.log("/search/movie ajax success")
                }else if(category === "moviePeople"){
                    console.log(data);
                    var data = $.parseHTML(data);
                    var dataHtml = $("<div>").append(data);
                    $("#profileSection").replaceWith(dataHtml.find("#profileSection"));
                    updateProfileSectionVisibility();
                    $(".noResults").show();

                    //결과로 프로필 리스트, 영화리스트 갱신
                    console.log("/search/people ajax success")
                }
            },
            error: function (err){
                hideLoading(); // 로딩 화면 숨김
                console.log(err)
                console.log("/search ajax failed")
            }

        })



    }
}





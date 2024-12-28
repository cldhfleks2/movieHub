$(document).ready(function() {
    initialize();
    settingStarRating();
    reviewForm();
    writeReviewButtonAnimation();
    pagination();

    filterSection()
    searchSection()

    reviewLikeBtn();
    reviewReportBtn()
});

let isCometoMovieDetailPage = false;
let movieCd;

//기본 세팅 : url파라미터 검색등
function initialize(){
    const urlParams = new URLSearchParams(window.location.search);
    const value = urlParams.get('isCometoMovieDetailPage');
    movieCd = urlParams.get("movieCd");

    if (value) { //다른 페이지에서 영화를 선택하고 넘어왔을때 값이 전달되는 파라미터
        isCometoMovieDetailPage = true

        $('.reviewSectionExplain').show();
        $('.reviewSection').css('display', 'flex').hide().slideDown(400);
        $("#writeReviewBtn").text('작성 취소');

        $('html, body').animate({
            scrollTop: $('.reviewSection').offset().top - 100
        }, 400);
        $('.reviewSection').focus();
    }
}

//작성하기 버튼, 작성취소 버튼 동작
function writeReviewButtonAnimation() {
    const $reviewSection = $('.reviewSection');
    const $reviewSectionExplain = $('.reviewSectionExplain');
    const $reviewContainer = $('.reviewContainer');
    const initialContainerHeight = $reviewContainer.height();

    const searchMessageHtml = `
        <div class="searchMessage" style="
            width: 100%;
            height: 200px;
            display: flex;
            justify-content: center;
            align-items: center;
            font-size: 1.2rem;
        ">
            <a href="/search" class="searchLink" style="
                color: #fff;
                text-decoration: none;
                position: relative;
                cursor: pointer;
            ">리뷰를 작성하려면 먼저 영화를 검색하세요.</a>
        </div>
    `;

    $('#writeReviewBtn').click(function() {
        const $button = $(this);
        const isVisible = $reviewSection.is(':visible');

        if (!isVisible) {
            if (!isCometoMovieDetailPage) { // 영화 선택안하고 페이지를 넘어왔을때
                $reviewSection.html(searchMessageHtml);
                initializeSearchLink();

                $reviewSection.css('display', 'flex').hide().slideDown(400);
                $button.text('작성 취소');
            } else { // 영화 선택한 상태일때는 리뷰작성이 가능하게
                $reviewSectionExplain.show();
                $reviewSection.css('display', 'flex').hide().slideDown(400);
                $button.text('작성 취소');

                $('html, body').animate({
                    scrollTop: $('.reviewSection').offset().top - 100
                }, 400);
            }
        } else {

            $reviewContainer.css('min-height', initialContainerHeight);

            $reviewContainer.animate({
                height: initialContainerHeight
            }, 300, function() {
                $reviewSection.hide();
                $reviewContainer.css('height', '');
                $reviewContainer.css('min-height', '');
                if (isCometoMovieDetailPage) {
                    resetReviewForm();
                }
            });

            $button.text('리뷰 작성하기');
            $reviewSectionExplain.hide(); //영화 리뷰 작성하기 제목 숨기기
        }
    });

    // 검색 링크 스타일 초기화 함수
    function initializeSearchLink() {
        // CSS를 JS로 추가
        const style = document.createElement('style');
        style.textContent = `
        .searchLink::after {
            content: '';
            position: absolute;
            width: 0;
            height: 1px;
            bottom: -2px;
            left: 0;
            background-color: #ffffff;
            transition: width 0.3s ease;
        }
        
        .searchLink:hover::after {
            width: 100%;
        }
    `;
        document.head.appendChild(style);
    }
}

//리뷰 작성란 : 입력 내용 초기화
function resetReviewForm() {
    $('.addReviewText').val('');
    $('.charCount').text('0/1000'); //별점 초기화

    // 별점 초기화
    $('#movieRating').val('3');
    updateStarDisplay(3);
    $('.ratingValue').text('3.0');
}

//리뷰 작성란 : 별점에 마우스 올리는동작, 클릭해서 별점 주는 동작
function settingStarRating() {
    $('.star').hover(
        function() {
            const rating = $(this).data('rating');
            updateStarDisplay(rating);
        },
        function() {
            const currentRating = $('#movieRating').val();
            updateStarDisplay(currentRating);
        }
    );

    $('.star').click(function() {
        const rating = $(this).data('rating');
        $('#movieRating').val(rating);
        updateStarDisplay(rating);
        $('.ratingValue').text(rating + '.0');
    });
}

//리뷰 작성란 : 별점선택시 에 별 칠하는 동작
function updateStarDisplay(rating) {
    $('.star').each(function(index) {
        $(this).toggleClass('active', index < rating);
        $(this).toggleClass('inactive', index >= rating);
    });
}

//리뷰 작성란 : 폼 설정, 입력한 텍스트 길이를 보여주는 동작
function reviewForm() {
    const maxLength = 1000;

    $('.addReviewText').on('input', function() {
        const currentLength = $(this).val().length;
        $('.charCount').text(`${currentLength}/${maxLength}`);
    });

    $('#reviewForm').submit(function(e) {
        e.preventDefault();
        submitReview();
    });
}

//리뷰 작성란 : 리뷰 등록 버튼 클릭시
function submitReview() {
    const movieReviewDTO = {
        movieCd: $("#movieCd").data("moviecd"),
        ratingValue: parseFloat($("#ratingValue").text()),
        content: $(".addReviewText").val().trim()
    };

    function validateReview() {
        if (movieReviewDTO.ratingValue === 0) {
            alert('별점을 선택해주세요.');
            return false;
        }
        if (!movieReviewDTO.content) {
            alert('리뷰 내용을 입력해주세요.');
            return false;
        }
        if (movieReviewDTO.content.length < 10) {
            alert('리뷰는 최소 10자 이상 작성해주세요.');
            return false;
        }
        if(!movieReviewDTO.movieCd){
            alert('알 수 없는 오류! 영화를 선택하지 않았나요?');
            return false;
        }
        return true;
    }
    if (!validateReview()) return; //검증 안되면 작성 안됌

    
    //서버로전송 ajax : 성공시 페이지 새로고침
    $.ajax({
        url: "/api/movieReview/add",
        method: "post",
        contentType: "application/json",
        data: JSON.stringify(movieReviewDTO),
        success: function (response, textStatus, xhr){
            if (xhr.status === 200) {
                alert("영화 리뷰를 작성했습니다.")
                console.log(response); // 콘솔 출력
            }else {
                alert("서버 응답이 이상합니다.")
                console.log(response); // 콘솔 출력
            }
            console.log("/api/movieReview/add ajax success")

            window.location.href = "/movieReview"; // 페이지 리로드
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("/api/movieReview/add ajax failed")
        }
    })
}

//페이지네이션 동작
function pagination(){
    $(document).on("click", "#prevPage, #nextPage, .pageNum", function () {
        const pageIdx = $(this).data("pageidx")
        const dateSort = $(".dropdownContent.latest .filterBtn.active").text() === "최신순" ? "recent" : "old";
        const ratingSort = $(".dropdownContent.rating .filterBtn.active").text() === "별점높은순" ? "high" : "low";
        $.ajax({
            url: "/movieReview",
            method: "get",
            data: {pageIdx: pageIdx, dateSort: dateSort, ratingSort: ratingSort},
            success: function (data){
                var data = $.parseHTML(data);
                var dataHtml = $("<div>").append(data);
                $("#reviewListSection").replaceWith(dataHtml.find("#reviewListSection"));

                console.log("/movieReview pagination ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("/movieReview pagination ajax failed")
            }
        });
    })
}

//정렬 기준 동작
function filterSection() {
    // 드롭다운 토글 : 정렬 기준을 선택가능
    $(document).on("click", ".dropdownBtn", function (e){
        e.stopPropagation();
        const $btn = $(this);
        const $dropdown = $btn.next('.dropdownContent');

        // 다른 열린 드롭다운 닫기
        $('.dropdownContent').not($dropdown).fadeOut(200);
        $('.dropdownBtn').not($btn).removeClass('active');

        // 현재 드롭다운 토글
        $dropdown.fadeToggle(200);
        $btn.toggleClass('active');

        // 드롭다운 버튼 텍스트 업데이트
        let $filterGroup = $btn.parent()
        const $activeFilter = $filterGroup.find('.filterBtn.active');
        if ($activeFilter.length) {
            const newText = $activeFilter.text();
            $filterGroup.find('.dropdownBtn').html(newText + ' <span class="arrow">▼</span>');
        }
    })

    // 정렬 기준 선택시
    $(document).on("click", ".filterBtn", function (e){
        e.stopPropagation();
        const $btn = $(this);

        // 해당 그룹의 다른 버튼들 비활성화
        $btn.closest('.dropdownContent').find('.filterBtn').removeClass('active');
        $btn.addClass('active');

        // 드롭다운 닫기
        $btn.closest('.dropdownContent').fadeOut(200);
        $btn.closest('.filterGroup').find('.dropdownBtn').removeClass('active');

        // 드롭다운 버튼 텍스트 업데이트
        let $filterGroup = $btn.closest('.filterGroup');
        const $activeFilter = $filterGroup.find('.filterBtn.active');
        if ($activeFilter.length) {
            const newText = $activeFilter.text();
            $filterGroup.find('.dropdownBtn').html(newText + ' <span class="arrow">▼</span>');
        }

        //정렬된 요소로 다시 페이지를 가져옴
        const pageIdx = $(this).data("pageidx")
        const dateSort = $(".dropdownContent.latest .filterBtn.active").text() === "최신순" ? "recent" : "old";
        const ratingSort = $(".dropdownContent.rating .filterBtn.active").text() === "별점높은순" ? "high" : "low";
        $.ajax({
            url: "/movieReview",
            method: "get",
            data: {pageIdx: pageIdx, dateSort: dateSort, ratingSort: ratingSort, movieCd: movieCd},
            success: function (data){
                var data = $.parseHTML(data);
                var dataHtml = $("<div>").append(data);
                $("#reviewListSection").replaceWith(dataHtml.find("#reviewListSection"));
                console.log("/movieReview select-filter ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("/movieReview select-filter ajax failed")
            }
        });
    })
    
    // 페이지의 다른곳 클릭시 드랍다운 메뉴 닫기
    $(document).click(function() {
        $('.dropdownContent').fadeOut(200);
        $('.dropdownBtn').removeClass('active');
    });
}

//서버로 검색을 요청해서 영화 리뷰 게시판 부분 새로고침
function searchingAndReplaceList(){
    const pageIdx = $(".pageNum.active").data("pageidx"); //페이지 네이션의 활성화된 버튼으로부터 페이지 번호를 가져옴
    const dateSort = $(".dropdownContent.latest .filterBtn.active").text() === "최신순" ? "recent" : "old";
    const ratingSort = $(".dropdownContent.rating .filterBtn.active").text() === "별점높은순" ? "high" : "low";
    const searchText = $(".searchInput").val().trim();

    // 검색 결과로 영화리뷰 게시판을 새로고침
    $.ajax({
        url: "/movieReview",
        method: "get",
        data: {
            pageIdx: pageIdx,
            dateSort: dateSort,
            ratingSort: ratingSort,
            searchText: searchText,
            movieCd: movieCd
        },
        success: function(data) {
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#reviewListSection").replaceWith(dataHtml.find("#reviewListSection"));
            console.log("/movieReview search ajax success");
        },
        error: function(xhr) {
            console.log(xhr.responseText);
            console.log("/movieReview search ajax failed");
        }
    });
}

//검색 뷰 동작
function searchSection() {
    let isSearchBarOpen = false;

    // 검색 아이콘에 호버했을 때
    $(document).on('mouseenter', '.searchIcon', function() {
        if (!isSearchBarOpen) {
            isSearchBarOpen = true;
            $('.searchBar').addClass('active');
            setTimeout(() => {
                $('.searchInput').focus();
            }, 300);
        }
    });

    
    // 엔터키 입력시 검색 실행
    $(document).on('keypress', '.searchInput', function(e) {
        if (e.which === 13) {
            if (isSearchBarOpen) {
                searchingAndReplaceList();
            }
        }
    });

    // 또는 검색바가 열리고난뒤 돋보기 클릭시 검색 실행
    $(document).on('click', '.searchInput', function() {
        if (isSearchBarOpen) {
            searchingAndReplaceList();
        }
    });

    // 검색바 외부 클릭시 닫기
    $(document).click(function(e) {
        if (!$(e.target).closest('.searchGroup').length) {
            if (isSearchBarOpen && $('.searchInput').val() === '') {
                isSearchBarOpen = false;
                $('.searchBar').removeClass('active');
            }
        }
    });
}


//리뷰 리스트 : 좋아요 버튼 클릭시
//좋아요를 이미 했는지도 표시 해줘야할듯
function reviewLikeBtn() {
    $(document).on('click', '.likeBtn', function() {
        const reviewId = $(this).data('reviewid');

        //리뷰 좋아요를 요청
        $.ajax({
            url: "/api/movieReview/like",
            method: "post",
            data: {reviewId: reviewId},
            success: function (response, textStatus, xhr){
                //리뷰 좋아요 성공시 리뷰 목록을 새로고침(총 좋아요수를 최신으로 반영하기 위함)
                if (xhr.status === 200) {
                    alert("리뷰 좋아요를 눌렀습니다.")
                    //두번째로 페이지 비동기 리로딩
                    searchingAndReplaceList()

                    console.log(response); // 콘솔 출력
                }else {
                    alert("이상하게 좋아요 성공. 페이지는 새로고침 X")
                    console.log(response); // 콘솔 출력
                }
                console.log("/api/movieReview/like ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("/api/movieReview/like ajax failed")
            }
        })
    });


}



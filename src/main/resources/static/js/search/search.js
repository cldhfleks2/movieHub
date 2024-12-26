$(document).ready(function() {
    updateProfileSectionVisibility(); //처음 보여지는 뷰 설정
    categorySelectSection();

    searchSection();

    filterSection();

    likeSection();

    gotoPostDetail();
});

//검색바 카테고리 선택시
function categorySelectSection() {
    // 카테고리 변경 이벤트
    $('.categorySelect').change(function() {
        $(".profileImageWrapper").removeClass("active"); //클릭한 이미지에서 active 지움

        updateProfileSectionVisibility();
        $('.searchInput').val('');// 카테고리 변경시 검색 결과 초기화
        $('.searchInput').focus();// input 태그로 포커스 이동
    });
}

//인물검색일때와 아닐때의 뷰 컨트롤하는 함수
function updateProfileSectionVisibility() {
    const selectedValue = $('.categorySelect').val();
    const $profileSection = $('.profileSection');

    //인물 검색
    if (selectedValue === 'moviePeople') {
        //기존 결과 제거
        $(".movieCard").empty();
        $profileSection.addClass('active');
        $('.sortSelect').hide(); //인물 검색은 정렬 기능이 없음
        $(".movieGrid").hide(); //영화 목록 숨김
        //카테고리를 바꿀때 검색결과 없다는 메시지를 숨김
        $(".noResults").hide();
        $(".noResultsMovie").hide();
    //영화이름 검색
    } else if(selectedValue === 'movieName'){
        //기존 결과 제거
        $(".movieCard").empty(); //영화 목록 리셋(지움)
        $profileSection.removeClass('active');
        $('.sortSelect').show(); //정렬 기능 보여줌
        $(".movieGrid").show(); //영화 목록 보여줌

        $(".noResults").hide();
        $(".noResultsMovie").hide();
    }
}

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
    // 필터 태그 토글 : 안씀
    // $('.filterTag').on('click', function() {
    //     $('.filterTag').removeClass('active');
    //     $(this).addClass('active');
    //     // 여기에 필터링 로직 추가
    // });
    
    //DOM요소를 정렬하는 함수
    function sortMovieCards(movieCards, keyword, sortBy) {
        movieCards.sort(function(a, b) {
            // 관련도순 (relevance)
            if (sortBy === "relevance") {
                let movieNmA = $(a).data('movienm').toLowerCase();
                let movieNmB = $(b).data('movienm').toLowerCase();
                let movie1Matches = movieNmA.includes(keyword.toLowerCase());
                let movie2Matches = movieNmB.includes(keyword.toLowerCase());
                return movie2Matches - movie1Matches; // 매칭된 영화가 앞에 오도록
            }

            // 개봉일순 (date)
            else if (sortBy === "date") {
                let openDtA = $(a).data('opendt');
                let openDtB = $(b).data('opendt');
                return new Date(openDtB) - new Date(openDtA); // 최신 개봉일이 먼저 오도록
            }

            // 평점순 (rating)
            else if (sortBy === "rating") {
                let ratingA = $(a).data('rating');
                let ratingB = $(b).data('rating');

                if (ratingA === "not-found" && ratingB !== "not-found") {
                    return 1; // "not-found"인 영화는 마지막으로 배치
                } else if (ratingA !== "not-found" && ratingB === "not-found") {
                    return -1; // "not-found"인 영화는 마지막으로 배치
                } else if (ratingA === "not-found" && ratingB === "not-found") {
                    return 0; // 둘 다 "not-found"일 경우 순서 유지
                } else {
                    return parseFloat(ratingB) - parseFloat(ratingA); // 평점 내림차순
                }
            }
            return 0; // 기본값: 변경 없이 그대로
        });

        return movieCards;
    }


    //정렬기준을 선택하면
    $('.sortSelect').on('change', function() {
        // 영화 카드들 가져오기
        const movieCards = $('.movieCard');
        const keyword = $('.searchInput').val();
        const sortBy = $(".sortSelect").val();// 정렬 기준 : 'relevance', 'date', 'rating'
        const category = $(".categorySelect").val();

        if(category === "movieName"){
            // 영화 카드 정렬
            const sortedMovies = sortMovieCards(movieCards, keyword, sortBy);

            // 정렬된 결과로 업데이트
            $('#movieGrid').empty(); // 기존 내용 제거
            sortedMovies.each(function() {
                $('#movieGrid').append(this); // 정렬된 영화 카드를 추가
            });
        }else if(category === "moviePeople"){
            //인물검색은 정렬이 없음.
        }

    });


}

function showLoading() {
    $('#loadingOverlay').addClass('active');
}

function hideLoading() {
    $('#loadingOverlay').removeClass('active');
}

//검색 요청 보내는 함수
function searchSection(){
    //검색 버튼 클릭
    $('.searchButton').on('click', function() {
        $(".profileImageWrapper").removeClass("active"); //클릭한 이미지에서 active 지움
        searchMovieListOrPeopleProfile($('.searchInput').val());
    });
    //검색바에서 엔터 입력
    $('.searchInput').on('keypress', function(e) {
        $(".profileImageWrapper").removeClass("active"); //클릭한 이미지에서 active 지움
        if (e.key === 'Enter') {
            searchMovieListOrPeopleProfile($(this).val());
        }
    });

    //인물 프로필 클릭시 영화 목록을 가져와야함
    $(document).on("click", ".profileImageWrapper", function (){
        $(".profileImageWrapper").removeClass("active"); //클릭한 이미지에서 active 지움
        $(this).addClass("active");
        const peopleId =  $(this).data("people-id");
        const category = "peopleClick";
        searchPeopleMovieList(peopleId, category);
    })
}

// 영화이름과 인물 프로필을 검색하는 함수
function searchMovieListOrPeopleProfile(keyword) {
    const category = $(".categorySelect").val();
    const sortBy = $(".sortSelect").val();

    if(!keyword.trim()){
        alert("검색어를 입력하세요.")
        return;
    }

    // "검색 중" 표시 보여주기
    showLoading();

    $.ajax({
        url: "/search",
        method: "get",
        data: {keyword: keyword, category: category, sortBy: sortBy},
        success: function (data){
            hideLoading(); // 로딩 화면 숨김

            if(category === "movieName"){
                //결과로 영화 리스트를 갱신
                var data = $.parseHTML(data);
                var dataHtml = $("<div>").append(data);
                $("#movieGrid").replaceWith(dataHtml.find("#movieGrid"));
                $(".noResultsMovie").show(); //만약 검색결과가 없다면 메시지를 출력

                console.log("/search/movie ajax success")
            }else if(category === "moviePeople"){
                var data = $.parseHTML(data);
                var dataHtml = $("<div>").append(data);
                $("#profileSection").replaceWith(dataHtml.find("#profileSection"));
                updateProfileSectionVisibility();
                $(".noResults").show(); //만약 검색결과가 없다면 메시지를 출력

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

//인물 프로필 클릭했을때 참여한 영화 목록을 가져옴
function searchPeopleMovieList(peopleId, category){
    showLoading();
    $(".explain").show();

    $.ajax({
        url: "/search",
        method: "get",
        data: {keyword: peopleId, category: category},
        success: function (data){
            hideLoading(); // 로딩 화면 숨김
            $(".explain").hide();

            //결과로 영화 리스트를 갱신
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#movieGrid").replaceWith(dataHtml.find("#movieGrid"));

            //기본의 영화 결과 뷰를 재사용
            // $(".noResults").hide();
            $(".noResultsMovie").show();
            $(".movieGrid").show();

            console.log("/search/people/movie ajax success")
        },
        error: function (err){
            hideLoading(); // 로딩 화면 숨김
            $(".explain").hide();
            console.log(err)
            console.log("/search/people/movie ajax failed")
        }

    })
}

//영화 상세 페이지로 이동 하는 함수
function gotoPostDetail(){
    $(document).on("click", ".btnLink", function (){
        const movieNm = $(this).data("movienm")
        var openDt = $(this).data("opendt")
        console.log("movieNm: " + movieNm);
        console.log("openDt: " + openDt);
        if(!openDt || !movieNm){ //날짜를 못 읽으면 검색이 불가
            alert("영화 정보를 불러올 수 없습니다.")
            return;
        }
        openDt = String(openDt) //문자열 변환후
        openDt = openDt.substring(0, 4); //yyyy 추출

        showLoading(); //로딩 표시 표현

        $.ajax({
            url: "/validate/movieNm",
            data: {movieNm: movieNm, openDt: openDt},
            type: 'GET',
            success: function (movieCd) {
                hideLoading();
                // 정상 응답일때 movieCd값이 전달됨
                window.location.href = "/detail/" + movieCd; //영화 상세 페이지로 이동
            },
            error: function (xhr) {
                hideLoading();
                console.warn(xhr);
                if (xhr.status === 404) {
                    // 영화 정보가 없는 경우 (status 404)
                    console.error("/validate/movieNm ajax failed")
                    alert("영화 정보를 찾을 수 없습니다.");
                } else {
                    // 기타 오류 처리
                    console.error("/validate/movieNm ajax failed")
                    alert("영화 정보를 불러올 수 없습니다.");
                }
            }
        });
    })
}


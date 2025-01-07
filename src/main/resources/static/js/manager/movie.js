$(document).ready(function (){
    initialize();
    movieSearchSection();
    moviePosterSection();
    movieItemSection();
    movieSaveBtn()
    movieEditBtn();
    pagination();
})

//페이지 초기 설정
function initialize(){
    // 동적으로 생성된 토글 버튼에 대한 클릭 이벤트 위임
    $(document).on('click', '.toggleBtn', function() {
        const listContainer = $(this).prev('.listContainer');
        const isExpanded = listContainer.hasClass('expanded');

        if (isExpanded) {
            collapseList(listContainer, $(this));
        } else {
            expandList(listContainer, $(this));
        }
    });

    searchingKeyword(); //전체 영화 목록을 보여줌
}

//검색한 영화의 상세 정보 : 영화 상세 정보 뷰로 스크롤바
function movieEditBtn(){
    // 수정하기 버튼 클릭 이벤트
    $(document).on("click", ".editBtn", function (e) {
        e.stopPropagation();  // 카드 클릭 이벤트 전파 방지

        //영화 상세 정보 조회
        const movieId = $(this).data("movie-id")
        searchMovieDetail(movieId);
    });
}

//영화 상세 정보 뷰를 ajax로 가져오는 코드
function searchMovieDetail(movieId){
    $.ajax({
        url: "/api/manager/movie/get",
        method: "get",
        data: {movieId: movieId},
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#movieContent").replaceWith(dataHtml.find("#movieContent"));

            $('html, body').animate({
                scrollTop: $('#movieContent').offset().top
            }, 500);  // 500ms 동안 부드럽게 스크롤 이동

            findToggleBtn(); //더보기 toggle버튼을 붙임

            console.log("get-movieDTO ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("get-movieDTO ajax failed")
        }

    })
}

//검색바 동작
function movieSearchSection() {
    $(document).on('input', '#movieContainer .searchInput', function () {
        searchingKeyword()
    });
}

//실제로 영화 검색하는 함수
function searchingKeyword(pageIdx = 1){
    const keyword = $("#movieContainer .searchInput").val();

    $.ajax({
        url: "/api/manager/movie/search",
        method: "get",
        data: {keyword: keyword, pageIdx: pageIdx},
        success: function (data){
            var data = $.parseHTML(data);
            var dataHtml = $("<div>").append(data);
            $("#searchResultSection").replaceWith(dataHtml.find("#searchResultSection"));

            console.log("search-movie ajax success")
        },
        error: function (xhr){
            console.log(xhr.responseText);
            console.log("search-movie ajax failed")
        }

    })
}

//페이지 버튼 동작
function pagination(){
    $(document).on("click", "#prevPage, #nextPage, .pageNum", function () {
        const pageIdx = $(this).data("pageidx")
        searchingKeyword(pageIdx)
    })
}

//가장 최근에 적용한 영화 포스터 이미지 타입
let imageType="url"
//영화 포스터 선택 창
function moviePosterSection() {
    //이미지 업로드 버튼
    $(document).on('change', '.fileInput', function (e) {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function (e) {
                $('.moviePoster').attr('src', e.target.result);
            };
            reader.readAsDataURL(file);
            $('.urlInput').val("") //이미지를 업로드한다면, URL입력창을 지움
            imageType = "file"
        }
    });

    //포스터 적용 버튼
    $(document).on('click', '.updatePosterBtn', function () {
        const urlInput = $('.urlInput').val();
        if (urlInput) {
            $('.moviePoster').attr('src', urlInput);
            imageType = "url"
        }
    });
}

//각 요소 추가/삭제버튼 동작
function movieItemSection() {
    $(document).on('click', '.addBtn', function () {
        const type = $(this).data('type');
        switch (type) {
            case 'genre':
                $('#genreList').append($('<div>')
                    .addClass('tag')
                    .html(`
                    <input class="genreNm" type="text" placeholder="장르">
                    <button class="removeBtn">&times;</button>
        `       ));
                break;
            case 'audit':
                $('#auditList').append($('<div>')
                    .addClass('tag')
                    .html(`
                    <input class="auditNm" type="text" placeholder="관람등급">
                    <button class="removeBtn">&times;</button>
        `       ));
                break;
            case 'director':
                $('#directorList').prepend($('<div>')
                    .addClass('listItem')
                    .html(`
                    <input type="text" class="directorNm" placeholder="감독 이름">
                    <input type="text" class="directorNmEn" placeholder="감독 영문 이름">
                    <button class="removeBtn">&times;</button>
        `       ));
                break;
            case 'actor':
                $('#actorList').prepend($('<div>')
                    .addClass('listItem')
                    .html(`
                    <input type="text" class="actorNm" placeholder="배우 이름">
                    <input type="text" class="actorNmEn" placeholder="배우 영문 이름">
                    <button class="removeBtn">&times;</button>
        `       ));
                break;
        }
    });
    //삭제 동작
    $(document).on('click', '.removeBtn', function () {
        $(this).closest('.tag, .listItem').remove();
    });
}

//영화 저장 버튼 동작
function movieSaveBtn() {
    $(document).on('click', '.saveBtn', function () {
        const movieDTO = {
            movieId: $('.movieId').val(),
            movieCd: $('.movieCd').val(),
            movieNm: $('.movieNm').val(),
            movieNmEn: $('.movieNmEn').val(),
            openDt: $('.openDt').val(),
            showTm: $('.showTm').val(),
            salesAcc: $('.salesAcc').val(),
            prdtYear: $('.prdtYear').val(),
            typeNm: $('.typeNm').val(),
            audiAcc: $('.audiAcc').val(),
            audiCnt: $('.audiCnt').val(),
            posterURL: imageType === "url" ? $('.moviePoster').attr('src') : null,
            genreList: $('#genreList .tag').map(function () {
                return {
                    genreNm: $(this).find('.genreNm').val(),
                    id: $(this).find('.genreId').val(),
                };
            }).get(),
            auditList: $('#auditList .tag').map(function () {
                return {
                    watchGradeNm: $(this).find('.auditNm').val(),
                    id: $(this).find('.auditId').val(),
                };
            }).get(),
            directorList: $('#directorList .listItem').map(function () {
                return {
                    peopleNm: $(this).find('.directorNm').val(),
                    peopleNmEn: $(this).find('.directorNmEn').val(),
                    id: $(this).find('.directorId').val(),
                };
            }).get(),
            actorList: $('#actorList .listItem').map(function () {
                return {
                    peopleNm: $(this).find('.actorNm').val(),
                    peopleNmEn: $(this).find('.actorNmEn').val(),
                    id: $(this).find('.actorId').val(),
                };
            }).get(),
        };

        // 영화 정보 수정 요청
        $.ajax({
            url: '/api/manager/movie/edit',
            type: 'patch',
            contentType: 'application/json',
            data: JSON.stringify(movieDTO),
            success: function (response, textStatus, xhr){
                if (xhr.status === 204) {
                    alert('영화 정보가 성공적으로 수정되었습니다.');
                }else{
                    alert("알 수 없는 성공")
                }
                $('.movieContent').hide();
                console.log("movie-edit ajax success")
            },
            error: function (xhr){
                alert('영화 정보 수정에 실패했습니다.');
                console.log(xhr.responseText);
                console.log("movie-edit ajax failed")
            }
        });

        // 포스터 이미지 파일이 있을 경우 추가 ajax요청 진행
        if (imageType === "file") {
            const formData = new FormData(); //이미지를 보내기위한 폼데이터
            const posterImgFile = $('.fileInput')[0].files[0];
            if (posterImgFile) {
                formData.append("posterImg", posterImgFile);
            }
            formData.append("movieId", $('.movieId').val());

            // 영화 이미지 수정 요청
            $.ajax({
                url: '/api/manager/movie/edit/img',
                type: 'patch',
                processData: false, // FormData 처리 설정
                contentType: false, // FormData 처리 설정
                data: formData,
                success: function (response, textStatus, xhr){
                    if (xhr.status === 204) {
                    }else{
                        alert("영화 포스터 수정에 알 수 없는 성공")
                    }
                    $('.movieContent').hide();
                    console.log("movie-image-edit ajax success")
                },
                error: function (xhr){
                    alert('영화 포스터 수정에 실패했습니다.');
                    console.log(xhr.responseText);
                    console.log("movie-image-edit ajax failed")
                }
            });
        }

    });

    $(document).on('click', '.cancelBtn', function () {
        if (confirm('변경사항이 저장되지 않습니다. 취소하시겠습니까?')) {
            $('.movieContent').hide();
        }
    });
}

//동적으로 생긴 요소에 토글 버튼 설정
function findToggleBtn(){
    //1. 감독란 더보기 버튼 활성화 여부
    const toggleBtnDirector = $(".toggleBtn.director");
    const listContainerDirector = toggleBtnDirector.prev('.listContainer.director');
    const itemCountDirector = listContainerDirector.find('.listItem').length;
    // 아이템 수가 2개 이하일 경우 토글 버튼 숨기기, 그 이상은 보이기
    if (itemCountDirector <= 2) {
        toggleBtnDirector.hide();
    } else {
        toggleBtnDirector.show();
    }

    //2. 배우란 더보기 버튼 활성화 여부
    const toggleBtnActor = $(".toggleBtn.actor");
    const listContainerActor = toggleBtnActor.prev('.listContainer.actor');
    const itemCountActor = listContainerActor.find('.listItem').length;
    // 아이템 수가 2개 이하일 경우 토글 버튼 숨기기, 그 이상은 보이기
    if (itemCountActor <= 2) {
        toggleBtnActor.hide();
    } else {
        toggleBtnActor.show();
    }
}

//펼치기 동작 : 펼치기버튼을 따라 스크롤바
function expandList(listContainer, toggleBtn) {
    listContainer.addClass('expanded');
    listContainer.find('.listItem.hidden').addClass('visible').removeClass('hidden');
    toggleBtn.text('접기');

    $('html, body').animate({
        scrollTop: toggleBtn.offset().top
    }, 500);  // 500ms 동안 부드럽게 스크롤 이동
}

//접기 동작
function collapseList(listContainer, toggleBtn) {
    listContainer.removeClass('expanded');
    listContainer.find('.listItem').each(function(index) {
        if (index >= 2) {
            $(this).addClass('hidden').removeClass('visible');
        }
    });
    toggleBtn.text('더보기');
}
$(document).ready(function (){
    clickMenuBar();
    displayContainer();
    movieSearchSection();
    moviePosterSection();
    movieItemSection();
    movieActionSection();
    movieEditBtn();
    initializeCollapsible();
})

function clickMenuBar(){
    $(document).on("click", ".menuItem", function () {
        $(".menuItem").removeClass("active"); //모든 active제거
        $(this).addClass("active"); //현재 활성화한것에만 active추가
    });
}

function displayContainer(){
    $(document).on("click", "#movieMenu", function (){
        $(".container").hide()
        $("#movieContainer").show()
    })
    $(document).on("click", "#reviewMenu", function (){
        $(".container").hide()
        $("#postContainer").show()
    })
}

//검색한 영화의 상세 정보
function movieEditBtn(){
    // 수정하기 버튼 클릭 이벤트
    $(document).on("click", ".editBtn", function (e) {
        e.stopPropagation();  // 카드 클릭 이벤트 전파 방지

        // AJAX: 영화 상세 정보 조회
        const movieId = $(this).data("movie-id")
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

                console.log("get-movieDTO ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("get-movieDTO ajax failed")
            }

        })
    });
}

//영화 검색 
function movieSearchSection() {
    $(document).on('input', '#movieContainer .searchInput', function () {
        const keyword = $(this).val();

        $.ajax({
            url: "/api/manager/movie/search",
            method: "get",
            data: {keyword: keyword},
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
    });
}

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

    $(document).on('click', '.removeBtn', function () {
        $(this).closest('.tag, .listItem').remove();
    });
}

function movieActionSection() {
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

function initializeCollapsible() {
    // Initializing the toggle buttons for pre-existing elements
    updateToggleButtons();

    // Delegate the click event for dynamically created toggle buttons
    $(document).on('click', '.toggleBtn', function() {
        const listContainer = $(this).prev('.listContainer');
        const isExpanded = listContainer.hasClass('expanded');

        if (isExpanded) {
            collapseList(listContainer, $(this));
        } else {
            expandList(listContainer, $(this));
        }
    });
}

function updateToggleButtons() {
    $('.toggleBtn').each(function() {
        updateToggleButton($(this));
    });
}

function expandList(listContainer, toggleBtn) {
    listContainer.addClass('expanded');
    listContainer.find('.listItem.hidden').addClass('visible').removeClass('hidden');
    toggleBtn.text('접기');
}

function collapseList(listContainer, toggleBtn) {
    listContainer.removeClass('expanded');
    listContainer.find('.listItem').each(function(index) {
        if (index >= 2) {
            $(this).addClass('hidden').removeClass('visible');
        }
    });
    toggleBtn.text('더보기');
}

function updateToggleButton(toggleBtn) {
    const listContainer = toggleBtn.prev('.listContainer');
    const itemCount = listContainer.find('.listItem').length;

    if (itemCount <= 2) {
        toggleBtn.hide();
    } else {
        toggleBtn.show();
    }
}
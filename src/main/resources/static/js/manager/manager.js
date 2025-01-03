$(document).ready(function (){
    clickMenuBar();
    displayContainer();
    movieSearchSection();
    moviePosterSection();
    movieItemSection();
    movieActionSection();
    movieEditBtn();
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

function movieEditBtn(){
    // 수정하기 버튼 클릭 이벤트
    $('.editBtn').on('click', function(e) {
        e.stopPropagation();  // 카드 클릭 이벤트 전파 방지
        $('#movieContainer').addClass('editing');

        // AJAX: 영화 상세 정보 조회
        const movieId = $(this).data("movie-id")
        $.ajax({
            url: "/api/manager/get/movie",
            method: "get",
            data: {movieId: movieId},
            success: function (data){
                var data = $.parseHTML(data);
                var dataHtml = $("<div>").append(data);
                $("#movieContent").replaceWith(dataHtml.find("#movieContent"));

                console.log("get-movieDTO ajax success")
            },
            error: function (xhr){
                console.log(xhr.responseText);
                console.log("get-movieDTO ajax failed")
            }

        })
    });
}

function movieSearchSection() {
    $(document).on('input', '#movieContainer .searchInput', function () {
        const keyword = $(this).val();

        $.ajax({
            url: "/api/manager/search/movie",
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

function moviePosterSection() {

    $(document).on('change', '.fileInput', function (e) {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function (e) {
                $('.moviePoster').attr('src', e.target.result);
            };
            reader.readAsDataURL(file);
        }
    });

    //포스터 적용 버튼
    $(document).on('click', '.updatePosterBtn', function () {
        const urlInput = $('.urlInput').val();
        if (urlInput) {
            $('.moviePoster').attr('src', urlInput);
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
                    <input type="text" value="${value}" placeholder="장르">
                    <button class="removeBtn">&times;</button>
        `       ));
                break;
            case 'audit':
                $('#auditList').append($('<div>')
                    .addClass('tag')
                    .html(`
                    <input type="text" value="${value}" placeholder="관람등급">
                    <button class="removeBtn">&times;</button>
        `       ));
                break;
            case 'director':
                $('#directorList').append($('<div>')
                    .addClass('listItem')
                    .html(`
                    <input type="text" class="directorNm" placeholder="감독 이름" value="${director.peopleNm || ''}">
                    <input type="text" class="directorNmEn" placeholder="감독 영문 이름" value="${director.peopleNmEn || ''}">
                    <button class="removeBtn">&times;</button>
        `       ));
                break;
            case 'actor':
                $('#actorList').append($('<div>')
                    .addClass('listItem')
                    .html(`
                    <input type="text" class="actorNm" placeholder="배우 이름" value="${actor.peopleNm || ''}">
                    <input type="text" class="actorNmEn" placeholder="배우 영문 이름" value="${actor.peopleNmEn || ''}">
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
        const movieData = {
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
            posterURL: $('.moviePoster').attr('src'),
            genreList: $('#genreList .tag').map(function () {
                return { genreNm: $(this).find('input').val() };
            }).get(),
            auditList: $('#auditList .tag').map(function () {
                return { watchGradeNm: $(this).find('input').val() };
            }).get(),
            directorList: $('#directorList .listItem').map(function () {
                return {
                    peopleNm: $(this).find('.directorNm').val(),
                    peopleNmEn: $(this).find('.directorNmEn').val(),
                };
            }).get(),
            actorList: $('#actorList .listItem').map(function () {
                return {
                    peopleNm: $(this).find('.actorNm').val(),
                    peopleNmEn: $(this).find('.actorNmEn').val(),
                };
            }).get(),
        };

        // AJAX: 영화 데이터 저장

    });

    $(document).on('click', '.cancelBtn', function () {
        if (confirm('변경사항이 저장되지 않습니다. 취소하시겠습니까?')) {
            $('.movieContent').hide();
        }
    });
}

function clearMovieData() {
    $('.movieCd').val('');
    $('.movieNm').val('');
    $('.movieNmEn').val('');
    $('.openDt').val('');
    $('.salesAcc').val('');
    $('.showTm').val('');
    $('.audiAcc').val('');
    $('.prdtYear').val('');
    $('.audiCnt').val('');
    $('.typeNm').val('');
    $('.moviePoster').attr('src', '');
    $('#genreList').empty();
    $('#auditList').empty();
    $('#directorList').empty();
    $('#actorList').empty();
}
